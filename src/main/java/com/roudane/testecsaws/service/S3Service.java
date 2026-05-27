package com.roudane.testecsaws.service;

import com.roudane.testecsaws.exception.S3Exception;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
public class S3Service {

    @Autowired
    private S3Client s3Client;

    @Value("${aws.s3.bucket-name}")
    private String bucketName;

    @Value("${aws.s3.region}")
    private String region;

    /**
     * Upload une image vers S3
     * @param file Le fichier multipart à uploader
     * @return La clé S3 du fichier uploadé
     */
    public String uploadImage(MultipartFile file) {
        // Validation du fichier
        if (file.isEmpty()) {
            throw new S3Exception("Le fichier ne peut pas être vide");
        }

        // Validation du type MIME
        String contentType = file.getContentType();
        if (!isValidImageType(contentType)) {
            throw new S3Exception("Type de fichier non autorisé. Seules les images sont acceptées. Type reçu: " + contentType);
        }

        // Génération d'une clé unique
        String key = generateUniqueKey(file.getOriginalFilename());

        try {
            // Upload du fichier vers S3
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .contentType(contentType)
                    .build();

            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

            log.info("Image uploadée avec succès : {} (clé: {})", file.getOriginalFilename(), key);

            return key;

        } catch (IOException e) {
            log.error("Erreur lors de la lecture du fichier : {}", file.getOriginalFilename(), e);
            throw new S3Exception("Erreur lors de la lecture du fichier", e);
        } catch (S3Exception e) {
            log.error("Erreur AWS S3 lors de l'upload : {}", e.getMessage(), e);
            throw new S3Exception("Erreur lors de l'upload vers S3 : " + e.getMessage(), e);
        }
    }

    /**
     * Télécharge une image depuis S3
     * @param filename La clé du fichier à télécharger
     * @return Les bytes du fichier
     */
    public byte[] downloadImage(String filename) {
        try {
            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            byte[] imageBytes = s3Client.getObjectAsBytes(getObjectRequest).asByteArray();

            log.info("Image téléchargée avec succès : {}", filename);

            return imageBytes;

        } catch (NoSuchKeyException e) {
            log.error("Le fichier n'existe pas : {}", filename);
            throw new S3Exception("Le fichier n'existe pas : " + filename);
        } catch (Exception e) {
            log.error("Erreur AWS S3 lors du download : {}", e.getMessage(), e);
            throw new S3Exception("Erreur lors du téléchargement depuis S3 : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère le type de contenu (MIME type) d'un fichier
     * @param filename La clé du fichier
     * @return Le MIME type du fichier
     */
    public String getImageContentType(String filename) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentType();

        } catch (NoSuchKeyException e) {
            log.error("Le fichier n'existe pas : {}", filename);
            throw new S3Exception("Le fichier n'existe pas : " + filename);
        } catch (S3Exception e) {
            log.error("Erreur AWS S3 : {}", e.getMessage(), e);
            throw new S3Exception("Erreur lors de la récupération des métadonnées : " + e.getMessage(), e);
        }
    }

    /**
     * Récupère la taille d'un fichier dans S3
     * @param filename La clé du fichier
     * @return La taille du fichier en bytes
     */
    public long getImageSize(String filename) {
        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(filename)
                    .build();

            HeadObjectResponse response = s3Client.headObject(headObjectRequest);
            return response.contentLength();

        } catch (NoSuchKeyException e) {
            log.error("Le fichier n'existe pas : {}", filename);
            throw new S3Exception("Le fichier n'existe pas : " + filename);
        } catch (S3Exception e) {
            log.error("Erreur AWS S3 : {}", e.getMessage(), e);
            throw new S3Exception("Erreur lors de la récupération de la taille : " + e.getMessage(), e);
        }
    }

    /**
     * Génère une clé unique pour le fichier
     * @param originalFilename Le nom original du fichier
     * @return La clé unique (UUID + extension)
     */
    private String generateUniqueKey(String originalFilename) {
        if (originalFilename == null || originalFilename.isEmpty()) {
            return UUID.randomUUID() + ".jpg";
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        return "images/" + UUID.randomUUID() + extension;
    }

    /**
     * Valide le type de fichier
     * @param contentType Le type MIME du fichier
     * @return true si le type est un type d'image autorisé
     */
    private boolean isValidImageType(String contentType) {
        return contentType != null && (
                contentType.equals("image/jpeg") ||
                contentType.equals("image/jpg") ||
                contentType.equals("image/png") ||
                contentType.equals("image/gif") ||
                contentType.equals("image/webp")
        );
    }

    /**
     * Génère l'URL publique d'une image dans S3
     * @param key La clé du fichier
     * @return L'URL publique du fichier
     */
    public String getPublicUrl(String key) {
        return String.format("https://%s.s3.%s.amazonaws.com/%s", bucketName, region, key);
    }
}
