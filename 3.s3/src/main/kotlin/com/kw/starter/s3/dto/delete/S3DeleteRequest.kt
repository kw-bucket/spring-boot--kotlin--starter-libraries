package com.kw.starter.s3.dto.delete

import software.amazon.awssdk.services.s3.model.DeleteObjectRequest

data class S3DeleteRequest(
    val bucket: String,
    val key: String,
)

fun S3DeleteRequest.asAwsDeleteObjectRequest(): DeleteObjectRequest =
    DeleteObjectRequest
        .builder()
        .bucket(bucket)
        .key(key)
        .build()
