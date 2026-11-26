package cl.duoc.deepsource.data.remote

import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.time.LocalDate

class HolidayRepository(private val api: HolidaysApi) {
    private val cache = mutableMapOf<Int, Map<LocalDate, String>>()
    private val mutex = Mutex()

    suspend fun forYear(year: Int): Map<LocalDate, String> = mutex.withLock {
        cache[year] ?: api.list(year)
            .filter { it.nationalHoliday && "Public" in it.holidayTypes }
            .mapNotNull { holiday ->
                val date = runCatching { LocalDate.parse(holiday.date) }.getOrNull()
                date?.takeIf { it.year == year }?.let { it to holiday.name }
            }
            .toMap()
            .also { cache[year] = it }
    }
}
