package com.GASB.slack_func.mapper;

import com.GASB.slack_func.entity.fileUpload;
import com.GASB.slack_func.entity.storedFiles;
import com.slack.api.model.File;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SlackFileMapper {

    public storedFiles toStoredFileEntity(File file) {
        if (file == null) {
            return null;
        }
        return storedFiles.builder()
                .fileId(file.getId())
                .saltedHash(null) // 필요에 따라 적절한 해시 값을 설정하세요.
                .size(file.getSize())
                .type(file.getFiletype())
                .savePath(file.getUrlPrivate()) // 지금은 다운로드 경로임. 추후 변경예정
                .build();
    }

    public List<storedFiles> toStoredFileEntity(List<File> files) {
        return files.stream()
                .map(this::toStoredFileEntity)
                .collect(Collectors.toList());
    }

    public fileUpload toFileUploadEntity(File file, int orgSaaSId) {
        if (file == null) {
            return null;
        }
        fileUpload fileUpload = new fileUpload();
        fileUpload.setOrgSaaSId(orgSaaSId);
        fileUpload.setSaasFileId(file.getId());
        fileUpload.setHash(null);
        fileUpload.setTimestamp(LocalDateTime.now());
        return fileUpload;
    }

    public List<fileUpload> toFileUploadEntity(List<File> files, int orgSaaSId) {
        return files.stream()
                .map(file -> toFileUploadEntity(file, orgSaaSId))
                .collect(Collectors.toList());
    }
}
