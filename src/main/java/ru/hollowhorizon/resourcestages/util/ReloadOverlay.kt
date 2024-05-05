package ru.hollowhorizon.resourcestages.util

import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.screens.Screen
import net.minecraft.network.chat.Component
import net.minecraft.server.packs.resources.ReloadInstance

object ReloadOverlay {
    var reload: ReloadInstance? = null
    var currentProgress = 0f

    fun render(stack: PoseStack, width: Int, height: Int) {
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

        Screen.fill(stack, 0, 0, (width * currentProgress).toInt(), 20, 0x44FF2222)

        Screen.fill(stack, 0, 0, width, 1, 0xFFFFFFFF.toInt())
        Screen.fill(stack, 0, 19, width, 20, 0xFFFFFFFF.toInt())
        Screen.fill(stack, 0, 0, 1, 20, 0xFFFFFFFF.toInt())
        Screen.fill(stack, width - 1, 0, width, 20, 0xFFFFFFFF.toInt())


        val text = Component.translatable("resourcestages.reloading")
            .append(": ${(currentProgress * 100).toInt()}%")
        val font = Minecraft.getInstance().font
        font.drawShadow(stack, text, width / 2f - font.width(text) / 2f, 6f, 0xFFFFFF)
    }
}