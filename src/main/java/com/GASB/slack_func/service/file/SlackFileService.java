package com.GASB.slack_func.service.file;

import com.GASB.slack_func.model.dto.file.SlackFileCountDto;
import com.GASB.slack_func.model.dto.file.SlackFileSizeDto;
import com.GASB.slack_func.model.dto.file.SlackRecentFileDTO;
import com.GASB.slack_func.model.dto.file.SlackTotalFileDataDto;
import com.GASB.slack_func.model.entity.*;
import com.GASB.slack_func.repository.AV.FileStatusRepository;
import com.GASB.slack_func.repository.AV.VtReportRepository;
import com.GASB.slack_func.repository.activity.FileActivityRepo;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.repository.files.SlackFileRepository;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.GASB.slack_func.service.SlackApiService;
import com.slack.api.methods.SlackApiException;
import com.slack.api.model.File;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackFileService {


    private final SlackApiService slackApiService;
    private final FileUtil fileUtil;
    private final FileUploadRepository fileUploadRepository;
    private final SlackFileRepository storedFilesRepository;
    private final FileActivityRepo activitiesRepository;
    private final SlackUserRepo slackUserRepo;
    private final OrgSaaSRepo orgSaaSRepo;
    private final FileStatusRepository fileStatusRepository;
    private final VtReportRepository vtReportRepository;
    @Transactional
    public void fetchAndStoreFiles(String spaceId) {
        try {
            List<File> fileList = fetchFileList();

            OrgSaaS orgSaaSObject = orgSaaSRepo.findBySpaceId(spaceId).get();
//            String spaceName = orgSaaS.getConfig().getSaasname();
//            String workspaceName = slackSpaceInfoService.getCurrentSpaceName();
            for (File file : fileList) {
                fileUtil.processAndStoreFile(file, orgSaaSObject);
            }
        } catch (Exception e) {
            log.error("Error processing files", e);
        }
    }

    protected List<File> fetchFileList() throws IOException, SlackApiException {
        return slackApiService.fetchFiles();
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
                        .uploadTimestamp(upload.getTimestamp().toLocalDateTime())
                        .build();
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toList());
    }

    public SlackTotalFileDataDto slackTotalFilesData() {
        List<fileUpload> fileUploads = fileUploadRepository.findAll();
        List<SlackTotalFileDataDto.FileDetail> fileDetails = fileUploads.stream().map(fileUpload -> {
            SlackTotalFileDataDto.FileDetail.FileDetailBuilder detailBuilder = SlackTotalFileDataDto.FileDetail.builder()
                    .fileId(fileUpload.getSaasFileId())
                    .timestamp(fileUpload.getTimestamp().toLocalDateTime());

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
                        .filePath(Objects.requireNonNull(activity).getUploadChannel());
//                        .filePath(storedFile.getSavePath());

                VtReport vtReport = vtReportRepository.findByStoredFile(storedFile).orElse(null);
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


    // 전달값으로 어떤 조직인지, 어떤 SaaS인지 구분 필요, 근데 지금 api 엔드포인트 자체가 SaaS를 내포해서 일단은 Org
    public SlackFileSizeDto slackFileSize(OrgSaaS orgSaaSObject) {
        List<fileUpload> TargetFileList = fileUploadRepository.findByOrgSaaS(orgSaaSObject);

        return SlackFileSizeDto.builder()
                .totalSize((float) fileUtil.calculateTotalFileSize(TargetFileList) / 1048576)
                .sensitiveSize((float) fileUtil.CalcSlackSensitiveSize(TargetFileList))
                .maliciousSize((float) fileUtil.CalcSlackMaliciousSize(TargetFileList))
                .build();
    }

    public SlackFileSizeDto SumOfSlackFileSize(List<OrgSaaS> orgSaaSList){
        int totalSize = 0;
        int sensitiveSize = 0;
        int maliciousSize = 0;

        for (OrgSaaS orgSaaSObject : orgSaaSList){
            List<fileUpload> TargetFileList = fileUploadRepository.findByOrgSaaS(orgSaaSObject);
            totalSize += fileUtil.calculateTotalFileSize(TargetFileList);
            sensitiveSize += fileUtil.CalcSlackSensitiveSize(TargetFileList);
            maliciousSize += fileUtil.CalcSlackMaliciousSize(TargetFileList);
        }

        return SlackFileSizeDto.builder()
                .totalSize((float) totalSize / 1048576)
                .sensitiveSize((float) sensitiveSize)
                .maliciousSize((float) maliciousSize)
                .build();
    }


    public SlackFileCountDto slackFileCount(OrgSaaS orgSaaSObject) {
        List<fileUpload> TargetFileList = fileUploadRepository.findByOrgSaaS(orgSaaSObject);
        int totalFileCount = TargetFileList.size();

        return SlackFileCountDto.builder()
                .totalFiles(totalFileCount)
                .sensitiveFiles(fileUtil.countSensitiveFiles(TargetFileList))
                .maliciousFiles(fileUtil.countMaliciousFiles(TargetFileList))
                .connectedAccounts(fileUtil.countConnectedAccounts(orgSaaSObject))
                .build();
    }

    public SlackFileCountDto SumOfSlackFileCount(List<OrgSaaS> orgSaaSList){
        int totalFiles = 0;
        int sensitiveFiles = 0;
        int maliciousFiles = 0;
        int connectedAccounts = 0;

        for (OrgSaaS orgSaaSObject : orgSaaSList){
            List<fileUpload> TargetFileList = fileUploadRepository.findByOrgSaaS(orgSaaSObject);
            totalFiles += TargetFileList.size();
            sensitiveFiles += fileUtil.countSensitiveFiles(TargetFileList);
            maliciousFiles += fileUtil.countMaliciousFiles(TargetFileList);
            connectedAccounts += fileUtil.countConnectedAccounts(orgSaaSObject);
        }

        return SlackFileCountDto.builder()
                .totalFiles(totalFiles)
                .sensitiveFiles(sensitiveFiles)
                .maliciousFiles(maliciousFiles)
                .connectedAccounts(connectedAccounts)
                .build();
    }
}
