package com.GASB.slack_func.mapper;

import com.GASB.slack_func.model.entity.*;
import com.GASB.slack_func.repository.org.OrgSaaSRepo;
import com.GASB.slack_func.repository.users.SlackUserRepo;
import com.slack.api.model.File;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
@RequiredArgsConstructor
public class SlackFileMapper {

    private SlackUserRepo monitoredUsersRepository;
    private OrgSaaSRepo orgSaaSRepo;

    public StoredFile toStoredFileEntity(File file, String hash, String filePath) {
        if (file == null) {
            return null;
        }
        return StoredFile.builder()
                .type(file.getFiletype())
                .size(file.getSize())
                .savePath(filePath)
                .saltedHash(hash)
                .build();
    }

    public List<StoredFile> toStoredFileEntity(List<File> files, List<String> hashes, List<String> filePaths) {
        return IntStream.range(0, files.size())
                .mapToObj(i -> toStoredFileEntity(files.get(i), hashes.get(i), filePaths.get(i)))
                .collect(Collectors.toList());
    }

    public fileUpload toFileUploadEntity(File file, OrgSaaS orgSaaS, String hash) {
        if (file == null) {
            return null;
        }
        return fileUpload.builder()
                .orgSaaS(orgSaaS)
                .saasFileId(file.getId())
                .hash(hash)
                .timestamp(file.getTimestamp())
                .build();
    }

    public List<fileUpload> toFileUploadEntity(List<File> files, OrgSaaS orgSaaS, String hash) {
        return files.stream()
                .map(file -> toFileUploadEntity(file, orgSaaS, hash))
                .collect(Collectors.toList());
    }


    public Activities toActivityEntity(File file, String eventType, MonitoredUsers user) {
        if (file == null) {
            return null;
        }
//
//        Optional<MonitoredUsers> optionalUser = monitoredUsersRepository.findByUserId(file.getUser());
//
//        MonitoredUsers user = optionalUser.orElseThrow(() ->
//                new IllegalArgumentException("MonitoredUser not found with userId: " + file.getUser()));

        return Activities.builder()
                .user(user)
                .eventType(eventType)
                .saasFileId(file.getId())
                .fileName(file.getName())
                .eventTs(LocalDateTime.ofInstant(Instant.ofEpochSecond(file.getTimestamp()), ZoneId.systemDefault()))
                .uploadChannel(file.getChannels().isEmpty() ? null : file.getChannels().get(0))
                .build();
    }
}
