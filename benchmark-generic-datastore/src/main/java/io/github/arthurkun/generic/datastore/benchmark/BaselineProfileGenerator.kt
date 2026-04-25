package io.github.arthurkun.generic.datastore.benchmark

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.benchmark.macro.junit4.BaselineProfileRule
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Rule
import org.junit.Test

private const val PACKAGE_NAME = "io.github.arthurkun.generic.datastore.benchmark.app"
private const val DEFAULT_TIMEOUT_MS = 5_000L

@RequiresApi(Build.VERSION_CODES.P)
class BaselineProfileGenerator {

    @get:Rule
    val baselineProfileRule = BaselineProfileRule()

    @Test
    fun startupAndCriticalJourney() = baselineProfileRule.collect(
        packageName = PACKAGE_NAME,
        stableIterations = 2,
        maxIterations = 8,
    ) {
        pressHome()
        startActivityAndWait()
        device.waitForIdle()

        if (!device.waitForTag("benchmark_ready")) {
            return@collect
        }

        device.clickTag("update_title_button")
        device.clickTag("increment_batch_button")
        device.clickTag("toggle_batch_button")
        device.clickTag("cycle_theme_button")
        device.clickTag("direct_batch_write_button")
        device.clickTag("reset_all_button")
        device.waitForIdle()
    }
}

private fun UiDevice.waitForTag(tag: String, timeoutMs: Long = DEFAULT_TIMEOUT_MS): Boolean {
    return wait(Until.hasObject(By.res(PACKAGE_NAME, tag)), timeoutMs)
}

private fun UiDevice.clickTag(tag: String, timeoutMs: Long = DEFAULT_TIMEOUT_MS) {
    check(waitForTag(tag, timeoutMs)) { "Could not find UI element with tag: $tag" }
    checkNotNull(findObject(By.res(PACKAGE_NAME, tag))) { "Could not click UI element with tag: $tag" }
        .click()
}
