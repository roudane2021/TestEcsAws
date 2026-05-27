package com.roudane.testecsaws.controller;

import com.roudane.testecsaws.dto.S3UploadResponse;
import com.roudane.testecsaws.exception.S3Exception;
import com.roudane.testecsaws.service.S3Service;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.Base64;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

@Slf4j
@RestController
@RequestMapping("/api/images")
public class ImageController {

    @Autowired
    private S3Service s3Service;

    /**
     * Endpoint POST pour uploader une image vers S3
     * @param file Le fichier image à uploader
     * @return Réponse avec les informations du fichier uploadé
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadImage(@RequestParam("file") MultipartFile file) {
        try {
            log.info("Début de l'upload du fichier : {}", file.getOriginalFilename());

            // Upload du fichier
            String key = s3Service.uploadImage(file);

            // Génération de l'URL publique
            String publicUrl = s3Service.getPublicUrl(key);

            // Création de la réponse
            S3UploadResponse response = new S3UploadResponse(
                    file.getOriginalFilename(),
                    key,
                    "Image uploadée avec succès",
                    file.getSize(),
                    file.getContentType(),
                    null
            );

            log.info("Upload réussi : {} (URL: {})", file.getOriginalFilename(), publicUrl);

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(response);

        } catch (S3Exception e) {
            log.error("Erreur lors de l'upload : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Erreur lors de l'upload : " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors de l'upload", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    /**
     * Endpoint GET pour télécharger une image depuis S3
     * @param filename La clé du fichier à télécharger
     * @return Les bytes de l'image avec le bon Content-Type
     */
    @GetMapping("/{filename}")
    public ResponseEntity<?> downloadImage(@PathVariable String filename) {
        try {
            log.info("Début du téléchargement du fichier : {}", filename);

            // Récupération du fichier depuis S3
            byte[] imageBytes = s3Service.downloadImage(filename);

            // Récupération du type de contenu
            String contentType = s3Service.getImageContentType(filename);

            log.info("Téléchargement réussi : {} (taille: {} bytes)", filename, imageBytes.length);

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"" + filename + "\"")
                    .contentType(MediaType.parseMediaType(contentType))
                    .body(imageBytes);

        } catch (S3Exception e) {
            log.error("Erreur lors du téléchargement : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erreur : " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue lors du téléchargement", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    /**
     * Endpoint GET pour obtenir l'URL publique d'une image
     * @param filename La clé du fichier
     * @return L'URL publique de l'image avec les vraies métadonnées et le contenu encodé en base64
     */
    @GetMapping("/url")
    public ResponseEntity<?> getImageUrl(@RequestParam("filename") String filename) {
        try {
            // Récupération du contenu du fichier
            byte[] imageBytes = s3Service.downloadImage(filename);

            // Récupération des métadonnées du fichier
            String contentType = s3Service.getImageContentType(filename);
            long fileSize = s3Service.getImageSize(filename);

            // Génération de l'URL publique
            String publicUrl = s3Service.getPublicUrl(filename);

            // Encodage du contenu en base64
            String contentBase64 = Base64.getEncoder().encodeToString(imageBytes);

            log.info("URL générée pour : {} -> {} (taille: {} bytes, type: {}, contenu encodé: {} chars)",
                    filename, publicUrl, fileSize, contentType, contentBase64.length());

            return ResponseEntity.ok()
                    .body(new S3UploadResponse(filename, filename, publicUrl, fileSize, contentType, contentBase64));

        } catch (S3Exception e) {
            log.error("Erreur lors de la génération de l'URL : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body("Erreur : " + e.getMessage());
        } catch (Exception e) {
            log.error("Erreur inattendue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    /**
     * Endpoint GET pour obtenir l'adresse IP publique du serveur
     * @return L'adresse IP publique du serveur avec d'autres informations réseau
     */
    @GetMapping("/server-ip")
    public ResponseEntity<?> getServerIp() {
        try {
            log.info("Récupération de l'adresse IP publique du serveur");

            // Récupération de l'adresse IP locale
            InetAddress inetAddress = InetAddress.getLocalHost();
            String privateIpAddress = inetAddress.getHostAddress();
            String hostname = inetAddress.getHostName();

            // Récupération de l'adresse IP publique via un service externe
            String publicIpAddress = getPublicIpAddress();

            // Création de la réponse
            Map<String, String> response = new HashMap<>();
            response.put("privateIpAddress", privateIpAddress);
            response.put("publicIpAddress", publicIpAddress);
            response.put("hostname", hostname);
            response.put("message", "Adresse IP du serveur");

            log.info("Adresse IP du serveur - Privée: {} | Publique: {} (hostname: {})", privateIpAddress, publicIpAddress, hostname);

            return ResponseEntity.ok(response);

        } catch (UnknownHostException e) {
            log.error("Erreur lors de la récupération de l'adresse IP : {}", e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur : impossible de récupérer l'adresse IP du serveur");
        } catch (Exception e) {
            log.error("Erreur inattendue", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Erreur serveur : " + e.getMessage());
        }
    }

    /**
     * Méthode utilitaire pour récupérer l'adresse IP publique
     * @return L'adresse IP publique du serveur
     * @throws Exception Si la requête échoue
     */
    private String getPublicIpAddress() throws Exception {
        URL url = new URL("http://checkip.amazonaws.com");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);

        try (BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()))) {
            String ip = reader.readLine().trim();
            return ip;
        } finally {
            connection.disconnect();
        }
    }
}
