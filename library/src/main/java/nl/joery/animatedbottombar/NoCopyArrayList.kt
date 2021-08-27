package nl.joery.animatedbottombar

internal class NoCopyArrayList<T>(private val data: Array<out T>): AbstractList<T>() {
    override val size: Int
        get() = data.size

    override fun get(index: Int): T = data[index]

    override fun isEmpty(): Boolean = data.isEmpty()

    override fun indexOf(element: T): Int {
        return data.indexOf(element)
    }

    override fun lastIndexOf(element: T): Int {
        return data.lastIndexOf(element)
    }

    override fun contains(element: T): Boolean {
        return data.contains(element)
    }

    override fun iterator(): Iterator<T> = IteratorImpl(data)

    override fun toArray(): Array<Any?> = data as Array<Any?>

    override fun equals(other: Any?): Boolean {
        if(other === this) return true
        if(other == null || javaClass !== other.javaClass) return false

        other as NoCopyArrayList<*>

        return data contentEquals other.data
    }

    override fun hashCode(): Int {
        return data.contentHashCode()
    }

    override fun toString(): String {
        return data.contentToString()
    }

    private class IteratorImpl<T>(private val data: Array<out T>): Iterator<T> {
        private var index = 0

        override fun hasNext(): Boolean = index < data.size
        override fun next(): T = data[index]
    }
}