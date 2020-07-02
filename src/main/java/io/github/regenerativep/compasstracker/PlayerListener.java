package io.github.regenerativep.compasstracker;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerListener implements Listener
{
  private Player listeningPlayer;
  private boolean keepCompass;
  private boolean keptInventory;
  public PlayerListener(Player player)
  {
    listeningPlayer = player;
    keepCompass = true;
    keptInventory = false;
  }
  public void updateTarget(Location loc)
  {
    if(listeningPlayer != null)
    {
      listeningPlayer.setCompassTarget(loc);
    }
  }
  @EventHandler
  public void onPlayerDeath(PlayerDeathEvent event)
  {
    Player player = event.getEntity();
    if(player != listeningPlayer) return;
    if(keepCompass && !event.getKeepInventory())
    {
      //if player has a compass, get rid of it
      List<ItemStack> drops = event.getDrops();
      for(int i = drops.size() - 1; i >= 0; i--)
      {
        ItemStack drop = drops.get(i);
        if(drop.getType() == Material.COMPASS)
        {
          drops.remove(i);
          break;
        }
      }
      keptInventory = false;
    }
    else
    {
      keptInventory = true;
    }
  }
  @EventHandler
  public void onPlayerRespawn(PlayerRespawnEvent event)
  {
    Player player = event.getPlayer();
    if(player != listeningPlayer) return;
    if(keepCompass && !keptInventory)
    {
      //give the player a compass
      ItemStack item = new ItemStack(Material.COMPASS);
      player.getInventory().addItem(item);
    }
  }
  public Player getPlayer()
  {
    return listeningPlayer;
  }
}