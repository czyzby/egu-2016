package com.ownedoutcomes.view.entity

import com.badlogic.gdx.math.Interpolation
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.kotcrab.vis.ui.widget.VisImage
import com.ownedoutcomes.music.MiscPrompt
import com.ownedoutcomes.view.Game
import ktx.actors.onClick
import ktx.actors.then

abstract class FallingObject(drawableName: String) : VisImage(drawableName) {
    var clicked = false

    init {
        onClick { event, image ->
            if (!clicked) {
                clicked = true
                doOnClick()
                addAction(Actions.fadeOut(0.2f) then Actions.removeActor())
            }
        }
        x = MathUtils.random(50f, 750f)
        y = 650f
        addAction(Actions.moveBy(0f, -MathUtils.random(570f, 620f), 2f, Interpolation.bounceOut)
                then Actions.delay(2f) then Actions.run { clicked = true }
                then Actions.fadeOut(0.2f) then Actions.removeActor())
    }

    abstract fun doOnClick()
}

class Vodka(val game: Game) : FallingObject("vodka-falling") {
    override fun doOnClick() {
        game.dragController.boostPoints(MiscPrompt.VODKA)
    }
}

class Battery(val game: Game) : FallingObject("battery") {
    override fun doOnClick() {
        game.improveVision()
        game.dragController.displayPrompt(MiscPrompt.BATTERY, negative = false)
    }
}

class Glasses(val game: Game) : FallingObject("glasses") {
    override fun doOnClick() {
        game.improveVision()
        game.dragController.displayPrompt(MiscPrompt.GLASSES, negative = false)
    }
}