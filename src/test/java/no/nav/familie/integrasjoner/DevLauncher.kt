package no.nav.familie.integrasjoner

import no.nav.familie.integrasjoner.config.ApplicationConfig
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Import
import org.springframework.test.context.ContextConfiguration

@Import(ApplicationConfig::class)
@ContextConfiguration(initializers = [MockOAuth2ServerInitializer::class])
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
                "mock-oppgave",
                "mock-personopplysninger",
                "mock-saf",
                "mock-sts",
                "mock-kodeverk",
                "mock-pdl",
                "mock-aareg",
                "mock-sak",
                "mock-regoppslag",
                "mock-modia-context-holder",
            ).build()
    app.run(*args)
}
