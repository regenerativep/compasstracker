package io.github.regenerativep.compasstracker;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.Location;
import org.bukkit.World;

public class PlayerNameCompassTarget extends CompassTarget implements Listener
{
  private String targetPlayerName;
  public PlayerNameCompassTarget(App app, String playerName)
  {
    super(app);
    targetPlayerName = playerName;
  }
  public Player getPlayer()
  {
    return app.getServer().getPlayerExact(targetPlayerName);
  }
  public String getName()
  {
    return targetPlayerName;
  }
  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event)
  {
    Player player = event.getPlayer();
    if(player.getName().equals(targetPlayerName))
    {
      Location playerLocation = player.getLocation();
      //only update if player is in overworld
      if(playerLocation.getWorld().getEnvironment() == World.Environment.NORMAL)
      {
        //update location
        targetLocation = playerLocation;
      }
    }
  }
  @Override
  public String getTrackingValue()
  {
    return "player \"" + targetPlayerName + "\"";
  }
}