package com.GASB.slack_func.service.file;

import com.GASB.slack_func.model.dto.file.SlackFileCountDto;
import com.GASB.slack_func.model.dto.file.SlackFileSizeDto;
import com.GASB.slack_func.model.dto.file.SlackRecentFileDTO;
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
                log.info("Processing file info : {}, {}", file.getMode(), file.getPrettyType());

                if (shouldSkipFile(file)) {
                    log.info("File is a quip or canvas file, skipping processing: Mode={}, PrettyType={}", file.getMode(), file.getPrettyType());
                    continue;
                }

                fileUtil.processAndStoreFile(file, orgSaaSObject, workspaceId);
            }
        } catch (Exception e) {
            log.error("Error processing files", e);
        }
    }
    private boolean shouldSkipFile(File file) {
        return "quip".equalsIgnoreCase(file.getMode()) ||
                "캔버스".equalsIgnoreCase(file.getPrettyType()) ||
                "canvas".equalsIgnoreCase(file.getPrettyType());
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

    public Long getTotalFileSize(int orgId, int saasId) {
        Long totalFileSize = storedFilesRepository.getTotalFileSize(orgId, saasId);
        return totalFileSize != null ? totalFileSize : 0L; // null 반환 방지
    }

    public Long getTotalMaliciousFileSize(int orgId, int saasId) {
        Long totalMaliciousFileSize = storedFilesRepository.getTotalMaliciousFileSize(orgId, saasId);
        return totalMaliciousFileSize != null ? totalMaliciousFileSize : 0L; // null 반환 방지
    }

    public Long getTotalDlpFileSize(int orgId, int saasId) {
        Long totalDlpFileSize = storedFilesRepository.getTotalDlpFileSize(orgId, saasId);
        return totalDlpFileSize != null ? totalDlpFileSize : 0L; // null 반환 방지
    }
    public SlackFileSizeDto SumOfSlackFileSize(int orgId, int saasId) {
        return SlackFileSizeDto.builder()
                .totalSize((float) getTotalFileSize(orgId,saasId) / 1073741824)
                .sensitiveSize((float) getTotalDlpFileSize(orgId,saasId) / 1073741824)
                .maliciousSize((float) getTotalMaliciousFileSize(orgId,saasId) / 1073741824)
                .build();
    }

    public SlackFileCountDto testCountSum(int orgId, int saasId) {
        return SlackFileCountDto.builder()
                .totalFiles(storedFilesRepository.countTotalFiles(orgId, saasId))
                .sensitiveFiles(storedFilesRepository.countSensitiveFiles(orgId, saasId))
                .maliciousFiles(storedFilesRepository.countMaliciousFiles(orgId, saasId))
                .connectedAccounts(storedFilesRepository.countConnectedAccounts(orgId, saasId))
                .build();
    }
}
