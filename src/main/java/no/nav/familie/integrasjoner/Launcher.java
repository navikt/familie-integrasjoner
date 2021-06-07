package no.nav.familie.integrasjoner;

import no.nav.familie.integrasjoner.config.ApplicationConfig;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.servlet.error.ErrorMvcAutoConfiguration;
import org.springframework.boot.builder.SpringApplicationBuilder;

@SpringBootApplication(exclude = ErrorMvcAutoConfiguration.class)
public class Launcher {

    public static void main(String... args) {
        SpringApplication app = new SpringApplicationBuilder(ApplicationConfig.class).build();
        app.setRegisterShutdownHook(false);
        app.run(args);
    }

}
