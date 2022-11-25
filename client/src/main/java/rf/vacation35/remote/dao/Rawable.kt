package rf.vacation35.remote.dao

@Suppress("SpellCheckingInspection")
interface Rawable<T> {

    fun toRaw(): T
}
