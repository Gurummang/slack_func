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
import com.slack.api.model.File;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
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
    public void fetchAndStoreFiles(int workspaceId) {
        try {
            OrgSaaS orgSaaSObject = orgSaaSRepo.findById(workspaceId).orElse(null);
            List<File> fileList = slackApiService.fetchFiles(workspaceId);

            for (File file : fileList) {
                fileUtil.processAndStoreFile(file, orgSaaSObject, workspaceId);
            }
        } catch (Exception e) {
            log.error("Error processing files", e);
        }
    }

    public List<SlackRecentFileDTO> slackRecentFiles(int org_id, Saas saas) {
        try {
            // 특정 조직과 Saas 애플리케이션에 따라 OrgSaaS 리스트를 가져옵니다.
            List<OrgSaaS> orgSaaSList = orgSaaSRepo.findAllByOrgIdAndSaas(org_id, saas);
            log.info("orgSaaSList: {}", orgSaaSList);
            // OrgSaaS 리스트를 기반으로 최근 파일 업로드 정보를 가져옵니다.
            List<fileUpload> recentFileUploads = fileUploadRepository.findTop10ByOrgSaaSInOrderByTimestampDesc(orgSaaSList);

            // DTO 리스트를 생성하여 반환합니다.
            return recentFileUploads.stream().map(upload -> {
                Optional<StoredFile> storedFileOpt = storedFilesRepository.findBySaltedHash(upload.getHash());
                Optional<Activities> activityOpt = activitiesRepository.findBysaasFileId(upload.getSaasFileId());

                // 저장된 파일과 활동이 존재하는 경우에만 처리합니다.
                if (storedFileOpt.isPresent() && activityOpt.isPresent()) {
                    StoredFile storedFile = storedFileOpt.get();
                    Activities activity = activityOpt.get();

                    // 사용자를 찾고 사용자 이름을 설정합니다.
                    Optional<MonitoredUsers> userOpt = slackUserRepo.findByUserId(activity.getUser().getUserId());
                    String uploadedBy = userOpt.map(MonitoredUsers::getUserName).orElse("Unknown User");

                    // DTO를 빌드하여 반환합니다.
                    return SlackRecentFileDTO.builder()
                            .fileName(activity.getFileName())
                            .uploadedBy(uploadedBy)
                            .fileType(storedFile.getType())
                            .uploadTimestamp(upload.getTimestamp())
                            .build();
                }

                // 활동이나 파일이 없는 경우 null 반환
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error retrieving recent files for org_id: {} and saas: {}", org_id, saas, e);
            return Collections.emptyList();
        }
    }








    public SlackTotalFileDataDto slackTotalFilesData() {
        List<fileUpload> fileUploads = fileUploadRepository.findAll();
        List<SlackTotalFileDataDto.FileDetail> fileDetails = fileUploads.stream().map(fileUpload -> {
            SlackTotalFileDataDto.FileDetail.FileDetailBuilder detailBuilder = SlackTotalFileDataDto.FileDetail.builder()
                    .fileId(fileUpload.getSaasFileId())
                    .timestamp(fileUpload.getTimestamp());

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
                .sensitiveSize((float) sensitiveSize / 1048576)
                .maliciousSize((float) maliciousSize / 1048576)
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
