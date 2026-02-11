package io.github.arthurkun.generic.datastore.proto

import org.junit.jupiter.api.io.TempDir
import java.io.File
import kotlin.test.AfterTest
import kotlin.test.BeforeTest

class DesktopNullableProtoFieldPreferenceBlockingTest :
    AbstractNullableProtoFieldPreferenceBlockingTest() {

    @TempDir
    lateinit var tempFolder: File

    private val helper =
        DesktopNullableProtoTestHelper.blocking("test_nullable_proto_field_blocking")

    override val nullableProtoDatastore get() = helper.protoDatastore

    @BeforeTest
    fun setup() = helper.setup(tempFolder.absolutePath)

    @AfterTest
    fun tearDown() = helper.tearDown()
}
