package com.ownedoutcomes.view

import com.badlogic.gdx.scenes.scene2d.Stage
import com.ownedoutcomes.Main
import ktx.actors.onChange
import ktx.inject.inject
import ktx.vis.table

class Menu(stage: Stage) : AbstractView(stage) {
    override val root = table {
        setFillParent(true)
        textButton(text = "Play!") {
            onChange { event, button ->
                inject<Main>().setCurrentView(inject<Game>())
            }
        }
    }
}