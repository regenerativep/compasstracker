package io.github.regenerativep.compasstracker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * main app for the compass tracker plugin
 */
public class App extends JavaPlugin implements Listener
{
  /**
   * ingame command for the compass tracker
   */
  public final String COMPASS_TRACKER_COMMMAND = "ctr";
  /**
   * tick period between compass updates
   */
  public final long compassUpdateRate = 20;
  public HashMap<String, PlayerListener> playerMap;
  public List<String> playerTargetList;
  public boolean autoListenStatus;
  /**
   * compass update loop
   */
  private BukkitTask updateTask;
  /**
   * called when this compass tracker plugin is enabled
   */
  @Override
  public void onEnable()
  {
    autoListenStatus = false;
    playerMap = new HashMap<>();
    playerTargetList = new ArrayList<>();
    updateTask = new BukkitRunnable() {
      @Override
      public void run() {
        updateListeners();
      }
    }.runTaskTimer(this, 0, compassUpdateRate);
    getCommand(COMPASS_TRACKER_COMMMAND).setExecutor(new CommandListener(this));
    getServer().getPluginManager().registerEvents(this, this);
  }
  /**
   * called when this compass tracker plugin is disabled
   */
  @Override
  public void onDisable()
  {
    updateTask.cancel();
  }
  /**
   * updates all of the listener's compasses
   */
  public void updateListeners()
  {
    for(String key : playerMap.keySet())
    {
      PlayerListener player = playerMap.get(key);
      player.update();
    }
  }
  public ILocatable getNextTarget(String targetName)
  {
    if(playerTargetList.size() == 0) return null;
    int ind = playerTargetList.indexOf(targetName);
    if(ind < 0) return playerMap.get(playerTargetList.get(0));
    ind++;
    if(ind == playerTargetList.size()) ind = 0;
    return playerMap.get(playerTargetList.get(ind));
  }
  public boolean listen(Player player)
  {
    if(playerMap.containsKey(player.getName())) return false;
    PlayerListener listener = new PlayerListener(this, player);
    listener.giveCompass();
    playerMap.put(player.getName(), listener);
    return true;
  }
  public boolean unlisten(String playerName)
  {
    PlayerListener listener = playerMap.remove(playerName);
    if(listener == null) return false;
    //remove targets from any listeners
    for(String listenerName : playerMap.keySet())
    {
      PlayerListener possibleListener = playerMap.get(listenerName);
      if(possibleListener.getTarget() == listener)
      {
        possibleListener.setTarget(null);
      }
    }
    listener.removeCompass();
    listener.resetCompassTarget();
    return true;
  }
  public boolean targetExists(String targetName)
  {
    return playerTargetList.contains(targetName);
  }
  public boolean setListenerTarget(String listenerName, String targetName)
  {
    PlayerListener listener = playerMap.get(listenerName);
    if(listener == null) return false;
    listener.setTarget(playerMap.get(targetName));
    return true;
  }
  public boolean setListenerTarget(String listenerName, Location loc)
  {
    PlayerListener listener = playerMap.get(listenerName);
    if(listener == null) return false;
    listener.setTarget(new TargetLocation(loc));
    return true;
  }
  public boolean addTarget(String targetName)
  {
    if(playerTargetList.contains(targetName)) return false;
    playerTargetList.add(targetName);
    return true;
  }
  public boolean removeTarget(String targetName)
  {
    return playerTargetList.remove(targetName);
  }
  public boolean autoListenEnabled()
  {
    return autoListenStatus;
  }
  public boolean setAutoListen(boolean status)
  {
    if(status == autoListenStatus) return false;
    //add everyone not already a listener as a listener
    if(status)
    {
      for(Player player : getServer().getOnlinePlayers())
      {
        listen(player);
      }
    }
    autoListenStatus = status;
    return true;
  }
  @EventHandler
  public void onPlayerJoin(PlayerJoinEvent event)
  {
    if(autoListenStatus)
    {
      Player player = event.getPlayer();
      listen(player);
    }
  }
  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event)
  {
    String name = event.getEntity().getName();
    if(playerMap.containsKey(name))
    {
      playerMap.get(name).onPlayerDeath(event);
    }
  }
  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event)
  {
    String name = event.getPlayer().getName();
    if(playerMap.containsKey(name))
    {
      playerMap.get(name).onPlayerRespawn(event);
    }
  }
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event)
  {
    String name = event.getPlayer().getName();
    if(playerMap.containsKey(name))
    {
      playerMap.get(name).onPlayerQuit(event);
    }
  }
  @EventHandler
  public void onPlayerItemHeld(PlayerItemHeldEvent event)
  {
    String name = event.getPlayer().getName();
    if(playerMap.containsKey(name))
    {
      playerMap.get(name).onPlayerItemHeld(event);
    }
  }
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event)
  {
    String name = event.getPlayer().getName();
    if(playerMap.containsKey(name))
    {
      playerMap.get(name).onPlayerInteract(event);
    }
  }
  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event)
  {
    String name = event.getPlayer().getName();
    if(playerMap.containsKey(name))
    {
      playerMap.get(name).onPlayerMove(event);
    }
  }
  // /**
  //  * creates a target to a player
  //  * @param playerName the player to target
  //  * @return if successful
  //  */
  // public boolean createTarget(String playerName)
  // {
  //   //ensure target doesnt already exist
  //   if(targetExists(playerName)) return false;
  //   PlayerNameCompassTarget target = new PlayerNameCompassTarget(this, playerName);
  //   //ensure target player exists
  //   if(target.getPlayer() == null) return false;
  //   //register this target to receive events from the game
  //   getServer().getPluginManager().registerEvents(target, this);
  //   targets.add(target);
  //   return true;
  // }
  // /**
  //  * creates a target to a specific location
  //  * @param loc the location to target
  //  * @param listeningPlayer the player that wants to listen
  //  * @return if successful
  //  */
  // public boolean createLocationTarget(Location loc, Player listeningPlayer) //todo: only take in a location
  // {
  //   CompassTarget target = new CompassTarget(this);
  //   target.targetLocation = loc;
  //   PlayerListener playerListener = new PlayerListener(this, listeningPlayer);
  //   getServer().getPluginManager().registerEvents(playerListener, this);
  //   target.listeners.add(playerListener);
  //   targets.add(target);
  //   return true;
  // }
  // /**
  //  * adds a player as a listener to a player target
  //  * @param player the player to add as a listener
  //  * @param targetPlayerName the target player's name to add the listener to
  //  * @return if successful
  //  */
  // public boolean addPlayerListener(Player player, String targetPlayerName)
  // {
  //   PlayerListener listener = new PlayerListener(this, player);
  //   PlayerNameCompassTarget target = getTarget(targetPlayerName);
  //   if(target == null) return false;
  //   target.listeners.add(listener);
  //   getServer().getPluginManager().registerEvents(listener, this);
  //   return true;
  // }
  // /**
  //  * checks if a target with the given player name exists
  //  * @param playerName name of the target to check for
  //  * @return if the target exists
  //  */
  // public boolean targetExists(String playerName)
  // {
  //   return getTarget(playerName) != null;
  //   // for(int i = targets.size() - 1; i >= 0; i--)
  //   // {
  //   //   CompassTarget target = targets.get(i);
  //   //   if(target instanceof PlayerNameCompassTarget)
  //   //   {
  //   //     PlayerNameCompassTarget playerTarget = (PlayerNameCompassTarget)target;
  //   //     Player player = playerTarget.getPlayer();
  //   //     if(player != null && player.getName() != null && player.getName().equals(playerName))
  //   //     {
  //   //       return true;
  //   //     }
  //   //   }
  //   // }
  //   // return false;
  // }
  // /**
  //  * removes a player's listener from the targets
  //  * @param player the player to remove
  //  * @return if the player was found
  //  */
  // public boolean removePlayer(Player player)
  // {
  //   boolean foundPlayer = false;
  //   for(int i = targets.size() - 1; i >= 0; i--)
  //   {
  //     CompassTarget target = targets.get(i);
  //     for(int j = target.listeners.size() - 1; j >= 0; j--)
  //     {
  //       PlayerListener listener = target.listeners.get(j);
  //       if(listener.getPlayer() == player)
  //       {
  //         //remove the player listener
  //         HandlerList.unregisterAll(listener);
  //         target.listeners.remove(j);
  //         foundPlayer = true;
  //       }
  //     }
  //     //remove location target if nobody is listening to it because no new listeners can be added to it
  //     if(target instanceof CompassTarget && !(target instanceof PlayerNameCompassTarget) && target.listeners.size() == 0)
  //     {
  //       targets.remove(i);
  //     }
  //   }
  //   return foundPlayer;
  // }
  // /**
  //  * removes a target given the targeted player's name
  //  * @param playerName the target's name
  //  * @return if the target was found
  //  */
  // public boolean removeTarget(String playerName)
  // {
  //   PlayerNameCompassTarget target = getTarget(playerName);
  //   if(target == null) return false;
  //   HandlerList.unregisterAll(target);
  //   //we're not moving the target's listeners anywhere, so we can just unregister them
  //   for(int i = 0; i < target.listeners.size(); i++)
  //   {
  //     HandlerList.unregisterAll(target.listeners.get(i));
  //   }
  //   target.listeners.clear();
  //   targets.remove(target);
  //   return true;

  //   // for(int i = targets.size() - 1; i >= 0; i--)
  //   // {
  //   //   CompassTarget target = targets.get(i);
  //   //   if(target instanceof PlayerNameCompassTarget)
  //   //   {
  //   //     PlayerNameCompassTarget playerTarget = (PlayerNameCompassTarget)target;
  //   //     Player player = playerTarget.getPlayer();
  //   //     if(player != null && player.getName() != null && player.getName().equals(playerName))
  //   //     {
  //   //       HandlerList.unregisterAll(playerTarget);
  //   //       targets.remove(i);
  //   //       return true;
  //   //     }
  //   //   }
  //   // }
  //   // return false;
  // }
  // /**
  //  * takes a compass away from the given player if they have one
  //  * @param player the player to take from
  //  * @return if the player had a compass to remove
  //  */
  // public boolean removeCompass(Player player)
  // {
  //   Inventory inv = player.getInventory();
  //   for(ItemStack stack : inv)
  //   {
  //     if(stack.getType() == Material.COMPASS)
  //     {
  //       int amount = stack.getAmount();
  //       if(amount <= 1)
  //       {
  //         inv.remove(stack);
  //       }
  //       else
  //       {
  //         stack.setAmount(amount - 1);
  //       }
  //       return true;
  //     }
  //   }
  //   return false;
  // }
  // /**
  //  * gets a target from the given player name
  //  * @param playerName the target's player name
  //  * @return the compass target; null if not found
  //  */
  // public PlayerNameCompassTarget getTarget(String playerName)
  // {
  //   for(CompassTarget target : targets)
  //   {
  //     if(target instanceof PlayerNameCompassTarget)
  //     {
  //       PlayerNameCompassTarget nameTarget = (PlayerNameCompassTarget)target;
  //       if(nameTarget.getName().equals(playerName))
  //       {
  //         return nameTarget;
  //       }
  //     }
  //   }
  //   return null;
  // }
  // /**
  //  * gets the player listener from a playername
  //  * @param playerName the listener's name
  //  * @return the player listener; null if not found
  //  */
  // public PlayerListener getPlayerListener(String playerName)
  // {
  //   for(int i = 0; i < targets.size(); i++)
  //   {
  //     CompassTarget target = targets.get(i);
  //     for(int j = 0; j < target.listeners.size(); j++)
  //     {
  //       PlayerListener listener = target.listeners.get(j);
  //       if(listener.getPlayer().getName().equals(playerName))
  //       {
  //         return listener;
  //       }
  //     }
  //   }
  //   return null;
  // }
  // /**
  //  * gets the target of a listener (todo: slow)
  //  * @param listener the listener
  //  * @return listener's compass target; null if not found
  //  */
  // public CompassTarget getListenersTarget(PlayerListener listener)
  // {
  //   for(int i = 0; i < targets.size(); i++)
  //   {
  //     CompassTarget target = targets.get(i);
  //     for(int j = 0; j < target.listeners.size(); j++)
  //     {
  //       PlayerListener testListener = target.listeners.get(j);
  //       if(testListener == listener)
  //       {
  //         return target;
  //       }
  //     }
  //   }
  //   return null;
  // }
  // /**
  //  * gets the next player name compass target after the given
  //  * @param target the current target
  //  * @return the next player name compass target in the list; null if not found
  //  */
  // public PlayerNameCompassTarget getNextTarget(CompassTarget target)
  // {
  //   int i = 0;
  //   if(target != null)
  //   {
  //     for(; i < targets.size(); i++)
  //     {
  //       CompassTarget testTarget = targets.get(i);
  //       if(testTarget == target)
  //       {
  //         break;
  //       }
  //     }
  //   }
  //   PlayerNameCompassTarget nextTarget = null;
  //   for(int j = 0; j < targets.size(); j++)
  //   {
  //     int ind = (j + i) % targets.size();
  //     CompassTarget testTarget = targets.get(ind);
  //     if(testTarget instanceof PlayerNameCompassTarget)
  //     {
  //       nextTarget = (PlayerNameCompassTarget)testTarget;
  //     }
  //   }
  //   return nextTarget;
  // }
}