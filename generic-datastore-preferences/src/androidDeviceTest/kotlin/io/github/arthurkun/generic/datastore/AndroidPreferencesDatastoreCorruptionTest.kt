package io.github.arthurkun.generic.datastore

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import java.io.File
import java.util.UUID

@RunWith(AndroidJUnit4::class)
class AndroidPreferencesDatastoreCorruptionTest : AbstractPreferencesDatastoreCorruptionTest() {

    private val context: Context = ApplicationProvider.getApplicationContext()

    private lateinit var corruptDir: File

    override val tempDirectory: String get() = corruptDir.absolutePath

    @Before
    fun setup() {
        corruptDir = File(context.cacheDir, "corruption/${UUID.randomUUID()}")
    }

    @After
    fun tearDown() {
        if (::corruptDir.isInitialized && corruptDir.exists()) {
            corruptDir.deleteRecursively()
        }
    }
}
