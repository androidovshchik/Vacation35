package rf.vacation35.remote.dao

import kotlin.random.Random

interface Nameable {

    val name: String
}

@Suppress("SpellCheckingInspection")
interface Rawable<T> {

    fun toRaw(): T
}

abstract class RandomComparable {

    override fun equals(other: Any?): Boolean {
        return false
    }

    override fun hashCode(): Int {
        return Random.nextInt()
    }
}
