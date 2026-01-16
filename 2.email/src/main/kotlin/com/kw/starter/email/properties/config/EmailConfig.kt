package com.kw.starter.email.properties.config

data class EmailConfig(
    val subject: String,
    val from: String,
    val to: String,
    val cc: String? = null,
    val bcc: String? = null,
    val bodyTemplate: String? = null,
)
