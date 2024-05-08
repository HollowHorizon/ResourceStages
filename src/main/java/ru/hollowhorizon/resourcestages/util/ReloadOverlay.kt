package ru.hollowhorizon.resourcestages.util

import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.resources.ReloadInstance

object ReloadOverlay {
    var reload: ReloadInstance? = null
    var currentProgress = 0f

    fun render(graphics: GuiGraphics, width: Int, height: Int) {
        val reload = reload ?: return

        if (reload.isDone) {
            this.reload = null
            currentProgress = 0f
            return
        }

        val actual = (reload.actualProgress - 0.7f) / 0.3f

        if (currentProgress < actual) {
            currentProgress += (actual - currentProgress) / 20
        }

        graphics.fill(0, 0, (width * currentProgress).toInt(), 20, 0x44FF2222)

        graphics.fill(0, 0, width, 1, 0xFFFFFFFF.toInt())
        graphics.fill(0, 19, width, 20, 0xFFFFFFFF.toInt())
        graphics.fill(0, 0, 1, 20, 0xFFFFFFFF.toInt())
        graphics.fill(width - 1, 0, width, 20, 0xFFFFFFFF.toInt())


        val text = Component.translatable("resourcestages.reloading")
            .append(": ${(currentProgress * 100).toInt()}%")
        val font = Minecraft.getInstance().font
        graphics.drawString(font, text, width / 2 - font.width(text) / 2, 6, 0xFFFFFF)
    }
}