package no.nav.familie.integrasjoner.medlemskap

import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.familie.integrasjoner.medlemskap.domain.MedlemskapsInfo
import no.nav.familie.integrasjoner.medlemskap.domain.MedlemskapsOversetter
import no.nav.familie.integrasjoner.medlemskap.internal.MedlClient
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.HttpStatus
import org.springframework.stereotype.Service

@Service
class MedlemskapService @Autowired constructor(private val medlClient: MedlClient) {
    fun hentMedlemskapsUnntak(aktørId: String): MedlemskapsInfo {
        return try {
            MedlemskapsOversetter.tilMedlemskapsInfo(medlClient.hentMedlemskapsUnntakResponse(aktørId))
        } catch (e: Exception) {
            throw OppslagException("Feil ved oppslag for Aktør " + aktørId + " og uri " + medlClient.medl2Uri,
                                   "MEDL2",
                                   OppslagException.Level.MEDIUM,
                                   HttpStatus.INTERNAL_SERVER_ERROR,
                                   e)
        }
    }

}