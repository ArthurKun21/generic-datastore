package io.github.arthurkun.generic.datastore.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import io.github.arthurkun.generic.datastore.app.domain.setAppCompatDelegateThemeMode
import io.github.arthurkun.generic.datastore.compose.app.theme.GenericDataStoreAppTheme
import io.github.arthurkun.generic.datastore.compose.app.ui.MainScreen
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as MainApplication).appContainer

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                appContainer.preferenceStore.theme.asFlow().collect { theme ->
                    withContext(Dispatchers.Main) {
                        setAppCompatDelegateThemeMode(theme)
                    }
                }
            }
        }

        setContent {
            GenericDataStoreAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.surface,
                ) {
                    MainScreen(
                        preferenceStore = appContainer.preferenceStore,
                    )
                }
            }
        }
    }
}
