package no.nav.familie.integrasjoner.config

import org.eclipse.jetty.client.HttpClient
import org.springframework.http.client.JettyClientHttpRequestFactory
import org.springframework.web.client.RestClient
import java.time.Duration

// Uten egen requestFactory bruker Spring Jetty med 10s timeout for hele kallet.
// Timeouten er total tid for kallet (inkl. opp- og nedlasting), ikke en ren lese-timeout.
// Brukes også som idle-timeout, ellers avbryter Jetty etter 30s uten trafikk.
// Den som lager HttpClient må stoppe den (DisposableBean).
fun lagJettyHttpClient(timeout: Duration): HttpClient = HttpClient().apply { idleTimeout = timeout.toMillis() }

fun RestClient.medTimeout(
    httpClient: HttpClient,
    timeout: Duration,
): RestClient =
    mutate()
        .requestFactory(JettyClientHttpRequestFactory(httpClient).apply { setReadTimeout(timeout) })
        .build()
