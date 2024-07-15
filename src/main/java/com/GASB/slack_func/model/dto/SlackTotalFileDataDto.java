package com.GASB.slack_func.model.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SlackTotalFileDataDto {
    private String Status;
    private List<FileDetail> files;

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class FileDetail {
        private String fileId;
        private LocalDateTime timestamp;
        private String fileName;
        private String fileType;
        @Builder.Default
        private String saasName = "slack";
        private String username;
        private String filePath;
        private GScanResult gScanResult;
        private VtScanResult vtScanResult;
        private DlpScanResult dlpScanResult;

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class GScanResult {
            @Builder.Default
            private String status = "unknown";
            @Builder.Default
            private String typeMatch = "false";
            @Builder.Default
            private List<String> detail = Collections.emptyList();
        }

        @Data
        @Builder
        @AllArgsConstructor
        @NoArgsConstructor
        public static class VtScanResult {
            @Builder.Default
            private String threatLabel = "Trojan";

            @Builder.Default
            private String hash = "03c7c0ace395d80182db07ae2c30f034";

            @Builder.Default
            private int detectEngine = 4;
            @Builder.Default
            private int score = 60;

            @Builder.Default
            private String V3 = "Trojan";
            @Builder.Default
            private String ALYac = "Trojan";
            @Builder.Default
            private String Kaspersky = "Trojan";
            @Builder.Default
            private String Falcon = "Trojan";
            @Builder.Default
            private String Avast = "Trojan";
            @Builder.Default
            private String Sentinelone = "Trojan";
            @Builder.Default
            private String reportUrl = "https://www.virustotal.com/gui/file/03c7c0ace395d80182db07ae2c30f034/detection";
        }

        @Data
        @Builder
        @NoArgsConstructor
        @AllArgsConstructor
        public static class DlpScanResult {
            @Builder.Default
            private String status = "unknown";
            @Builder.Default
            private String result = "unknown";
            @Builder.Default
            private String details = "unknown";
            @Builder.Default
            private List<String> sensitiveDataTypes = Collections.emptyList();
            @Builder.Default
            private int instancesFound = 0;
        }
    }
}
