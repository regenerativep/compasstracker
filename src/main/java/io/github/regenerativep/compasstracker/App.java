package io.github.regenerativep.compasstracker;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

public class App extends JavaPlugin
{
  public final long compassUpdateRate = 20;
  public List<CompassTarget> targets;
  public BukkitTask updateTask;
  @Override
  public void onEnable()
  {
    targets = new ArrayList<>();
    updateTask = new RegularUpdate(this).runTaskTimer(this, 0, compassUpdateRate);
    getCommand("ctrack").setExecutor(new CommandListener(this));
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
  public String createTarget(String playerName)
  {
    if(targetExists(playerName)) return "Target already exists.";
    PlayerNameCompassTarget target = new PlayerNameCompassTarget(this, playerName);
    if(target.getPlayer() == null)
    {
      return "Failed to create a target for \"" + playerName + "\".";
    }
    getServer().getPluginManager().registerEvents(target, this);
    targets.add(target);
    return "Created a target for \"" + target.getPlayer().getName() + "\".";
  }
  public String createLocationTarget(Location loc, Player listeningPlayer)
  {
    CompassTarget target = new CompassTarget(this);
    target.targetLocation = loc;
    PlayerListener playerListener = new PlayerListener(listeningPlayer);
    getServer().getPluginManager().registerEvents(playerListener, this);
    target.listeners.add(playerListener);
    targets.add(target);
    return "Created a target for location xz: " + loc.getX() + ", " + loc.getZ() + " .";
  }
  public String addPlayerListener(Player player, String targetPlayerName)
  {
    PlayerListener listener = new PlayerListener(player);
    boolean foundTarget = false;
    for(int i = targets.size() - 1; i >= 0; i--)
    {
      CompassTarget target = targets.get(i);
      if(target instanceof PlayerNameCompassTarget)
      {
        PlayerNameCompassTarget playerTarget = (PlayerNameCompassTarget)target;
        if(playerTarget.getName().equals(targetPlayerName))
        {
          target.listeners.add(listener);
          foundTarget = true;
        }
      }
    }
    if(foundTarget)
    {
      getServer().getPluginManager().registerEvents(listener, this);
      return "Added \"" + player.getName() + "\" to listens of target.";
    }
    return "Could not find target under the name of \"" + targetPlayerName + "\".";
  }
  public boolean targetExists(String playerName)
  {
    for(int i = targets.size() - 1; i >= 0; i--)
    {
      CompassTarget target = targets.get(i);
      if(target instanceof PlayerNameCompassTarget)
      {
        PlayerNameCompassTarget playerTarget = (PlayerNameCompassTarget)target;
        if(playerTarget.getPlayer().getName().equals(playerName))
        {
          return true;
        }
      }
    }
    return false;
  }
  public String removePlayer(Player player)
  {
    for(int i = targets.size() - 1; i >= 0; i--)
    {
      CompassTarget target = targets.get(i);
      for(int j = target.listeners.size() - 1; j >= 0; j--)
      {
        PlayerListener listener = target.listeners.get(j);
        HandlerList.unregisterAll(listener);
        if(listener.getPlayer() == player)
        {
          //remove the player listener
          target.listeners.remove(j);
        }
      }
      if(target instanceof CompassTarget && !(target instanceof PlayerNameCompassTarget) && target.listeners.size() == 0)
      {
        targets.remove(i);
      }
    }
    return "Player \"" + player.getName() + "\" has been removed from listeners.";
  }
  public String removeTarget(String playerName)
  {
    boolean foundTarget = false;
    for(int i = targets.size() - 1; i >= 0; i--)
    {
      CompassTarget target = targets.get(i);
      if(target instanceof PlayerNameCompassTarget)
      {
        PlayerNameCompassTarget playerTarget = (PlayerNameCompassTarget)target;
        if(playerTarget.getPlayer().getName().equals(playerName))
        {
          targets.remove(i);
          foundTarget = true;
        }
      }
    }
    if(foundTarget)
    {
      return "Removed \"" + playerName + "\"";
    }
    return "Failed to find target with name \"" + playerName + "\"";
  }
}