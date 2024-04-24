package ru.hollowhorizon.resourcestages.mixins;

import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.AbstractTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import org.apache.logging.log4j.LogManager;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.hollowhorizon.resourcestages.ITextureManager;

import java.util.Collection;
import java.util.Map;

@Mixin(TextureManager.class)
public class TextureManagerMixin implements ITextureManager {
    @Shadow(aliases = "f_118468_")
    @Final
    private Map<ResourceLocation, AbstractTexture> byPath;

    @Override
    public void resourceStages$resetPaths(@NotNull Collection<? extends ResourceLocation> resources) {
        LogManager.getLogger().info("Resetting");
        byPath.keySet().removeAll(resources);
        LogManager.getLogger().info("Resetted");
    }
}
