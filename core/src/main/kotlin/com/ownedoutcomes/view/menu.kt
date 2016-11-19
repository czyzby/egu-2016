package com.ownedoutcomes.view

import com.badlogic.gdx.graphics.Texture
import com.badlogic.gdx.graphics.g2d.TextureRegion
import com.badlogic.gdx.scenes.scene2d.Stage
import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable
import com.badlogic.gdx.utils.Align
import com.ownedoutcomes.Main
import ktx.actors.onChange
import ktx.assets.getValue
import ktx.assets.loadOnDemand
import ktx.inject.inject
import ktx.vis.table

class Menu(stage: Stage) : AbstractView(stage) {
    val backgroundImage by loadOnDemand<Texture>(path = "menu.png")
    override val root = table {
        setFillParent(true)
        backgroundImage.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear)
        background = TextureRegionDrawable(TextureRegion(backgroundImage, 0, 0, 1000, 750))

        button(styleName = "start") {
            onChange { event, button ->
                inject<Main>().setCurrentView(inject<Game>())
            }
        }.expand().align(Align.bottomRight).padBottom(80f).padRight(40f)
    }
}