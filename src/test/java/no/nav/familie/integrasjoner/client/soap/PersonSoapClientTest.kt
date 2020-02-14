package no.nav.familie.integrasjoner.client.soap

import io.mockk.every
import io.mockk.impl.annotations.InjectMockKs
import io.mockk.impl.annotations.MockK
import io.mockk.junit5.MockKExtension
import no.nav.familie.integrasjoner.felles.OppslagException
import no.nav.tjeneste.virksomhet.person.v3.binding.HentPersonhistorikkPersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.binding.PersonV3
import no.nav.tjeneste.virksomhet.person.v3.feil.PersonIkkeFunnet
import no.nav.tjeneste.virksomhet.person.v3.meldinger.HentPersonhistorikkRequest
import org.assertj.core.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(MockKExtension::class)
class PersonSoapClientTest {

    @MockK
    lateinit var port: PersonV3

    @InjectMockKs
    private lateinit var  personSoapClient: PersonSoapClient

    @Test
    fun `client skal kaste OppslagException med lav info ved feil`() {
        every { port.hentPersonhistorikk(any<HentPersonhistorikkRequest>()) } throws HentPersonhistorikkPersonIkkeFunnet("test", PersonIkkeFunnet())

        Assertions.assertThatThrownBy { personSoapClient.hentPersonhistorikkResponse(HentPersonhistorikkRequest()) }
                .hasMessageContaining("Prøver å hente historikk for person som ikke finnes i TPS")
                .isInstanceOf(OppslagException::class.java)
    }

}