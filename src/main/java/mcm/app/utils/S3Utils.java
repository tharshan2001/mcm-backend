package mcm.app.utils;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Utility class for uploading files to AWS S3.
 * Ensures public URL works even with special characters in filenames.
 */
@Component
public class S3Utils {

    private final S3Client s3Client;

    @Value("${AWS_BUCKET_NAME}")
    private String bucketName;

    @Value("${AWS_REGION}")
    private String region;

    public S3Utils(S3Client s3Client) {
        this.s3Client = s3Client;
    }

    public String uploadFile(MultipartFile file, String folder) throws IOException {
        if (folder == null || folder.isEmpty()) folder = "";
        else if (!folder.endsWith("/")) folder += "/";

        // Generate unique key
        String key = folder + UUID.randomUUID() + "-" + file.getOriginalFilename();

        // Build request with Content-Type fallback
        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(file.getContentType() != null ? file.getContentType() : "application/octet-stream")
                .build();

        s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));

        // URL encode the key to handle spaces/special characters
        String encodedKey = URLEncoder.encode(key, StandardCharsets.UTF_8.toString()).replace("+", "%20");

        // Return public URL
        return "https://" + bucketName + ".s3." + region + ".amazonaws.com/" + encodedKey;
    }

    public String uploadProductImage(MultipartFile file) throws IOException {
        return uploadFile(file, "products");
    }

    public List<String> uploadProductImages(List<MultipartFile> files) {
        return files.stream().map(file -> {
            try {
                return uploadProductImage(file);
            } catch (IOException e) {
                throw new RuntimeException("Failed to upload file: " + file.getOriginalFilename(), e);
            }
        }).collect(Collectors.toList());
    }
}