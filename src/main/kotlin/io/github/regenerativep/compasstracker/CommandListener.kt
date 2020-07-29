package io.github.regenerativep.compasstracker;

import io.github.regenerativep.commandmanager.CommandSpecifier;
import io.github.regenerativep.commandmanager.CommandArgumentType;
import io.github.regenerativep.commandmanager.inputCommand;
import io.github.regenerativep.compasstracker.giveCompass;

import org.bukkit.command.CommandSender;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.World;
import org.bukkit.entity.Player;

val PERM_GIVE_SELF = "ctrack.give.self"
val PERM_GIVE_ANY = "ctrack.give"
val PERM_MANAGE_TARGETS = "ctrack.target"
val PERM_MANAGE_AUTOGIVE = "ctrack.autogive"
val PERM_MANAGE_AUTOTARGET = "ctrack.autotarget"
val PERM_MANAGE_ENVIRONMENT = "ctrack.environment"
val PERM_MANAGE_TICKRATE = "ctrack.tickrate"

fun errTargetExists(sender: CommandSender, targetName: String)
{
    sender.sendMessage("Error: Target \"${targetName}\" already exists.")
}
fun errTargetDoesNotExist(sender: CommandSender, targetName: String)
{
    sender.sendMessage("Error: Target \"${targetName}\" doesn't exist.")
}
fun errPlayerDoesNotExist(sender: CommandSender, targetName: String)
{
    sender.sendMessage("Error: Player \"${targetName}\" doesn't exist.")
}
fun errYouAreNotTarget(sender: CommandSender)
{
    sender.sendMessage("Error: You are not a target.")
}
fun errTargetHasCompass(sender: CommandSender, targetName: String)
{
    sender.sendMessage("Error: Target \"${targetName}\" already has a compass.")
}
fun errYouHaveCompass(sender: CommandSender)
{
    sender.sendMessage("Error: You already have a compass.")
}
fun errYouNotPlayer(sender: CommandSender)
{
    sender.sendMessage("Error: You are not a player.")
}
fun warnTargetNotInServer(sender: CommandSender, targetName: String)
{
    sender.sendMessage("Warning: Target \"${targetName}\" is not in the server.")
}
class CommandListener(val app: CompassTracker) : CommandExecutor
{
    val commands: List<CommandSpecifier> = listOf(
        CommandSpecifier(
            arrayOf("target"), arrayOf(CommandArgumentType.STRING), {
                sender, _ ->
                if(sender is Player)
                {
                    app.addTarget(sender.name)
                }
                else
                {
                    errYouNotPlayer(sender)
                }
                true
            }, arrayOf(PERM_MANAGE_TARGETS)
        ),
        CommandSpecifier(
            arrayOf("target"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING), {
                sender, args ->
                val targetName = args[1]
                if(targetName is String)
                {
                    app.addTarget(targetName)
                    if(!playerExists(targetName))
                    {
                        warnTargetNotInServer(sender, targetName)
                    }
                }
                true
            }, arrayOf(PERM_MANAGE_TARGETS)
        ),
        CommandSpecifier(
            arrayOf("removetarget"), arrayOf(CommandArgumentType.STRING), {
                sender, _ ->
                if(sender is Player)
                {
                    if(!app.removeTarget(sender.name))
                    {
                        errYouAreNotTarget(sender)
                    }
                }
                else
                {
                    errYouNotPlayer(sender)
                }
                true
            }, arrayOf(PERM_MANAGE_TARGETS)
        ),
        CommandSpecifier(
            arrayOf("removetarget"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING), {
                sender, args ->
                val targetName = args[1]
                if(targetName is String)
                {
                    if(!app.removeTarget(targetName))
                    {
                        errTargetDoesNotExist(sender, targetName)
                    }
                }
                true
            }, arrayOf(PERM_MANAGE_TARGETS)
        ),
        CommandSpecifier(
            arrayOf("targetlist"), arrayOf(CommandArgumentType.STRING), {
                sender, _ ->
                var message: String
                if(app.targets.size > 0)
                {
                    val keys = app.targets.keys.toTypedArray()
                    message = "Targets: ${keys[0]}"
                    for(i in 1..(keys.size - 1))
                    {
                        message += ", ${keys[i]}"
                    }
                }
                else
                {
                    message = "No targets."
                }
                sender.sendMessage(message);
                true
            }
        ),
        CommandSpecifier(
            arrayOf("give"), arrayOf(CommandArgumentType.STRING), {
                sender, _ ->
                if(sender is Player)
                {
                    if(!giveCompass(sender))
                    {
                        errYouHaveCompass(sender)
                    }
                }
                else
                {
                    errYouNotPlayer(sender)
                }
                true
            }, arrayOf(PERM_GIVE_SELF)
        ),
        CommandSpecifier(
            arrayOf("give"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING), {
                sender, args ->
                val targetName = args[1]
                if(targetName is String)
                {
                    val player = app.server.getPlayerExact(targetName)
                    if(player == null)
                    {
                        errPlayerDoesNotExist(sender, targetName)
                    }
                    else if(!giveCompass(player))
                    {
                        errTargetHasCompass(sender, targetName)
                    }
                }
                true
            }, arrayOf(PERM_GIVE_ANY)
        ),
        CommandSpecifier(
            arrayOf("environment"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.BOOLEAN), {
                sender, args ->
                if(sender is Player)
                {
                    val environment = sender.location.world?.environment
                    val enabled = args[1]
                    if(environment is World.Environment && enabled is Boolean)
                    {
                        app.setEnvironment(environment, enabled)
                    }
                }
                else
                {
                    errYouNotPlayer(sender)
                }
                true
            }, arrayOf(PERM_MANAGE_ENVIRONMENT)
        ),
        CommandSpecifier(
            arrayOf("environment"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.BOOLEAN, CommandArgumentType.ENVIRONMENT), {
                _, args ->
                val environment = args[2]
                val enabled = args[1]
                if(environment is World.Environment && enabled is Boolean)
                {
                    app.setEnvironment(environment, enabled)
                }
                true
            }, arrayOf(PERM_MANAGE_ENVIRONMENT)
        ),
        CommandSpecifier(
            arrayOf("environment"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.ENVIRONMENT, CommandArgumentType.BOOLEAN), {
                _, args ->
                val environment = args[1]
                val enabled = args[2]
                if(environment is World.Environment && enabled is Boolean)
                {
                    app.setEnvironment(environment, enabled)
                }
                true
            }, arrayOf(PERM_MANAGE_ENVIRONMENT)
        ),
        CommandSpecifier(
            arrayOf("environmentlist"), arrayOf(CommandArgumentType.STRING), {
                sender, _ ->
                var message: String
                if(app.targets.size > 0)
                {
                    val envs = app.permittedEnvironments
                    message = "Trackable environments: ${envs[0]}"
                    for(i in 1..(envs.size - 1))
                    {
                        message += ", ${envs[i]}"
                    }
                }
                else
                {
                    message = "No trackable environments."
                }
                sender.sendMessage(message);
                true
            }
        ),
        CommandSpecifier(
            arrayOf("who"), arrayOf(CommandArgumentType.STRING), {
                sender, _ ->
                if(sender is Player)
                {
                    val targetName = app.listeners.get(sender.name)?.targetName
                    if(targetName != null)
                    {
                        sender.sendMessage("You are tracking \"${targetName}\".")
                    }
                    else
                    {
                        sender.sendMessage("You are not tracking anyone.")
                    }
                }
                else
                {
                    errYouNotPlayer(sender)
                }
                true
            }
        ),
        CommandSpecifier(
            arrayOf("who"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING), {
                sender, args ->
                val listenerName = args[1]
                if(listenerName is String)
                {
                    val targetName = app.listeners.get(listenerName)?.targetName
                    if(targetName != null)
                    {
                        sender.sendMessage("Player \"${listenerName}\" is tracking \"${targetName}\".")
                    }
                    else
                    {
                        sender.sendMessage("Player \"${listenerName}\" is not tracking anyone.")
                    }
                }
                true
            }
        ),
        CommandSpecifier(
            arrayOf("track"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING), {
                sender, args ->
                if(sender is Player)
                {
                    val targetName = args[1]
                    if(targetName is String)
                    {
                        if(targetName in app.targets.keys)
                        {
                            app.setTarget(sender.name, targetName)
                        }
                        else
                        {
                            errTargetDoesNotExist(sender, targetName)
                        }
                    }
                }
                else
                {
                    errYouNotPlayer(sender)
                }
                true
            }
        ),
        CommandSpecifier(
            arrayOf("track"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING, CommandArgumentType.STRING), {
                sender, args ->
                val targetName = args[1]
                val listenerName = args[2]
                if(targetName is String && listenerName is String)
                {
                    if(!playerExists(listenerName))
                    {
                        errPlayerDoesNotExist(sender, listenerName)
                    }
                    else if(targetName in app.targets.keys)
                    {
                        app.setTarget(listenerName, targetName)
                    }
                    else
                    {
                        errTargetDoesNotExist(sender, targetName)
                    }
                }
                true
            }
        ),
        CommandSpecifier(
            arrayOf("autogive"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.BOOLEAN), {
                sender, args ->
                val enabled = args[1]
                if(enabled is Boolean)
                {
                    app.setAutoGive(enabled)
                }
                true
            }, arrayOf(PERM_MANAGE_AUTOGIVE)
        ),
        CommandSpecifier(
            arrayOf("tickrate"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.INTEGER), {
                sender, args ->
                val ticks = args[1]
                if(ticks is Int)
                {
                    app.runUpdateTimer(ticks.toLong())
                }
                true
            }, arrayOf(PERM_MANAGE_TICKRATE)
        ),
        CommandSpecifier(
            arrayOf("autotarget"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.BOOLEAN), {
                sender, args ->
                val enabled = args[1]
                if(enabled is Boolean)
                {
                    app.autoTarget = enabled
                }
                true
            }, arrayOf(PERM_MANAGE_AUTOTARGET)
        )
    )
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean
    {
        return inputCommand(commands, sender, args)
    }
    fun playerExists(targetName: String): Boolean {
        return app.server.getPlayerExact(targetName) != null
    }
}