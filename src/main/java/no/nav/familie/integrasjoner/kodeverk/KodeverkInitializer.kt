package no.nav.familie.integrasjoner.kodeverk

import org.slf4j.LoggerFactory
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.context.ApplicationListener
import org.springframework.context.annotation.Profile
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Component

@Component
@Profile("!dev & !integrasjonstest")
class KodeverkInitializer(
    private val cachedKodeverkService: CachedKodeverkService,
) : ApplicationListener<ApplicationReadyEvent> {
    private val logger = LoggerFactory.getLogger(javaClass)

    @Scheduled(cron = "0 0 1 * * *")
    fun syncKodeverk() {
        logger.info("Kjører schedulert jobb for å hente kodeverk")
        sync()
    }

    override fun onApplicationEvent(p0: ApplicationReadyEvent) {
        sync()
    }

    private fun sync() {
        syncKodeverk("Inntekt", cachedKodeverkService::hentInntekt)
    }

    private fun syncKodeverk(
        navn: String,
        henter: () -> Unit,
    ) {
        try {
            logger.info("Henter $navn")
            henter.invoke()
        } catch (e: Exception) {
            logger.warn("Feilet henting av $navn ${e.message}")
        }
    }
}
