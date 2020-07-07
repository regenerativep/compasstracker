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

/**
 * provides the interface between command input and the application
 */
public class CommandListener implements CommandExecutor
{
  /**
   * permission for adding anyone as a listener
   */
  public final String PERM_ADD_LISTENER_ANY = "ctrack.addlistener";
  /**
   * permission for adding theirself as a listener
   */
  public final String PERM_ADD_LISTENER_SELF = "ctrack.addlistener.self";
  /**
   * permission for creating and listening to a location target
   */
  public final String PERM_ADD_TARGET_LOCATION = "ctrack.addtargetlocation";
  /**
   * permission for removing any listener from a target
   */
  public final String PERM_REMOVE_LISTENER_ANY = "ctrack.removelistener";
  /**
   * permission for removing theirself from a target
   */
  public final String PERM_REMOVE_LISTENER_SELF = "ctrack.removelistener.self";
  /**
   * permission for adding anyone as a target
   */
  public final String PERM_ADD_TARGET_ANY = "ctrack.addtarget";
  /**
   * permission for adding theirself as a target
   */
  public final String PERM_ADD_TARGET_SELF = "ctrack.addtarget.self";
  /**
   * permission for removing a target
   */
  public final String PERM_REMOVE_TARGET = "ctrack.removetarget";
  /**
   * premission for checking what they are targeting
   */
  public final String PERM_CHECK_TARGET = "ctrack.checktarget";
  /**
   * message for when the command sender is not a player when they should be
   */
  public final String NOT_A_PLAYER = "You are not a player!";
  /**
   * message for when the player cannot be found
   */
  public final String CANNOT_FIND_PLAYER = "Could not find player by the name of \"**name**\".";
  /**
   * message for when the target cannot be found
   */
  public final String CANNOT_FIND_TARGET = "Could not find target by the name of \"**name**\". Do you need to create a target?";
  /**
   * message for when the caller has insufficient permission
   */
  public final String NO_PERMISSION = "Insufficient permissions.";
  /**
   * message for when a target already exists
   */
  public final String TARGET_ALREADY_EXISTS = "Target \"**name**\" already exists.";
  /**
   * message for when the sender is not listening to a target
   */
  public final String NOT_LISTENING = "You are not listening to any targets.";
  /**
   * message for when the sender is listening to a target and to what target
   */
  public final String LISTENING_TO = "You are listening to **name**.";

  /**
   * a reference to the main application
   */
  private App app;
  /**
   * the command manager
   */
  private CommandManager cmdManager;
  /**
   * constructs a command listener for the compass tracker plugin
   * @param app a reference to the main application
   */
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
    cmdManager.commands.add(new CommandSpecifier( //ctr listen [x] [z]
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
    cmdManager.commands.add(new CommandSpecifier( //ctr listen [x] [z] [listener]
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
    cmdManager.commands.add(new CommandSpecifier( //ctr listen [target name]
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
    cmdManager.commands.add(new CommandSpecifier( //ctr listen [target name] [listener name]
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
    cmdManager.commands.add(new CommandSpecifier( //ctr remove
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
    cmdManager.commands.add(new CommandSpecifier( //ctr remove [listener name]
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
    cmdManager.commands.add(new CommandSpecifier( //ctr target
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
    cmdManager.commands.add(new CommandSpecifier( //ctr target [target name]
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
    cmdManager.commands.add(new CommandSpecifier( //ctr stop
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
    cmdManager.commands.add(new CommandSpecifier( //ctr stop [target name]
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
    cmdManager.commands.add(new CommandSpecifier( //ctr who
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
    cmdManager.commands.add(new CommandSpecifier( //ctr who [listener name]
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
  /**
   * called to display not a player error
   * @param sender sender to send message to
   */
  public void errorNotAPlayer(CommandSender sender)
  {
    sender.sendMessage(NOT_A_PLAYER);
  }
  /**
   * called to display cannot find player error
   * @param sender sender to send message to
   * @param playerName name of the player
   */
  public void errorCannotFindPlayer(CommandSender sender, String playerName)
  {
    sender.sendMessage(CANNOT_FIND_PLAYER.replace("**name**", playerName));
  }
  /**
   * called to display cannot find target error
   * @param sender sender to send message to
   * @param playerName name of the player
   */
  public void errorCannotFindTarget(CommandSender sender, String playerName)
  {
    sender.sendMessage(CANNOT_FIND_TARGET.replace("**name**", playerName));
  }
  /**
   * called to display no permissions error
   * @param sender sender to send message to
   */
  public void errorNoPermission(CommandSender sender)
  {
    sender.sendMessage(NO_PERMISSION);
  }
  /**
   * called to display target already exists error
   * @param sender sender to send message to
   * @param targetName name of the target
   */
  public void errorTargetAlreadyExists(CommandSender sender, String targetName)
  {
    sender.sendMessage(TARGET_ALREADY_EXISTS.replace("**name**", targetName));
  }
  /**
   * gets a player for a command given their name
   * @param sender sender to use if no listener specified
   * @param listenerName name of the listener
   * @return the player found; null if not found
   */
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
  /**
   * check if player already has compass, and gives them one if they do not
   * @param player player to give compass to
   */
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
  /**
   * tries to listen given inputs
   * @param x x position
   * @param z z position
   * @param sender command sender
   * @param listenerName name of listener
   * @return if successful
   */
  public boolean tryListen(int x, int z, CommandSender sender, String listenerName)
  {
    Player playerToListen = getPlayerFromName(sender, listenerName);
    if(playerToListen == null) return false;
    Location loc = new Location(playerToListen.getWorld(), x, playerToListen.getLocation().getY(), z);
    checkAndGiveCompass(playerToListen);
    app.createLocationTarget(loc, playerToListen);
    return true;
  }
  /**
   * tries to listen based on given inputs
   * @param targetName name of the target to listen to
   * @param sender command sender
   * @param listenerName name of listener
   * @return if successful
   */
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
  /**
   * tries to remove based on given inputs
   * @param sender command sender
   * @param listenerName name of listener
   * @return if successful
   */
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
  /**
   * tries to create a target based on given inputs
   * @param sender command sender
   * @param listenerName name of listener
   * @return if successful
   */
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
  /**
   * tries to remove a target based on given inputs
   * @param sender command sender
   * @param listenerName name of listener
   * @return if successful
   */
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
  /**
   * checks and send to sender who they are listening to
   * @param sender command sender
   * @param listenerName name of listener
   * @return if successful
   */
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