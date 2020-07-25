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
data class TargetListener(val name: String, var locationsMap: MutableMap<World.Environment, Location>)
val COMPASS_TAG_KEY = "TrackerDevice"

class CompassTrackerTask(val app: CompassTracker) : BukkitRunnable() {
    override fun run()
    {
        for(listener in app.listeners.values)
        {
            val player = app.server.getPlayerExact(listener.name)
            if(player == null) {
                continue
            }
            app.updatePlayer(player, listener)
        }
    }
}
fun getPlayerCompass(player: Player): ItemStack?
{
    val inv = player.inventory
    var possibleCompass: ItemStack? = null
    val checkItem: (ItemStack?) -> ItemStack? = {
        item: ItemStack? ->
        if(item != null && item.type == Material.COMPASS)
        {
            if(possibleCompass == null) {
                possibleCompass = item
            }
            //check to see if it has our special tag on it
            val nbti = NBTItem(item)
            if(nbti.hasKey(COMPASS_TAG_KEY))
            {
                val status = nbti.getByte(COMPASS_TAG_KEY) ?: 0
                if(status.toInt() == 1)
                {
                    item
                }
            }
        }
        null
    }

    val foundItem = checkItem(inv.itemInOffHand)
    if(foundItem != null)
    {
        return foundItem
    }
    for(item in inv)
    {
        val foundItem = checkItem(item)
        if(foundItem != null)
        {
            return foundItem
        }
    }
    return possibleCompass
}
fun getDimension(env: World.Environment?) = when(env) {
    World.Environment.NORMAL -> "minecraft:overworld"
    World.Environment.NETHER -> "minecraft:the_nether"
    World.Environment.THE_END -> "minecraft:the_end"
    else -> ""
}
fun setPlayerCompassTarget(player: Player, loc: Location?)
{
    val compass = getPlayerCompass(player)
    if(compass == null) { return }
    val nbti = NBTItem(ItemStack(Material.COMPASS))
    nbti.setByte(COMPASS_TAG_KEY, 1)
    if(loc == null)
    {
        nbti.setByte("LodestoneTracked", 1)
    }
    else
    {
        nbti.setByte("LodestoneTracked", 0)
        val posComp = nbti.addCompound("LodestonePos")
        posComp.setInteger("X", loc.blockX)
        posComp.setInteger("Y", loc.blockY)
        posComp.setInteger("Z", loc.blockZ)
        val dimension = getDimension(loc.world?.environment)
        nbti.setString("LodestoneDimension", dimension)
    }
    val finalItem = nbti.item
    val invPos = player.inventory.first(compass)
    if(invPos < 0) //if cannot find in inventory it *should* be in the off hand
    {
        player.inventory.setItemInOffHand(finalItem)
    }
    else
    {
        player.inventory.setItem(invPos, finalItem)
    }
}
class CompassTracker() : JavaPlugin(), Listener
{
    var permittedEnvironments: MutableList<World.Environment> = mutableListOf(World.Environment.NORMAL)
    var targets: MutableMap<String, TargetListener> = mutableMapOf<String, TargetListener>()
    var listeners: MutableMap<String, CompassListener> = mutableMapOf<String, CompassListener>()
    var updateTask: BukkitTask? = null
    var autoGiveCompass = true
    
    override fun onEnable()
    {
        runUpdateTimer(60)
        getCommand("ctr")?.setExecutor(CommandListener(this))
        server.pluginManager.registerEvents(this, this)
    }
    override fun onDisable()
    {
        updateTask?.cancel()
    }
    fun runUpdateTimer(ticks: Long)
    {
        updateTask?.cancel()
        if(ticks > 0)
        {
            updateTask = CompassTrackerTask(this).runTaskTimer(this, 0, ticks)
        }
        else
        {
            CompassTrackerTask(this).run()
        }
    }
    fun updatePlayer(player: Player, listener: CompassListener)
    {
        val targetListener = targets.get(listener.targetName)
        if(targetListener == null) {
            return
        }
        val playerEnv = player.location.world?.environment
        if(playerEnv !in permittedEnvironments) {
            return
        }
        //ensure player environment is in the same as a possible location on the target
        val targetLoc = targetListener.locationsMap.get(playerEnv)
        setPlayerCompassTarget(player, targetLoc)
    }
    fun listenPlayer(playerName: String): CompassListener
    {
        val listener = CompassListener(playerName, null)
        listeners.set(playerName, listener)
        return listener
    }
    fun giveCompass(playerName: String): Boolean
    {
        val player = server.getPlayerExact(playerName)
        if(player != null)
        {
            return giveCompass(player)
        }
        return false
    }
    fun giveCompass(player: Player): Boolean
    {
        //give player compass if player does not have compass
        if(!player.inventory.contains(Material.COMPASS))
        {
            player.inventory.addItem(ItemStack(Material.COMPASS))
            return true
        }
        return false
    }
    fun sendTrackingMessage(playerName: String)
    {
        val player = server.getPlayerExact(playerName)
        if(player == null) return
        val listener = listeners.get(playerName)
        val targetMessage = listener?.targetName ?: "unknown"
        var message = TextComponent("Tracking \"")
        message.setColor(ChatColor.YELLOW)
        var infoMessage = TextComponent(targetMessage)
        infoMessage.setColor(ChatColor.WHITE)
        var endMessage = TextComponent("\"")
        endMessage.setColor(ChatColor.YELLOW)
        message.addExtra(infoMessage)
        message.addExtra(endMessage)
        player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message)
    }
    fun getNextTarget(targetName: String?): String?
    {
        var foundTarget = false
        var nextTargetName: String? = null
        var firstTargetName: String? = null
        for(targetListener in targets.values)
        {
            if(firstTargetName == null)
            {
                firstTargetName = targetListener.name
            }
            if(targetListener.name == targetName)
            {
                foundTarget = true
                continue
            }
            if(foundTarget)
            {
                nextTargetName = targetListener.name
                break
            }
        }
        if(nextTargetName == null)
        {
            nextTargetName = firstTargetName
        }
        return nextTargetName
    }
    fun setTarget(playerName: String, targetName: String?)
    {
        var listener = listeners.get(playerName)
        if(listener == null)
        {
            listener = listenPlayer(playerName)
        }
        listener.targetName = targetName
        sendTrackingMessage(playerName)
    }
    fun addTarget(targetName: String)
    {
        targets.set(targetName, TargetListener(targetName, mutableMapOf()))
    }
    fun removeTarget(targetName: String): Boolean
    {
        return targets.remove(targetName) != null
    }
    fun setEnvironment(env: World.Environment, enabled: Boolean)
    {
        if(enabled)
        {
            permittedEnvironments.add(env)
        }
        else
        {
            permittedEnvironments.remove(env)
        }
    }
    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent)
    {
        val target = targets.get(event.player.name)
        if(target != null)
        {
            val player = this.server.getPlayerExact(target.name)
            val location = player?.location
            val env = location?.world?.environment
            if(env != null)
            {
                target.locationsMap.put(env, location)
            }
        }
    }
    @EventHandler
    fun onPlayerJoin(event: PlayerJoinEvent)
    {
        val playerName = event.player.name
        if(playerName !in listeners.keys)
        {
            listenPlayer(playerName)
            if(autoGiveCompass)
            {
                giveCompass(event.player)
            }
        }
    }
    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent)
    {
        if(autoGiveCompass)
        {
            val drops = event.drops
            //remove compass on death
            for(i in 0..(drops.size - 1))
            {
                val drop = drops.get(i)
                if(drop.type == Material.COMPASS)
                {
                    drops.removeAt(i)
                    break
                }
            }
        }
    }
    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent)
    {
        if(autoGiveCompass)
        {
            giveCompass(event.player)
        }
    }
    @EventHandler
    fun onPlayerItemHeld(event: PlayerItemHeldEvent)
    {
        val player = event.player
        val item = player.inventory.getItem(event.newSlot)
        if(item != null && item.type == Material.COMPASS)
        {
            val name = player.name
            val listener = listeners.get(name)
            if(listener != null)
            {
                sendTrackingMessage(name)
            }
        }
    }
    @EventHandler
    fun onPlayerInteract(event: PlayerInteractEvent)
    {
        if(event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK) {
            val item = event.item
            if(item != null && item.type == Material.COMPASS)
            {
                val listener = listeners.get(event.player.name)
                setTarget(event.player.name, getNextTarget(listener?.targetName))
            }
        }
        else if(event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
            val item = event.item
            if(item != null && item.type == Material.COMPASS)
            {
                val listener = listeners.get(event.player.name)
                if(listener != null) {
                    updatePlayer(event.player, listener)
                }
            }
        }
    }
}