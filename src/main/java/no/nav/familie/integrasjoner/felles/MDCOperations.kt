package no.nav.familie.integrasjoner.felles

import no.nav.familie.log.mdc.MDCConstants.MDC_CALL_ID
import org.slf4j.MDC

object MDCOperations {

    fun getCallId(): String = MDC.get(MDC_CALL_ID)

}