package com.kw.starter.common.http.constant

import kotlin.collections.plus

object Paths {
    val actuator =
        listOf(
            "/health",
            "/info",
            "/metrics",
        )

    val exclusion =
        actuator +
            listOf(
                "/prometheus",
                "/actuator**",
                "/swagger-ui.html",
                "/swagger-resources/**",
                "/webjars/**",
                "/v2/api-docs",
                "/configuration/**",
            )
}
