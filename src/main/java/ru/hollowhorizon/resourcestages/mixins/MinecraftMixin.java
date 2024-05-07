package ru.hollowhorizon.resourcestages.mixins;

import net.minecraft.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.ResourceLoadStateTracker;
import net.minecraft.client.gui.screens.LoadingOverlay;
import net.minecraft.client.gui.screens.Overlay;
import net.minecraft.server.packs.repository.PackRepository;
import net.minecraft.server.packs.resources.ReloadableResourceManager;
import net.minecraft.util.Unit;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import ru.hollowhorizon.resourcestages.util.IReloadNoScreen;
import ru.hollowhorizon.resourcestages.util.ReloadOverlay;

import java.util.concurrent.CompletableFuture;

@Mixin(Minecraft.class)
public abstract class MinecraftMixin implements IReloadNoScreen {
    @Shadow(aliases = "f_91024_")
    @Nullable
    private CompletableFuture<Void> pendingReload;

    @Shadow(aliases = "f_91081_")
    @Nullable
    private Overlay overlay;

    @Shadow(aliases = "f_91038_")
    @Final
    private PackRepository resourcePackRepository;

    @Shadow(aliases = "f_167847_")
    @Final
    private ResourceLoadStateTracker reloadStateTracker;

    @Shadow(aliases = "f_91036_")
    @Final
    private ReloadableResourceManager resourceManager;

    @Shadow(aliases = "f_90983_")
    @Final
    private static CompletableFuture<Unit> RESOURCE_RELOAD_INITIAL_TASK;

    @NotNull
    @Override
    public CompletableFuture<Void> reloadNoScreen(boolean error) {
        if (pendingReload != null) return pendingReload;
        else {
            var compFuture = new CompletableFuture<Void>();

            if (!error && this.overlay instanceof LoadingOverlay) {
                this.pendingReload = compFuture;
                return compFuture;
            } else {
                compFuture.completeAsync(() -> {
                    resourcePackRepository.reload();
                    var presoruces = resourcePackRepository.openAllSelected();

                    if (!error) {
                        reloadStateTracker.startReload(ResourceLoadStateTracker.ReloadReason.MANUAL, presoruces);
                    }

                    var reload = resourceManager.createReload(
                            Util.backgroundExecutor(),
                            (Minecraft) (Object) this,
                            RESOURCE_RELOAD_INITIAL_TASK,
                            presoruces
                    );
                    ReloadOverlay.INSTANCE.setReload(reload);
                    return null;
                });

            }

            return compFuture;
        }
    }
}
