package no.nav.familie.integrasjoner.kodeverk

import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.kodeverk.InntektKodeverkDto
import no.nav.familie.kontrakter.felles.kodeverk.KodeverkDto
import no.nav.security.token.support.core.api.Unprotected
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

/**
 * UNPROTECTED
 * Hvis man legger til noe her må man vurdere at det er greit å hente informasjonen uten ProtectedWithClaims
 */
@Unprotected
@RestController
@RequestMapping(path = ["/api/kodeverk"], produces = [MediaType.APPLICATION_JSON_VALUE])
class KodeverkController(
    private val kodeverkService: CachedKodeverkService,
) {
    @GetMapping("/poststed")
    fun hentPoststed(): ResponseEntity<Ressurs<KodeverkDto>> = ResponseEntity.ok(Ressurs.success(kodeverkService.hentPostnummerMedHistorikk()))

    @GetMapping("/landkoder")
    fun hentLandkoder(): ResponseEntity<Ressurs<KodeverkDto>> = ResponseEntity.ok(Ressurs.success(kodeverkService.hentLandkoderMedHistorikk()))

    @GetMapping("/landkoder/{landkode}")
    fun hentLandkod(
        @PathVariable landkode: String,
    ): ResponseEntity<Ressurs<String>> = ResponseEntity.ok(Ressurs.success(kodeverkService.hentLandkoder().getOrDefault(landkode, "")))

    @GetMapping("/landkoderISO2")
    fun hentLandkoderISO2(): ResponseEntity<Ressurs<Map<String, String>>> = ResponseEntity.ok(Ressurs.success(kodeverkService.hentLandkoderISO2()))

    @GetMapping("/landkoder/eea")
    fun hentEEALand(): ResponseEntity<Ressurs<KodeverkDto>> = ResponseEntity.ok(Ressurs.success(kodeverkService.hentEEALandkoder()))

    @GetMapping("inntekt")
    fun hentKodeverkForInntekt(): Ressurs<InntektKodeverkDto> = Ressurs.success(kodeverkService.hentInntekt())
}
