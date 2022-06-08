package no.nav.familie.integrasjoner.helse

import no.nav.familie.http.health.AbstractHealthIndicator
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service

@Service
@Profile("!dev")
class TriggerHelsesjekkService {
    private val log: Logger = LoggerFactory.getLogger(this::class.java)

    @Autowired
    lateinit var helsesjekkerEksterneSystemer: List<AbstractHealthIndicator>

    @Scheduled(fixedDelay = 30000, initialDelay = 30000)
    fun triggerPing() {
        log.debug("Kjører helsesjekker")
        helsesjekkerEksterneSystemer.forEach {
            it.health()
        }
    }
}
