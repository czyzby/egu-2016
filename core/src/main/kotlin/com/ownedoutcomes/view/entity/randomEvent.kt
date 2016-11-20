package com.ownedoutcomes.view.entity

import com.badlogic.gdx.math.MathUtils

enum class RandomEvent {
    VODKA,
    GRANNY,
    GLASSES,
    BATTERY
}

fun getRandomRandomEvent() = RandomEvent.values()[MathUtils.random(0, RandomEvent.values().size - 1)]