package no.nav.familie.integrasjoner.client

import com.fasterxml.jackson.module.kotlin.readValue
import no.nav.familie.kontrakter.felles.objectMapper
import org.springframework.util.LinkedMultiValueMap

object QueryParamUtil {
    fun toQueryParams(any: Any): LinkedMultiValueMap<String, String> {
        val writeValueAsString = objectMapper.writeValueAsString(any)
        val readValue: LinkedHashMap<String, Any?> = objectMapper.readValue(writeValueAsString)
        val queryParams = LinkedMultiValueMap<String, String>()
        readValue
            .filterNot { it.value == null }
            .filterNot { it.value is List<*> && (it.value as List<*>).isEmpty() }
            .forEach {
                if (it.value is List<*>) {
                    val liste = (it.value as List<*>).map { elem -> elem.toString() }
                    queryParams.addAll(it.key, liste)
                } else {
                    queryParams.add(it.key, it.value.toString())
                }
            }
        return queryParams
    }
}
