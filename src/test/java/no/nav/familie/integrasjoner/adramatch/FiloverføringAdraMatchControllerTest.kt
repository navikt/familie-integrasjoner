package no.nav.familie.integrasjoner.adramatch

import com.github.stefanbirkner.fakesftpserver.rule.FakeSftpServerRule
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Fil
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions.assertThat
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.springframework.boot.test.web.client.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder

@ActiveProfiles("integrasjonstest", "mock-sts", "mock-oauth")
class FiloverføringAdraMatchControllerTest : OppslagSpringRunnerTest() {

    @get:Rule
    val sftpServer: FakeSftpServerRule = FakeSftpServerRule().apply { port = MOCK_SERVER_PORT }

    @Before
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    fun `skal koble opp og laste opp fil`() {
        sftpServer.createDirectory("/inbound")
        val uri = UriComponentsBuilder.fromHttpUrl(localhost(BASE_URL)).toUriString()
        val payload = Fil("file.txt", "Filinnhold".toByteArray())
        val response: ResponseEntity<Ressurs<String>> = restTemplate.exchange(
            uri,
            HttpMethod.PUT,
            HttpEntity(payload, headers)
        )
        assertThat(response.body!!.data).isEqualTo("Fil lastet opp!")
        val fileContent = sftpServer.getFileContent("/inbound/file.txt")
        assertThat(fileContent).isEqualTo("Filinnhold".toByteArray())
    }

    companion object {

        const val BASE_URL = "/api/adramatch/avstemming"
        const val MOCK_SERVER_PORT = 18321
    }
}
