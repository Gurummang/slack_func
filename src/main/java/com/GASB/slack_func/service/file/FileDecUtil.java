package com.GASB.slack_func.service.file;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Base64;

@Component
@Slf4j
@RequiredArgsConstructor
public class FileDecUtil {

    @Value("${file.aes.key}")
    private String file_key;

    @Value("${file.dec.basepath}")
    private String basePath;

    @Value("${aws.s3.bucket}")
    private String bucketName;

    private final S3Client s3Client;




    /**
     * 파일을 S3에서 받아 복호화한 후 메모리에 저장하는 메서드
     * @param bucketName S3 버킷 이름
     * @param objectKey S3 객체 키
     * @return 복호화된 파일의 바이트 배열
     * @throws Exception
     */
    public byte[] decryptFileFromS3(String bucketName, String objectKey) throws Exception {
        InputStream s3InputStream = getS3ObjectInputStream(bucketName, objectKey);
        CipherInputStream cipherInputStream = createDecryptionStream(s3InputStream, file_key);
        return convertStreamToByteArray(cipherInputStream);
    }

    private InputStream getS3ObjectInputStream(String bucketName, String objectKey) {
        GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .build();

        ResponseInputStream<GetObjectResponse> s3InputStream = s3Client.getObject(getObjectRequest);
        return s3InputStream;
    }

    private CipherInputStream createDecryptionStream(InputStream inputStream, String aesKey) throws Exception {
        byte[] decodeKey = Base64.getDecoder().decode(aesKey);
        SecretKeySpec secretKeySpec = new SecretKeySpec(decodeKey, "AES");

        byte[] iv = new byte[16];
        inputStream.read(iv);

        IvParameterSpec ivParameterSpec = new IvParameterSpec(iv);

        Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
        cipher.init(Cipher.DECRYPT_MODE, secretKeySpec, ivParameterSpec);

        return new CipherInputStream(inputStream, cipher);
    }

    private byte[] convertStreamToByteArray(InputStream inputStream) throws Exception {
        try (ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[8192];
            int bytesRead;

            // 스트리밍 데이터를 ByteArrayOutputStream에 누적
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                byteArrayOutputStream.write(buffer, 0, bytesRead);
            }

            return byteArrayOutputStream.toByteArray();
        }
    }
}
