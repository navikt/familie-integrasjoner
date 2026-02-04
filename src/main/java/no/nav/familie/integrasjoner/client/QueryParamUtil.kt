package no.nav.familie.integrasjoner.client

import no.nav.familie.kontrakter.felles.jsonMapper
import org.springframework.util.LinkedMultiValueMap
import tools.jackson.module.kotlin.readValue

object QueryParamUtil {
    fun toQueryParams(any: Any): LinkedMultiValueMap<String, String> {
        val writeValueAsString = jsonMapper.writeValueAsString(any)
        val readValue: LinkedHashMap<String, Any?> = jsonMapper.readValue(writeValueAsString)
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
