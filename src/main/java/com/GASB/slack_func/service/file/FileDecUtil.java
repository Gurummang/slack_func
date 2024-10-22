package com.GASB.slack_func.service.file;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileDecUtil {

    @Value("${file.aes.key}")
    private String fileEncAESkey;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;

    // S3에서 파일을 스트리밍으로 다운로드 후 바로 복호화하는 메서드
    public byte[] decryptFileFromS3(String objectKey) throws Exception {
        if (objectKey == null || fileEncAESkey == null) {
            throw new IllegalArgumentException("objectKey나 AES 키는 null일 수 없습니다.");
        }

        // S3에서 스트리밍으로 파일 다운로드
        try (InputStream s3InputStream = downloadFile(bucketName, objectKey);
             CipherInputStream cipherInputStream = createDecryptionStream(s3InputStream)) {

            log.info("S3에서 파일을 성공적으로 다운로드 및 복호화 중입니다. 파일 키: {}", objectKey);

            // 복호화된 데이터를 바이트 배열로 변환
            return convertStreamToByteArray(cipherInputStream);
        }
    }

    private CipherInputStream createDecryptionStream(InputStream inputStream) throws Exception {
        SecretKey decodeKey = decodeKeyFromBase64(fileEncAESkey);
        Cipher cipher = Cipher.getInstance("AES");  // AES/ECB 모드 사용
        cipher.init(Cipher.DECRYPT_MODE, decodeKey);

        return new CipherInputStream(inputStream, cipher);  // 복호화 스트림 반환
    }

    // Base64로 인코딩된 AES 키를 복원하는 메서드
    public static SecretKey decodeKeyFromBase64(String base64Key) {
        if (base64Key == null || base64Key.isEmpty()) {
            throw new IllegalArgumentException("복원할 Base64 인코딩된 키가 없습니다.");
        }
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    // 스트리밍 데이터를 바이트 배열로 변환
    private byte[] convertStreamToByteArray(InputStream inputStream) throws Exception {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192]; // 8KB 버퍼
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }
            return byteArrayOutputStream.toByteArray();
        }
    }

    // S3에서 스트리밍 방식으로 파일을 다운로드
    public InputStream downloadFile(String bucketName, String key) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(getObjectRequest);
            log.info("S3 파일 스트리밍 시작. 버킷: {}, 키: {}", bucketName, key);
            return s3InputStream;

        } catch (RuntimeException e) {
            log.error("S3에서 파일 다운로드 실패. 버킷: {}, 키: {}, 오류: {}",
                    bucketName, key, e.getMessage(), e);
            throw e;
        }
    }
}
