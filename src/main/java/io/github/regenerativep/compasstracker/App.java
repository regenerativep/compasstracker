package io.github.regenerativep.compasstracker;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

public class App extends JavaPlugin
{
  public final String COMPASS_TRACKER_COMMMAND = "ctr";
  public final long compassUpdateRate = 20;
  public List<CompassTarget> targets;
  public BukkitTask updateTask;
  @Override
  public void onEnable()
  {
    targets = new ArrayList<>();
    updateTask = new BukkitRunnable() {
      @Override
      public void run() {    
        updateListeners();
      }
    }.runTaskTimer(this, 0, compassUpdateRate);
    getCommand(COMPASS_TRACKER_COMMMAND).setExecutor(new CommandListener(this));
  }
  @Override
  public void onDisable()
  {
    //
  }
  public void updateListeners()
  {
    for(int i = 0; i < targets.size(); i++)
    {
      CompassTarget target = targets.get(i);
      target.updateListeners();
    }
  }
  public boolean createTarget(String playerName)
  {
    if(targetExists(playerName)) return false; //return "Target already exists.";
    PlayerNameCompassTarget target = new PlayerNameCompassTarget(this, playerName);
    if(target.getPlayer() == null) return false;
    getServer().getPluginManager().registerEvents(target, this);
    targets.add(target);
    return true;
  }
  public boolean createLocationTarget(Location loc, Player listeningPlayer)
  {
    CompassTarget target = new CompassTarget(this);
    target.targetLocation = loc;
    PlayerListener playerListener = new PlayerListener(this, listeningPlayer);
    getServer().getPluginManager().registerEvents(playerListener, this);
    target.listeners.add(playerListener);
    targets.add(target);
    return true;
  }
  public boolean addPlayerListener(Player player, String targetPlayerName)
  {
    PlayerListener listener = new PlayerListener(this, player);
    for(int i = targets.size() - 1; i >= 0; i--)
    {
      CompassTarget target = targets.get(i);
      if(target instanceof PlayerNameCompassTarget)
      {
        PlayerNameCompassTarget playerTarget = (PlayerNameCompassTarget)target;
        if(playerTarget.getName().equals(targetPlayerName))
        {
          target.listeners.add(listener);
          getServer().getPluginManager().registerEvents(listener, this);
          return true;
        }
      }
    }
    return false;
  }
  public boolean targetExists(String playerName)
  {
    for(int i = targets.size() - 1; i >= 0; i--)
    {
      CompassTarget target = targets.get(i);
      if(target instanceof PlayerNameCompassTarget)
      {
        PlayerNameCompassTarget playerTarget = (PlayerNameCompassTarget)target;
        Player player = playerTarget.getPlayer();
        if(player != null && player.getName() != null && player.getName().equals(playerName))
        {
          return true;
        }
      }
    }
    return false;
  }
  public boolean removePlayer(Player player)
  {
    boolean foundPlayer = false;
    for(int i = targets.size() - 1; i >= 0; i--)
    {
      CompassTarget target = targets.get(i);
      for(int j = target.listeners.size() - 1; j >= 0; j--)
      {
        PlayerListener listener = target.listeners.get(j);
        if(listener.getPlayer() == player)
        {
          //remove the player listener
          HandlerList.unregisterAll(listener);
          target.listeners.remove(j);
          foundPlayer = true;
        }
      }
      if(target instanceof CompassTarget && !(target instanceof PlayerNameCompassTarget) && target.listeners.size() == 0)
      {
        targets.remove(i);
      }
    }
    return foundPlayer;
  }
  public boolean removeTarget(String playerName)
  {
    for(int i = targets.size() - 1; i >= 0; i--)
    {
      CompassTarget target = targets.get(i);
      if(target instanceof PlayerNameCompassTarget)
      {
        PlayerNameCompassTarget playerTarget = (PlayerNameCompassTarget)target;
        Player player = playerTarget.getPlayer();
        if(player != null && player.getName() != null && player.getName().equals(playerName))
        {
          HandlerList.unregisterAll(playerTarget);
          targets.remove(i);
          return true;
        }
      }
    }
    return false;
  }
  public boolean removeCompass(Player player)
  {
    Inventory inv = player.getInventory();
    for(ItemStack stack : inv)
    {
      if(stack.getType() == Material.COMPASS)
      {
        int amount = stack.getAmount();
        if(amount <= 1)
        {
          inv.remove(stack);
        }
        else
        {
          stack.setAmount(amount - 1);
        }
        return true;
      }
    }
    return false;
  }
  public PlayerNameCompassTarget getTarget(String playerName)
  {
    for(CompassTarget target : targets)
    {
      if(target instanceof PlayerNameCompassTarget)
      {
        PlayerNameCompassTarget nameTarget = (PlayerNameCompassTarget)target;
        if(nameTarget.getName().equals(playerName))
        {
          return nameTarget;
        }
      }
    }
    return null;
  }
  public PlayerListener getPlayerListener(String playerName)
  {
    for(int i = 0; i < targets.size(); i++)
    {
      CompassTarget target = targets.get(i);
      for(int j = 0; j < target.listeners.size(); j++)
      {
        PlayerListener listener = target.listeners.get(j);
        if(listener.getPlayer().getName().equals(playerName))
        {
          return listener;
        }
      }
    }
    return null;
  }
  public CompassTarget getListenersTarget(PlayerListener listener)
  {
    for(int i = 0; i < targets.size(); i++)
    {
      CompassTarget target = targets.get(i);
      for(int j = 0; j < target.listeners.size(); j++)
      {
        PlayerListener testListener = target.listeners.get(j);
        if(testListener == listener)
        {
          return target;
        }
      }
    }
    return null;
  }
  public CompassTarget getNextTarget(CompassTarget target)
  {
    int i;
    for(i = 0; i < targets.size(); i++)
    {
      CompassTarget testTarget = targets.get(i);
      if(testTarget == target)
      {
        break;
      }
    }
    int j;
    CompassTarget nextTarget = target;
    for(j = 0; j < targets.size(); j++)
    {
      int ind = (j + i) % targets.size();
      CompassTarget testTarget = targets.get(ind);
      if(testTarget instanceof PlayerNameCompassTarget)
      {
        nextTarget = testTarget;
      }
    }
    return nextTarget;
  }
}