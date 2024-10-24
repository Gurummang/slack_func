package com.GASB.slack_func.service.file;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
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
    public InputStream dowloadFile(String bucketName, String objectKey) throws Exception {
        if (objectKey == null || objectKey.isEmpty() || fileEncAESkey == null || fileEncAESkey.isEmpty()) {
            log.error("잘못된 파라미터: objectKey: {}, AES 키: {}", objectKey, fileEncAESkey == null ? "null" : "제공됨");
            throw new IllegalArgumentException("objectKey나 AES 키는 null 또는 빈 값일 수 없습니다.");
        }

        log.info("S3 파일 복호화 시작. 파일 키: {}", objectKey);
        try {
            // S3에서 스트리밍으로 파일 다운로드 downloadFile
            InputStream s3InputStream = streamingDownloadFile(bucketName, objectKey);
            CipherInputStream cipherInputStream = createDecryptionStream(s3InputStream);

            log.info("S3에서 파일을 성공적으로 다운로드 및 복호화 중입니다. 파일 키: {}", objectKey);

            // 복호화된 데이터를 InputStream으로 반환
            return cipherInputStream;
        } catch (Exception e) {
            log.error("파일 복호화 중 오류 발생. 파일 키: {}, 오류: {}", objectKey, e.getMessage(), e);
            throw new Exception("파일 복호화 중 오류가 발생했습니다.", e);
        }
    }

    private CipherInputStream createDecryptionStream(InputStream inputStream) throws Exception {
        try {
            log.debug("AES 키 복호화 시작.");
            SecretKey decodeKey = decodeKeyFromBase64(fileEncAESkey);

            Cipher cipher = Cipher.getInstance("AES");  // AES/ECB 모드 사용
            cipher.init(Cipher.DECRYPT_MODE, decodeKey);

            log.debug("복호화 스트림 생성 성공.");
            return new CipherInputStream(inputStream, cipher);  // 복호화 스트림 반환
        } catch (Exception e) {
            log.error("복호화 스트림 생성 중 오류 발생. 오류: {}", e.getMessage(), e);
            throw new Exception("복호화 스트림 생성 중 오류가 발생했습니다.", e);
        }
    }

    // Base64로 인코딩된 AES 키를 복원하는 메서드
    private static SecretKey decodeKeyFromBase64(String base64Key) {
        if (base64Key == null || base64Key.isEmpty()) {
            log.error("복원할 Base64 인코딩된 키가 없습니다. base64Key: {}", base64Key);
            throw new IllegalArgumentException("복원할 Base64 인코딩된 키가 없습니다.");
        }
        log.debug("Base64 AES 키 복원 시작.");
        byte[] decodedKey = Base64.getDecoder().decode(base64Key);
        return new SecretKeySpec(decodedKey, 0, decodedKey.length, "AES");
    }

    // S3에서 스트리밍 방식으로 파일을 다운로드
    public InputStream streamingDownloadFile(String bucketName, String key) {
        try {
            log.info("S3 파일 다운로드 시작. 버킷: {}, 파일 키: {}", bucketName, key);
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();

            ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(getObjectRequest);
            log.info("S3 파일 스트리밍 시작. 버킷: {}, 파일 키: {}", bucketName, key);
            return s3InputStream;

        } catch (RuntimeException e) {
            log.error("S3에서 파일 다운로드 실패. 버킷: {}, 파일 키: {}, 오류: {}", bucketName, key, e.getMessage(), e);
            throw new RuntimeException("S3에서 파일 다운로드 중 오류가 발생했습니다.", e);
        }
    }
}
