package com.ownedoutcomes.view

import box2dLight.ConeLight
import box2dLight.PointLight
import box2dLight.RayHandler
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.Color
import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.Batch
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.math.MathUtils
import com.badlogic.gdx.physics.box2d.World
import com.badlogic.gdx.scenes.scene2d.Actor
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.ui.Image
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.kotcrab.vis.ui.widget.Draggable
import com.kotcrab.vis.ui.widget.VisImage
import com.ownedoutcomes.view.entity.Pedestrian
import com.ownedoutcomes.view.entity.PedestrianType
import com.ownedoutcomes.view.entity.getRandomPedestrian
import ktx.assets.getValue
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
    override val root: Actor
    lateinit var urineImage: Image
    lateinit var beerImage: Image
    lateinit var vodkaImage: Image
    lateinit var smokeImage: Image

    init {
        rayHandler.setAmbientLight(0f, 0f, 0f, 0.1f)
        light.isXray = true
        ConeLight(rayHandler, 15, Color.BLACK, 600f, 600f, 400f, 45f, 45f)  // Bonuses.
        ConeLight(rayHandler, 15, Color.BLACK, 250f, 300f, 540f, -45f, 45f) // Room light.
        root = table {
            backgroundImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
            background = TextureRegionDrawable(TextureRegion(backgroundImage, 0, 0, 1000, 750))
            setFillParent(true)
            table {
                urineImage = image(drawableName = "urine").actor
                image(drawableName = "beer") {
                    beerImage = this
                }.row()
                vodkaImage = image(drawableName = "vodka").actor
                smokeImage = image(drawableName = "smoke").actor
            }.expand().align(Align.topRight)
        }
        handImage.setPosition(312f, 387f)
        handImage.setOrigin(10f, 70f)
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
        super.show()
        stage.addActor(handImage)
        spawner.reset()
        dragController.reset()
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
    var lives = 2
    var points = 0

    fun reset() {
        lives = 2
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
            incrementPoints()
        } else {
            decrementPoints()
        }
    }

    fun incrementPoints() {
        lives++
        points++
        println("Points++")
        // TODO display image!
    }

    fun decrementPoints() {
        lives--
        println("Points--")
        if (lives == 0) {
            // TODO display dialog, go to menu
        } else if (lives > 0) {
            // TODO display image
        }
    }
}