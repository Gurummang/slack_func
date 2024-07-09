package com.GASB.slack_func.service;

import com.GASB.slack_func.entity.storedFiles;
import com.GASB.slack_func.entity.fileUpload;
import com.GASB.slack_func.repository.SlackFileRepository;
import com.GASB.slack_func.repository.FileUploadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.files.FilesListRequest;
import com.slack.api.methods.response.files.FilesListResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class SlackFileService {

    private final String token = ""; // Token은 추후에 db에서 받아오도록 수정
    private final Slack slack;
    private final SlackFileRepository storedFilesRepository;
    private final FileUploadRepository fileUploadRepository;
    private static final Logger logger = LoggerFactory.getLogger(SlackFileService.class);
    private static final ObjectMapper objectMapper = new ObjectMapper();

    public SlackFileService( SlackFileRepository storedFilesRepository, FileUploadRepository fileUploadRepository) {
//        this.token = token;
        this.slack = Slack.getInstance();
        this.storedFilesRepository = storedFilesRepository;
        this.fileUploadRepository = fileUploadRepository;
    }

    /**
     * Fetches a list of files from Slack and stores them in the database.
     *
     * @param count Number of files to fetch
     * @param page  Page number to fetch
     * @return JSON string of success message
     * @throws IOException
     * @throws SlackApiException
     */
    public Map<String, Object> listFiles(int count, int page) throws IOException, SlackApiException {
        logger.info("Fetching files list from Slack. Count: {}, Page: {}", count, page);

        FilesListRequest request = FilesListRequest.builder()
                .count(count)
                .page(page)
                .build();

        FilesListResponse response;
        try {
            response = slack.methods(token).filesList(request);
        } catch (SlackApiException e) {
            // 로그에 실제 응답 정보 포함
            logger.error("Slack API exception: {}", e.getMessage());
            throw e;
        }

        if (!response.isOk()) {
            String error = String.format("Error fetching files list from Slack: %s", response.getError());
            logger.error(error);
            throw new RuntimeException(error);
        }

        logger.info("Successfully fetched files list from Slack.");

        List<storedFiles> storedFilesList = response.getFiles().stream()
                .filter(file->!storedFilesRepository.findByFileId(file.getId()).isPresent())
                .map(file -> {
                    storedFiles sf = new storedFiles();
                    sf.setFileId(file.getId());
//                    sf.setHash(file.getHash());
                    sf.setHash(null);
                    sf.setSize(file.getSize());
                    sf.setType(file.getFiletype());
                    sf.setFileName(file.getName());
//                    sf.setSavePath(file.getUrlPrivateDownload());
                    sf.setSavePath(null);
                    return sf;
                })
                .collect(Collectors.toList());

        storedFilesRepository.saveAll(storedFilesList);

        List<fileUpload> fileUploadList = response.getFiles().stream()
                .filter(file->!fileUploadRepository.findBySaasFileId(file.getId()).isPresent())
                .map(file -> {
                    fileUpload fu = new fileUpload();
                    fu.setOrgSaaSId(1); // 추후에 org_saas_id테이블에서 얻어 오도록 수정 필요
                    fu.setSaasFileId(file.getId());
//                    fu.setHash(file.getHash()); // 파일 다운로드 기능 추가 이후 해시값 계산 기능 추가 예정
                    fu.setHash(null);
                    fu.setTimestamp(LocalDateTime.now());
                    return fu;
                })
                .collect(Collectors.toList());

        fileUploadRepository.saveAll(fileUploadList);

        return Map.of("ok", true, "message", "success");
    }
}
