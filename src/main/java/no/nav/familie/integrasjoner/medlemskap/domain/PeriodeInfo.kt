package no.nav.familie.integrasjoner.medlemskap.domain

import java.time.LocalDate

class PeriodeInfo(val periodeStatus: PeriodeStatus,
                  val periodeStatusÅrsak: PeriodeStatusÅrsak?,
                  val fom: LocalDate,
                  val tom: LocalDate,
                  val gjelderMedlemskapIFolketrygden: Boolean,
                  val grunnlag: String,
                  val dekning: String? = null)