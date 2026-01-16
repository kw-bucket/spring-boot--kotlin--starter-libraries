package com.kw.starter.email.extension.request

import com.kw.starter.email.dto.notification.EmailNotificationRequest
import org.springframework.core.io.FileSystemResource
import org.springframework.util.LinkedMultiValueMap
import org.springframework.util.MultiValueMap

fun EmailNotificationRequest.asMap(): MultiValueMap<String, Any> {
    val map: MultiValueMap<String, Any> = LinkedMultiValueMap()
    map.add("email_from", this.from)
    map.add("emails_to", this.to)
    map.add("subject", this.subject)
    map.add("body", this.body)

    this.cc
        ?.takeIf {
            it.isNotBlank()
        }?.also {
            map.add("emails_cc", it)
        }

    this.bcc
        ?.takeIf {
            it.isNotBlank()
        }?.also {
            map.add("emails_bcc", it)
        }

    this.files
        ?.map {
            FileSystemResource(it)
        }?.takeIf {
            it.isNotEmpty()
        }?.also {
            map.addAll("files", it)
        }

    return map
}
