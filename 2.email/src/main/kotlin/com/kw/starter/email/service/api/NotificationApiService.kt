package com.kw.starter.email.service.api

import com.kw.starter.common.dto.ApiOutput
import com.kw.starter.common.http.constant.HeaderFields
import com.kw.starter.common.log.constant.LogbackFields
import com.kw.starter.common.service.api.ApiResponse
import com.kw.starter.common.service.api.ApiService
import com.kw.starter.email.dto.notification.EmailNotificationRequest
import com.kw.starter.email.extension.request.asMap
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.slf4j.MDC
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponents
import org.springframework.web.util.UriComponentsBuilder
import java.util.UUID

@Service
class NotificationApiService(
    restTemplate: RestTemplate,
) : ApiService(restTemplate) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)

    @Value("\${api.notification.endpoint.send-email}")
    private val sendEmailUrl: String = "send-email"

    fun sendEmail(request: EmailNotificationRequest): ApiResponse<ApiOutput<Nothing>> {
        val suffix = UUID.randomUUID().toString().take(6)

        val headers = HttpHeaders()
        headers.contentType = MediaType.MULTIPART_FORM_DATA
        headers.set(HeaderFields.CORRELATIONID, "${MDC.get(LogbackFields.CORRELATION_ID)}-$suffix")
        headers.set(HeaderFields.X_CORRELATION_ID, "${MDC.get(LogbackFields.CORRELATION_ID)}-$suffix")

        val uriComponents: UriComponents = UriComponentsBuilder.fromHttpUrl(sendEmailUrl).build()
        val responseType = object : ParameterizedTypeReference<ApiOutput<Nothing>>() {}
        val arguments = request.asMap()

        logger.debug(
            """
            Send Email Notification:
                - Endpoint: {}
                - Header: {}
                - Arguments: {}
            """.trimIndent(),
            uriComponents.toUriString(),
            headers,
            arguments,
        )

        return execute(HttpMethod.POST, uriComponents, HttpEntity(arguments, headers), responseType)
    }
}
