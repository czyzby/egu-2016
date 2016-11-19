package com.ownedoutcomes

import com.badlogic.gdx.ApplicationAdapter
import com.badlogic.gdx.Gdx
import com.badlogic.gdx.graphics.g2d.BitmapFont
import com.badlogic.gdx.graphics.g2d.SpriteBatch
import com.badlogic.gdx.graphics.g2d.TextureAtlas
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.actions.Actions
import com.badlogic.gdx.scenes.scene2d.ui.Skin
import com.badlogic.gdx.utils.viewport.FitViewport
import com.badlogic.gdx.utils.viewport.Viewport
import com.kotcrab.vis.ui.VisUI
import com.ownedoutcomes.music.loadMusic
import com.ownedoutcomes.music.playMusic
import com.ownedoutcomes.view.Game
import com.ownedoutcomes.view.Menu
import com.ownedoutcomes.view.MockView
import com.ownedoutcomes.view.View
import com.ownedoutcomes.view.entity.loadSounds
import ktx.app.KotlinApplication
import ktx.app.LetterboxingViewport
import ktx.assets.Assets
import ktx.assets.loadOnDemand
import ktx.assets.toInternalFile
import ktx.inject.inject
import ktx.inject.register
import ktx.style.button
import ktx.style.label

class Main : KotlinApplication() {
    private var view: View = MockView()
    override fun create() {
        val viewport: Viewport = FitViewport(800f, 600f)
        val batch = SpriteBatch()
        val stage = Stage(viewport, batch)
        VisUI.load(VisUI.SkinScale.X2)
        loadSounds()
        loadMusic()
        val skin = createSkin()

        val menuView = Menu(stage)
        val gameView = Game(stage, skin, batch)
        val main = this
        register {
            bindSingleton(main)
            bindSingleton(skin)
            bindSingleton(batch)
            bindSingleton(stage)
            bindSingleton(menuView)
            bindSingleton(gameView)
        }
        Gdx.input.inputProcessor = stage
        stage.addAction(Actions.sequence(Actions.alpha(0f), Actions.fadeIn(0.5f)))
        Assets.manager.finishLoading()
        Gdx.input.inputProcessor = stage
        view = menuView
        playMusic()
        view.show()
    }

    private fun createSkin(): Skin {
        val skin = VisUI.getSkin()
        skin.addRegions(loadOnDemand<TextureAtlas>(path = "ui/skin.atlas").asset)
        skin.label("title") {
            font = BitmapFont("title.fnt".toInternalFile(), skin.getRegion("title"))
        }
        skin.button("start") {
            up = skin.getDrawable("start-up")
            unpressedOffsetX = -1f
            unpressedOffsetY = -1f
            pressedOffsetX = 1f
            pressedOffsetY = 1f
            over = skin.getDrawable("start-over")
            down = skin.getDrawable("start-down")
        }
        return skin
    }

    override fun resize(width: Int, height: Int) {
        view.resize(width, height)
    }

    override fun render(delta: Float) {
        view.render(delta)
    }

    fun setCurrentView(nextView: View) {
        inject<Stage>().addAction(Actions.sequence(
                Actions.fadeOut(0.5f),
                Actions.run {
                    view.hide()
                    view = nextView
                    nextView.show()
                    nextView.resize(Gdx.graphics.width, Gdx.graphics.height)
                },
                Actions.fadeIn(0.5f),
                Actions.alpha(1f)
        ))
    }
}