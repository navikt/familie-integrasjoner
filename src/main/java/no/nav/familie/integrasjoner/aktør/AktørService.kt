package no.nav.familie.integrasjoner.aktør

import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.kontrakter.felles.Tema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service

@Service
class AktørService(
    private val pdlRestClient: PdlRestClient,
) {
    @Cacheable(value = ["aktør_aktørId_pdl"], unless = "#result == null")
    fun getPersonIdentFraPdl(
        aktørId: String,
        tema: Tema,
    ): String {
        val responseFraRegister = pdlRestClient.hentGjeldendePersonident(aktørId, tema)
        secureLogger.info("Legger aktørid {} med fnr {} i personident-cache", aktørId, responseFraRegister)
        return responseFraRegister
    }

    companion object {
        private val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")
    }
}
