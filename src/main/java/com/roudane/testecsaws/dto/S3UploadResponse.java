package com.roudane.testecsaws.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class S3UploadResponse {
    private String filename;
    private String key;
    private String message;
    private long fileSize;
    private String contentType;
    private String content; // Contenu du fichier encodé en base64
}
