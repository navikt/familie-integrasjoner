package no.nav.familie.integrasjoner

import no.nav.familie.integrasjoner.config.ApplicationConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Import
import org.springframework.context.annotation.Profile

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
@Import(ApplicationConfig::class)
@Profile("integrasjonstest")
class UnitTestLauncher

fun main(args: Array<String>) {
    val app = SpringApplicationBuilder(ApplicationConfig::class.java)
        .profiles(
            "integrasjonstest",
            "mock-aktor",
            "mock-dokarkiv",
            "mock-dokdist",
            "mock-egenansatt",
            "mock-infotrygd",
            "mock-infotrygdsak",
            "mock-medlemskap",
            "mock-oppgave",
            "mock-personopplysninger",
            "mock-saf",
            "mock-sts",
            "mock-pdl"
        ).build()
    app.run(*args)
}
