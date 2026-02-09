package io.github.arthurkun.generic.datastore.core

import okio.FileSystem

/**
 * Provides the platform-specific [okio.FileSystem] instance for accessing the local file system.
 *
 * This uses the expect/actual pattern because [okio.FileSystem.SYSTEM] cannot be resolved in
 * `commonMain` when compiling for iOS targets. Each platform source set provides its own
 * `actual` that returns `FileSystem.SYSTEM`.
 */
internal expect val systemFileSystem: FileSystem
