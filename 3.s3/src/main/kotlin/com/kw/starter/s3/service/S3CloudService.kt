package com.kw.starter.s3.service

import com.kw.starter.s3.dto.copy.S3CopyRequest
import com.kw.starter.s3.dto.copy.S3CopyResult
import com.kw.starter.s3.dto.copy.asAwsCopyObjectRequest
import com.kw.starter.s3.dto.delete.S3DeleteRequest
import com.kw.starter.s3.dto.delete.asAwsDeleteObjectRequest
import com.kw.starter.s3.dto.download.S3DownloadRequest
import com.kw.starter.s3.dto.download.S3DownloadResult
import com.kw.starter.s3.dto.download.asAwsGetObjectRequest
import com.kw.starter.s3.dto.upload.S3UploadRequest
import com.kw.starter.s3.dto.upload.S3UploadResult
import com.kw.starter.s3.dto.upload.asAwsPutObjectRequest
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.model.HeadObjectRequest

@Service
class S3CloudService(
    private val s3Client: S3Client,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun uploadObject(request: S3UploadRequest): S3UploadResult {
        val putObjectResultOrNull =
            try {
                s3Client.putObject(
                    request.asAwsPutObjectRequest(),
                    request.localObjectPath,
                )
            } catch (ex: Exception) {
                null.also { displayLogError("Upload", ex) }
            }

        return S3UploadResult(
            bucket = request.bucket,
            key = request.key,
            isSuccess = putObjectResultOrNull != null,
        ).also { uploadResult ->
            displayLogInfo(
                command = "Upload",
                bucket = uploadResult.bucket,
                key = uploadResult.key,
                isSuccess = uploadResult.isSuccess,
            )
        }
    }

    fun downloadObject(request: S3DownloadRequest): S3DownloadResult {
        val objectMetaDataOrNull =
            try {
                s3Client.getObject(
                    request.asAwsGetObjectRequest(),
                    request.objectDownloadPath,
                )
            } catch (ex: Exception) {
                null.also { displayLogError("Download", ex) }
            }

        return S3DownloadResult(
            isSuccess = objectMetaDataOrNull != null,
        ).also { downloadResult ->
            displayLogInfo(
                command = "Download",
                bucket = request.bucket,
                key = request.key,
                isSuccess = downloadResult.isSuccess,
            )
        }
    }

    fun deleteObject(request: S3DeleteRequest) =
        s3Client.deleteObject(request.asAwsDeleteObjectRequest()).also {
            displayLogInfo(
                command = "Delete",
                bucket = request.bucket,
                key = request.key,
                isSuccess = true,
            )
        }

    fun copyObject(request: S3CopyRequest): S3CopyResult {
        val copyObjectResultOrNull =
            try {
                s3Client.copyObject(request.asAwsCopyObjectRequest())
            } catch (ex: Exception) {
                null.also { displayLogError("Copy", ex) }
            }

        return S3CopyResult(
            isSuccess = copyObjectResultOrNull != null,
        ).also { downloadResult ->
            displayLogInfo(
                command = "Copy",
                bucket = request.destinationBucket,
                key = request.destinationKey,
                isSuccess = downloadResult.isSuccess,
            )
        }
    }

    fun doesObjectExist(
        bucket: String,
        key: String,
    ): Boolean { // s3Client.doesObjectExist(bucket, key)
        val headObjectRequest =
            HeadObjectRequest
                .builder()
                .bucket(bucket)
                .key(key)
                .build()

        val headObjectResponseOrNull =
            try {
                s3Client.headObject(headObjectRequest)
            } catch (ex: Exception) {
                null.also { displayLogError("HeadObject", ex) }
            }

        return headObjectResponseOrNull != null
    }

    private fun displayLogInfo(
        command: String,
        bucket: String,
        key: String,
        isSuccess: Boolean,
    ) = logger.info(
        """
        |AWS S3 {}
        |:. Bucket: [{}]
        |:. Key: [{}]
        |:. Successful? [{}]
        """.trimMargin(),
        command,
        bucket,
        key,
        isSuccess,
    )

    private fun displayLogError(
        command: String,
        ex: Exception,
    ) = logger.error(
        """
        |AWS S3 {} Error!
        |:. Exception! [{}]
        |:. Cause: [{}]
        |:. Stack Trace: {}
        """.trimMargin(),
        command,
        ex.javaClass.canonicalName,
        ex.cause?.javaClass?.canonicalName,
        ex.stackTrace.joinToString(separator = "\n\t") { s -> s.toString() },
    )
}
