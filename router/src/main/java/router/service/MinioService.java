package router.service;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.InputStream;

@Service
public class MinioService {

    private final MinioClient minioClient;

    @Value("${filepasser.storage.bucket-name}")
    private String bucketName;

    @Value("${filepasser.storage.endpoint}")
    private String endpoint;

    public MinioService(
            @Value("${filepasser.storage.endpoint}") String endpoint,
            @Value("${filepasser.storage.access-key}") String accessKey,
            @Value("${filepasser.storage.secret-key}") String secretKey) {

        this.minioClient = MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }

    @PostConstruct
    public void ensureBucket() {
        try {
            boolean found = minioClient.bucketExists(
                    BucketExistsArgs.builder().bucket(bucketName).build());
            if (!found) {
                minioClient.makeBucket(
                        MakeBucketArgs.builder().bucket(bucketName).build());
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize bucket: " + e.getMessage(), e);
        }
    }

    public String upload(String objectName, InputStream stream, long size) {
        try {
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(stream, size, -1)
                            .build());

            return String.format("%s/%s/%s", endpoint, bucketName, objectName);

        } catch (Exception e) {
            throw new RuntimeException("File upload failed: " + e.getMessage(), e);
        }
    }
}
