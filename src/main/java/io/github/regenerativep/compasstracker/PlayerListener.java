package io.github.regenerativep.compasstracker;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerListener implements Listener
{
  public static final String TRACKING_INFO = "Tracking ";
  private App app;
  private Player listeningPlayer;
  private boolean keepCompass;
  private boolean keptInventory;
  public PlayerListener(App app, Player player)
  {
    listeningPlayer = player;
    keepCompass = true;
    keptInventory = false;
    this.app = app;
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
    if(!player.getName().equals(listeningPlayer.getName())) return;
    listeningPlayer = player;
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
    if(!player.getName().equals(listeningPlayer.getName())) return;
    listeningPlayer = player;
    if(keepCompass && !keptInventory)
    {
      //give the player a compass
      ItemStack item = new ItemStack(Material.COMPASS);
      player.getInventory().addItem(item);
    }
  }
  @EventHandler
  public void onPlayerQuit(PlayerQuitEvent event)
  {
    Player player = event.getPlayer();
    if(!player.getName().equals(listeningPlayer.getName())) return;
    listeningPlayer = player;
    app.removePlayer(listeningPlayer);
  }
  public void sendTrackingMessage(Player player)
  {
    CompassTarget target = getTarget();
    String targetMessage;
    if(target == null)
    {
      targetMessage = "unknown";
    }
    else
    {
      targetMessage = target.getTrackingValue();
    }
    //display tracking information
    TextComponent message = new TextComponent(TRACKING_INFO);
    message.setColor(ChatColor.YELLOW);
    TextComponent infoMessage = new TextComponent(targetMessage);
    infoMessage.setColor(ChatColor.WHITE);
    message.addExtra(infoMessage);
    player.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
  }
  @EventHandler
  public void onPlayerItemHeld(PlayerItemHeldEvent event)
  {
    Player player = event.getPlayer();
    if(!player.getName().equals(listeningPlayer.getName())) return;
    listeningPlayer = player;
    ItemStack heldItem = player.getInventory().getItem(event.getNewSlot());
    if(heldItem != null && heldItem.getType() == Material.COMPASS)
    {
      sendTrackingMessage(player);
    }
  }
  @EventHandler
  public void onPlayerInteract(PlayerInteractEvent event)
  {
    Player player = event.getPlayer();
    if(!player.getName().equals(listeningPlayer.getName())) return;
    listeningPlayer = player;
    if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    ItemStack item = event.getItem();
    if(item != null && item.getType() == Material.COMPASS)
    {
      CompassTarget target = getTarget();
      CompassTarget nextTarget = app.getNextTarget(target);
      //move the listener to the new target
      target.removeListener(this);
      nextTarget.listeners.add(this);

      sendTrackingMessage(player);
    }
  }
  public Player getPlayer()
  {
    return listeningPlayer;
  }
  public CompassTarget getTarget()
  {
    return app.getListenersTarget(this);
  }
}