package cl.duoc.deepsource.domain

import cl.duoc.deepsource.domain.model.*
import org.junit.Assert.*
import org.junit.Test
import java.time.Clock
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

class BusinessRulesTest {
    @Test
    fun calculatesProgressAndOvertimeFromEntriesWithoutDuplicatingMinutes() {
        val entries = listOf(
            TimeEntry("a", "project", "marina", 50, "2026-11-26", ""),
            TimeEntry("b", "project", "camila", 30, "2026-11-26", ""),
            TimeEntry("c", "project", "marina", 30, "2026-11-26", ""),
            TimeEntry("d", "project", "tomas", -5, "2026-11-26", "")
        )
        val partial = ProjectStatistics.calculate(200, entries)
        assertEquals(listOf("marina" to 80L, "camila" to 30L), partial.byUser)
        assertEquals(110L, partial.registeredMinutes)
        assertEquals(90L, partial.pendingMinutes)
        assertEquals(55, partial.percent)
        assertFalse(partial.hasOvertime)
        val overtime = ProjectStatistics.calculate(100, entries)
        assertEquals(0L, overtime.pendingMinutes)
        assertEquals(100, overtime.percent)
        assertTrue(overtime.hasOvertime)
        assertEquals(0, ProjectStatistics.calculate(0, entries).percent)
        assertEquals(100L, ProjectStatistics.calculate(100, emptyList()).pendingMinutes)
    }

    @Test
    fun usesSantiagoDateAndKeepsBothWeekBoundaries() {
        val clock = Clock.fixed(Instant.parse("2027-01-05T01:00:00Z"), ZoneId.of("America/Santiago"))
        val policy = RegistrationDates(clock)
        assertEquals(LocalDate.parse("2027-01-04"), policy.today)
        assertEquals(LocalDate.parse("2026-12-28"), policy.earliest)
        assertTrue(policy.allows(policy.earliest))
        assertTrue(policy.allows(policy.today))
        assertFalse(policy.allows(LocalDate.parse("2027-01-03")))
        assertFalse(policy.allows(LocalDate.parse("2026-12-25")))
        assertFalse(policy.allows(LocalDate.parse("2027-01-05")))
    }

    @Test
    fun classifiesProgressAndKeepsCompletedStatus() {
        val project = Project("one", "Proyecto", "Descripción", "Cliente", 64, 100)
        assertEquals(ProjectStatus.Initial, project.status)
        assertEquals(ProjectStatus.Medium, project.copy(registeredMinutes = 65).status)
        assertEquals(ProjectStatus.Medium, project.copy(registeredMinutes = 89).status)
        assertEquals(ProjectStatus.NoTime, project.copy(registeredMinutes = 90).status)
        assertEquals(ProjectStatus.NoTime, project.copy(registeredMinutes = 100).status)
        assertEquals(ProjectStatus.Overtime, project.copy(registeredMinutes = 101).status)
        assertEquals(ProjectStatus.Finished, project.copy(isCompleted = true).status)
        assertEquals(ProjectStatus.Initial, project.copy(registeredMinutes = 0).status)
        assertEquals(1f, project.copy(registeredMinutes = 110).progress, 0f)
        assertEquals(0f, project.copy(totalMinutes = 0).progress, 0f)
        assertEquals("1 h", 60.toTimeLabel())
        assertEquals("1 h 04 min", project.registeredTimeLabel)
        assertEquals("1 h 40 min", project.totalTimeLabel)
    }
}
