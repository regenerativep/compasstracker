package io.github.regenerativep.compasstracker;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandListener implements CommandExecutor
{
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
    String res = "";
    if(args[0].equals("listen"))
    {
      if(args.length >= 3 && isNumber(args[1]) && isNumber(args[2]))
      {
        int x = Integer.parseInt(args[1]);
        int z = Integer.parseInt(args[2]);
        if(args.length >= 4 && args[3].length() > 0)
        {
          if(sender.hasPermission("ctrack.addlistener") && sender.hasPermission("ctrack.addtargetlocation"))
          {
            Player playerToListen = app.getServer().getPlayerExact(args[3]);
            if(playerToListen == null)
            {
              sender.sendMessage("Could not find player by the name of \"" + args[3] + "\".");
              return false;
            }
            res = app.removePlayer(playerToListen);
            res += "\n" + app.createLocationTarget(new Location(playerToListen.getWorld(), x, playerToListen.getLocation().getY(), z), playerToListen);
          }
          else
          {
            sender.sendMessage("Insufficient permissions.");
            return false;
          }
        }
        else if(senderIsPlayer)
        {
          if(sender.hasPermission("ctrack.addtargetlocation"))
          {
            Player playerToListen = (Player)sender;
            res = app.removePlayer(playerToListen);
            res += "\n" + app.createLocationTarget(new Location(playerToListen.getWorld(), x, playerToListen.getLocation().getY(), z), playerToListen);
          }
          else
          {
            sender.sendMessage("Insufficient permissions.");
            return false;
          }
        }
        else
        {
          sender.sendMessage("You need to specify the player to listen, because you are not a player.");
          return false;
        }
      }
      else if(args.length >= 2)
      {
        if(args.length >= 3 && args[2].length() > 0)
        {
          if(sender.hasPermission("ctrack.addlistener"))
          {
            Player playerToListen = app.getServer().getPlayerExact(args[2]);
            if(playerToListen == null)
            {
              sender.sendMessage("Could not find player by the name of \"" + args[3] + "\".");
              return false;
            }
            res = app.removePlayer(playerToListen);
            res += "\n" + app.addPlayerListener(playerToListen, args[1]);
          }
          else
          {
            sender.sendMessage("Insufficient permissions.");
            return false;
          }
        }
        else if(senderIsPlayer)
        {
          if(sender.hasPermission("ctrack.addlistener.self"))
          {
            Player playerToListen = (Player)sender;
            res = app.removePlayer(playerToListen);
            res += "\n" + app.addPlayerListener(playerToListen, args[1]);
          }
          else
          {
            sender.sendMessage("Insufficient permissions.");
            return false;
          }
        }
        else
        {
          sender.sendMessage("You need to specify the player to listen, because you are not a player.");
          return false;
        }
      }
      else
      {
        return false;
      }
    }
    else if(args[0].equals("remove"))
    {
      Player playerToRemove;
      if(args.length >= 2 && args[1].length() > 0)
      {
        if(!sender.hasPermission("ctrack.removelistener"))
        {
          sender.sendMessage("Insufficient permissions.");
          return false;
        }
        playerToRemove = app.getServer().getPlayerExact(args[1]);
      }
      else if(senderIsPlayer)
      {
        if(!sender.hasPermission("ctrack.removelistener.self"))
        {
          sender.sendMessage("Insufficient permissions.");
          return false;
        }
        playerToRemove = (Player)sender;
      }
      else
      {
        sender.sendMessage("You need to specify the player to listen, because you are not a player.");
        return false;
      }
      res = app.removePlayer(playerToRemove);
    }
    else if(args[0].equals("target"))
    {
      Player playerToTarget;
      if(args.length >= 2 && args[1].length() > 0)
      {
        if(!sender.hasPermission("ctrack.addtarget"))
        {
          sender.sendMessage("Insufficient permissions.");
          return false;
        }
        playerToTarget = app.getServer().getPlayerExact(args[1]);
      }
      else if(senderIsPlayer)
      {
        if(!sender.hasPermission("ctrack.addtarget.self"))
        {
          sender.sendMessage("Insufficient permissions.");
          return false;
        }
        playerToTarget = (Player)sender;
      }
      else
      {
        sender.sendMessage("You need to specify the player to target, because you are not a player.");
        return false;
      }
      res = app.createTarget(playerToTarget.getName());
    }
    else if(args[0].equals("stop"))
    {
      if(!sender.hasPermission("ctrack.removetarget"))
      {
        sender.sendMessage("Insufficient permissions.");
        return false;
      }
      if(args.length >= 2 && args[1].length() > 0)
      {
        res = app.removeTarget(args[1]);
      }
      else
      {
        return false;
      }
    }
    if(res.length() > 0)
    {
      sender.sendMessage(res);
    }
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