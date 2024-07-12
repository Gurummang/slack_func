package com.GASB.slack_func.mapper;

import com.GASB.slack_func.entity.Activities;
import com.GASB.slack_func.entity.fileUpload;
import com.GASB.slack_func.entity.storedFiles;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.slack.api.model.File;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class SlackFileMapper {

    private final SlackUserRepo slackUserRepo;

    public SlackFileMapper(SlackUserRepo slackUserRepo) {
        this.slackUserRepo = slackUserRepo;
    }

    public storedFiles toStoredFileEntity(File file, String hash, String filePath) {
        if (file == null) {
            return null;
        }
        return storedFiles.builder()
                .fileId(file.getId())
                .saltedHash(hash)
                .size(file.getSize())
                .type(file.getFiletype())
                .savePath(filePath)
                .build();
    }

    public List<storedFiles> toStoredFileEntity(List<File> files, List<String> hashes, List<String> filePaths) {
        return IntStream.range(0, files.size())
                .mapToObj(i -> toStoredFileEntity(files.get(i), hashes.get(i), filePaths.get(i)))
                .collect(Collectors.toList());
    }

    public fileUpload toFileUploadEntity(File file, int orgSaaSId, String Hash) {
        if (file == null) {
            return null;
        }
        fileUpload fileUpload = new fileUpload();
        fileUpload.setOrgSaaSId(orgSaaSId);
        fileUpload.setSaasFileId(file.getId());
        fileUpload.setHash(Hash);
        fileUpload.setTimestamp(file.getTimestamp());
        return fileUpload;
    }

    public List<fileUpload> toFileUploadEntity(List<File> files, int orgSaaSId,String Hash) {
        return files.stream()
                .map(file -> toFileUploadEntity(file, orgSaaSId, Hash))
                .collect(Collectors.toList());
    }

    public Activities toActivityEntity(File file,String eventType) {
        if (file == null) {
            return null;
        }

//        MonitoredUsers user = slackUserRepo.findByUserId(file.getUser()).orElse(null);
//        String userId = user == null ? null : user.getUserId();
        return Activities.builder()
                .user(file.getUser())
                .eventType(eventType)
                .saasFileId(file.getId())
                .fileName(file.getName())
                .eventTs(LocalDateTime.ofInstant(Instant.ofEpochSecond(file.getTimestamp()), ZoneId.systemDefault()))
                .uploadChannel(file.getChannels().isEmpty() ? null : file.getChannels().get(0))
                .build();
    }
}
