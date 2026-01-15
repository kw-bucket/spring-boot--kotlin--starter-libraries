package com.kw.starter.common.http.intercept.secure

import com.fasterxml.jackson.module.kotlin.jsonMapper
import com.kw.starter.common.config.annotation.SecureEndpoint
import com.kw.starter.common.dto.ApiOutput
import com.kw.starter.common.http.constant.HeaderFields
import com.kw.starter.common.service.log.HttpLoggingService
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.MediaType
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.HandlerInterceptor

@Component
class SecureEndpointInterceptor(
    private val httpLoggingService: HttpLoggingService,
) : HandlerInterceptor {
    @Value("\${starter.application-code:APP}")
    private val applicationCode: String = "APP"

    @Value("\${starter.api.auth.enabled:false}")
    private val apiAuthEnabled: Boolean = false

    @Value("\${starter.api.auth.key:DEFAULT-AUTH-KEY}")
    private val apiAuthKey: String = "DEFAULT-AUTH-KEY"

    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        if (!apiAuthEnabled) {
            return true
        }

        val handlerMethod = handler as HandlerMethod

        val isSecureEndpointAnnotationPresent =
            handlerMethod.bean.javaClass.isAnnotationPresent(SecureEndpoint::class.java) ||
                handlerMethod.hasMethodAnnotation(SecureEndpoint::class.java)
        if (!isSecureEndpointAnnotationPresent) {
            return true
        }

        val incomingKey: String? = request.getHeader(HeaderFields.X_API_KEY)
        if (incomingKey == apiAuthKey) {
            return true
        }

        response.status = HttpServletResponse.SC_UNAUTHORIZED
        response.contentType = MediaType.APPLICATION_JSON_VALUE
        with(response.writer) {
            val o: ApiOutput<Nothing> =
                ApiOutput.Companion.fromCustomStatus(
                    code = "${applicationCode}4010",
                    message = "Unauthorized",
                    description = "Please recheck authentication key!",
                )

            print(
                jsonMapper().writeValueAsString(o),
            ).also {
                httpLoggingService.displayResp(
                    httpRequest = request,
                    httpResponse = response,
                    responseBody = o,
                )
            }
            flush()
        }

        return false
    }
}
