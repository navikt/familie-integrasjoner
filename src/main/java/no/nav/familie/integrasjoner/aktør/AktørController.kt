package no.nav.familie.integrasjoner.aktør

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.Tema
import no.nav.familie.kontrakter.felles.jsonMapper
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/api/aktoer")
class AktørController(
    private val aktørService: AktørService,
) {
    /**
     * Denne brukes av familie-ef-mottak for å finne behandlende enhet.
     * Kan fjernes når eller hvis det blir mulig å assosiere journalposter med kun personident.
     */
    @PostMapping("v2/fraaktorid/{tema}")
    fun finnPersonIdentForAktørId(
        @RequestBody(required = true) aktørId: String,
        @PathVariable tema: Tema,
    ): ResponseEntity<Ressurs<Map<String, String>>> {
        secureLogger.info(
            "Oppslag fra aktør-ID. tema={}, requestBody={}",
            tema,
            jsonMapper.writeValueAsString(aktørId),
        )

        val personIdentFraPdl = aktørService.getPersonIdentFraPdl(aktørId, tema)

        secureLogger.info(
            "ident fra pdl for tema={}: {}",
            tema,
            jsonMapper.writeValueAsString(personIdentFraPdl),
        )

        return ResponseEntity.ok(
            success(
                mapOf("personIdent" to personIdentFraPdl),
                "Hent aktør for personident OK",
            ),
        )
    }

    companion object {
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
    }
}
