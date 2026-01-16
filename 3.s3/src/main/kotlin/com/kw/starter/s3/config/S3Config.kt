package com.kw.starter.s3.config

import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.s3.S3Client

@Configuration
class S3Config {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${cloud.aws.credentials.access-key:#{null}}")
    private val awsAccessKey: String? = null

    @Value("\${cloud.aws.credentials.secret-key:#{null}}")
    private val awsSecretKey: String? = null

    @Value("\${cloud.aws.region.static:ap-southeast-1}")
    private val region: String = "ap-southeast-1"

    @Bean
    fun s3Client(): S3Client =
        Triple(region, awsAccessKey, awsSecretKey)
            .also {
                logger.debug(
                    """
                        |AWS S3 Configuration:
                        |:. Region[{}]
                        |:. Access Key [Shhh!]
                        |:. Secret Key [Shhh!]
                    """.trimMargin(),
                    region,
                )
            }.takeIf {
                !it.first.isNullOrBlank() &&
                    !it.second.isNullOrBlank() &&
                    !it.third.isNullOrBlank()
            }?.let {
                staticS3Client(region!!, awsAccessKey!!, awsSecretKey!!)
            } ?: defaultS3Client()

    fun staticS3Client(
        region: String,
        awsAccessKey: String,
        awsSecretKey: String,
    ): S3Client =
        S3Client
            .builder()
            .region(Region.of(region))
            .credentialsProvider(
                StaticCredentialsProvider.create(
                    AwsBasicCredentials.create(awsAccessKey, awsSecretKey),
                ),
            ).build()
            .also { logger.debug("Initiate S3 Client Using Static Credentials Provider") }

    fun defaultS3Client(): S3Client =
        S3Client
            .builder()
            .region(Region.of(region))
            .build()
            .also { logger.debug("Initiate S3 Client Using Default Credentials Provider Chain") }
}
