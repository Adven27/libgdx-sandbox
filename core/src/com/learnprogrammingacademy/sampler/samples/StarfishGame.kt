package com.learnprogrammingacademy.sampler.samples

import com.learnprogrammingacademy.sampler.common.BaseGame
import com.learnprogrammingacademy.sampler.common.MenuScreen

object StarfishGame : BaseGame() {
    override fun create() {
        super.create()
        screen = MenuScreen()
    }
}