package com.GASB.slack_func.service;

import com.slack.api.Slack;
import com.slack.api.methods.SlackApiException;
import com.slack.api.methods.request.files.FilesListRequest;
import com.slack.api.methods.response.files.FilesListResponse;
import com.slack.api.model.File;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.List;

@Service
public class SlackFileService {
    private final String token;
    private final Slack slack;
    private static final Logger logger = LoggerFactory.getLogger(SlackFileService.class);

    public SlackFileService(@Value("${slack.token}") String token) {
        this.token = token;
        this.slack = Slack.getInstance();
    }

    /**
     * Fetches a list of files from Slack.
     *
     * @param count Number of files to fetch
     * @param page  Page number to fetch
     * @return List of files
     * @throws IOException
     * @throws SlackApiException
     */
    public List<File> listFiles(int count, int page) throws IOException, SlackApiException {
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
        return response.getFiles();
    }

}
