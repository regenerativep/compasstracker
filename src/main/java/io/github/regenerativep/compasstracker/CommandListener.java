package io.github.regenerativep.compasstracker;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CommandListener implements CommandExecutor
{
  public final String PERM_ADD_LISTENER_ANY = "ctrack.addlistener";
  public final String PERM_ADD_LISTENER_SELF = "ctrack.addlistener.self";
  public final String PERM_ADD_TARGET_LOCATION = "ctrack.addtargetlocation";
  public final String PERM_REMOVE_LISTENER_ANY = "ctrack.removelistener";
  public final String PERM_REMOVE_LISTENER_SELF = "ctrack.removelistener.self";
  public final String PERM_ADD_TARGET_ANY = "ctrack.addtarget";
  public final String PERM_ADD_TARGET_SELF = "ctrack.addtarget.self";
  public final String PERM_REMOVE_TARGET = "ctrack.removetarget";
  public final String NOT_A_PLAYER = "You are not a player!";
  public final String CANNOT_FIND_PLAYER = "Could not find player by the name of \"**name**\".";
  public final String CANNOT_FIND_TARGET = "Could not find target by the name of \"**name**\". Do you need to create a target?";
  public final String NO_PERMISSION = "Insufficient permissions.";
  public final String TARGET_ALREADY_EXISTS = "Target \"**name**\" already exists.";
  private App app;
  public CommandListener(App app)
  {
    this.app = app;
  }
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
  {
    /*
    listen
      [x] [z]
        // (3)
        [player] (4)
      [player]
        // (2)
        [player] (3)
    remove
      // (1)
      player (2)
    target
      // (1)
      player (2)
    stop
      player (2)
    */
    if(args.length < 1) return false;
    boolean senderIsPlayer = sender instanceof Player;
    if(args[0].equals("listen"))
    {
      if(args.length >= 3 && isNumber(args[1]) && isNumber(args[2]))
      {
        int x = Integer.parseInt(args[1]);
        int z = Integer.parseInt(args[2]);
        String listenerName = "";
        if(args.length >= 4 && args[3].length() > 0)
        {
          listenerName = args[3];
        }
        tryListen(x, z, sender, listenerName);
      }
      else if(args.length >= 2)
      {
        String listenerName = "";
        if(args.length >= 3 && args[2].length() > 0)
        {
          listenerName = args[2];
        }
        String targetName = args[1];
        tryListen(targetName, sender, listenerName);
      }
      else
      {
        return false;
      }
    }
    else if(args[0].equals("remove"))
    {
      String listenerName = "";
      if(args.length >= 2 && args[1].length() > 0)
      {
        listenerName = args[1];
      }
      else if(!senderIsPlayer)
      {
        return false;
      }
      tryRemove(sender, listenerName);
    }
    else if(args[0].equals("target"))
    {
      String targetName = "";
      if(args.length >= 2 && args[1].length() > 0)
      {
        targetName = args[1];
      }
      else if(!senderIsPlayer)
      {
        return false;
      }
      tryTarget(sender, targetName);
    }
    else if(args[0].equals("stop"))
    {
      String targetName = "";
      if(args.length >= 2 && args[1].length() > 0)
      {
        targetName = args[1];
      }
      else if(!senderIsPlayer)
      {
        return false;
      }
      tryStop(sender, targetName);
    }
    return true;
  }
  public void errorNotAPlayer(CommandSender sender)
  {
    sender.sendMessage(NOT_A_PLAYER);
  }
  public void errorCannotFindPlayer(CommandSender sender, String playerName)
  {
    sender.sendMessage(CANNOT_FIND_PLAYER.replace("**name**", playerName));
  }
  public void errorCannotFindTarget(CommandSender sender, String playerName)
  {
    sender.sendMessage(CANNOT_FIND_TARGET.replace("**name**", playerName));
  }
  public void errorNoPermission(CommandSender sender)
  {
    sender.sendMessage(NO_PERMISSION);
  }
  public void errorTargetAlreadyExists(CommandSender sender, String targetName)
  {
    sender.sendMessage(TARGET_ALREADY_EXISTS.replace("**name**", targetName));
  }
  public Player getPlayerFromName(CommandSender sender, String listenerName)
  {
    Player player;
    if(listenerName.length() == 0)
    {
      if(sender instanceof Player)
      {
        player = (Player)sender;
      }
      else
      {
        errorNotAPlayer(sender);
        return null;
      }
    }
    else
    {
      player = app.getServer().getPlayerExact(listenerName);
      if(player == null)
      {
        errorCannotFindPlayer(sender, listenerName);
        return null;
      }
    }
    return player;
  }
  public boolean tryListen(int x, int z, CommandSender sender, String listenerName)
  {
    boolean affectOthersPerm = sender.hasPermission(PERM_ADD_LISTENER_ANY);
    boolean affectSelfPerm = sender.hasPermission(PERM_ADD_TARGET_LOCATION);
    if(!affectOthersPerm && !affectSelfPerm)
    {
      errorNoPermission(sender);
      return false;
    }
    boolean isSelf = sender instanceof Player;
    Player playerToListen = getPlayerFromName(sender, listenerName);
    if(playerToListen == null) return false;
    if((isSelf && !affectSelfPerm) || (!isSelf && !affectOthersPerm))
    {
      errorNoPermission(sender);
      return false;
    }
    app.removePlayer(playerToListen);
    Location loc = new Location(playerToListen.getWorld(), x, playerToListen.getLocation().getY(), z);
    app.createLocationTarget(loc, playerToListen);
    return true;
  }
  public boolean tryListen(String targetName, CommandSender sender, String listenerName)
  {
    boolean affectOthersPerm = sender.hasPermission(PERM_ADD_LISTENER_ANY);
    boolean affectSelfPerm = sender.hasPermission(PERM_ADD_LISTENER_SELF);
    if(!affectOthersPerm && !affectSelfPerm)
    {
      errorNoPermission(sender);
      return false;
    }
    boolean isSelf = sender instanceof Player;
    Player playerToListen = getPlayerFromName(sender, listenerName);
    if(playerToListen == null) return false;
    if((isSelf && !affectSelfPerm) || (!isSelf && !affectOthersPerm))
    {
      errorNoPermission(sender);
      return false;
    }
    boolean targetExists = app.targetExists(targetName);
    if(!targetExists)
    {
      errorCannotFindTarget(sender, targetName);
      return false;
    }
    boolean foundPlayer = app.removePlayer(playerToListen);
    if(!foundPlayer)
    {
      //the player might not have a compass, check and give one
      Inventory inv = playerToListen.getInventory();
      if(!inv.contains(Material.COMPASS))
      {
        inv.addItem(new ItemStack(Material.COMPASS));
      }
    }
    app.addPlayerListener(playerToListen, targetName);
    return true;
  }
  public boolean tryRemove(CommandSender sender, String listenerName)
  {
    boolean affectOthersPerm = sender.hasPermission(PERM_ADD_LISTENER_ANY);
    boolean affectSelfPerm = sender.hasPermission(PERM_ADD_LISTENER_SELF);
    if(!affectOthersPerm && !affectSelfPerm)
    {
      errorNoPermission(sender);
      return false;
    }
    boolean isSelf = sender instanceof Player;
    Player playerToRemove = getPlayerFromName(sender, listenerName);
    if(playerToRemove == null) return false;
    if((isSelf && !affectSelfPerm) || (!isSelf && !affectOthersPerm))
    {
      errorNoPermission(sender);
      return false;
    }
    if(app.removePlayer(playerToRemove))
    {
      //check for a compass and remove it
      app.removeCompass(playerToRemove);
    }
    return true;
  }
  public boolean tryTarget(CommandSender sender, String targetName)
  {
    boolean affectOthersPerm = sender.hasPermission(PERM_ADD_TARGET_ANY);
    boolean affectSelfPerm = sender.hasPermission(PERM_ADD_TARGET_SELF);
    if(!affectOthersPerm && !affectSelfPerm)
    {
      errorNoPermission(sender);
      return false;
    }
    boolean isSelf = sender instanceof Player;
    Player targetPlayer = getPlayerFromName(sender, targetName);
    if(targetPlayer == null) return false;
    targetName = targetPlayer.getName();
    if((isSelf && !affectSelfPerm) || (!isSelf && !affectOthersPerm))
    {
      errorNoPermission(sender);
      return false;
    }
    if(app.targetExists(targetName))
    {
      errorTargetAlreadyExists(sender, targetName);
      return false;
    }
    app.createTarget(targetName);
    return true;
  }
  public boolean tryStop(CommandSender sender, String targetName)
  {
    boolean affectTargetPerm = sender.hasPermission(PERM_REMOVE_TARGET);
    if(!affectTargetPerm)
    {
      errorNoPermission(sender);
      return false;
    }
    //boolean isSelf = sender instanceof Player;
    Player targetPlayer = getPlayerFromName(sender, targetName);
    if(targetPlayer == null) return false;
    targetName = targetPlayer.getName();
    if(!app.targetExists(targetName))
    {
      errorCannotFindTarget(sender, targetName);
      return false;
    }
    //we must remove compasses of the listening players
    PlayerNameCompassTarget target = app.getTarget(targetName);
    for(PlayerListener playerListener : target.listeners)
    {
      app.removeCompass(playerListener.getPlayer());
    }
    app.removeTarget(targetName);
    return true;
  }
  private boolean isNumber(String value)
  {
    try
    {
      int numVal = Integer.parseInt(value);
    }
    catch(Exception e)
    {
      return false;
    }
    return true;
  }
}