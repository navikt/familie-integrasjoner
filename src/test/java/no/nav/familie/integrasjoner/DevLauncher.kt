package no.nav.familie.integrasjoner

import no.nav.familie.integrasjoner.config.ApplicationConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.context.annotation.Import

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
@Import(ApplicationConfig::class)
class DevLauncher

fun main(args: Array<String>) {
    val app =
        SpringApplicationBuilder(ApplicationConfig::class.java)
            .profiles(
                "dev",
                "mock-aktor",
                "mock-dokarkiv",
                "mock-dokdist",
                "mock-dokdistkanal",
                "mock-egenansatt",
                "mock-infotrygd",
                "mock-infotrygdsak",
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
