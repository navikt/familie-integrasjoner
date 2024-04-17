package no.nav.familie.integrasjoner.config

import io.micrometer.core.instrument.Counter
import io.micrometer.core.instrument.Metrics
import jakarta.annotation.PostConstruct
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component
import org.springframework.web.method.HandlerMethod
import org.springframework.web.servlet.mvc.method.RequestMappingInfo
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping

@Component
class MetrikkerForEndepunkterInitialiserer {
    @Autowired
    lateinit var applicationContext: ApplicationContext

    @PostConstruct
    fun init() {
        val requestMappingHandlerMapping: RequestMappingHandlerMapping =
            applicationContext
                .getBean("requestMappingHandlerMapping", RequestMappingHandlerMapping::class.java)
        val requestMappings: Map<RequestMappingInfo, HandlerMethod> = requestMappingHandlerMapping.handlerMethods

        requestMappings.forEach { (info, handler) ->
            info.patternValues.forEach { path ->
                if (path.startsWith("/api")) {
                    val metrikknavn = "${info.methodsCondition}$path".sanerMetrikkNavn()
                    metrikker.put(
                        "${info.methodsCondition}$path",
                        Metrics.counter(
                            metrikknavn,
                            "patternVerdi",
                            path,
                        ),
                    )
                }
            }
        }
    }

    private fun String.sanerMetrikkNavn() =
        this
            .replace("[", "")
            .replace("]", "")
            .replace("{", "")
            .replace("}", "")
            .replace("/", "_")

    companion object {
        private val metrikker = mutableMapOf<String, Counter>()
        val metrikkerForEndepunkter
            get() = metrikker.toMap()
    }
}
