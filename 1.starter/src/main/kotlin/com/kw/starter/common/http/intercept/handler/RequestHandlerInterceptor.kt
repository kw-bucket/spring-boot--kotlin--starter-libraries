package com.kw.starter.common.http.intercept.handler

import com.kw.starter.common.http.constant.HeaderFields
import com.kw.starter.common.log.constant.LogbackFields
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.slf4j.MDC
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class RequestHandlerInterceptor : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        MDC.put(LogbackFields.CORRELATION_ID, request.getHeader(HeaderFields.X_CORRELATION_ID))
        MDC.put(LogbackFields.TRACE_ID, request.getHeader(HeaderFields.TRACE_ID))
        MDC.put(LogbackFields.SPAN_ID, request.getHeader(HeaderFields.SPAN_ID))
        MDC.put(LogbackFields.SPAN_EXPORT, request.getHeader(HeaderFields.SPAN_EXPORT))

        return true
    }
}
