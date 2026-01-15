package com.kw.starter.common.service.api

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.kw.starter.common.extension.string.collapse
import org.apache.hc.client5.http.ConnectTimeoutException
import org.apache.hc.core5.http.ConnectionRequestTimeoutException
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.core.ParameterizedTypeReference
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpStatusCodeException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponents
import java.lang.reflect.Type
import java.net.SocketTimeoutException

open class ApiService(
    private val restTemplate: RestTemplate,
) {
    private val logger: Logger = LoggerFactory.getLogger(this::class.java)
    private val mapper =
        jacksonObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
            .registerModule(JavaTimeModule())

    @Value("\${starter.api.logging.enabled:true}")
    private val isLogEnabled: Boolean = true

    @Value("\${starter.api.logging.body-length:100}")
    private val strLogLength: Int = 100

    fun <T> execute(
        httpMethod: HttpMethod,
        uriComponents: UriComponents,
        httpEntity: HttpEntity<Any>?,
        responseType: ParameterizedTypeReference<T>,
        uriVariables: Map<String, Any> = emptyMap(),
    ): ApiResponse<T> {
        if (isLogEnabled) {
            logger.info(
                """
                    |Api Request >> {} {}
                    |:. query= {}
                    |:. uri.variables= {}
                    |:. headers= {}
                    |:. body= [{}]
                """.trimMargin(),
                httpMethod.name(),
                uriComponents.path,
                uriComponents.query ?: "None",
                uriVariables.entries.joinToString(prefix = "[", postfix = "]") { "${it.key}: \"${it.value}\"" },
                httpEntity?.headers?.toString() ?: "None",
                httpEntity?.body?.let {
                    val body =
                        when (httpEntity.headers.contentType) {
                            MediaType.APPLICATION_JSON -> mapper.writeValueAsString(it)
                            else -> it.toString()
                        }

                    body.collapse(length = strLogLength)
                } ?: "None",
            )
        }

        val apiResponse =
            try {
                val response = call(httpMethod, uriComponents, httpEntity, responseType, uriVariables)
                val httpStatus = HttpStatus.valueOf(response.statusCode.value())

                if (response.statusCode.is2xxSuccessful) {
                    ApiResponse.Success(
                        httpStatus = httpStatus,
                        httpHeaders = response.headers,
                        body = response.body,
                    )
                } else {
                    ApiResponse.Failure(
                        httpStatus = httpStatus,
                        httpHeaders = response.headers,
                        body = response.body,
                    )
                }
            } catch (ex: HttpStatusCodeException) {
                ApiResponse
                    .Error(
                        httpStatus = HttpStatus.valueOf(ex.statusCode.value()),
                        httpHeaders = ex.responseHeaders ?: HttpHeaders.EMPTY,
                        bodyAsString = ex.responseBodyAsString,
                        cause = ex,
                    ).also {
                        logger.error(
                            """
                                |Api Request ** {} {}
                                |:. Http Specific Exception! <{}>
                                |:. Stack Trace: {}
                            """.trimMargin(),
                            httpMethod.name(),
                            uriComponents.path,
                            ex.javaClass.canonicalName,
                            ex.stackTrace?.joinToString(separator = "\n\t") { s -> s.toString() },
                        )
                    }
            } catch (ex: Exception) {
                val (httpStatus, cause) =
                    when (ex) {
                        is ResourceAccessException ->
                            when (val cause: Throwable? = ex.cause) {
                                is ConnectionRequestTimeoutException,
                                is ConnectTimeoutException,
                                -> Pair(HttpStatus.INTERNAL_SERVER_ERROR, cause)
                                is SocketTimeoutException -> Pair(HttpStatus.GATEWAY_TIMEOUT, cause)
                                else -> Pair(HttpStatus.INTERNAL_SERVER_ERROR, ex)
                            }

                        else ->
                            Pair(HttpStatus.INTERNAL_SERVER_ERROR, ex)
                    }

                ApiResponse
                    .Error(
                        httpStatus = httpStatus,
                        cause = cause,
                    ).also {
                        logger.error(
                            """
                                |Api Request !! {} {}
                                |:. Unexpected Exception! <{}>
                                |:. Stack Trace: {}
                            """.trimMargin(),
                            httpMethod.name(),
                            uriComponents.path,
                            (it.cause ?: ex).javaClass.canonicalName,
                            (it.cause ?: ex).stackTrace.joinToString(separator = "\n\t") { s -> s.toString() },
                        )
                    }
            }

        return apiResponse.also {
            if (isLogEnabled) {
                logger.info(
                    """
                        |Api Response << {} {} [{}]
                        |:. headers= {}
                        |:. body= [{}]
                    """.trimMargin(),
                    httpMethod.name(),
                    uriComponents.path,
                    it.httpStatus,
                    it.httpHeaders.toString(),
                    it.body?.let { b ->
                        mapper.writeValueAsString(b).collapse(length = strLogLength)
                    } ?: "None",
                )
            }
        }
    }

    @Throws(
        HttpStatusCodeException::class,
        RestClientException::class,
    )
    fun <T> call(
        httpMethod: HttpMethod,
        uriComponents: UriComponents,
        httpEntity: HttpEntity<Any>?,
        responseType: ParameterizedTypeReference<T>,
        uriVariables: Map<String, Any>,
    ): ResponseEntity<T> =
        try {
            restTemplate.exchange(
                uriComponents.toUriString(),
                httpMethod,
                httpEntity,
                responseType,
                uriVariables,
            )
        } catch (ex: HttpStatusCodeException) {
            handleHttpException(ex, responseType.type)
        } catch (ex: RestClientException) {
            throw ex
        }

    @Throws(HttpStatusCodeException::class)
    private fun <T> handleHttpException(
        httpException: HttpStatusCodeException,
        responseType: Type,
    ): ResponseEntity<T> =
        try {
            val appResponse: T =
                mapper.readValue(
                    httpException.responseBodyAsString,
                    object : TypeReference<T>() {
                        override fun getType(): Type = responseType
                    },
                )

            ResponseEntity
                .status(httpException.statusCode)
                .headers(httpException.responseHeaders)
                .body(appResponse)
        } catch (ex: Exception) {
            throw httpException
        }
}
