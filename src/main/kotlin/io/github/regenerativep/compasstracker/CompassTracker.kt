package io.github.regenerativep.compasstracker

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.event.Listener
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.entity.Player
import org.bukkit.World
import org.bukkit.Location
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import org.bukkit.event.block.Action
import org.bukkit.NamespacedKey
import org.bukkit.persistence.PersistentDataType
import org.bukkit.persistence.PersistentDataContainer
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bukkit.inventory.meta.CompassMeta
import org.bukkit.plugin.Plugin

import io.github.regenerativep.commandmanager.toEnvironmentOrNull

data class CompassListener(val name: String, var targetName: String?)
data class TargetListener(val name: String, var locationsMap: MutableMap<World.Environment, Location> = mutableMapOf())
val COMPASS_TAG_KEY = "TrackerDevice"
const val UNKNOWN_TARGET = "unknown"
const val COMMAND_NAME = "ctr"

class CompassTrackerTask(val app: CompassTracker) : BukkitRunnable() {
    override fun run() {
        app.listeners.values.forEach { listener ->
            app.server.getPlayerExact(listener.name)?.let { player ->
                app.updatePlayer(player, listener)
            }
        }
    }
}
class CompassTracker() : JavaPlugin(), Listener {
    var permittedEnvironments: List<World.Environment> = listOf(World.Environment.NORMAL)
    var targets: MutableMap<String, TargetListener> = mutableMapOf<String, TargetListener>()
    var listeners: MutableMap<String, CompassListener> = mutableMapOf<String, CompassListener>()
    var updateTask: BukkitTask? = null
    var autoGiveCompass = true
    var autoTarget = false
    var allowTargetSelf = false
    
    override fun onEnable() {
        this.runUpdateTimer(60)
        this.getCommand(COMMAND_NAME)?.setExecutor(CommandListener(this, COMMAND_NAME))
        this.server.pluginManager.registerEvents(this, this)
        if(!canUseLodestoneCompasses()) {
            this.logger.info("Warning: This server is detected to be a version before 1.16 . Tracking outside of the overworld will not work.")
        }
        val config = this.config;
        config.options().copyDefaults(true)
        this.saveConfig()
        this.autoTarget = config.getBoolean("autoTarget")
        this.allowTargetSelf = config.getBoolean("allowTargetSelf")
        this.autoGiveCompass = config.getBoolean("autoGiveCompasss")
        this.permittedEnvironments = config.getStringList("allowedEnvironments").let {
            List(it.size, { i -> it.get(i).toEnvironmentOrNull() ?: World.Environment.NORMAL })
        }
    }
    override fun onDisable() {
        this.updateTask?.cancel()
        val config = this.config
        config.set("autoTarget", this.autoTarget)
        config.set("allowTargetSelf", this.allowTargetSelf)
        config.set("autoGiveCompass", this.autoGiveCompass)
        config.set("allowedEnvironments", List(this.permittedEnvironments.size, { i -> this.permittedEnvironments.get(i).toString() } ))
        this.saveConfig()
    }
    fun runUpdateTimer(ticks: Long) {
        this.updateTask?.cancel()
        if(ticks > 0) {
            this.updateTask = CompassTrackerTask(this).runTaskTimer(this, 0, ticks)
        }
        else {
            CompassTrackerTask(this).run()
        }
    }
    fun updatePlayer(player: Player, listener: CompassListener) {
        this.targets.get(listener.targetName)?.let { targetListener ->
            player.location.world?.let { world ->
                if(world.environment in this.permittedEnvironments) {
                    targetListener.locationsMap.get(world.environment)?.let { targetLoc ->
                        player.updatePlayerCompassTarget(this, targetLoc)
                    }
                }
            }
        }
    }
    fun sendTrackingMessage(playerName: String) {
        this.server.getPlayerExact(playerName)?.let { player ->
            this.listeners.get(playerName)?.let { listener ->
                player.sendTrackingMessage(listener)
            }
        }
    }
    fun setTarget(playerName: String, targetName: String?) {
        if(playerName !in this.listeners.keys) {
            listeners.set(playerName, CompassListener(playerName, null))
        }
        listeners.get(playerName)?.let { listener ->
            listener.targetName = targetName
        }
        sendTrackingMessage(playerName)
    }
    fun setEnvironment(env: World.Environment, enabled: Boolean): Boolean {
        return this.permittedEnvironments.let { currentPerm ->
            this.permittedEnvironments = if(enabled) {
                currentPerm.allowEnvironment(env)
            }
            else {
                currentPerm.disallowEnvironment(env)
            }
            this.permittedEnvironments.size != currentPerm.size
        }
    }
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {
        this.targets.get(event.player.name)?.let { target ->
            this.server.getPlayerExact(target.name)?.location?.let { location ->
                location.world?.environment?.let { env ->
                    target.locationsMap.put(env, location)
                }
            }
        }
    }
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.name.let { playerName ->
            if(playerName !in this.listeners.keys) {
                this.listeners.set(playerName, CompassListener(playerName, null))
            }
            if(autoGiveCompass) {
                event.player.giveCompass(this)
            }
            if(autoTarget && playerName !in this.targets.keys) {
                this.targets.set(playerName, TargetListener(playerName))
            }
        }
    }
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if(autoGiveCompass) {
            event.drops.removeAll(event.drops.filter { it.isValidCompass(this) } )
        }
    }
    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        if(autoGiveCompass) {
            event.player.giveCompass(this)
        }
    }
    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        event.player.inventory.getItem(event.newSlot).let { item ->
            if(item != null && item.type == Material.COMPASS) {
                event.player.name.let { name ->
                    if(this.listeners.get(name) != null) {
                        this.sendTrackingMessage(name)
                    }
                }
            }
        }
    }
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        event.item?.let { item ->
            if(item.type == Material.COMPASS) {
                if(event.action.isRightClick()) {
                    this.setTarget(
                        event.player.name,
                        if(allowTargetSelf) {
                            getNextTarget(
                                this.targets,
                                this.listeners.get(event.player.name)?.targetName
                            )
                        }
                        else {
                            getNextTargetExcept(
                                this.targets,
                                this.listeners.get(event.player.name)?.targetName,
                                event.player.name
                            )
                        }
                    )
                }
                else if(event.action.isLeftClick()) {
                    this.listeners.get(event.player.name)?.let { listener ->
                        updatePlayer(event.player, listener)
                    }
                }
            }
        }
    }
}
fun getNextTarget(targets: MutableMap<String, TargetListener>, targetName: String?): String? {
    return getNextTarget(targets.keys.toList(), targetName)
}
fun getNextTarget(targetKeyList: List<String>, targetName: String?): String? {
    return if(targetKeyList.size == 0) {
        null
    }
    else {
        ( (targetKeyList.indexOf(targetName) + 1) % targetKeyList.size).let { nextInd ->
            targetKeyList.get(nextInd)
        }
    }
}
fun getNextTargetExcept(targets: MutableMap<String, TargetListener>, targetName: String?, exceptName: String): String? {
    return getNextTargetExcept(targets.keys.toList(), targetName, exceptName)
}
fun getNextTargetExcept(targetKeyList: List<String>, targetName: String?, exceptName: String): String? {
    return getNextTarget(targetKeyList, targetName)?.let { foundTarget ->
        if(foundTarget == exceptName) {
            getNextTarget(targetKeyList, foundTarget)?.let { secondFoundTarget ->
                if(secondFoundTarget == exceptName) {
                    null
                }
                else {
                    secondFoundTarget
                }
            } ?: null //kotlin might say not to use elvis here, but im using it for the scenario that getNextTarget returns null, and then the let function will not be called
        }
        else {
            foundTarget
        }
    } ?: null
}
fun World.Environment.getDimensionCode() = when(this) {
    World.Environment.NORMAL -> "minecraft:overworld"
    World.Environment.NETHER -> "minecraft:the_nether"
    World.Environment.THE_END -> "minecraft:the_end"
}
fun createTrackingCompass(plugin: Plugin): ItemStack {
    var compass = ItemStack(Material.COMPASS)
    var meta = compass.itemMeta
    meta!!.persistentDataContainer.set(NamespacedKey(plugin, COMPASS_TAG_KEY), PersistentDataType.BYTE, 1)
    compass.setItemMeta(meta)
    return compass
}
fun createLodestoneCompass(plugin: Plugin, loc: Location?): ItemStack {
    var compass = createTrackingCompass(plugin)
    var meta = compass.itemMeta as CompassMeta
    if(loc == null) {
        meta.setLodestoneTracked(true)
    }
    else {
        meta.setLodestoneTracked(false)
        meta.setLodestone(loc)
    }
    compass.setItemMeta(meta)
    return compass
}
fun ItemStack.isValidCompass(plugin: Plugin): Boolean {
    return this.type == Material.COMPASS && this.itemMeta?.persistentDataContainer?.let { container -> NamespacedKey(plugin, COMPASS_TAG_KEY).let { key ->
        container.has(key, PersistentDataType.BYTE)
            && container.get(key, PersistentDataType.BYTE) ?: 0 == 1.toByte()
    }} ?: false
}
fun Player.getPlayerCompass(plugin: Plugin): ItemStack? {
    return if(this.inventory.itemInOffHand.isValidCompass(plugin)) {
        return this.inventory.itemInOffHand
    }
    else {
        this.inventory.find {
            it?.isValidCompass(plugin) ?: false
        }
    }
}
fun Player.updatePlayerCompassTarget(plugin: Plugin, loc: Location?) {
    this.getPlayerCompass(plugin)?.let { compass ->
        (if(loc?.world?.environment == World.Environment.NORMAL) {
            this.setCompassTarget(loc)
            createTrackingCompass(plugin)
        }
        else {
            if(canUseLodestoneCompasses()) {
                createLodestoneCompass(plugin, loc)
            }
            else {
                null
            }
        }).let { newCompass ->
            if(newCompass != null) {
                this.inventory.first(compass).let { invPos ->
                    if(invPos < 0) { // *should* be in off hand
                        this.inventory.setItemInOffHand(newCompass)
                    }
                    else {
                        this.inventory.setItem(invPos, newCompass)
                    }
                }
            }
        }
    }// ?: this.sendMessage("oops! no player compass!")
}
fun Player.giveCompass(plugin: Plugin): Boolean {
    //give player compass if player does not have compass
    return (this.getPlayerCompass(plugin) == null).let { foundCompass ->
        if(foundCompass) {
            this.inventory.addItem(
                if(this.location.world?.environment == World.Environment.NORMAL || !canUseLodestoneCompasses()) {
                    createTrackingCompass(plugin)
                }
                else {
                    createLodestoneCompass(plugin, null)
                }
            )
        }
        foundCompass
    }
}
fun Player.sendTrackingMessage(listener: CompassListener?) {
    val targetMessage = listener?.targetName ?: UNKNOWN_TARGET
    var message = TextComponent("Tracking \"")
    message.setColor(ChatColor.YELLOW)
    var infoMessage = TextComponent(targetMessage)
    infoMessage.setColor(ChatColor.WHITE)
    var endMessage = TextComponent("\"")
    endMessage.setColor(ChatColor.YELLOW)
    message.addExtra(infoMessage)
    message.addExtra(endMessage)
    this.spigot().sendMessage(ChatMessageType.ACTION_BAR, message)
}
fun List<World.Environment>.allowEnvironment(env: World.Environment): List<World.Environment> {
    return if(env !in this) {
        this + env
    }
    else {
        this
    }
}
fun List<World.Environment>.disallowEnvironment(env: World.Environment): List<World.Environment> {
    return if(env in this) {
        this - env
    }
    else {
        this
    }
}
fun Action.isRightClick(): Boolean {
    return this == Action.RIGHT_CLICK_AIR || this == Action.RIGHT_CLICK_BLOCK
}
fun Action.isLeftClick(): Boolean {
    return this == Action.LEFT_CLICK_AIR || this == Action.LEFT_CLICK_BLOCK
}
fun getVersion(): String {
    return org.bukkit.Bukkit.getVersion().split(": ").get(1).let { it.substring(0, it.length - 1) }
}
fun canUseLodestoneCompasses(): Boolean {
    return getVersion().let { version -> version.substring(0, 2) == "1." && version.substring(2, 4).toInt() >= 16 }
}