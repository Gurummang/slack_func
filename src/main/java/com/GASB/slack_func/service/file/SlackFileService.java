package com.GASB.slack_func.service.file;

import com.GASB.slack_func.mapper.SlackFileMapper;
import com.GASB.slack_func.model.dto.SlackRecentFileDTO;
import com.GASB.slack_func.model.dto.SlackTotalFileDataDto;
import com.GASB.slack_func.model.entity.*;
import com.GASB.slack_func.repository.AV.FileStatusRepository;
import com.GASB.slack_func.repository.AV.VtReportRepository;
import com.GASB.slack_func.repository.activity.FileActivityRepo;
import com.GASB.slack_func.repository.channel.SlackChannelRepository;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.repository.files.SlackFileRepository;
import com.GASB.slack_func.repository.orgSaaS.OrgSaaSRepo;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.GASB.slack_func.service.SlackApiService;
import com.GASB.slack_func.service.SlackSpaceInfoService;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.File;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
public class SlackFileService {

    private final SlackApiService slackApiService;
    private final SlackFileRepository storedFilesRepository;
    private final SlackFileMapper slackFileMapper;
    private final FileUploadRepository fileUploadRepository;
    private final SlackChannelRepository slackChannelRepository;
    private final SlackUserRepo slackUserRepo;
    private final SlackSpaceInfoService slackSpaceInfoService;
    private final FileActivityRepo activitiesRepository;
    private final RestTemplate restTemplate;

    private final VtReportRepository vtReportRepository;
    private final FileStatusRepository fileStatusRepository;
    private final OrgSaaSRepo orgSaaSRepo;

    public SlackFileService(SlackApiService slackApiService,
                            SlackFileRepository storedFilesRepository,
                            SlackFileMapper slackFileMapper,
                            FileUploadRepository fileUploadRepository,
                            SlackChannelRepository slackChannelRepository,
                            SlackUserRepo slackUserRepo,
                            SlackSpaceInfoService slackSpaceInfoService,
                            FileActivityRepo activitiesRepository,
                            RestTemplate restTemplate,
                            VtReportRepository vtReportRepository,
                            FileStatusRepository fileStatusRepository,
                            OrgSaaSRepo orgSaaSRepo) {
        this.slackApiService = slackApiService;
        this.storedFilesRepository = storedFilesRepository;
        this.slackFileMapper = slackFileMapper;
        this.fileUploadRepository = fileUploadRepository;
        this.slackChannelRepository = slackChannelRepository;
        this.slackUserRepo = slackUserRepo;
        this.slackSpaceInfoService = slackSpaceInfoService;
        this.activitiesRepository = activitiesRepository;
        this.restTemplate = restTemplate;
        this.vtReportRepository = vtReportRepository;
        this.fileStatusRepository = fileStatusRepository;
        this.orgSaaSRepo = orgSaaSRepo;
    }

    @Transactional
    public void fetchAndStoreFiles() {
        try {
            List<File> fileList = fetchFileList();
            String workspaceName = slackSpaceInfoService.getCurrentSpaceName();
            for (File file : fileList) {
                byte[] fileData = downloadFile(file.getUrlPrivateDownload());
                String hash = calculateHash(fileData);

                // 채널 및 사용자 정보 가져오기
                String channelId = file.getChannels().isEmpty() ? null : file.getChannels().get(0);
                String userId = file.getUser();

                String channelName = fetchChannelName(channelId);
                String uploadedUserName = fetchUserName(userId);
                MonitoredUsers user = slackUserRepo.findByUserId(userId).orElse(null);
                OrgSaaS saas = orgSaaSRepo.findById(user.getOrgSaaS().getId()).orElse(null);
                String saasName = (saas != null) ? saas.getSaas().getSaasName() : "unknown_saas";

                String filePath = saveFileToLocal(fileData, saasName, workspaceName, channelName, file.getName());

                StoredFile storedFile = slackFileMapper.toStoredFileEntity(file, hash, filePath);
                fileUpload fileUploadObject = slackFileMapper.toFileUploadEntity(file, 1, hash);
                Activities activity = slackFileMapper.toActivityEntity(file, "file_uploaded", user);

                if (storedFilesRepository.findBySaltedHash(storedFile.getSaltedHash()).isEmpty()
                        && fileUploadRepository.findBySaasFileId(fileUploadObject.getSaasFileId()).isEmpty()) {
                    storedFilesRepository.save(storedFile);
                    fileUploadRepository.save(fileUploadObject);
                    activitiesRepository.save(activity);
                }
            }
        } catch (Exception e) {
            log.error("Error processing files", e);
        }
    }

    protected List<File> fetchFileList() throws IOException, SlackApiException {
        return slackApiService.fetchFiles();
    }

    protected byte[] downloadFile(String fileUrl) throws IOException {
        ResponseEntity<byte[]> response = restTemplate.getForEntity(fileUrl, byte[].class);
        if (response.getStatusCode() == HttpStatus.OK) {
            return response.getBody();
        } else {
            throw new IOException("Failed to download file from URL: " + fileUrl);
        }
    }

    protected String calculateHash(byte[] fileData) throws NoSuchAlgorithmException {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(fileData);
        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    protected String saveFileToLocal(byte[] fileData, String saasName, String workspaceName, String channelName, String fileName) throws IOException {
        Path basePath = Paths.get("downloaded_files");
        Path filePath = basePath.resolve(Paths.get(saasName, workspaceName, channelName, fileName));
        Files.createDirectories(filePath.getParent());
        Files.write(filePath, fileData);
        return basePath.relativize(filePath).toString().replace("\\", "/");
    }

    protected String fetchChannelName(String channelId) {
        if (channelId == null) return "unknown_channel";
        Optional<ChannelList> channel = slackChannelRepository.findByChannelId(channelId);
        return channel.map(ChannelList::getChannelName).orElse("unknown_channel");
    }

    protected String fetchUserName(String userId) {
        if (userId == null) return "unknown_user";
        Optional<MonitoredUsers> user = slackUserRepo.findByUserId(userId);
        return user.map(MonitoredUsers::getUserName).orElse("unknown_user");
    }

    public List<SlackRecentFileDTO> slackRecentFiles() {
        List<fileUpload> recentFileUploads = fileUploadRepository.findTop10ByOrderByTimestampDesc();
        return recentFileUploads.stream().map(upload -> {
            Optional<StoredFile> storedFileOpt = storedFilesRepository.findBySaltedHash(upload.getHash());
            Optional<Activities> activityOpt = activitiesRepository.findBysaasFileId(upload.getSaasFileId());
            if (storedFileOpt.isPresent() && activityOpt.isPresent()) {
                StoredFile storedFile = storedFileOpt.get();
                Activities activity = activityOpt.get();
                Optional<MonitoredUsers> userOpt = slackUserRepo.findByUserId(activity.getUser().getUserId());
                String uploadedBy = userOpt.map(MonitoredUsers::getUserName).orElse("Unknown User");
                return SlackRecentFileDTO.builder()
                        .fileName(activity.getFileName())
                        .uploadedBy(uploadedBy)
                        .fileType(storedFile.getType())
                        .uploadTimestamp(LocalDateTime.ofInstant(Instant.ofEpochSecond(upload.getTimestamp()), ZoneId.systemDefault()))
                        .build();
            }
            return null;
        }).filter(dto -> dto != null).collect(Collectors.toList());
    }

    public SlackTotalFileDataDto slackTotalFilesData() {
        List<fileUpload> fileUploads = fileUploadRepository.findAll();
        List<SlackTotalFileDataDto.FileDetail> fileDetails = fileUploads.stream().map(fileUpload -> {
            SlackTotalFileDataDto.FileDetail.FileDetailBuilder detailBuilder = SlackTotalFileDataDto.FileDetail.builder()
                    .fileId(fileUpload.getSaasFileId())
                    .timestamp(LocalDateTime.ofInstant(Instant.ofEpochSecond(fileUpload.getTimestamp()), ZoneId.systemDefault()));

            Activities activity = activitiesRepository.findBysaasFileId(fileUpload.getSaasFileId()).orElse(null);
            if (activity != null) {
                detailBuilder.fileName(activity.getFileName());
                MonitoredUsers user = slackUserRepo.findByUserId(activity.getUser().getUserId()).orElse(null);
                if (user != null) {
                    detailBuilder.username(user.getUserName());
                    OrgSaaS saas = orgSaaSRepo.findById(user.getOrgSaaS().getId()).orElse(null);
                    if (saas != null) {
                        detailBuilder.saasName(saas.getSaas().getSaasName());
                    } else {
                        detailBuilder.saasName("unknown_saas");
                    }
                } else {
                    detailBuilder.saasName("unknown_saas");
                }
            }

            StoredFile storedFile = storedFilesRepository.findBySaltedHash(fileUpload.getHash()).orElse(null);
            if (storedFile != null) {
                detailBuilder.fileType(storedFile.getType())
                        .filePath(storedFile.getSavePath());

                VtReport vtReport = vtReportRepository.findByStoredFile(storedFile);
                if (vtReport != null) {
                    SlackTotalFileDataDto.FileDetail.VtScanResult vtScanResult = SlackTotalFileDataDto.FileDetail.VtScanResult.builder()
                            .threatLabel(vtReport.getThreatLabel())
                            .hash(fileUpload.getHash())
                            .detectEngine(vtReport.getDetectEngine())
                            .score(vtReport.getScore())
                            .V3(vtReport.getV3())
                            .ALYac(vtReport.getALYac())
                            .Kaspersky(vtReport.getKaspersky())
                            .Falcon(vtReport.getFalcon())
                            .Avast(vtReport.getAvast())
                            .Sentinelone(vtReport.getSentinelone())
                            .reportUrl(vtReport.getReportUrl())
                            .build();
                    detailBuilder.vtScanResult(vtScanResult);
                }

                FileStatus fileStatus = fileStatusRepository.findByStoredFile(storedFile);
                if (fileStatus != null) {
                    SlackTotalFileDataDto.FileDetail.GScanResult gScanResult = SlackTotalFileDataDto.FileDetail.GScanResult.builder()
                            .status(String.valueOf(fileStatus.getGscanStatus()))
                            .build();
                    detailBuilder.gScanResult(gScanResult);

                    SlackTotalFileDataDto.FileDetail.DlpScanResult dlpScanResult = SlackTotalFileDataDto.FileDetail.DlpScanResult.builder()
                            .status(String.valueOf(fileStatus.getDlpStatus()))
                            .build();
                    detailBuilder.dlpScanResult(dlpScanResult);
                }
            }

            return detailBuilder.build();
        }).collect(Collectors.toList());

        SlackTotalFileDataDto totalFileDataDto = new SlackTotalFileDataDto();
        totalFileDataDto.setStatus("success");
        totalFileDataDto.setFiles(fileDetails);

        return totalFileDataDto;
    }
}
