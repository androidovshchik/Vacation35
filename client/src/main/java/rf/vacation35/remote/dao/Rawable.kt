package rf.vacation35.remote.dao

interface Nameable {

    val name: String
}

@Suppress("SpellCheckingInspection")
interface Rawable<T> {

    fun toRaw(): T
}
