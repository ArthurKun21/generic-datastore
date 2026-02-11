package io.github.arthurkun.generic.datastore.proto.app

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import io.github.arthurkun.generic.datastore.proto.app.domain.AppContainer
import io.github.arthurkun.generic.datastore.proto.app.ui.proto2.Proto2Screen
import io.github.arthurkun.generic.datastore.proto.app.ui.proto2.Proto2ViewModel
import io.github.arthurkun.generic.datastore.proto.app.ui.proto3.Proto3Screen
import io.github.arthurkun.generic.datastore.proto.app.ui.proto3.Proto3ViewModel

@Composable
fun ProtoApp(
    appContainer: AppContainer,
) {
    var selectedTab by remember { mutableIntStateOf(0) }
    val tabs = listOf("Proto2 — UserSettings", "Proto3 — AppConfig")

    Column(modifier = Modifier.fillMaxSize()) {
        PrimaryTabRow(
            selectedTabIndex = selectedTab,
            modifier = Modifier.fillMaxWidth(),
        ) {
            tabs.forEachIndexed { index, title ->
                Tab(
                    selected = selectedTab == index,
                    onClick = { selectedTab = index },
                    text = { Text(title) },
                )
            }
        }

        when (selectedTab) {
            0 -> Proto2Screen(
                viewModel = Proto2ViewModel(
                    datastore = appContainer.userSettingsDatastore,
                ),
            )

            1 -> Proto3Screen(
                viewModel = Proto3ViewModel(
                    datastore = appContainer.appConfigDatastore,
                ),
            )
        }
    }
}
