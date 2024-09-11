package com.GASB.slack_func.service.file;

import com.GASB.slack_func.model.dto.file.SlackFileCountDto;
import com.GASB.slack_func.model.dto.file.SlackFileSizeDto;
import com.GASB.slack_func.model.dto.file.SlackRecentFileDTO;
import com.GASB.slack_func.model.entity.FileUploadTable;
import com.GASB.slack_func.model.entity.OrgSaaS;
import com.GASB.slack_func.repository.files.FileUploadRepository;
import com.GASB.slack_func.repository.files.SlackFileRepository;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.repository.org.WorkspaceConfigRepo;
import com.GASB.slack_func.service.SlackApiService;
import com.slack.api.model.File;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@Slf4j
@RequiredArgsConstructor
public class SlackFileService {


    private final SlackApiService slackApiService;
    private final FileUtil fileUtil;
    private final FileUploadRepository fileUploadRepository;
    private final SlackFileRepository storedFilesRepository;
    private final OrgSaaSRepo orgSaaSRepo;
    private final WorkspaceConfigRepo workspaceConfigRepo;
    

    @Transactional
    public void fetchAndStoreFiles(int workspaceId, String event_type) {
        try {
            OrgSaaS orgSaaSObject = orgSaaSRepo.findById(workspaceId).orElse(null);
            List<File> fileList = slackApiService.fetchFiles(workspaceId);

            for (File file : fileList) {
                log.info("Processing file info : {}, {}", file.getMode(), file.getPrettyType());

                if (shouldSkipFile(file)) {
                    log.info("File is a quip or canvas file, skipping processing: Mode={}, PrettyType={}", file.getMode(), file.getPrettyType());
                    continue;
                }

                fileUtil.processAndStoreFile(file, orgSaaSObject, workspaceId, event_type);
            }
        } catch (Exception e) {
            log.error("Error processing files", e);
        }
    }

    public boolean fileDelete(int idx, String fileHash) {
        try {
            // 파일 ID와 해시값을 통해 파일 조회
            FileUploadTable targetFile = fileUploadRepository.findByIdAndFileHash(idx, fileHash).orElse(null);
            if (targetFile == null) {
                log.error("File not found or invalid: id={}, hash={}", idx, fileHash);
                return false;
            }
            // 해당 파일이 Slack 파일인지 확인
            if (orgSaaSRepo.findSaaSIdByid(targetFile.getOrgSaaS().getId()) != 1) {
                log.error("File is not a Slack file: id={}, saasId={}", idx, targetFile.getOrgSaaS().getId());
                return false;
            }
            // Slack API를 통해 파일 삭제 요청
            return slackApiService.SlackFileDeleteApi(targetFile.getOrgSaaS().getId(), targetFile.getSaasFileId());

        } catch (Exception e) {
            log.error("Error processing file delete: id={}, hash={}", idx, fileHash, e);
            return false;
        }
    }

    private boolean shouldSkipFile(File file) {
        return "quip".equalsIgnoreCase(file.getMode()) ||
                "캔버스".equalsIgnoreCase(file.getPrettyType()) ||
                "canvas".equalsIgnoreCase(file.getPrettyType());
    }

    public List<SlackRecentFileDTO> slackRecentFiles(int orgId, int saasId) {
        try {
            return fileUploadRepository.findRecentFilesByOrgIdAndSaasId(orgId, saasId);
        } catch (Exception e) {
            log.error("Error retrieving recent files for org_id: {} and saas_id: {}", orgId, saasId, e);
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
    public SlackFileSizeDto sumOfSlackFileSize(int orgId, int saasId) {
        return SlackFileSizeDto.builder()
                .totalSize((float) getTotalFileSize(orgId,saasId) / 1073741824)
                .sensitiveSize((float) getTotalDlpFileSize(orgId,saasId) / 1073741824)
                .maliciousSize((float) getTotalMaliciousFileSize(orgId,saasId) / 1073741824)
                .build();
    }

    public SlackFileCountDto SlackFileCountSum(int orgId, int saasId) {
        return SlackFileCountDto.builder()
                .totalFiles(storedFilesRepository.countTotalFiles(orgId, saasId))
                .sensitiveFiles(storedFilesRepository.countSensitiveFiles(orgId, saasId))
                .maliciousFiles(storedFilesRepository.countMaliciousFiles(orgId, saasId))
                .connectedAccounts(storedFilesRepository.countConnectedAccounts(orgId, saasId))
                .build();
    }
}
