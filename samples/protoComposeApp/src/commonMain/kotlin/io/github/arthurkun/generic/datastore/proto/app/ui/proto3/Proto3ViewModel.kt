package io.github.arthurkun.generic.datastore.proto.app.ui.proto3

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import io.github.arthurkun.generic.datastore.proto.GenericProtoDatastore
import io.github.arthurkun.generic.datastore.proto.ProtoPreference
import io.github.arthurkun.generic.datastore.proto.app.wire.AppConfig
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel for the Proto3 (AppConfig) screen.
 *
 * Showcases `field()` with 3 levels of nesting (AppConfig → NetworkConfig → RetryPolicy)
 * and all [ProtoDatastore] operations.
 */
class Proto3ViewModel(
    private val datastore: GenericProtoDatastore<AppConfig>,
) : ViewModel() {

    /** Whole-object preference */
    val wholeData: ProtoPreference<AppConfig> = datastore.data()

    // --- Top-level fields ---

    val appNamePref: ProtoPreference<String> = datastore.field(
        defaultValue = "",
        getter = { it.app_name },
        updater = { proto, value -> proto.copy(app_name = value) },
    )

    val maxRetriesPref: ProtoPreference<Int> = datastore.field(
        defaultValue = 0,
        getter = { it.max_retries },
        updater = { proto, value -> proto.copy(max_retries = value) },
    )

    val debugModePref: ProtoPreference<Boolean> = datastore.field(
        defaultValue = false,
        getter = { it.debug_mode },
        updater = { proto, value -> proto.copy(debug_mode = value) },
    )

    val refreshIntervalPref: ProtoPreference<Double> = datastore.field(
        defaultValue = 0.0,
        getter = { it.refresh_interval },
        updater = { proto, value -> proto.copy(refresh_interval = value) },
    )

    // --- Nested NetworkConfig fields ---

    val networkPref: ProtoPreference<AppConfig.NetworkConfig?> = datastore.field(
        defaultValue = null,
        getter = { it.network },
        updater = { proto, value -> proto.copy(network = value) },
    )

    val baseUrlPref: ProtoPreference<String> = datastore.field(
        defaultValue = "",
        getter = { it.network?.base_url ?: "" },
        updater = { proto, value ->
            proto.copy(
                network = (proto.network ?: AppConfig.NetworkConfig()).copy(base_url = value),
            )
        },
    )

    val timeoutMsPref: ProtoPreference<Int> = datastore.field(
        defaultValue = 0,
        getter = { it.network?.timeout_ms ?: 0 },
        updater = { proto, value ->
            proto.copy(
                network = (proto.network ?: AppConfig.NetworkConfig()).copy(timeout_ms = value),
            )
        },
    )

    // --- Deeply nested RetryPolicy fields (3 levels) ---

    val maxAttemptsPref: ProtoPreference<Int> = datastore.field(
        defaultValue = 0,
        getter = { it.network?.retry_policy?.max_attempts ?: 0 },
        updater = { proto, value ->
            val network = proto.network ?: AppConfig.NetworkConfig()
            val policy = network.retry_policy ?: AppConfig.NetworkConfig.RetryPolicy()
            proto.copy(
                network = network.copy(
                    retry_policy = policy.copy(max_attempts = value),
                ),
            )
        },
    )

    val backoffMsPref: ProtoPreference<Long> = datastore.field(
        defaultValue = 0L,
        getter = { it.network?.retry_policy?.backoff_ms ?: 0L },
        updater = { proto, value ->
            val network = proto.network ?: AppConfig.NetworkConfig()
            val policy = network.retry_policy ?: AppConfig.NetworkConfig.RetryPolicy()
            proto.copy(
                network = network.copy(
                    retry_policy = policy.copy(backoff_ms = value),
                ),
            )
        },
    )

    val exponentialPref: ProtoPreference<Boolean> = datastore.field(
        defaultValue = false,
        getter = { it.network?.retry_policy?.exponential ?: false },
        updater = { proto, value ->
            val network = proto.network ?: AppConfig.NetworkConfig()
            val policy = network.retry_policy ?: AppConfig.NetworkConfig.RetryPolicy()
            proto.copy(
                network = network.copy(
                    retry_policy = policy.copy(exponential = value),
                ),
            )
        },
    )

    /** stateIn() demo for a deeply nested field */
    val maxAttemptsState: StateFlow<Int> = maxAttemptsPref.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
    )

    /** Combine all field flows into a single UI state */
    val uiState: StateFlow<Proto3ScreenState> = combine(
        appNamePref.asFlow(),
        maxRetriesPref.asFlow(),
        debugModePref.asFlow(),
        refreshIntervalPref.asFlow(),
        baseUrlPref.asFlow(),
    ) { appName, maxRetries, debugMode, refreshInterval, baseUrl ->
        Proto3ScreenState(
            appName = appName,
            maxRetries = maxRetries,
            debugMode = debugMode,
            refreshInterval = refreshInterval,
            baseUrl = baseUrl,
        )
    }.combine(
        combine(
            timeoutMsPref.asFlow(),
            maxAttemptsPref.asFlow(),
            backoffMsPref.asFlow(),
            exponentialPref.asFlow(),
        ) { timeoutMs, maxAttempts, backoffMs, exponential ->
            Proto3PartialState(timeoutMs, maxAttempts, backoffMs, exponential)
        },
    ) { partial1, partial2 ->
        partial1.copy(
            timeoutMs = partial2.timeoutMs,
            maxAttempts = partial2.maxAttempts,
            backoffMs = partial2.backoffMs,
            exponential = partial2.exponential,
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = Proto3ScreenState(),
    )

    // --- Whole-object operations ---

    fun resetAll() {
        viewModelScope.launch { wholeData.resetToDefault() }
    }

    fun deleteAll() {
        viewModelScope.launch { wholeData.delete() }
    }

    // --- Per-field set operations ---

    fun setAppName(value: String) {
        viewModelScope.launch { appNamePref.set(value) }
    }

    fun setMaxRetries(value: Int) {
        viewModelScope.launch { maxRetriesPref.set(value) }
    }

    /** Demonstrates `update()` — atomic increment */
    fun incrementMaxRetries() {
        viewModelScope.launch {
            maxRetriesPref.update { it + 1 }
        }
    }

    fun setDebugMode(value: Boolean) {
        viewModelScope.launch { debugModePref.set(value) }
    }

    fun setRefreshInterval(value: Double) {
        viewModelScope.launch { refreshIntervalPref.set(value) }
    }

    fun setBaseUrl(value: String) {
        viewModelScope.launch { baseUrlPref.set(value) }
    }

    fun setTimeoutMs(value: Int) {
        viewModelScope.launch { timeoutMsPref.set(value) }
    }

    fun setMaxAttempts(value: Int) {
        viewModelScope.launch { maxAttemptsPref.set(value) }
    }

    fun setBackoffMs(value: Long) {
        viewModelScope.launch { backoffMsPref.set(value) }
    }

    fun toggleExponential() {
        viewModelScope.launch {
            exponentialPref.update { !it }
        }
    }

    /** Demonstrates `field().resetToDefault()` on a nested object */
    fun resetNetwork() {
        viewModelScope.launch { networkPref.resetToDefault() }
    }

    // --- Blocking operations ---

    fun getAppNameBlocking(): String = appNamePref.getBlocking()

    fun setAppNameBlocking(value: String) {
        appNamePref.setBlocking(value)
    }

    fun resetAppNameBlocking() {
        appNamePref.resetToDefaultBlocking()
    }
}

/** Helper for combining more than 5 flows */
private data class Proto3PartialState(
    val timeoutMs: Int,
    val maxAttempts: Int,
    val backoffMs: Long,
    val exponential: Boolean,
)
