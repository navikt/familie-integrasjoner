package no.nav.familie.integrasjoner

import no.nav.familie.integrasjoner.config.ApplicationConfig
import no.nav.security.token.support.spring.test.EnableMockOAuth2Server
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Import

@Import(ApplicationConfig::class)
@EnableMockOAuth2Server
class DevLauncher

fun main(args: Array<String>) {
    System.setProperty("spring.profiles.active", "dev")
    val app =
        SpringApplicationBuilder(DevLauncher::class.java)
            .profiles(
                "dev",
                "mock-aktor",
                "mock-dokarkiv",
                "mock-dokdist",
                "mock-dokdistkanal",
                "mock-egenansatt",
                "mock-medlemskap",
                "mock-oppgave",
                "mock-personopplysninger",
                "mock-saf",
                "mock-sts",
                "mock-kodeverk",
                "mock-pdl",
                "mock-aareg",
                "mock-sak",
                "mock-regoppslag",
            ).build()
    app.run(*args)
}
