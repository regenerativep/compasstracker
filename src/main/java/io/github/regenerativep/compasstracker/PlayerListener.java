package io.github.regenerativep.compasstracker;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;

public class PlayerListener implements ILocatable
{
  public static final String TRACKING_INFO = "Tracking ";

  public ILocatable target;

  private App app;
  private Player listeningPlayer;
  private boolean keepCompass;
  private boolean keptInventory;
  private boolean receivedCompass;
  private Location lastLocation;

  public PlayerListener(App app, Player player)
  {
    listeningPlayer = player;
    receivedCompass = false;
    keepCompass = true;
    keptInventory = false;
    target = null;
    lastLocation = null;
    this.app = app;
  }
  public void update()
  {
    if(target != null && listeningPlayer != null)
    {
      listeningPlayer.setCompassTarget(target.getLocation());
    }
  }
  public void resetCompassTarget()
  {
    if(listeningPlayer != null)
    {
      listeningPlayer.setCompassTarget(listeningPlayer.getWorld().getSpawnLocation());
    }
  }
  public boolean removeCompass()
  {
    if(!receivedCompass || listeningPlayer == null) return false;
    Inventory inv = listeningPlayer.getInventory();
    for(ItemStack stack : inv)
    {
      if(stack != null && stack.getType() == Material.COMPASS)
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
        receivedCompass = false;
        return true;
      }
    }
    return false;
  }
  public boolean giveCompass()
  {
    if(receivedCompass || listeningPlayer == null) return false;
    Inventory inv = listeningPlayer.getInventory();
    if(inv.contains(Material.COMPASS)) return false;
    ItemStack item = new ItemStack(Material.COMPASS);
    inv.addItem(item);
    receivedCompass = true;
    return true;
  }
  public Player getPlayer()
  {
    return listeningPlayer;
  }
  @Override
  public String getName()
  {
    if(listeningPlayer == null) return "";
    return listeningPlayer.getName();
  }
  public ILocatable getTarget()
  {
    return target;
  }

  @Override
  public Location getLocation()
  {
    return lastLocation;
  }

  @Override
  public String getLocationDescription()
  {
    if(listeningPlayer == null) return "nothing";
    return "player \"" + listeningPlayer.getName() + "\"";
  }
  public void setTarget(ILocatable target)
  {
    this.target = target;
    sendTrackingMessage();
  }
  public void onPlayerDeath(PlayerDeathEvent event)
  {
    if(keepCompass && !event.getKeepInventory())
    {
      //if player has a compass in their drops, get rid of it
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
  public void onPlayerRespawn(PlayerRespawnEvent event)
  {
    if(keepCompass && !keptInventory)
    {
      giveCompass();
    }
  }
  public void onPlayerQuit(PlayerQuitEvent event)
  {
    app.unlisten(getName());
  }
  public void sendTrackingMessage()
  {
    if(listeningPlayer == null) return;
    String targetMessage;
    if(target == null)
    {
      targetMessage = "unknown";
    }
    else
    {
      targetMessage = target.getLocationDescription();
    }
    //display tracking information
    TextComponent message = new TextComponent(TRACKING_INFO);
    message.setColor(ChatColor.YELLOW);
    TextComponent infoMessage = new TextComponent(targetMessage);
    infoMessage.setColor(ChatColor.WHITE);
    message.addExtra(infoMessage);
    listeningPlayer.spigot().sendMessage(ChatMessageType.ACTION_BAR, message);
  }
  public void onPlayerItemHeld(PlayerItemHeldEvent event)
  {
    ItemStack heldItem = listeningPlayer.getInventory().getItem(event.getNewSlot());
    if(heldItem != null && heldItem.getType() == Material.COMPASS)
    {
      sendTrackingMessage();
    }
  }
  public void onPlayerInteract(PlayerInteractEvent event)
  {
    if(event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
    ItemStack item = event.getItem();
    if(item != null && item.getType() == Material.COMPASS)
    {
      String targetName;
      if(target == null)
      {
        targetName = null;
      }
      else
      {
        targetName = target.getName();
      }
      setTarget(app.getNextTarget(targetName));
    }
  }
  public void onPlayerMove(PlayerMoveEvent event)
  {
    if(listeningPlayer == null) return;
    Location playerLocation = listeningPlayer.getLocation();
    //only update if player is in overworld
    if(playerLocation.getWorld().getEnvironment() == World.Environment.NORMAL)
    {
      lastLocation = playerLocation;
    }
  }
}