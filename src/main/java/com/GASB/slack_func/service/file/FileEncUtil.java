package com.GASB.slack_func.service.file;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

@Component
@Slf4j
public class FileEncUtil {

    @Value("${file.aes.key}")
    private String fileEncAESkey;

    @Value("${file.enc.basepath}")
    private String basePath;

    private SecretKey secretKey;

    @PostConstruct
    public void setUp(){
        secretKey = decodeKeyFromBase64(fileEncAESkey);
    }

    // 파일 암호화
    public Path encryptAndSaveFile(String sourceFilePath) throws Exception {

        // null check
        if (sourceFilePath == null || fileEncAESkey == null) {
            throw new IllegalArgumentException("파일 경로나 AES 키가 null일 수 없습니다.");
        }

        // 경로에서 파일명 추출
        String fileName = Paths.get(sourceFilePath).getFileName().toString();
        log.info("fileName : {}", fileName);
        // 원본 파일 읽기
        byte[] fileContent = readFileContent(sourceFilePath);

        // 파일 암호화
        byte[] encryptedContent = encryptFile(fileContent, secretKey);
        System.out.println("암호화 데이터 출력 : " + new String(encryptedContent, "UTF-8"));

        // 암호화된 파일 저장
        return saveFileContent(encryptedContent, fileName);
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

    public static byte[] encryptFile(byte[] content, SecretKey secretKey) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey);
        return cipher.doFinal(content);
    }

    public static SecretKey decodeKeyFromBase64(String base64Key) {
        if (base64Key == null || base64Key.isEmpty()) {
            throw new IllegalArgumentException("복원할 Base64 인코딩된 키가 없습니다.");
        }
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }
}
