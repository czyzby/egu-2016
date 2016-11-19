package com.ownedoutcomes.view

import box2dLight.ConeLight
import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.audio.Sound
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Action
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Label
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.Draggable
import com.kotcrab.vis.ui.widget.VisImage
import com.kotcrab.vis.ui.widget.VisLabel
import com.ownedoutcomes.view.entity.*
import ktx.actors.alpha
import ktx.actors.then
import ktx.assets.asset
import ktx.assets.getValue
import ktx.assets.load
import ktx.assets.loadOnDemand
import ktx.math.vec2
import ktx.vis.table
import java.util.*

class Game(stage: Stage, skin: Skin, val batch: Batch) : AbstractView(stage) {
    val backgroundImage by loadOnDemand<Texture>(path = "background.png")
    val rayHandler = RayHandler(World(vec2(), true))
    val light = PointLight(rayHandler, 30, Color(0f, 0f, 0f, 1f), 150f, 100f, 100f)
    val lightPosition = vec2()
    val dragController = DragController(this, stage)
    val spawner = Spawner(stage, dragController)
    val handImage = VisImage("hand")
    val promptLabel = VisLabel("", "title")
    override val root: Actor
    lateinit var urineImage: Image
    lateinit var beerImage: Image
    lateinit var vodkaImage: Image
    lateinit var smokeImage: Image

    init {
        light.isXray = true
        ConeLight(rayHandler, 15, Color.BLACK, 600f, 600f, 400f, 45f, 45f)  // Bonuses.
        ConeLight(rayHandler, 15, Color.BLACK, 350f, 300f, 540f, -45f, 45f) // Room light.
        root = table {
            backgroundImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            background = TextureRegionDrawable(TextureRegion(backgroundImage, 0, 0, 1000, 750))
            setFillParent(true)
            table {
                urineImage = image(drawableName = "piss").actor
                image(drawableName = "beer") {
                    beerImage = this
                }.row()
                vodkaImage = image(drawableName = "vodka").actor
                smokeImage = image(drawableName = "smoke").actor
            }.expand().align(Align.topRight)
        }
        handImage.setPosition(312f, 387f)
        handImage.setOrigin(10f, 70f)
        resetPrompt()
    }

    fun resetPrompt() {
        promptLabel.clearActions()
        promptLabel.setPosition(380f, 510f)
        promptLabel.alpha = 0f
    }

    val actorsComparator = Comparator<Actor> { o1, o2 ->
        when {
            o1 === o2 -> 0
            o1 === root -> -1
            o2 === root -> 1
            o1 === handImage -> -1
            o2 === handImage -> 1
            o1 is Draggable.MimicActor -> 1
            o2 is Draggable.MimicActor -> 1
            o1 is Label -> 1
            o2 is Label -> 1
            o1 is Urine && o2 is Urine -> 0
            o1 is Urine -> -1
            o2 is Urine -> 1
            o1.y < o2.y -> 1
            o1.y == o2.y -> 0
            else -> -1
        }
    }

    override fun render(delta: Float) {
        sortActors()
        updateHand()
        spawner.update(delta)
        super.render(delta)
        stage.screenToStageCoordinates(lightPosition.set(Gdx.input.x.toFloat(), Gdx.input.y.toFloat()));
        light.position = lightPosition
        light.update()
        rayHandler.setCombinedMatrix(stage.camera.combined)
        rayHandler.updateAndRender()
    }

    private fun updateHand() {
        val angle = MathUtils.atan2(lightPosition.y - handImage.y, lightPosition.x - handImage.x)
        handImage.rotation = MathUtils.radiansToDegrees * angle
    }

    override fun show() {
        // TODO Tutorial on first show!
        super.show()
        rayHandler.setAmbientLight(0f, 0f, 0f, 0.1f)
        stage.addActor(handImage)
        spawner.reset()
        dragController.reset()
        stage.addActor(promptLabel)
    }

    private fun sortActors() {
        stage.actors.sort(actorsComparator)
    }
}

class Spawner(val stage: Stage, val dragController: DragController) {
    var timeSinceSpawn = 0f

    fun update(delta: Float) {
        timeSinceSpawn += delta
        if (timeSinceSpawn > MathUtils.random(2f, 3f)) {
            spawn()
            timeSinceSpawn = 0f
        }
    }

    private fun spawn() {
        stage.addActor(getRandomPedestrian(dragController))
    }

    fun reset() {
        timeSinceSpawn = 0f
    }
}

class DragController(val game: Game, val stage: Stage) {
    var currentSound: Sound? = null
    var lives = 3
    var points = 0

    fun reset() {
        lives = 3
    }

    fun getDraggable(): Draggable {
        val draggable = Draggable()
        draggable.listener = object : Draggable.DragAdapter() {
            override fun onEnd(draggable: Draggable, actor: Actor, stageX: Float, stageY: Float): Boolean {
                val hit = stage.hit(stageX, stageY, false)
                if (hit == null) {
                    return Draggable.DragListener.APPROVE
                }
                val pedestrian = actor as Pedestrian
                when (hit) {
                    game.urineImage -> checkPedestrian(pedestrian, PedestrianType.URINE)
                    game.beerImage -> checkPedestrian(pedestrian, PedestrianType.BEER)
                    game.smokeImage -> checkPedestrian(pedestrian, PedestrianType.SMOKE)
                    game.vodkaImage -> checkPedestrian(pedestrian, PedestrianType.VODKA)
                }
                return Draggable.DragListener.APPROVE
            }
        }
        return draggable
    }

    fun checkPedestrian(pedestrian: Pedestrian, pedestrianType: PedestrianType) {
        if (pedestrian.type == pedestrianType) {
            pedestrian.clearActions()
            pedestrian.removeListener(pedestrian.listeners.first())
            pedestrian.addAction(Actions.sequence(Actions.fadeOut(0.2f), Actions.removeActor()))
            incrementPoints()
        } else {
            decrementPoints()
        }
    }

    fun incrementPoints() {
        lives++
        if (lives > 5) {
            lives = 5
        }
        points++
        displayPrompt(randomCaughtPrompt(), negative = false)
    }

    private fun displayPrompt(prompt: Prompt, negative: Boolean = true) {
        game.resetPrompt()
        if (negative) {
            game.promptLabel.color = negativeColor
        } else {
            game.promptLabel.color = positiveColor
        }
        game.promptLabel.setText(prompt.prompt)
        currentSound?.stop()
        currentSound = asset<Sound>("sounds/${prompt.toString()}.mp3").apply {
            play(0.5f)
        }
        game.promptLabel.addAction(Actions.parallel(
                Actions.fadeIn(0.1f),
                Actions.moveBy(15f, 10f, 0.15f)
        ) then Actions.delay(0.15f) then Actions.parallel(
                Actions.fadeOut(0.15f)
        ))
    }

    fun decrementPoints(run: Boolean = false) {
        lives--
        if (lives == 0) {
            // TODO display dialog, go to menu
        } else if (lives > 0) {
            val prompt: Prompt = if (run) {
                randomRunPrompt()
            } else {
                randomMissmatchPrompt()
            }
            displayPrompt(prompt)
        }
    }
}

val positiveColor = Color(0.6f, 1f, 0.6f, 1f)
val negativeColor = Color(1f, 0.6f, 0.6f, 1f)