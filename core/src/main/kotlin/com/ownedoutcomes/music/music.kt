package com.ownedoutcomes.music

import com.badlogic.gdx.audio.Music
import com.badlogic.gdx.math.MathUtils
import ktx.assets.asset
import ktx.assets.loadOnDemand

enum class Theme(val id: String, val volumeModifier: Float) {
    _0("music/disco0.mp3", 0.9f),
    _1("music/disco1.mp3", 0.25f),
    _2("music/disco2.mp3", 1f),
    _3("music/disco3.wav", 0.25f),
}

fun playMusic() {
    val theme = randomTheme()
    val music = asset<Music>(theme.id);
    music.setOnCompletionListener {
        playMusic()
    }
    music.volume = theme.volumeModifier * 0.5f
    music.play()
}

private fun randomTheme() = Theme.values()[MathUtils.random(0, Theme.values().size - 1)]

fun loadMusic() {
    Theme.values().forEach { loadOnDemand<Music>(it.id).asset }
}