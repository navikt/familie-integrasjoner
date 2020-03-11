package no.nav.familie.integrasjoner.dokarkiv.api

class TilknyttFagsakRequest (val bruker: Bruker,
                             val tema: String,
                             val sak: Sak)

class Sak(val fagsakId: String,
          val fagsaksystem: String)

class Bruker(val idType: IdType,
             val id: String)

enum class IdType {
    FNR, ORGNR
}