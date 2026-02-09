package no.nav.familie.integrasjoner

import no.nav.familie.integrasjoner.config.ApplicationConfig
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.builder.SpringApplicationBuilder
import org.springframework.boot.webmvc.autoconfigure.error.ErrorMvcAutoConfiguration

@SpringBootApplication(exclude = [ErrorMvcAutoConfiguration::class])
class Launcher

fun main(args: Array<String>) {
    val app = SpringApplicationBuilder(ApplicationConfig::class.java).build()
    app.run(*args)
}
