package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.client.rest.SafHentDokumentRestClient
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.integrasjoner.journalpost.internal.JournalposterForVedleggRequest
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import no.nav.familie.kontrakter.felles.journalpost.TilgangsstyrtJournalpost
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JournalpostService
    @Autowired
    constructor(
        private val safRestClient: SafRestClient,
        private val safHentDokumentRestClient: SafHentDokumentRestClient,
        private val baksTilgangsstyrtJournalpostService: BaksTilgangsstyrtJournalpostService,
    ) {
        fun hentJournalpost(journalpostId: String): Journalpost = safRestClient.hentJournalpost(journalpostId)

        fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> = safRestClient.finnJournalposter(journalposterForBrukerRequest)

        fun finnTilgangsstyrteBaksJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<TilgangsstyrtJournalpost> {
            val journalposter = safRestClient.finnJournalposter(journalposterForBrukerRequest)
            return baksTilgangsstyrtJournalpostService.mapTilTilgangsstyrteJournalposter(journalposter)
        }

        fun finnJournalposter(journalposterForVedleggRequest: JournalposterForVedleggRequest): List<Journalpost> = safRestClient.finnJournalposter(journalposterForVedleggRequest)

        fun hentDokument(
            journalpostId: String,
            dokumentInfoId: String,
            variantFormat: String,
        ): ByteArray = safHentDokumentRestClient.hentDokument(journalpostId, dokumentInfoId, variantFormat)
    }
