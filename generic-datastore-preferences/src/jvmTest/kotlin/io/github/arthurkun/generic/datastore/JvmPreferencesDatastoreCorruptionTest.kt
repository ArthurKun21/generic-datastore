package io.github.arthurkun.generic.datastore

import org.junit.jupiter.api.io.TempDir
import java.io.File

class JvmPreferencesDatastoreCorruptionTest : AbstractPreferencesDatastoreCorruptionTest() {

    @TempDir
    lateinit var tempFolder: File

    override val tempDirectory: String get() = tempFolder.absolutePath
}
