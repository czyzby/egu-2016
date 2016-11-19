package com.ownedoutcomes.view.entity

import com.badlogic.gdx.math.MathUtils

enum class CaughtPrompt(val prompt: String) {
    GOOD("Dobrze!"),
    GOOD_FOR_HIM("Dobrze mu tak!"),
    CALLING_COPS("Dzwonię na milicję!")
}

enum class MissmatchPrompt(val prompt: String) {
    NOT_HERE("Nie tu!"),
    WRONG("Ja się pomyliła...")

}

enum class RunPrompt(val prompt: String) {
    RUN0("Zwiał złoczyńca!"),
    RUN1("Uciekł mnie!")
}

fun randomCaughtPrompt() = CaughtPrompt.values()[MathUtils.random(0, CaughtPrompt.values().size - 1)].prompt
fun randomMissmatchPrompt() = MissmatchPrompt.values()[MathUtils.random(0, MissmatchPrompt.values().size - 1)].prompt
fun randomRunPrompt() = RunPrompt.values()[MathUtils.random(0, RunPrompt.values().size - 1)].prompt