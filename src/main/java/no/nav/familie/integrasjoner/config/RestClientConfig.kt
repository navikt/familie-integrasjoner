package no.nav.familie.integrasjoner.config

import no.nav.familie.felles.tokenklient.tokenx.TokenXClient
import no.nav.familie.felles.tokenklient.tokenx.TokenXInterceptor
import no.nav.familie.log.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.log.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.familie.sikkerhet.EksternBrukerUtils
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig(
    private val consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    private val mdcValuesPropagatingClientInterceptor: MdcValuesPropagatingClientInterceptor,
) {
    @Bean("utenAuthHttpClient")
    fun utenAuthHttpClient(): RestClient =
        RestClient
            .builder()
            .requestInterceptor(consumerIdClientInterceptor)
            .requestInterceptor(mdcValuesPropagatingClientInterceptor)
            .build()

    @Bean("safSelvbetjeningTokenXRestClient")
    fun safSelvbetjeningTokenXRestClient(
        tokenXClient: TokenXClient,
        @Value("\${SAF_TOKENX_SCOPE}") scope: String,
    ): RestClient =
        RestClient
            .builder()
            .requestInterceptor(TokenXInterceptor(tokenXClient, scope) { EksternBrukerUtils.getBearerTokenForLoggedInUser() })
            .requestInterceptor(consumerIdClientInterceptor)
            .requestInterceptor(mdcValuesPropagatingClientInterceptor)
            .build()
}
