package com.kw.starter.common.config.metric

import io.micrometer.common.KeyValue
import io.micrometer.common.KeyValues
import org.springframework.http.client.observation.ClientHttpObservationDocumentation.LowCardinalityKeyNames
import org.springframework.http.client.observation.ClientRequestObservationContext
import org.springframework.http.client.observation.DefaultClientRequestObservationConvention

class CustomizedClientRequestObservationConvention : DefaultClientRequestObservationConvention() {
    override fun getLowCardinalityKeyValues(context: ClientRequestObservationContext): KeyValues =
        super
            .getLowCardinalityKeyValues(context)
            .and(additionalTags(context))

    private fun additionalTags(context: ClientRequestObservationContext): KeyValues {
        val keyValues = KeyValues.empty()

        val uriVariablePattern = "\\{(.*?)}".toRegex()
        val originalUri = super.uri(context).value
        val newUri = originalUri.replace(regex = uriVariablePattern, replacement = "{_}")

        val uriSegments = newUri.split("?")

        return keyValues
            .and(KeyValue.of(LowCardinalityKeyNames.URI, newUri))
            .and("uri.path", uriSegments.getOrElse(0) { "none" })
            .and("uri.query", uriSegments.getOrElse(1) { "none" })
    }
}
