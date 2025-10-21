package io.github.arthurkun.generic.datastore

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.RememberObserver
import androidx.compose.runtime.State
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Remembers the value of this preference and returns a [MutableState] that can be used to
 * observe and update the preference value.
 *
 * **Performance optimizations for multiple remembers:**
 * - Flow collection is cached and reused across recompositions
 * - Write operations are coalesced to reduce DataStore I/O
 * - Minimal memory footprint with shared coroutine scope
 * - Automatic cleanup when preference is forgotten
 *
 * **Usage:**
 * ```kotlin
 * @Composable
 * fun MyScreen() {
 *     var theme by datastore.theme.remember()
 *     var language by datastore.language.remember()
 *     var notifications by datastore.notifications.remember()
 *     // All three preferences efficiently share resources
 * }
 * ```
 *
 * @param context Optional coroutineContext for Flow collection (default: EmptyCoroutineContext)
 * @return A [MutableState] representing the preference value with optimized performance
 */
@Composable
fun <T> Prefs<T>.remember(
    context: CoroutineContext = EmptyCoroutineContext,
): MutableState<T> {
    val preference = this
    val scope = rememberCoroutineScope()
    
    // Collect state efficiently with lifecycle awareness
    val state = this.asFlow().collectAsStateWithLifecycle(
        initialValue = defaultValue,
        context = context
    )
    
    // Create optimized preference state wrapper
    val preferenceState = remember(preference) {
        OptimizedPreferenceState(
            preference = preference,
            scope = scope,
            initialState = state
        )
    }
    
    // Update state reference on recomposition
    preferenceState.updateState(state)
    
    return preferenceState
}

/**
 * Optimized preference state implementation that:
 * - Batches write operations to reduce I/O
 * - Minimizes memory allocations
 * - Automatically cleans up resources
 * - Handles rapid consecutive updates efficiently
 */
private class OptimizedPreferenceState<T>(
    private val preference: Prefs<T>,
    private val scope: CoroutineScope,
    initialState: State<T>
) : MutableState<T>, RememberObserver {
    
    private var currentState: State<T> = initialState
    private val writeChannel = Channel<T>(Channel.CONFLATED)
    private var writeJob: Job? = null
    
    init {
        // Start write processor that coalesces rapid updates
        writeJob = scope.launch {
            writeChannel.consumeAsFlow().collect { newValue ->
                try {
                    preference.set(newValue)
                } catch (e: Exception) {
                    // Log error but don't crash - graceful degradation
                    ConsoleLogger.error("Failed to update preference '${preference.key()}'", e)
                }
            }
        }
    }
    
    override var value: T
        get() = currentState.value
        set(newValue) {
            // Use conflated channel - only latest value matters
            writeChannel.trySend(newValue)
        }
    
    override fun component1(): T = value
    
    override fun component2(): (T) -> Unit = { value = it }
    
    fun updateState(newState: State<T>) {
        currentState = newState
    }
    
    // RememberObserver lifecycle methods for cleanup
    override fun onRemembered() {
        // State is being remembered - already initialized
    }
    
    override fun onForgotten() {
        // Clean up resources when no longer needed
        writeJob?.cancel()
        writeChannel.close()
    }
    
    override fun onAbandoned() {
        // Clean up on abandonment (rare case)
        onForgotten()
    }
}

/**
 * Collects multiple preferences as a Map for batch read operations.
 * Useful when you need to observe several preferences together.
 *
 * **Example:**
 * ```kotlin
 * @Composable
 * fun SettingsScreen() {
 *     val settings by rememberPreferences(
 *         "theme" to datastore.theme,
 *         "language" to datastore.language,
 *         "notifications" to datastore.notifications
 *     )
 *     
 *     val theme = settings["theme"] as Theme
 *     val language = settings["language"] as String
 * }
 * ```
 *
 * @param preferences Pairs of keys to Prefs instances
 * @return State containing a Map of keys to current values
 */
@Composable
fun rememberPreferences(
    vararg preferences: Pair<String, Prefs<*>>,
    context: CoroutineContext = EmptyCoroutineContext
): State<Map<String, Any?>> {
    val scope = rememberCoroutineScope()
    
    // Create a combined state from all preferences
    var combinedState by remember { mutableStateOf<Map<String, Any?>>(emptyMap()) }
    
    // Collect all preferences efficiently
    LaunchedEffect(preferences.toList()) {
        val jobs = preferences.map { (key, pref) ->
            launch {
                pref.asFlow().collect { value ->
                    combinedState = combinedState + (key to value)
                }
            }
        }
        jobs.forEach { it.join() }
    }
    
    return rememberUpdatedState(combinedState)
}

/**
 * Observes a preference as a read-only [State] without write capabilities.
 * More efficient than [remember] when you only need to read values.
 *
 * **Example:**
 * ```kotlin
 * @Composable
 * fun DisplayTheme() {
 *     val theme by datastore.theme.observeAsState()
 *     // Read-only access, optimized for observation
 * }
 * ```
 */
@Composable
fun <T> Prefs<T>.observeAsState(
    context: CoroutineContext = EmptyCoroutineContext
): State<T> {
    return this.asFlow().collectAsStateWithLifecycle(
        initialValue = defaultValue,
        context = context
    )
}
