package no.nav.familie.integrasjoner.medlemskap.domain

class MedlemskapsInfo(val personIdent: String,
                      val gyldigePerioder: List<PeriodeInfo>,
                      val avvistePerioder: List<PeriodeInfo>,
                      val uavklartePerioder: List<PeriodeInfo>)
