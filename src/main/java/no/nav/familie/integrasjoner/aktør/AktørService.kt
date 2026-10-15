package no.nav.familie.integrasjoner.aktør

import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.kontrakter.felles.Tema
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.http.HttpStatus
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
        val rensetAktørId = aktørId.filter { it.isDigit() }
        if (rensetAktørId.isEmpty()) {
            throw OppslagException(
                message = "Aktør-ID inneholder ingen sifre",
                kilde = "AktørService.getPersonIdentFraPdl",
                level = OppslagException.Level.LAV,
                httpStatus = HttpStatus.BAD_REQUEST,
            )
        }

        val responseFraRegister = pdlRestClient.hentGjeldendePersonident(rensetAktørId, tema)
        secureLogger.info("Legger aktørid {} med fnr {} i personident-cache", rensetAktørId, responseFraRegister)
        return responseFraRegister
    }

    companion object {
        private val secureLogger: Logger = LoggerFactory.getLogger("secureLogger")
    }
}
