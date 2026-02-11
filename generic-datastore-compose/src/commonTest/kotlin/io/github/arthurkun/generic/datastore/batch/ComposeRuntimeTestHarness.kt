package io.github.arthurkun.generic.datastore.batch

import androidx.compose.runtime.AbstractApplier
import androidx.compose.runtime.BroadcastFrameClock
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Composition
import androidx.compose.runtime.Recomposer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.TestDispatcher

internal class ComposeRuntimeTestHarness(
    testDispatcher: TestDispatcher,
) {
    private val frameClock = BroadcastFrameClock()
    private val scope = CoroutineScope(Job() + testDispatcher + frameClock)
    private val recomposer = Recomposer(scope.coroutineContext)
    private val composition = Composition(NoOpApplier(), recomposer)

    init {
        scope.launch { recomposer.runRecomposeAndApplyChanges() }
    }

    fun setContent(content: @Composable () -> Unit) {
        composition.setContent(content)
    }

    private var frameTimeNanos = 0L

    suspend fun awaitIdle() {
        frameClock.sendFrame(frameTimeNanos)
        frameTimeNanos += 16_000_000L
    }

    fun dispose() {
        composition.dispose()
        scope.cancel()
    }
}

private object NoOpNode

private class NoOpApplier : AbstractApplier<Any?>(NoOpNode) {
    override fun insertTopDown(index: Int, instance: Any?) = Unit

    override fun insertBottomUp(index: Int, instance: Any?) = Unit

    override fun remove(index: Int, count: Int) = Unit

    override fun move(from: Int, to: Int, count: Int) = Unit

    override fun onClear() = Unit
}
