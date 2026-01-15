package com.kw.starter.common.http.intercept.log

import com.kw.starter.common.service.log.HttpLoggingService
import jakarta.servlet.DispatcherType
import jakarta.servlet.http.HttpServletRequest
import jakarta.servlet.http.HttpServletResponse
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.servlet.HandlerInterceptor

@Component
class LogInterceptor(
    private val httpLoggingService: HttpLoggingService,
) : HandlerInterceptor {
    override fun preHandle(
        request: HttpServletRequest,
        response: HttpServletResponse,
        handler: Any,
    ): Boolean {
        val isDispatcherTypeRequest = request.dispatcherType == DispatcherType.REQUEST
        val isMethodGet = HttpMethod.valueOf(request.method) == HttpMethod.GET

        if (isDispatcherTypeRequest && isMethodGet) {
            httpLoggingService.displayReq(httpRequest = request, requestBody = null)
        }

        return true
    }
}
