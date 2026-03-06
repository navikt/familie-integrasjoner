package no.nav.familie.integrasjoner

import no.nav.familie.integrasjoner.config.ApplicationConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
@Import(ApplicationConfig::class)
@Profile("integrasjonstest")
class UnitTestLauncher

fun main(args: Array<String>) {
    val app =
        SpringApplicationBuilder(ApplicationConfig::class.java)
            .profiles(
                "integrasjonstest",
                "mock-aktor",
                "mock-dokarkiv",
                "mock-dokdist",
                "mock-dokdistkanal",
                "mock-egenansatt",
                "mock-oppgave",
                "mock-personopplysninger",
                "mock-saf",
                "mock-sts",
                "mock-pdl",
                "mock-regoppslag",
                "mock-modia-context-holder",
            ).build()
    app.run(*args)
}
