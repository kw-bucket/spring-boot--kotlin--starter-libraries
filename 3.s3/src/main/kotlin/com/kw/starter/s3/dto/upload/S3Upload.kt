package com.kw.starter.s3.dto.upload

import software.amazon.awssdk.services.s3.model.PutObjectRequest
import java.nio.file.Path

data class S3UploadRequest(
    val bucket: String,
    val key: String,
    val localObjectPath: Path,
)

data class S3UploadResult(
    val bucket: String,
    val key: String,
    val isSuccess: Boolean,
)

fun S3UploadRequest.asAwsPutObjectRequest(): PutObjectRequest =
    PutObjectRequest
        .builder()
        .bucket(bucket)
        .key(key)
        .build()
