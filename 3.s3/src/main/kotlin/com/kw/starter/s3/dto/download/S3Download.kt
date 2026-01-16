package com.kw.starter.s3.dto.download

import software.amazon.awssdk.services.s3.model.GetObjectRequest
import java.nio.file.Path

data class S3DownloadRequest(
    val bucket: String,
    val key: String,
    val objectDownloadPath: Path,
)

data class S3DownloadResult(
    val isSuccess: Boolean,
)

fun S3DownloadRequest.asAwsGetObjectRequest(): GetObjectRequest =
    GetObjectRequest
        .builder()
        .bucket(bucket)
        .key(key)
        .build()
