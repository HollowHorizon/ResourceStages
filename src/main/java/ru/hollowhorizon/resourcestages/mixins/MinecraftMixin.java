/*
 * All Rights Received
 * Copyright (c) 2024 0mods.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

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
    @Shadow
    @Nullable
    private CompletableFuture<Void> pendingReload;

    @Shadow
    @Nullable
    private Overlay overlay;

    @Shadow
    @Final
    private PackRepository resourcePackRepository;

    @Shadow
    @Final
    private ResourceLoadStateTracker reloadStateTracker;

    @Shadow
    @Final
    private ReloadableResourceManager resourceManager;

    @Shadow
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
