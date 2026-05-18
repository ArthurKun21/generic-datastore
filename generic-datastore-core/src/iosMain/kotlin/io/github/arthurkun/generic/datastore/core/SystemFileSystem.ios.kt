package io.github.arthurkun.generic.datastore.core

import okio.FileSystem

@InternalGenericDatastoreApi
public actual val systemFileSystem: FileSystem = FileSystem.SYSTEM
