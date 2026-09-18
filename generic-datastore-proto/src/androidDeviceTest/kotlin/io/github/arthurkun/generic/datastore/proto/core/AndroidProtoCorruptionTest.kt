package io.github.arthurkun.generic.datastore.proto.core

import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidProtoCorruptionTest : AbstractProtoCorruptionTest() {
    override val testDispatcher: TestDispatcher = StandardTestDispatcher()
}
