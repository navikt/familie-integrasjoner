package no.nav.familie.integrasjoner.modiacontextholder

import no.nav.familie.integrasjoner.client.rest.ModiaContextHolderClient
import no.nav.familie.integrasjoner.modiacontextholder.domene.ModiaContextHolderRequest
import no.nav.familie.integrasjoner.modiacontextholder.domene.ModiaContextHolderResponse
import org.springframework.stereotype.Service

@Service
class ModiaContextHolderService(
    private val modiaContextHolderClient: ModiaContextHolderClient,
) {
    fun hentContext(): ModiaContextHolderResponse = modiaContextHolderClient.hentContext()

    fun settContext(request: ModiaContextHolderRequest): ModiaContextHolderResponse = modiaContextHolderClient.settContext(request)
}
