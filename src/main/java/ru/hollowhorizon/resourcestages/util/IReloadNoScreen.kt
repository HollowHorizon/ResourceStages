package ru.hollowhorizon.resourcestages.util

import java.util.concurrent.CompletableFuture

interface IReloadNoScreen {
    fun reloadNoScreen(error: Boolean): CompletableFuture<Void>
}
