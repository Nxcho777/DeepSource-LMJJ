package cl.duoc.deepsource.domain.model

import java.time.Clock
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.TemporalAdjusters

class RegistrationDates(clock: Clock = Clock.system(ZoneId.of("America/Santiago"))) {
    val today: LocalDate = LocalDate.now(clock)
    val earliest: LocalDate = today.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).minusWeeks(1)

    fun allows(date: LocalDate): Boolean =
        date in earliest..today && date.dayOfWeek !in setOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)
}
