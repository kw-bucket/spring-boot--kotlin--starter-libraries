package com.kw.starter.email.dto.notification

import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.databind.annotation.JsonNaming
import java.io.File

@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy::class)
@JsonInclude(JsonInclude.Include.NON_NULL)
data class EmailNotificationRequest(
    val from: String,
    val to: String,
    val subject: String,
    val body: String,
    val cc: String? = null,
    val bcc: String? = null,
    val files: List<File>? = null,
)
