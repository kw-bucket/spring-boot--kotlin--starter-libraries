package com.kw.starter.email.extension.config

import com.kw.starter.email.dto.notification.EmailNotificationRequest
import com.kw.starter.email.properties.config.EmailConfig
import java.io.File

fun EmailConfig.toRequest(
    subject: String? = null,
    body: String,
    attachments: List<File>? = null,
): EmailNotificationRequest =
    EmailNotificationRequest(
        subject = subject ?: this.subject!!,
        from = this.from!!,
        to = this.to!!,
        cc = this.cc,
        bcc = this.bcc,
        body = body,
        files = attachments,
    )
