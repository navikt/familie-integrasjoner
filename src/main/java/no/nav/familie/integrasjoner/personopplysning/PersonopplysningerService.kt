package no.nav.familie.integrasjoner.personopplysning

import no.nav.familie.integrasjoner.client.rest.PdlRestClient
import no.nav.familie.integrasjoner.client.rest.PersonInfoQuery
import no.nav.familie.integrasjoner.client.soap.PersonSoapClient
import no.nav.familie.integrasjoner.felles.ws.DateUtil
import no.nav.familie.integrasjoner.personopplysning.domene.PersonIdent
import no.nav.familie.integrasjoner.personopplysning.domene.PersonhistorikkInfo
import no.nav.familie.integrasjoner.personopplysning.domene.Personinfo
import no.nav.familie.integrasjoner.personopplysning.domene.TpsOversetter
import no.nav.familie.integrasjoner.personopplysning.internal.Person
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Informasjonsbehov
import no.nav.tjeneste.virksomhet.person.v3.informasjon.NorskIdent
import no.nav.tjeneste.virksomhet.person.v3.informasjon.Periode
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonRequest
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonResponse
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import org.slf4j.LoggerFactory
import org.springframework.cache.annotation.Cacheable
import org.springframework.stereotype.Service
import org.springframework.web.context.annotation.ApplicationScope
import java.time.LocalDate
import no.nav.tjeneste.virksomhet.person.v3.informasjon.PersonIdent as TpsPersonIdent

@Service
@ApplicationScope
class PersonopplysningerService(private val personSoapClient: PersonSoapClient,
                                private val oversetter: TpsOversetter,
                                private val pdlRestClient: PdlRestClient) {

    fun hentHistorikkFor(personIdent: String, fom: LocalDate, tom: LocalDate): PersonhistorikkInfo {
        val request = HentPersonhistorikkRequest()
        request.aktoer = TpsPersonIdent().withIdent(NorskIdent().withIdent(personIdent))
        request.periode = Periode().withFom(DateUtil.convertToXMLGregorianCalendar(fom))
                .withTom(DateUtil.convertToXMLGregorianCalendar(tom))
        val response = personSoapClient.hentPersonhistorikkResponse(request)
        return oversetter.tilPersonhistorikkInfo(PersonIdent(personIdent), response)
    }

    fun hentPersoninfoFor(personIdent: String?): Personinfo {
        val request: HentPersonRequest = HentPersonRequest()
                .withAktoer(TpsPersonIdent().withIdent(NorskIdent().withIdent(personIdent)))
                .withInformasjonsbehov(listOf(Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.ADRESSE))
        val response = personSoapClient.hentPersonResponse(request)
        return oversetter.tilPersoninfo(PersonIdent(personIdent), response)
    }

    @Cacheable(cacheNames = [PERSON], key = "#personIdent", condition = "#personIdent != null")
    fun hentPersoninfo(personIdent: String): Personinfo {
        val request: HentPersonRequest =
                HentPersonRequest().withAktoer(TpsPersonIdent().withIdent(NorskIdent().withIdent(personIdent)))
                        .withInformasjonsbehov(listOf(Informasjonsbehov.FAMILIERELASJONER, Informasjonsbehov.ADRESSE))
        val response: HentPersonResponse
        response = personSoapClient.hentPersonResponse(request)
        return oversetter.tilPersoninfo(PersonIdent(personIdent), response)
    }

    fun hentPersoninfo(personIdent: String,
                       tema: String,
                       personInfoQuery: PersonInfoQuery): Person {
        return pdlRestClient.hentPerson(personIdent, tema, personInfoQuery)
    }

    companion object {
        private val LOG = LoggerFactory.getLogger(PersonopplysningerService::class.java)
        private val secureLogger = LoggerFactory.getLogger("secureLogger")
        const val PERSON = "PERSON"
    }

}