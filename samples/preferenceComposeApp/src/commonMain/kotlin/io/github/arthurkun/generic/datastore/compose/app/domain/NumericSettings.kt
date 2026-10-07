package io.github.arthurkun.generic.datastore.compose.app.domain

/**
 * Display-scale setting stored through `serializedAsInt`, whose serialized form is an [Int].
 */
@JvmInline
value class FontScale(val percent: Int)

/**
 * Session timeout stored through `serializedAsLong`, whose serialized form is a [Long].
 */
@JvmInline
value class SessionTimeout(val millis: Long)

/**
 * Playback volume stored through `serializedAsFloat`, whose serialized form is a [Float].
 */
@JvmInline
value class Volume(val level: Float)

/**
 * Device latitude stored through `serializedAsDouble`, whose serialized form is a [Double].
 */
@JvmInline
value class Latitude(val degrees: Double)
