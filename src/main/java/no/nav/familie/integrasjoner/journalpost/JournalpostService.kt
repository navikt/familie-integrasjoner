package no.nav.familie.integrasjoner.journalpost

import no.nav.familie.integrasjoner.client.rest.SafHentDokumentRestClient
import no.nav.familie.integrasjoner.client.rest.SafRestClient
import no.nav.familie.integrasjoner.journalpost.internal.JournalposterForVedleggRequest
import no.nav.familie.kontrakter.felles.journalpost.Journalpost
import no.nav.familie.kontrakter.felles.journalpost.JournalposterForBrukerRequest
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.stereotype.Service

@Service
class JournalpostService
    @Autowired
    constructor(
        private val safRestClient: SafRestClient,
        private val safHentDokumentRestClient: SafHentDokumentRestClient,
    ) {
        fun hentSaksnummer(journalpostId: String): String? {
            val journalpost = safRestClient.hentJournalpost(journalpostId)
            return if (journalpost.sak != null && journalpost.sak?.arkivsaksystem == "GSAK") {
                journalpost.sak?.arkivsaksnummer
            } else {
                null
            }
        }

        fun hentJournalpost(journalpostId: String): Journalpost {
            return safRestClient.hentJournalpost(journalpostId)
        }

        fun finnJournalposter(journalposterForBrukerRequest: JournalposterForBrukerRequest): List<Journalpost> {
            return safRestClient.finnJournalposter(journalposterForBrukerRequest)
        }

        fun finnJournalposter(journalposterForVedleggRequest: JournalposterForVedleggRequest): List<Journalpost> {
            return safRestClient.finnJournalposter(journalposterForVedleggRequest)
        }

        fun hentDokument(
            journalpostId: String,
            dokumentInfoId: String,
            variantFormat: String,
        ): ByteArray {
            return safHentDokumentRestClient.hentDokument(journalpostId, dokumentInfoId, variantFormat)
        }
    }
