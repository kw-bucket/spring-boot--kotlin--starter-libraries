package com.kw.starter.s3.dto.copy

import software.amazon.awssdk.services.s3.model.CopyObjectRequest

data class S3CopyRequest(
    val sourceBucket: String,
    val sourceKey: String,
    val destinationBucket: String,
    val destinationKey: String,
)

data class S3CopyResult(
    val isSuccess: Boolean,
)

fun S3CopyRequest.asAwsCopyObjectRequest(): CopyObjectRequest =
    CopyObjectRequest
        .builder()
        .sourceBucket(sourceBucket)
        .sourceKey(sourceKey)
        .destinationBucket(destinationBucket)
        .destinationKey(destinationKey)
        .build()
