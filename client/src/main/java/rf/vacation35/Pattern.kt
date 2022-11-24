package rf.vacation35

import java.time.format.DateTimeFormatter

val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")

val dayFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM")

val dayShortFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMM")

val dayYearFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
