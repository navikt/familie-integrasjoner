package no.nav.familie.integrasjoner.config

import no.nav.familie.felles.tokenklient.entraid.EntraIDRestClientFactory
import no.nav.familie.log.interceptor.ConsumerIdClientInterceptor
import no.nav.familie.log.interceptor.MdcValuesPropagatingClientInterceptor
import no.nav.security.token.support.spring.SpringTokenValidationContextHolder
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestClient

@Configuration
class RestClientConfig(
    private val entraIDRestClientFactory: EntraIDRestClientFactory,
    private val consumerIdClientInterceptor: ConsumerIdClientInterceptor,
    private val mdcValuesPropagatingClientInterceptor: MdcValuesPropagatingClientInterceptor,
) {
    private fun hentBrukerToken(): String? =
        try {
            SpringTokenValidationContextHolder()
                .getTokenValidationContext()
                .getJwtToken("azuread")
                ?.encodedToken
        } catch (_: Exception) {
            null
        }

    // --- Hybrid (OBO + CC-fallback) ---

    @Bean("safRestClient")
    fun safRestClient(
        @Value("\${SAF_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { hentBrukerToken() }

    @Bean("pdlRestClient")
    fun pdlRestClient(
        @Value("\${PDL_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { hentBrukerToken() }

    @Bean("oppgaveRestClient")
    fun oppgaveRestClient(
        @Value("\${OPPGAVE_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { hentBrukerToken() }

    @Bean("dokdistRestClient")
    fun dokdistRestClient(
        @Value("\${DOKDIST_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { hentBrukerToken() }

    @Bean("dokdistkanalRestClient")
    fun dokdistkanalRestClient(
        @Value("\${DOKDISTKANAL_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { hentBrukerToken() }

    @Bean("regoppslagRestClient")
    fun regoppslagRestClient(
        @Value("\${REGOPPSLAG_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { hentBrukerToken() }

    @Bean("aaregRestClient")
    fun aaregRestClient(
        @Value("\${AAREG_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { hentBrukerToken() }

    @Bean("førstesidegeneratorRestClient")
    fun førstesidegeneratorRestClient(
        @Value("\${FORSTESIDEGENERATOR_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { hentBrukerToken() }

    @Bean("azureGraphRestClient")
    fun azureGraphRestClient(
        @Value("\${AAD_GRAPH_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { hentBrukerToken() }

    @Bean("dokarkivRestClient")
    fun dokarkivRestClient(
        @Value("\${DOKARKIV_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagHybridRestKlient(scope) { hentBrukerToken() }

    // --- OBO-only ---

    @Bean("modiaContextHolderRestClient")
    fun modiaContextHolderRestClient(
        @Value("\${MODIA_CONTEXT_HOLDER_SCOPE}") scope: String,
    ): RestClient =
        entraIDRestClientFactory.lagOboRestKlient(scope) {
            hentBrukerToken() ?: error("OBO-kall til ModiaContextHolder uten innlogget bruker")
        }

    // --- Maskin-til-maskin ---

    @Bean("pdlClientCredentialRestClient")
    fun pdlClientCredentialRestClient(
        @Value("\${PDL_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagMaskinTilMaskinRestKlient(scope)

    @Bean("kodeverkRestClient")
    fun kodeverkRestClient(
        @Value("\${KODEVERK_SCOPE}") scope: String,
    ): RestClient = entraIDRestClientFactory.lagMaskinTilMaskinRestKlient(scope)

    // --- Uten auth ---

    @Bean("utenAuthRestClient")
    fun utenAuthRestClient(): RestClient =
        RestClient
            .builder()
            .requestInterceptor(consumerIdClientInterceptor)
            .requestInterceptor(mdcValuesPropagatingClientInterceptor)
            .build()
}
