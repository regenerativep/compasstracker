package io.github.regenerativep.compasstracker;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;

public class CompassTarget
{
  public List<PlayerListener> listeners;
  public Location targetLocation;
  protected App app;
  public CompassTarget(App app)
  {
    this.app = app;
    listeners = new ArrayList<>();
    targetLocation = null;
  }
  public void updateListeners()
  {
    for(int i = 0; i < listeners.size(); i++)
    {
      PlayerListener listener = listeners.get(i);
      Player player = listener.getPlayer();
      if(player != null && targetLocation != null)
      {
        //only will update player's compass if player and target are in same dimension
        if(player.getWorld().getEnvironment() == targetLocation.getWorld().getEnvironment())
        {
          listener.updateTarget(targetLocation);
        }
      }
    }
  }
}