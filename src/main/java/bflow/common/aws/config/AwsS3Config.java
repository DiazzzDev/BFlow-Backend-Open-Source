package bflow.common.aws.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;

/**
 * AWS S3 configuration class.
 * Provides bean configuration for AWS S3 object storage.
 */
@Configuration
public class AwsS3Config {

    /**
     * AWS region for S3 service.
     */
    @Value("${aws.region}")
    private String region;

    /**
     * Create and configure S3 client bean.
     * Uses the AWS default credential provider chain; no static
     * credentials are configured here.
     *
     * @return configured S3Client instance
     */
    @Bean
    public S3Client s3Client() {
        return S3Client.builder()
                .region(Region.of(region))
                .build();
    }

    /**
     * Create and configure the S3 presigner used to issue presigned
     * upload/download URLs. Uses the AWS default credential provider
     * chain; no static credentials are configured here.
     *
     * @return configured S3Presigner instance
     */
    @Bean
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(region))
                .build();
    }
}
