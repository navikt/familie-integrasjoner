package no.nav.familie.integrasjoner.medlemskap.domain

import java.time.LocalDate

class PeriodeInfo {
    var periodeStatus: PeriodeStatus? = null
        private set
    var periodeStatusÅrsak: PeriodeStatusÅrsak? = null
        private set
    var fom: LocalDate? = null
        private set
    var tom: LocalDate? = null
        private set
    var isGjelderMedlemskapIFolketrygden = false
        private set
    var grunnlag: String? = null
        private set
    var dekning: String? = null
        private set

    class Builder {
        private val periodeInfo: PeriodeInfo
        fun medPeriodeStatus(periodeStatus: PeriodeStatus?): Builder {
            periodeInfo.periodeStatus = periodeStatus
            return this
        }

        fun medPeriodeStatusÅrsak(årsak: PeriodeStatusÅrsak?): Builder {
            periodeInfo.periodeStatusÅrsak = årsak
            return this
        }

        fun medFom(fom: LocalDate?): Builder {
            periodeInfo.fom = fom
            return this
        }

        fun medTom(tom: LocalDate?): Builder {
            periodeInfo.tom = tom
            return this
        }

        fun medGjelderMedlemskapIFolketrygden(gjelderMedlemskap: Boolean): Builder {
            periodeInfo.isGjelderMedlemskapIFolketrygden = gjelderMedlemskap
            return this
        }

        fun medGrunnlag(grunnlag: String?): Builder {
            periodeInfo.grunnlag = grunnlag
            return this
        }

        fun medDekning(dekning: String?): Builder {
            periodeInfo.dekning = dekning
            return this
        }

        fun build(): PeriodeInfo {
            return periodeInfo
        }

        init {
            periodeInfo = PeriodeInfo()
        }
    }
}