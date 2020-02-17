package no.nav.familie.integrasjoner.medlemskap.domain

class MedlemskapsInfo {
    var personIdent: String? = null
        private set
    var gyldigePerioder: List<PeriodeInfo>? = null
        private set
    var avvistePerioder: List<PeriodeInfo>? = null
        private set
    var uavklartePerioder: List<PeriodeInfo>? = null
        private set

    class Builder {
        private val medlemskapsInfo: MedlemskapsInfo
        fun medGyldigePerioder(perioder: List<PeriodeInfo>?): Builder {
            medlemskapsInfo.gyldigePerioder = perioder
            return this
        }

        fun medAvvistePerioder(perioder: List<PeriodeInfo>?): Builder {
            medlemskapsInfo.avvistePerioder = perioder
            return this
        }

        fun medUavklartePerioder(perioder: List<PeriodeInfo>?): Builder {
            medlemskapsInfo.uavklartePerioder = perioder
            return this
        }

        fun medPersonIdent(personIdent: String?): Builder {
            medlemskapsInfo.personIdent = personIdent
            return this
        }

        fun build(): MedlemskapsInfo {
            return medlemskapsInfo
        }

        init {
            medlemskapsInfo = MedlemskapsInfo()
        }
    }
}