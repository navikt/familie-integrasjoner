package no.nav.familie.integrasjoner.dokarkiv.api

import no.nav.familie.integrasjoner.dokarkiv.client.domene.DokarkivBruker

class TilknyttFagsakRequest (val bruker: DokarkivBruker,
                             val tema: String,
                             val sak: Sak)

class Sak(val fagsakId: String,
          val fagsaksystem: String)