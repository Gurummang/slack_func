package com.GASB.slack_func.service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
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

    @Value("${file.aes.iv}")
    private String aesIv;
    // 파일 암호화
    public Path encryptAndSaveFile(String sourceFilePath) throws Exception {

        // 경로에서 파일명 추출
        String fileName = Paths.get(sourceFilePath).getFileName().toString();
        // 원본 파일 읽기
        byte[] fileContent = readFileContent(sourceFilePath);

        // 파일 암호화
        byte[] encryptedContent = encryptFile(fileContent);

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

    private byte[] encryptFile(byte[] content) throws Exception {
        byte[] decodeKey = Base64.getDecoder().decode(fileEncAESkey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(decodeKey, "AES");


        // 초기화 벡터 생성
        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

        byte[] iv = aesIv.getBytes();
        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec, ivParameterSpec);
        byte[] result = cipher.doFinal(content);

        ByteArrayOutputStream ouputStream = new ByteArrayOutputStream();
        ouputStream.write(iv);
        ouputStream.write(result);

        return ouputStream.toByteArray();

    }

}
