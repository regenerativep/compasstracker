package io.github.regenerativep.compasstracker;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import io.github.regenerativep.commandmanager.CommandArgumentType;
import io.github.regenerativep.commandmanager.CommandFunction;
import io.github.regenerativep.commandmanager.CommandManager;
import io.github.regenerativep.commandmanager.CommandSpecifier;

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
  public final String PERM_CHECK_TARGET = "ctrack.checktarget";
  public final String NOT_A_PLAYER = "You are not a player!";
  public final String CANNOT_FIND_PLAYER = "Could not find player by the name of \"**name**\".";
  public final String CANNOT_FIND_TARGET = "Could not find target by the name of \"**name**\". Do you need to create a target?";
  public final String NO_PERMISSION = "Insufficient permissions.";
  public final String TARGET_ALREADY_EXISTS = "Target \"**name**\" already exists.";
  public final String NOT_LISTENING = "You are not listening to any targets.";
  public final String LISTENING_TO = "You are listening to **name**.";

  private App app;
  private CommandManager cmdManager;

  public CommandListener(App app)
  {
    this.app = app;
    cmdManager = new CommandManager(app);
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
      //
      player (2)
    */
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "listen" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING,
        CommandArgumentType.INTEGER,
        CommandArgumentType.INTEGER
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryListen((int)args[1], (int)args[2], sender, "");
        }
      },
      new String[] { PERM_ADD_TARGET_LOCATION }
    ));
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "listen" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING,
        CommandArgumentType.INTEGER,
        CommandArgumentType.INTEGER,
        CommandArgumentType.STRING
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryListen((int)args[1], (int)args[2], sender, (String)args[3]);
        }
      },
      new String[] { PERM_ADD_LISTENER_ANY }
    ));
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "listen" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING,
        CommandArgumentType.STRING
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryListen((String)args[1], sender, "");
        }
      },
      new String[] { PERM_ADD_LISTENER_SELF }
    ));
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "listen" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING,
        CommandArgumentType.STRING,
        CommandArgumentType.STRING
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryListen((String)args[1], sender, (String)args[2]);
        }
      },
      new String[] { PERM_ADD_LISTENER_ANY }
    ));
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "remove" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryRemove(sender, "");
        }
      },
      new String[] { PERM_ADD_LISTENER_SELF }
    ));
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "remove" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING,
        CommandArgumentType.STRING
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryRemove(sender, (String)args[1]);
        }
      },
      new String[] { PERM_ADD_LISTENER_ANY }
    ));
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "target" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryTarget(sender, "");
        }
      },
      new String[] { PERM_ADD_TARGET_SELF }
    ));
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "target" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING,
        CommandArgumentType.STRING
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryTarget(sender, (String)args[1]);
        }
      },
      new String[] { PERM_ADD_TARGET_ANY }
    ));
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "stop" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryStop(sender, "");
        }
      },
      new String[] { PERM_REMOVE_TARGET }
    ));
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "stop" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING,
        CommandArgumentType.STRING
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryStop(sender, (String)args[1]);
        }
      },
      new String[] { PERM_REMOVE_TARGET }
    ));
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "who" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryCheck(sender, "");
        }
      },
      new String[] { }
    ));
    cmdManager.commands.add(new CommandSpecifier(
      new Object[] { "who" },
      new CommandArgumentType[] {
        CommandArgumentType.STRING,
        CommandArgumentType.STRING
      },
      new CommandFunction()
      {
        @Override
        public boolean call(CommandSender sender, Object[] args) {
          return tryCheck(sender, (String)args[1]);
        }
      },
      new String[] { PERM_CHECK_TARGET }
    ));
  }
  @Override
  public boolean onCommand(CommandSender sender, Command command, String label, String[] args)
  {
    return cmdManager.inputCommand(sender, args);
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
  public void checkAndGiveCompass(Player player)
  {
    boolean foundPlayer = app.removePlayer(player);
    if(!foundPlayer)
    {
      //the player might not have a compass, check and give one
      Inventory inv = player.getInventory();
      if(!inv.contains(Material.COMPASS))
      {
        inv.addItem(new ItemStack(Material.COMPASS));
      }
    }
  }
  public boolean tryListen(int x, int z, CommandSender sender, String listenerName)
  {
    Player playerToListen = getPlayerFromName(sender, listenerName);
    if(playerToListen == null) return false;
    Location loc = new Location(playerToListen.getWorld(), x, playerToListen.getLocation().getY(), z);
    checkAndGiveCompass(playerToListen);
    app.createLocationTarget(loc, playerToListen);
    return true;
  }
  public boolean tryListen(String targetName, CommandSender sender, String listenerName)
  {
    Player playerToListen = getPlayerFromName(sender, listenerName);
    if(playerToListen == null) return false;
    boolean targetExists = app.targetExists(targetName);
    if(!targetExists)
    {
      errorCannotFindTarget(sender, targetName);
      return false;
    }
    checkAndGiveCompass(playerToListen);
    app.addPlayerListener(playerToListen, targetName);
    return true;
  }
  public boolean tryRemove(CommandSender sender, String listenerName)
  {
    Player playerToRemove = getPlayerFromName(sender, listenerName);
    if(playerToRemove == null) return false;
    if(app.removePlayer(playerToRemove))
    {
      //check for a compass and remove it
      app.removeCompass(playerToRemove);
    }
    return true;
  }
  public boolean tryTarget(CommandSender sender, String targetName)
  {
    Player targetPlayer = getPlayerFromName(sender, targetName);
    if(targetPlayer == null) return false;
    targetName = targetPlayer.getName();
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
  public boolean tryCheck(CommandSender sender, String playerName)
  {
    Player player = getPlayerFromName(sender, playerName);
    if(player == null) return false;
    playerName = player.getName();
    PlayerListener listener = app.getPlayerListener(playerName);
    if(listener == null)
    {
      sender.sendMessage(NOT_LISTENING);
    }
    else
    {
      CompassTarget target = app.getListenersTarget(listener); //todo: dont look again for the target
      sender.sendMessage(LISTENING_TO.replace("**name**", target.getTrackingValue()));
    }
    return true;
  }
}