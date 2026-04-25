package io.github.arthurkun.generic.datastore.benchmark.app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.unit.dp
import io.github.arthurkun.generic.datastore.batch.ProvidePreferencesDatastore
import io.github.arthurkun.generic.datastore.batch.rememberPreferences
import io.github.arthurkun.generic.datastore.preferences.GenericPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.createPreferencesDatastore
import io.github.arthurkun.generic.datastore.preferences.enum
import io.github.arthurkun.generic.datastore.remember
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val benchmarkStore by lazy { BenchmarkStore(applicationContext) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background,
                ) {
                    BenchmarkProfileScreen(benchmarkStore)
                }
            }
        }
    }
}

private class BenchmarkStore(context: Context) {
    val datastore: GenericPreferencesDatastore = createPreferencesDatastore(
        fileName = "baseline-profile.preferences_pb",
        producePath = { context.filesDir.absolutePath },
    )

    val title = datastore.string(
        key = "title",
        defaultValue = "Generic Datastore",
    )

    val launchCount = datastore.int(
        key = "launch_count",
        defaultValue = 0,
    )

    val enabled = datastore.bool(
        key = "enabled",
        defaultValue = false,
    )

    val theme = datastore.enum(
        key = "theme",
        defaultValue = BenchmarkTheme.SYSTEM,
    )
}

private enum class BenchmarkTheme {
    SYSTEM,
    LIGHT,
    DARK,
}

@Composable
private fun BenchmarkProfileScreen(store: BenchmarkStore) {
    ProvidePreferencesDatastore(store.datastore) {
        var title by store.title.remember()
        var theme by store.theme.remember()
        val (batchLaunchCount, batchEnabled) = rememberPreferences(store.launchCount, store.enabled)
        var launchCount by batchLaunchCount
        var enabled by batchEnabled
        val scope = rememberCoroutineScope()

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .semantics { testTagsAsResourceId = true },
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Baseline Profile Benchmark",
                modifier = Modifier.testTag("benchmark_ready"),
                style = MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Title: $title",
                modifier = Modifier.testTag("title_value"),
            )
            Text(
                text = "Launch Count: $launchCount",
                modifier = Modifier.testTag("launch_count_value"),
            )
            Text(
                text = "Enabled: $enabled",
                modifier = Modifier.testTag("enabled_value"),
            )
            Text(
                text = "Theme: $theme",
                modifier = Modifier.testTag("theme_value"),
            )

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("update_title_button"),
                onClick = {
                    title = if (title == store.title.defaultValue) {
                        "Generic Datastore Ready"
                    } else {
                        "$title!"
                    }
                },
            ) {
                Text("Update title")
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("increment_batch_button"),
                onClick = { launchCount += 1 },
            ) {
                Text("Increment counter")
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("toggle_batch_button"),
                onClick = { enabled = !enabled },
            ) {
                Text("Toggle enabled")
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("cycle_theme_button"),
                onClick = { theme = theme.next() },
            ) {
                Text("Cycle theme")
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("direct_batch_write_button"),
                onClick = {
                    scope.launch {
                        store.datastore.batchWrite {
                            this[store.title] = "Batched-$launchCount"
                            this[store.launchCount] = launchCount + 10
                            this[store.enabled] = true
                            this[store.theme] = BenchmarkTheme.DARK
                        }
                    }
                },
            ) {
                Text("Run direct batch write")
            }

            Button(
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("reset_all_button"),
                onClick = {
                    scope.launch {
                        store.title.resetToDefault()
                        store.launchCount.resetToDefault()
                        store.enabled.resetToDefault()
                        store.theme.resetToDefault()
                    }
                },
            ) {
                Text("Reset all")
            }
        }
    }
}

private fun BenchmarkTheme.next(): BenchmarkTheme = when (this) {
    BenchmarkTheme.SYSTEM -> BenchmarkTheme.LIGHT
    BenchmarkTheme.LIGHT -> BenchmarkTheme.DARK
    BenchmarkTheme.DARK -> BenchmarkTheme.SYSTEM
}
