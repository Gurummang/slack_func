package com.GASB.slack_func.mapper;

import com.GASB.slack_func.entity.fileUpload;
import com.GASB.slack_func.entity.storedFiles;
import com.slack.api.model.File;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Component
public class SlackFileMapper {
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
        fileUpload.setTimestamp(LocalDateTime.now());
        return fileUpload;
    }

    public List<fileUpload> toFileUploadEntity(List<File> files, int orgSaaSId,String Hash) {
        return files.stream()
                .map(file -> toFileUploadEntity(file, orgSaaSId, Hash))
                .collect(Collectors.toList());
    }
}
