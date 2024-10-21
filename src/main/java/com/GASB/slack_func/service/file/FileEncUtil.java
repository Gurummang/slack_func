package com.GASB.slack_func.service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Service
@Slf4j
public class FileEncUtil {

    @Value("${file.aes.key}")
    private String file_key;

    @Value("${file.enc.basepath}")
    private String basePath;
    // 파일 암호화
    public Path encryptAndSaveFile(String sourceFilePath) throws Exception {

        // 경로에서 파일명 추출
        String fileName = Paths.get(sourceFilePath).getFileName().toString();
        // 원본 파일 읽기
        byte[] fileContent = readFileContent(sourceFilePath);

        // 파일 암호화
        byte[] encryptedContent = encryptFile(fileContent, file_key);

        // 암호화된 파일 저장
        return saveFileContent(encryptedContent, fileName);
    }

    private void deleteFile(String filePath) {
        Path path = Paths.get(filePath);
        try {
            Files.delete(path);
        } catch (IOException e) {
            log.error("파일 삭제 실패: " + filePath);
        }
    }
    private Path saveFileContent(byte[] content, String fileName) throws IOException {
        Path dstPath = Paths.get(basePath).resolve(fileName);
        Files.createDirectories(dstPath.getParent()); // 상위 디렉터리 생성(없는 경우)
        Files.write(dstPath, content);
        return dstPath;
    }
    private byte[] readFileContent(String filePath) throws IOException {
        Path path = Paths.get(filePath);
        return Files.readAllBytes(path);
    }

    public byte[] encryptFile(byte[] content, String base64Key) throws Exception {
        // String 키를 SecretKey로 변환
        byte[] decodedKey = Base64.getDecoder().decode(base64Key); // Base64 인코딩된 키 디코딩
        SecretKeySpec secretKey = new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");

        // AES 암호화 설정 및 암호화
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(content);
    }

}
