package io.github.arthurkun.generic.datastore.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import io.github.arthurkun.generic.datastore.app.theme.GenericDataStoreAppTheme
import io.github.arthurkun.generic.datastore.app.ui.MainScreen
import io.github.arthurkun.generic.datastore.app.ui.MainViewModel

class MainActivity : AppCompatActivity() {

    private lateinit var mainViewModel: MainViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val appContainer = (application as MainApplication).appContainer

        mainViewModel = MainViewModel(appContainer.preferenceStore)

        setContent {
            GenericDataStoreAppTheme {
                MainScreen(
                    vm = mainViewModel
                )
            }
        }
    }
}

