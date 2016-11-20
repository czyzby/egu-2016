package com.ownedoutcomes.view.entity

import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.Sprite
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.kotcrab.vis.ui.VisUI
import com.kotcrab.vis.ui.widget.VisImage
import com.ownedoutcomes.view.DragController
import ktx.actors.then
import ktx.collections.toGdxArray

enum class PedestrianType {
    REGULAR,
    URINE,
    VODKA,
    BEER,
    SMOKE
}

fun getRandomPedestrian(dragController: DragController): Pedestrian {
    if (MathUtils.random() < 0.4f) {
        return GoodCitizen(dragController)
    }
    return when (PedestrianType.values()[MathUtils.random(0, PedestrianType.values().size - 1)]) {
        PedestrianType.REGULAR -> GoodCitizen(dragController)
        PedestrianType.URINE -> Pisser(dragController)
        PedestrianType.BEER -> BeerDrinker(dragController)
        PedestrianType.VODKA -> VodkaDrinker(dragController)
        PedestrianType.SMOKE -> Smoker(dragController)
    }
}

abstract class Pedestrian(val drawableName: String, val dragController: DragController) : VisImage(drawableName) {
    private val sprite = Sprite(VisUI.getSkin().getRegion(drawableName))
    abstract val type: PedestrianType

    init {
        val targetX = setInitialPos()
        addListener(dragController.getDraggable())
        addInitialActions(targetX)
    }

    open protected fun setInitialPos(): Float {
        var x = -100f
        var targetX = 900f
        if (MathUtils.randomBoolean()) {
            x = targetX
            targetX = -100f
        } else {
            sprite.setFlip(true, false)
        }
        setPosition(x, randomY())
        return targetX
    }

    override fun draw(batch: Batch, parentAlpha: Float) {
        sprite.setPosition(x, y)
        sprite.rotation = rotation
        sprite.draw(batch, parentAlpha * color.a)
    }

    protected fun randomY() = MathUtils.random(10f, 100f)

    open protected fun addInitialActions(targetX: Float) {
        setOrigin(width / 2f, height / 2f)
        addAction(Actions.forever(Actions.sequence(
                Actions.rotateBy(20f, 0.2f),
                Actions.rotateBy(-40f, 0.4f),
                Actions.rotateBy(20f, 0.2f)
        )))
        addAction(Actions.moveTo(targetX, randomY(), MathUtils.random(3f, 6f)) then Actions.run {
            if (type != PedestrianType.REGULAR) {
                dragController.decrementPoints(run = true)
            }
        } then Actions.removeActor())
    }
}

class GoodCitizen(dragController: DragController) :
        Pedestrian((0..7).map { "regular$it" }.toGdxArray().random(), dragController) {
    override val type: PedestrianType
        get() = PedestrianType.REGULAR
}

class Pisser(dragController: DragController) :
        Pedestrian((0..7).map { "regular$it" }.toGdxArray().random(), dragController) {
    override val type: PedestrianType
        get() = PedestrianType.URINE

    override fun addInitialActions(targetX: Float) {
        setOrigin(width / 2f, height / 2f)
        val totalTime = MathUtils.random(3f, 6f)
        val pissingTime = MathUtils.random(1f, totalTime - 1.5f)
        val rotationAction = addRotation()
        val walkingAction = addWalkingAction(targetX, totalTime)
        addAction(Actions.delay(pissingTime) then Actions.rotateTo(0f, 0.15f) then Actions.run {
            removeAction(walkingAction)
            removeAction(rotationAction)
            piss()
        } then Actions.delay(1f) then Actions.run {
            addRotation()
            addWalkingAction(targetX, totalTime - pissingTime)
        })
    }

    private fun piss() {
        val urine = Urine()
        urine.setScale(0f)
        urine.setOrigin(urine.width / 2f, urine.height / 2f)
        urine.setPosition(x + 5, y)
        urine.addAction(Actions.scaleTo(1f, 1f, 0.5f) then Actions.delay(3f)
                then Actions.fadeOut(0.4f) then Actions.removeActor())
        stage.addActor(urine)
    }

    private fun addRotation(): Action {
        val action = Actions.forever(Actions.sequence(
                Actions.rotateBy(20f, 0.2f),
                Actions.rotateBy(-40f, 0.4f),
                Actions.rotateBy(20f, 0.2f)
        ))
        addAction(action)
        return action
    }

    private fun addWalkingAction(targetX: Float, totalTime: Float): Action {
        val action = Actions.moveTo(targetX, randomY(), totalTime) then Actions.run {
            dragController.decrementPoints(run = true)
        } then Actions.removeActor();
        addAction(action)
        return action
    }
}

class Urine : VisImage("urine")

class BeerDrinker(dragController: DragController) :
        Pedestrian((0..0).map { "beer$it" }.toGdxArray().random(), dragController) {
    override val type: PedestrianType
        get() = PedestrianType.BEER
}

class VodkaDrinker(dragController: DragController) :
        Pedestrian((0..0).map { "vodka$it" }.toGdxArray().random(), dragController) {
    override val type: PedestrianType
        get() = PedestrianType.VODKA
}

class Smoker(dragController: DragController) :
        Pedestrian((0..0).map { "smoke$it" }.toGdxArray().random(), dragController) {
    override val type: PedestrianType
        get() = PedestrianType.SMOKE
}