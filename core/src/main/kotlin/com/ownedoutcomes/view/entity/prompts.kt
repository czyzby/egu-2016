package com.ownedoutcomes.view.entity

import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.math.MathUtils
import ktx.assets.loadOnDemand

interface Prompt {
    val prompt: String
}
enum class CaughtPrompt(override val prompt: String) : Prompt {
    CAUGHT0("Dobrze!"),
    CAUGHT1("Dobrze mu tak!"),
    CAUGHT2("Dzwonię na milicję!"),
    CAUGHT3("Już nie pocwaniaczy!"),
    CAUGHT4("Ja wam dam, łobuzy!")
}

enum class MissmatchPrompt(override val prompt: String) : Prompt{
    MISS0("Nie tu!"),
    MISS1("Ja się pomyliła..."),
    MISS2("Motyla noga!"),
    MISS3("Kurza twarz!"),
    MISS4("Niech to dunder świśnie!")
}

enum class RunPrompt(override val prompt: String) : Prompt{
    RUN0("Zwiał złoczyńca!"),
    RUN1("Uciekł mnie!"),
    RUN2("A gdzie on!"),
    RUN3("O, polazł mnie!"),
    RUN4("Niech cię chudy grzyb!")
}

fun randomCaughtPrompt() = CaughtPrompt.values()[MathUtils.random(0, CaughtPrompt.values().size - 1)]
fun randomMissmatchPrompt() = MissmatchPrompt.values()[MathUtils.random(0, MissmatchPrompt.values().size - 1)]
fun randomRunPrompt() = RunPrompt.values()[MathUtils.random(0, RunPrompt.values().size - 1)]

fun loadSounds() {
    CaughtPrompt.values().forEach { loadOnDemand<Sound>("sounds/${it.name}.wav").asset }
    MissmatchPrompt.values().forEach { loadOnDemand<Sound>("sounds/${it.name}.wav").asset }
    RunPrompt.values().forEach { loadOnDemand<Sound>("sounds/${it.name}.wav").asset }
}