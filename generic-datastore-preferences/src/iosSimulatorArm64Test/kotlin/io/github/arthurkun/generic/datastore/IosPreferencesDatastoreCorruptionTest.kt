package io.github.arthurkun.generic.datastore

import platform.Foundation.NSFileManager
import platform.Foundation.NSTemporaryDirectory
import platform.Foundation.NSUUID
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class IosPreferencesDatastoreCorruptionTest : AbstractPreferencesDatastoreCorruptionTest() {

    private lateinit var corruptDir: String

    override val tempDirectory: String get() = corruptDir

    @BeforeTest
    fun setup() {
        corruptDir = NSTemporaryDirectory() + "corruption-${NSUUID().UUIDString}"
    }

    @AfterTest
    fun tearDown() {
        if (::corruptDir.isInitialized) {
            NSFileManager.defaultManager.removeItemAtPath(corruptDir, null)
        }
    }
}
