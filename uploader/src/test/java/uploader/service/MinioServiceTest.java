package uploader.service;

import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.lang.reflect.Field;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MinioServiceTest {

    private MinioService service;
    private MinioClient minio;

    @BeforeEach
    void setup() throws Exception {
        service = new MinioService();
        minio = mock(MinioClient.class);

        Field clientField = MinioService.class.getDeclaredField("minioClient");
        clientField.setAccessible(true);
        clientField.set(service, minio);

        Field bucket = MinioService.class.getDeclaredField("bucketName");
        bucket.setAccessible(true);
        bucket.set(service, "test");
    }

    @Test
    void upload_invokes_minio() throws Exception {
        service.upload("obj", new ByteArrayInputStream("x".getBytes()), 1);
        verify(minio).putObject(any(PutObjectArgs.class));
    }

    @Test
    void upload_wraps_exception() throws Exception {
        doThrow(new RuntimeException("fail")).when(minio).putObject(any(PutObjectArgs.class));
        assertThatThrownBy(() -> service.upload("obj", new ByteArrayInputStream(new byte[0]), 0))
                .hasMessageContaining("MinIO upload failed");
    }
}
