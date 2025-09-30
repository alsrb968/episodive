package io.jacob.episodive.core.model

enum class Playback(val value: Int) {
    IDLE(1), BUFFERING(2), READY(3), ENDED(4);

    companion object {
        fun fromValue(value: Int) = entries.first { it.value == value }
    }
}