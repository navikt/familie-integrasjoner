package no.nav.familie.integrasjoner.medlemskap

import no.nav.familie.integrasjoner.client.rest.MedlRestClient
import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.medlemskap.domain.MedlemskapsinfoMapper
import no.nav.familie.kontrakter.felles.medlemskap.Medlemskapsinfo
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class MedlemskapService(private val medlRestClient: MedlRestClient) {

    fun hentMedlemskapsunntak(aktørId: String): Medlemskapsinfo {
        return try {
            MedlemskapsinfoMapper.tilMedlemskapsInfo(medlRestClient.hentMedlemskapsUnntakResponse(aktørId))
        } catch (e: Exception) {
            throw OppslagException("Feil ved oppslag for Aktør " + aktørId + " og uri " + medlRestClient.medlemskapsunntakUri,
                                   "MEDL2",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.INTERNAL_SERVER_ERROR,
                                   e)
        }
    }

    fun hentMedlemskapsunntakForIdent(ident: String): Medlemskapsinfo {
        return MedlemskapsinfoMapper.tilMedlemskapsInfo(medlRestClient.hentMedlemskapsUnntakResponse(ident))
    }

}