package com.GASB.slack_func.service.file;

import com.GASB.slack_func.model.entity.TypeScan;
import com.GASB.slack_func.model.entity.fileUpload;
import com.GASB.slack_func.repository.files.TypeScanRepo;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.tika.Tika;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Service
@Slf4j
@RequiredArgsConstructor
public class ScanUtil {

    private final Tika tika;
    private final TypeScanRepo typeScanRepo;

    @Async
    public void scanFile(String path, fileUpload fileUploadObject, String MIMEType) {
        try {
            File inputFile = getFile(path);
            if (!inputFile.exists() || !inputFile.isFile()) {
                log.error("Invalid file path: {}", path);
                return;
            }

            String fileExtension = getFileExtension(inputFile);
            String mimeType = MIMEType;
            if (mimeType == null || mimeType.isEmpty()) {
                mimeType = detectMimeType(inputFile);
            }
            String expectedMimeType = MimeType.getMimeTypeByExtension(fileExtension);
            String fileSignature = null;
            boolean isMatched = false;
            log.info("expectedMimeType : {}",expectedMimeType);
            log.info("mime type: {}", mimeType);
            // slack의 경우 캔버스 파일의 경우에는 확장자가 없고 mime타입이 text/html로 나오는데 어떻게하지...
            if (fileExtension.equals("txt")) {
                isMatched = mimeType.equals(expectedMimeType);
                AddData(fileUploadObject, isMatched, mimeType, "txt", fileExtension);
            } else if (fileExtension.isEmpty()){

                AddData(fileUploadObject, false, mimeType, "unknown", "unsupported");
            } else {
                fileSignature = getFileSignature(inputFile, fileExtension);
                isMatched = mimeType.equals(expectedMimeType) && fileSignature.equalsIgnoreCase(fileExtension);
                AddData(fileUploadObject, isMatched, mimeType, fileSignature, fileExtension);
            }
        } catch (IOException e) {
            log.error("Error scanning file", e);
        }
    }

    @Async
    @Transactional
    protected void AddData(fileUpload fileUploadObject, boolean correct, String mimeType, String signature, String extension) {
        if (fileUploadObject == null || fileUploadObject.getId() == null) {
            log.error("Invalid file upload object: {}", fileUploadObject);
            throw new IllegalArgumentException("Invalid file upload object");
        }
        TypeScan typeScan = TypeScan.builder()
                .file_upload(fileUploadObject)
                .correct(correct)
                .mimetype(mimeType)
                .signature(signature)
                .extension(extension)
                .build();
        typeScanRepo.save(typeScan);
    }


    private File getFile(String path) {
        return new File(path);
    }

    private String detectMimeType(File file) throws IOException {
        return tika.detect(file);
    }

    private String getFileExtension(File file) {
        String fileName = file.getName();
        int lastIndexOfDot = fileName.lastIndexOf('.');
        if (lastIndexOfDot == -1) {
            return ""; // 확장자가 없는 경우
        }
        return fileName.substring(lastIndexOfDot + 1).toLowerCase();
    }

    private String getFileSignature(File file, String extension) throws IOException {
        if (extension == null || extension.isEmpty()) {
            log.error("Invalid file extension: {}", extension);
            return "unknown"; // 기본값으로 "unknown"을 반환
        }

        int signatureLength = HeaderSignature.getSignatureLengthByExtension(extension);
        if (signatureLength == 0) {
            throw new IllegalArgumentException("Invalid file extension: " + extension);
        }

        byte[] bytes = new byte[signatureLength];

        try (FileInputStream fis = new FileInputStream(file)) {
            int bytesRead = fis.read(bytes);
            if (bytesRead < signatureLength) {
                throw new IOException("Could not read the complete file signature");
            }
        }

        StringBuilder sb = new StringBuilder(signatureLength * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02X", b));
        }

        String signatureHex = sb.toString();
        String detectedExtension = HeaderSignature.getExtensionBySignature(signatureHex, extension);
        log.info("Detected extension for signature {}: {}", signatureHex, detectedExtension);

        return detectedExtension;
    }
}
