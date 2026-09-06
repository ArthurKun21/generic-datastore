package io.github.arthurkun.generic.datastore.preferences.batch

/**
 * An immutable, ordered declaration of preferences produced by [prefBatch].
 *
 * The batch is the single source of truth consumed by every batch operation:
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchRead] and
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchReadFlow] map every
 * declared preference to its value in one emission,
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchWrite] and
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchUpdate] write the
 * declared keys in one transaction, and
 * [io.github.arthurkun.generic.datastore.preferences.PreferencesDatastore.batchDelete] removes
 * every declared key in one transaction.
 *
 * The class implements [List] in declaration order, so batches can also be iterated directly.
 */
public class PreferenceBatch internal constructor(
    private val entries: List<BatchPref<*>>,
) : List<BatchPref<*>> by entries {

    override fun equals(other: Any?): Boolean = other is PreferenceBatch && other.entries == entries

    override fun hashCode(): Int = entries.hashCode()

    override fun toString(): String = "PreferenceBatch(size=$size)"
}
