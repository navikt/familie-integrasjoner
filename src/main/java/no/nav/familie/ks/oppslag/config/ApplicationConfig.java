package no.nav.familie.ks.oppslag.config;

import no.nav.familie.ks.oppslag.personopplysning.domene.AktørId;
import no.nav.familie.log.filter.LogFilter;
import org.ehcache.CacheManager;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.ExpiryPolicy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.web.embedded.jetty.JettyServletWebServerFactory;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.boot.web.servlet.server.ServletWebServerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;

import java.time.Duration;

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;

@SpringBootConfiguration
@ComponentScan({"no.nav.familie.ks.oppslag"})
public class ApplicationConfig {

    private static final Logger LOG = LoggerFactory.getLogger(ApplicationConfig.class);

    @Bean
    ServletWebServerFactory servletWebServerFactory() {

        JettyServletWebServerFactory serverFactory = new JettyServletWebServerFactory();

        serverFactory.setPort(8085);

        return serverFactory;
    }

    @Bean
    public FilterRegistrationBean<LogFilter> logFilter() {
        LOG.info("Registering LogFilter filter");
        final FilterRegistrationBean<LogFilter> filterRegistration = new FilterRegistrationBean<>();
        filterRegistration.setFilter(new LogFilter());
        filterRegistration.setOrder(1);
        return filterRegistration;
    }

    @Bean
    public CacheManager aktørCacheManager() {
        ResourcePools pools = ResourcePoolsBuilder.heap(1000).build();
        ExpiryPolicy<Object, Object> expiryPolicy = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(1));
        final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                .withCache("aktørIdCache", newCacheConfigurationBuilder(String.class, String.class, pools).withExpiry(expiryPolicy))
                .withCache("personIdentCache", newCacheConfigurationBuilder(AktørId.class, String.class, pools).withExpiry(expiryPolicy))
                .build();
        cacheManager.init();
        return cacheManager;
    }
}
