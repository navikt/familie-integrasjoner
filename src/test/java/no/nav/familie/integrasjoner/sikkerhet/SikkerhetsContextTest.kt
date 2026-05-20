package no.nav.familie.integrasjoner.sikkerhet

import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.assertThatThrownBy
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Test
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import org.springframework.security.oauth2.jwt.Jwt
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken

class SikkerhetsContextTest {
    @AfterEach
    fun cleanUp() {
        SecurityContextHolder.clearContext()
    }

    @Test
    fun `hentSaksbehandlerEllerSystembruker returnerer NAVident når claim finnes`() {
        mockToken(mapOf("NAVident" to "Z999999"))

        assertThat(SikkerhetsContext.hentSaksbehandlerEllerSystembruker()).isEqualTo("Z999999")
    }

    @Test
    fun `hentSaksbehandlerEllerSystembruker returnerer VL når NAVident mangler`() {
        mockToken(mapOf("sub" to "maskinsystem"))

        assertThat(SikkerhetsContext.hentSaksbehandlerEllerSystembruker()).isEqualTo("VL")
    }

    @Test
    fun `hentSaksbehandlerNavn returnerer name når claim finnes`() {
        mockToken(mapOf("name" to "Ola Nordmann"))

        assertThat(SikkerhetsContext.hentSaksbehandlerNavn()).isEqualTo("Ola Nordmann")
    }

    @Test
    fun `hentSaksbehandlerNavn returnerer System når name mangler og strict er false`() {
        mockToken(mapOf("sub" to "maskinsystem"))

        assertThat(SikkerhetsContext.hentSaksbehandlerNavn(strict = false)).isEqualTo("System")
    }

    @Test
    fun `hentSaksbehandlerNavn kaster feil når name mangler og strict er true`() {
        mockToken(mapOf("sub" to "maskinsystem"))

        assertThatThrownBy { SikkerhetsContext.hentSaksbehandlerNavn(strict = true) }
            .isInstanceOf(IllegalStateException::class.java)
            .hasMessage("Finner ikke navn i azuread token")
    }

    @Test
    fun `hentClaim returnerer claim-verdi når claim finnes`() {
        mockToken(mapOf("custom-claim" to "custom-value"))

        assertThat(SikkerhetsContext.hentClaim<String>("custom-claim")).isEqualTo("custom-value")
    }

    @Test
    fun `hentClaim returnerer null når claim ikke finnes`() {
        mockToken(mapOf("sub" to "test"))

        assertThat(SikkerhetsContext.hentClaim<String>("finnes-ikke")).isNull()
    }

    @Test
    fun `hentJwt returnerer JWT når JwtAuthenticationToken er i context`() {
        mockToken(mapOf("sub" to "test-subject"))

        val jwt = SikkerhetsContext.hentJwt()

        assertThat(jwt).isNotNull()
        assertThat(jwt.subject).isEqualTo("test-subject")
    }

    @Test
    fun `hentJwt kaster AuthenticationCredentialsNotFoundException når SecurityContext er tomt`() {
        assertThatThrownBy { SikkerhetsContext.hentJwt() }
            .isInstanceOf(AuthenticationCredentialsNotFoundException::class.java)
            .hasMessageContaining("Klarte ikke hente token fra context")
    }

    @Test
    fun `hentJwt kaster AuthenticationCredentialsNotFoundException når auth ikke er JwtAuthenticationToken`() {
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = UsernamePasswordAuthenticationToken("user", "pass")
        SecurityContextHolder.setContext(securityContext)

        assertThatThrownBy { SikkerhetsContext.hentJwt() }
            .isInstanceOf(AuthenticationCredentialsNotFoundException::class.java)
            .hasMessageContaining("Klarte ikke hente token fra context")
    }

    private fun mockToken(claims: Map<String, Any>) {
        val jwt =
            Jwt
                .withTokenValue("token")
                .header("alg", "RS256")
                .apply { claims.forEach { (key, value) -> claim(key, value) } }
                .build()
        val securityContext = SecurityContextHolder.createEmptyContext()
        securityContext.authentication = JwtAuthenticationToken(jwt)
        SecurityContextHolder.setContext(securityContext)
    }
}
