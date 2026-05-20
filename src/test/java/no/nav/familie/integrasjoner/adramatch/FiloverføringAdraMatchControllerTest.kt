package no.nav.familie.integrasjoner.adramatch

import com.github.stefanbirkner.fakesftpserver.rule.FakeSftpServerRule
import no.nav.familie.integrasjoner.OppslagSpringRunnerTest
import no.nav.familie.kontrakter.felles.Fil
import no.nav.familie.kontrakter.felles.Ressurs
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtensionContext
import org.junit.jupiter.api.extension.InvocationInterceptor
import org.junit.jupiter.api.extension.ReflectiveInvocationContext
import org.junit.jupiter.api.extension.RegisterExtension
import org.junit.runner.Description
import org.junit.runners.model.Statement
import org.springframework.boot.resttestclient.exchange
import org.springframework.http.HttpEntity
import org.springframework.http.HttpMethod
import org.springframework.http.ResponseEntity
import org.springframework.test.context.ActiveProfiles
import org.springframework.web.util.UriComponentsBuilder
import java.lang.reflect.Method

@ActiveProfiles("integrasjonstest", "mock-sts", "mock-oauth")
class FiloverføringAdraMatchControllerTest : OppslagSpringRunnerTest() {
    @field:RegisterExtension
    private val sftpServer = FakeSftpServerExtension(MOCK_SFTP_SERVER_PORT)

    @BeforeEach
    fun setUp() {
        headers.setBearerAuth(lokalTestToken)
    }

    @Test
    fun `skal koble opp og laste opp fil`() {
        sftpServer.createDirectory("/inbound")
        val uri = UriComponentsBuilder.fromUriString(localhost(BASE_URL)).toUriString()
        val payload = Fil("file.txt", "Filinnhold".toByteArray())
        val response: ResponseEntity<Ressurs<String>> =
            restTemplate.exchange(
                uri,
                HttpMethod.PUT,
                HttpEntity(payload, headers),
            )
        assertThat(response.body!!.data).isEqualTo("Fil lastet opp!")
        val fileContent = sftpServer.getFileContent("/inbound/file.txt")
        assertThat(fileContent).isEqualTo("Filinnhold".toByteArray())
    }

    companion object {
        const val BASE_URL = "/api/adramatch/avstemming"
        const val MOCK_SFTP_SERVER_PORT = 18321
    }
}

/**
 * JUnit 5-extension som wrapper [FakeSftpServerRule] (JUnit 4) via [InvocationInterceptor].
 */
internal class FakeSftpServerExtension(
    port: Int,
) : InvocationInterceptor {
    private val rule = FakeSftpServerRule().setPort(port)

    fun createDirectory(path: String) = rule.createDirectory(path)

    fun getFileContent(path: String): ByteArray = rule.getFileContent(path)

    override fun interceptTestMethod(
        invocation: InvocationInterceptor.Invocation<Void?>,
        invocationContext: ReflectiveInvocationContext<Method>,
        extensionContext: ExtensionContext,
    ) {
        rule
            .apply(
                object : Statement() {
                    override fun evaluate() {
                        invocation.proceed()
                    }
                },
                Description.createTestDescription(
                    extensionContext.requiredTestClass,
                    extensionContext.displayName,
                ),
            ).evaluate()
    }
}
