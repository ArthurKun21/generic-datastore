package io.github.arthurkun.generic.datastore.proto.core

import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher

class IosProtoCorruptionTest : AbstractProtoCorruptionTest() {
    override val testDispatcher: TestDispatcher = StandardTestDispatcher()
}
