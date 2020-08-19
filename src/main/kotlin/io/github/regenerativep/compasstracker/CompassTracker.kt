package io.github.regenerativep.compasstracker;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.event.Listener;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.entity.Player;
import org.bukkit.World;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.event.block.Action;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.persistence.PersistentDataContainer;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import de.tr7zw.changeme.nbtapi.NBTItem;

data class CompassListener(val name: String, var targetName: String?)
data class TargetListener(val name: String, var locationsMap: MutableMap<World.Environment, Location> = mutableMapOf())
val COMPASS_TAG_KEY = "TrackerDevice"
const val UNKNOWN_TARGET = "unknown"
const val COMMAND_NAME = "ctr"

class CompassTrackerTask(val app: CompassTracker) : BukkitRunnable() {
    override fun run() {
        app.listeners.values.forEach { listener
            -> app.server.getPlayerExact(listener.name)?.let { player
                -> app.updatePlayer(player, listener)
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
    
    override fun onEnable() {
        runUpdateTimer(60)
        getCommand(COMMAND_NAME)?.setExecutor(CommandListener(this))
        server.pluginManager.registerEvents(this, this)
    }
    override fun onDisable() {
        updateTask?.cancel()
    }
    fun runUpdateTimer(ticks: Long) {
        updateTask?.cancel()
        if(ticks > 0) {
            updateTask = CompassTrackerTask(this).runTaskTimer(this, 0, ticks)
        }
        else {
            CompassTrackerTask(this).run()
        }
    }
    fun updatePlayer(player: Player, listener: CompassListener) {
        this.targets.get(listener.targetName)?.let { targetListener
            -> player.location.world?.let { world
                -> if(world.environment in this.permittedEnvironments) {
                    targetListener.locationsMap.get(world.environment)?.let { targetLoc
                        -> player.updatePlayerCompassTarget(targetLoc)
                    } ?: this.server.logger.info("oops! no location!")
                }
                else {
                    this.server.logger.info("oops! illegal environment!")
                }
            } ?: this.server.logger.info("oops! no world!")
        } ?: this.server.logger.info("oops! no target!")
    }
    fun sendTrackingMessage(playerName: String) {
        this.server.getPlayerExact(playerName)?.let { player
            -> this.listeners.get(playerName)?.let { listener
                -> player.sendTrackingMessage(listener)
            }
        }
    }
    fun getNextTarget(targetName: String?): String? {
        return this.targets.keys.toList().let { keyList
            -> if(keyList.size == 0) {
                null
            }
            else {
                ( (keyList.indexOf(targetName) + 1) % keyList.size).let { nextInd
                    -> keyList.get(nextInd)
                }
            }
        }
    }
    fun setTarget(playerName: String, targetName: String?) {
        if(playerName !in this.listeners.keys) {
            listeners.set(playerName, CompassListener(playerName, null))
        }
        listeners.get(playerName)?.let { listener
            -> listener.targetName = targetName
        }
        sendTrackingMessage(playerName)
    }
    fun setEnvironment(env: World.Environment, enabled: Boolean): Boolean {
        return this.permittedEnvironments.let { currentPerm
            -> this.permittedEnvironments = if(enabled) {
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
        this.targets.get(event.player.name)?.let { target
            -> this.server.getPlayerExact(target.name)?.location?.let { location
                -> location.world?.environment?.let { env
                    -> target.locationsMap.put(env, location)
                }
            }
        }
    }
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent) {
        event.player.name.let { playerName
            -> if(playerName !in this.listeners.keys) {
                this.listeners.set(playerName, CompassListener(playerName, null))
            }
            if(autoGiveCompass) {
                event.player.giveCompass()
            }
            if(autoTarget && playerName !in this.targets.keys) {
                this.targets.set(playerName, TargetListener(playerName))
            }
        }
    }
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        if(autoGiveCompass) {
            event.drops.removeAll(event.drops.filter { it.isValidCompass() } )
        }
    }
    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        if(autoGiveCompass) {
            event.player.giveCompass()
        }
    }
    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent) {
        event.player.inventory.getItem(event.newSlot).let { item
            -> if(item != null && item.type == Material.COMPASS) {
                event.player.name.let { name
                    -> if(this.listeners.get(name) != null) {
                        this.sendTrackingMessage(name)
                    }
                }
            }
        }
    }
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent) {
        event.item?.let { item
            -> if(item.type == Material.COMPASS) {
                if(event.action.isRightClick()) {
                    this.setTarget(
                        event.player.name,
                        getNextTarget(
                            this.listeners.get(event.player.name)?.targetName
                        )
                    )
                }
                else if(event.action.isLeftClick()) {
                    this.listeners.get(event.player.name)?.let { listener
                        -> updatePlayer(event.player, listener)
                    }
                }
            }
        }
    }
}
fun World.Environment.getDimensionCode() = when(this) {
    World.Environment.NORMAL -> "minecraft:overworld"
    World.Environment.NETHER -> "minecraft:the_nether"
    World.Environment.THE_END -> "minecraft:the_end"
}
fun createCompass(loc: Location?): ItemStack {
    return NBTItem(ItemStack(Material.COMPASS)).let { nbti
        -> nbti.setByte(COMPASS_TAG_KEY, 1)
        if(loc == null) {
            nbti.setByte("LodestoneTracked", 1)
        }
        else {
            nbti.setByte("LodestoneTracked", 0)
            nbti.addCompound("LodestonePos").let { posComp
                -> posComp.setInteger("X", loc.blockX)
                posComp.setInteger("Y", loc.blockY)
                posComp.setInteger("Z", loc.blockZ)
            }
            nbti.setString(
                "LodestoneDimension",
                loc.world?.let { world -> world.environment.getDimensionCode() } ?: ""
            )
        }
        nbti.item
    }
}
fun ItemStack.isValidCompass(): Boolean {
    return if(this.type == Material.COMPASS) {
        NBTItem(this).let { nbti
            -> if(!nbti.hasKey(COMPASS_TAG_KEY)) {
                false
            }
            else {
                (nbti.getByte(COMPASS_TAG_KEY)?.toInt() ?: 0) == 1
            }
        }
    }
    else {
        false
    }
}
fun Player.getPlayerCompass(): ItemStack? {
    return if(this.inventory.itemInOffHand.isValidCompass()) {
        return this.inventory.itemInOffHand
    }
    else {
        this.inventory.find {
            it?.isValidCompass() ?: false
        }
    }
}
fun Player.updatePlayerCompassTarget(loc: Location?) {
    this.getPlayerCompass()?.let { compass
        -> createCompass(loc).let { newCompass
            -> this.inventory.first(compass).let { invPos
                -> if(invPos < 0) { //*should be in off hand
                    this.inventory.setItemInOffHand(newCompass)
                }
                else {
                    this.inventory.setItem(invPos, newCompass)
                }
            }
        }
    } ?: this.sendMessage("oops! no player compass!")
}
fun Player.giveCompass(): Boolean {
    //give player compass if player does not have compass
    return (this.getPlayerCompass() == null).let { foundCompass
        -> if(foundCompass) {
            this.inventory.addItem(createCompass(null))
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