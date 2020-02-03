package no.nav.familie.integrasjoner.tilgangskontroll.domene

import java.io.Serializable
import java.util.*

class Tilgang : Serializable {
    var isHarTilgang = false
    var begrunnelse: String? = null

    fun withHarTilgang(harTilgang: Boolean): Tilgang {
        isHarTilgang = harTilgang
        return this
    }

    fun withBegrunnelse(begrunnelse: String?): Tilgang {
        this.begrunnelse = begrunnelse
        return this
    }

    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }
        val tilgang = o as Tilgang
        return isHarTilgang == tilgang.isHarTilgang &&
               begrunnelse == tilgang.begrunnelse
    }

    override fun hashCode(): Int {
        return Objects.hash(isHarTilgang, begrunnelse)
    }

    companion object {
        private const val serialVersionUID = 1L
    }
}
