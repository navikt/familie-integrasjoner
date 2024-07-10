package no.nav.familie.integrasjoner.oppgave

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import no.nav.familie.kontrakter.felles.Ressurs
import no.nav.familie.kontrakter.felles.Ressurs.Companion.success
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnMappeResponseDto
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveRequest
import no.nav.familie.kontrakter.felles.oppgave.FinnOppgaveResponseDto
import no.nav.familie.kontrakter.felles.oppgave.MappeDto
import no.nav.familie.kontrakter.felles.oppgave.Oppgave
import no.nav.familie.kontrakter.felles.oppgave.OppgaveResponse
import no.nav.familie.kontrakter.felles.oppgave.OpprettOppgaveRequest
import no.nav.security.token.support.core.api.ProtectedWithClaims
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.RestController

@RestController
@ProtectedWithClaims(issuer = "azuread")
@RequestMapping("/api/oppgave")
class OppgaveController(
    private val oppgaveService: OppgaveService,
) {
    @GetMapping(path = ["/{oppgaveId}"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun hentOppgave(
        @PathVariable(name = "oppgaveId") oppgaveId: String,
    ): ResponseEntity<Ressurs<Oppgave>> {
        val oppgave = oppgaveService.hentOppgave(oppgaveId.toLong())
        return ResponseEntity.ok().body(success(oppgave, "Hent Oppgave OK"))
    }

    @PostMapping(path = ["/v4"])
    fun finnOppgaverV4(
        @RequestBody finnOppgaveRequest: FinnOppgaveRequest,
    ): Ressurs<FinnOppgaveResponseDto> = success(oppgaveService.finnOppgaver(finnOppgaveRequest))

    @GetMapping(path = ["/mappe/sok"])
    fun finnMapperV1(finnMappeRequest: FinnMappeRequest): Ressurs<FinnMappeResponseDto> = success(oppgaveService.finnMapper(finnMappeRequest))

    @GetMapping(path = ["/mappe/finn/{enhetNr}"])
    fun finnMapper(
        @PathVariable enhetNr: String,
    ): Ressurs<List<MappeDto>> = success(oppgaveService.finnMapper(enhetNr))

    @PostMapping(path = ["/{oppgaveId}/fordel"])
    fun fordelOppgave(
        @PathVariable(name = "oppgaveId") oppgaveId: Long,
        @RequestParam("saksbehandler") saksbehandler: String?,
        @RequestParam("versjon") versjon: Int?,
    ): ResponseEntity<Ressurs<OppgaveResponse>> {
        if (saksbehandler == null) {
            oppgaveService.tilbakestillFordelingPåOppgave(oppgaveId, versjon)
        } else {
            oppgaveService.fordelOppgave(oppgaveId, saksbehandler, versjon)
        }

        return ResponseEntity.ok(
            success(
                OppgaveResponse(oppgaveId = oppgaveId),
                if (saksbehandler !== null) {
                    "Oppgaven ble tildelt saksbehandler $saksbehandler"
                } else {
                    "Fordeling på oppgaven ble tilbakestilt"
                },
            ),
        )
    }

    @PatchMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/{oppgaveId}/oppdater"])
    fun patchOppgave(
        @RequestBody oppgave: Oppgave,
    ): ResponseEntity<Ressurs<OppgaveResponse>> {
        val oppgaveId = oppgaveService.patchOppgave(oppgave)
        return ResponseEntity.ok().body(success(OppgaveResponse(oppgaveId = oppgaveId), "Oppdatering av oppgave OK"))
    }

    @PostMapping(consumes = [MediaType.APPLICATION_JSON_VALUE], path = ["/opprett"])
    fun opprettOppgaveV2(
        @RequestBody oppgave: OpprettOppgaveRequest,
    ): ResponseEntity<Ressurs<OppgaveResponse>> {
        val oppgaveId = oppgaveService.opprettOppgave(oppgave)
        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(success(OppgaveResponse(oppgaveId = oppgaveId), "Opprett oppgave OK"))
    }

    @PatchMapping(path = ["/{oppgaveId}/ferdigstill"])
    fun ferdigstillOppgave(
        @PathVariable(name = "oppgaveId") oppgaveId: Long,
        @RequestParam(name = "versjon") versjon: Int?,
    ): ResponseEntity<Ressurs<OppgaveResponse>> {
        oppgaveService.ferdigstill(oppgaveId, versjon)
        return ResponseEntity.ok(success(OppgaveResponse(oppgaveId = oppgaveId), "ferdigstill OK"))
    }

    @Operation(description = "Flytter oppgaven fra en enhet til en annen enhet.")
    @PatchMapping(path = ["/{oppgaveId}/enhet/{enhet}"])
    fun tilordneOppgaveNyEnhet(
        @Parameter(description = "Oppgavens id")
        @PathVariable(name = "oppgaveId")
        oppgaveId: Long,
        @Parameter(description = "Enhet oppgaven skal flytte til")
        @PathVariable(name = "enhet")
        enhet: String,
        @Parameter(description = "Settes til true hvis man ønsker å flytte en oppgave uten å ta med seg mappa opp på oppgaven. Noen mapper hører spesifikt til en enhet, og man får da ikke flyttet oppgaven uten at mappen fjernes ")
        @RequestParam(name = "fjernMappeFraOppgave")
        fjernMappeFraOppgave: Boolean,
        @Parameter(description = "Vil feile med 409 Conflict dersom versjonen ikke stemmer overens med oppgavesystemets versjon")
        @RequestParam(name = "versjon")
        versjon: Int?,
    ): ResponseEntity<Ressurs<OppgaveResponse>> {
        oppgaveService.tilordneEnhet(oppgaveId, enhet, fjernMappeFraOppgave, versjon)
        return ResponseEntity.ok().body(success(OppgaveResponse(oppgaveId = oppgaveId), "Oppdatering av oppgave OK"))
    }
}
