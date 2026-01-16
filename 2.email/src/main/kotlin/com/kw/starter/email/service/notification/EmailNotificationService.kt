package com.kw.starter.email.service.notification

import com.kw.starter.email.dto.notification.EmailNotificationRequest
import com.kw.starter.email.service.api.NotificationApiService
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service

@Service
class EmailNotificationService(
    private val notificationApiService: NotificationApiService,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    fun sendEmail(request: EmailNotificationRequest) = callEmailNotificationApi(request)

    private fun callEmailNotificationApi(request: EmailNotificationRequest) = notificationApiService.sendEmail(request)
}
