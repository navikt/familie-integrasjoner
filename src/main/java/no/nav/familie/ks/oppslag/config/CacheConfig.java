package no.nav.familie.ks.oppslag.config;

import org.ehcache.CacheManager;
import org.ehcache.config.ResourcePools;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.expiry.ExpiryPolicy;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

import static org.ehcache.config.builders.CacheConfigurationBuilder.newCacheConfigurationBuilder;

@Configuration
@EnableCaching
public class CacheConfig {

    @Bean
    public CacheManager aktørCacheManager() {
        ResourcePools pools = ResourcePoolsBuilder.heap(1000).build();
        ExpiryPolicy<Object, Object> expiryPolicy = ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofHours(1));
        final CacheManager cacheManager =
                CacheManagerBuilder.newCacheManagerBuilder()
                                   .withCache("aktørIdCache", newCacheConfigurationBuilder(String.class, String.class, pools).withExpiry(expiryPolicy))
                                   .withCache("personIdentCache", newCacheConfigurationBuilder(String.class, String.class, pools).withExpiry(expiryPolicy))
                                   .withCache("tilgangtilbruker", newCacheConfigurationBuilder(String.class, String.class, pools).withExpiry(expiryPolicy))
                                   .withCache("tilgangtiltjenesten", newCacheConfigurationBuilder(String.class, String.class, pools).withExpiry(expiryPolicy))
                                   .withCache("tilgangtilenhet", newCacheConfigurationBuilder(String.class, String.class, pools).withExpiry(expiryPolicy))
                                   .build();
        cacheManager.init();
        return cacheManager;
    }
}
