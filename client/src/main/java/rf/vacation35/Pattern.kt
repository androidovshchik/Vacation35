package rf.vacation35

import java.time.format.DateTimeFormatter

val timeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm")

val dateFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy")

val dateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm")

val shortDateTimeFormatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd.MM HH:mm")
