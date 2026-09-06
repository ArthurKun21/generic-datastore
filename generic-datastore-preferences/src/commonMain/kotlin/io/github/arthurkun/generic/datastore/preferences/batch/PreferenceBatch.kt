package io.github.arthurkun.generic.datastore.preferences.batch

/**
 * An immutable, ordered declaration of preferences produced by [prefBatch].
 *
 * The inline batch operations on
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore] build one of these
 * internally from their `declare: PrefBuilder.() -> Unit` block: reads map every declared
 * preference to its value in one emission, writes/updates apply the declared keys in one
 * transaction, and deletes remove every declared key in one transaction.
 *
 * The class implements [List] in declaration order, so batches can also be iterated directly.
 * Equality is order-sensitive (declaration order matters); the read result
 * ([BatchValues] equality) is order-insensitive since it compares the decoded value map.
 */
public class PreferenceBatch internal constructor(
    private val entries: List<BatchPref<*>>,
) : List<BatchPref<*>> by entries {

    override fun equals(other: Any?): Boolean = other is PreferenceBatch && other.entries == entries

    override fun hashCode(): Int = entries.hashCode()

    override fun toString(): String = "PreferenceBatch(size=$size)"
}
