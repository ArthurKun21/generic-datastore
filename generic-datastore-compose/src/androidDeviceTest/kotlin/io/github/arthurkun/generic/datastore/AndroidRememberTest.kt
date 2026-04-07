package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.TestDispatcher
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith

private const val TEST_DATASTORE_NAME = "test_remember"

@RunWith(AndroidJUnit4::class)
class AndroidRememberTest : AbstractRememberTest() {

    private val helper = AndroidTestHelper.standard(TEST_DATASTORE_NAME)
    private val lifecycleOwner = ResumedLifecycleOwner()

    override val testDispatcher: TestDispatcher get() = helper.testDispatcher

    @Composable
    override fun PlatformProviders(content: @Composable () -> Unit) {
        CompositionLocalProvider(LocalLifecycleOwner provides lifecycleOwner, content = content)
    }

    @Before
    fun setup() = helper.setup()

    @After
    fun tearDown() = helper.tearDown()
}

private class ResumedLifecycleOwner : LifecycleOwner {
    private val registry = LifecycleRegistry.createUnsafe(this).apply {
        currentState = Lifecycle.State.RESUMED
    }

    override val lifecycle: Lifecycle get() = registry
}
