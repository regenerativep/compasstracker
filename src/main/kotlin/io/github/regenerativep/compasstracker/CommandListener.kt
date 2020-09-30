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

fun errTargetExists(sender: CommandSender, targetName: String): Boolean {
    sender.sendMessage("Error: Target \"${targetName}\" already exists.")
    return false
}
fun errTargetDoesNotExist(sender: CommandSender, targetName: String): Boolean {
    sender.sendMessage("Error: Target \"${targetName}\" doesn't exist.")
    return false
}
fun errPlayerDoesNotExist(sender: CommandSender, targetName: String): Boolean {
    sender.sendMessage("Error: Player \"${targetName}\" doesn't exist.")
    return false
}
fun errYouAreNotTarget(sender: CommandSender): Boolean {
    sender.sendMessage("Error: You are not a target.")
    return false
}
fun errTargetHasCompass(sender: CommandSender, targetName: String): Boolean {
    sender.sendMessage("Error: Target \"${targetName}\" already has a compass.")
    return false
}
fun errYouHaveCompass(sender: CommandSender): Boolean {
    sender.sendMessage("Error: You already have a compass.")
    return false
}
fun errYouNotPlayer(sender: CommandSender): Boolean {
    sender.sendMessage("Error: You are not a player.")
    return false
}
fun errIndeterminableEnvironment(sender: CommandSender): Boolean {
    sender.sendMessage("Error: Cannot determine your environment.")
    return false
}
fun warnTargetNotInServer(sender: CommandSender, targetName: String): Boolean {
    sender.sendMessage("Warning: Target \"${targetName}\" is not in the server.")
    return false
}
fun sucTargetedPlayer(sender: CommandSender, targetName: String): Boolean {
    sender.sendMessage("Success: Player \"${targetName}\" has been added as a target.")
    return true
}
fun sucRetargetedPlayer(sender: CommandSender, targetName: String): Boolean {
    sender.sendMessage("Success: Player \"${targetName}\" has been re-added as a target.")
    return true
}
fun sucTargetedSelf(sender: CommandSender): Boolean {
    sender.sendMessage("Success: You have been added as a target.")
    return true
}
fun sucRetargetedSelf(sender: CommandSender): Boolean {
    sender.sendMessage("Success: You have been re-added as a target.")
    return true
}
fun sucRemovedTarget(sender: CommandSender, targetName: String): Boolean {
    sender.sendMessage("Success: Player \"${targetName}\" has been removed from the targets.")
    return true
}
fun sucRemovedTargetSelf(sender: CommandSender): Boolean {
    sender.sendMessage("Success: You have been removed from the targets.")
    return true
}
fun sucGaveCompassSelf(sender: CommandSender): Boolean {
    sender.sendMessage("Success: Gave you a compass.")
    return true
}
fun sucGaveCompass(sender: CommandSender, playerName: String): Boolean {
    sender.sendMessage("Success: Gave player \"${playerName}\" a compass.")
    return true
}
fun sucSetEnvironment(sender: CommandSender, environment: World.Environment, status: Boolean): Boolean {
    sender.sendMessage("Success: Set the environment \"${environment}\" to ${status}")
    return true
}
fun sucNowTrackingSelf(sender: CommandSender, targetName: String): Boolean {
    sender.sendMessage("Success: You are now tracking player \"${targetName}\".")
    return true
}
fun sucNowTracking(sender: CommandSender, targetName: String, listenerName: String): Boolean {
    sender.sendMessage("Success: Player \"${listenerName}\" is now tracking player \"${targetName}\".")
    return true
}
fun sucAutoGiveStatus(sender: CommandSender, status: Boolean): Boolean {
    sender.sendMessage("Success: Automatically giving compasses to players has been set to ${status}.")
    return true
}
fun sucChangeTickRate(sender: CommandSender, tickrate: Long): Boolean {
    sender.sendMessage("Success: Set the tick amount between compass updates to ${tickrate}")
    return true
}
fun sucAutoTargetStatus(sender: CommandSender, status: Boolean): Boolean {
    sender.sendMessage("Success: Automatically targeting players as they join has been set to ${status}.")
    return true
}

class CommandListener(val app: CompassTracker, val cmdName: String) : CommandExecutor {
    val commands: List<CommandSpecifier> = listOf(
        CommandSpecifier(
            arrayOf("target"), arrayOf(CommandArgumentType.STRING), { sender, _
                -> if(sender is Player) {
                    if(sender.name in app.targets.keys) {
                        sucRetargetedSelf(sender)
                    }
                    else {
                        sucTargetedSelf(sender)
                    }.let { res ->
                        app.targets.set(sender.name, TargetListener(sender.name))
                        res
                    }
                }
                else {
                    errYouNotPlayer(sender)
                }
            }, arrayOf(PERM_MANAGE_TARGETS)
        ),
        CommandSpecifier(
            arrayOf("target"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING), { sender, args
                -> (args[1] as String).let { targetName
                    -> if(!playerExists(targetName)) {
                        warnTargetNotInServer(sender, targetName)
                    }
                    if(targetName in app.targets.keys) {
                        sucRetargetedPlayer(sender, targetName)
                    }
                    else {
                        sucTargetedPlayer(sender, targetName)
                    }.let{ res ->
                        app.targets.set(targetName, TargetListener(targetName))
                        res
                    }
                }
            }, arrayOf(PERM_MANAGE_TARGETS)
        ),
        CommandSpecifier(
            arrayOf("removetarget"), arrayOf(CommandArgumentType.STRING), { sender, _
                -> if(sender is Player) {
                    if(app.targets.remove(sender.name) == null) {
                        errYouAreNotTarget(sender)
                    }
                    else {
                        sucRemovedTargetSelf(sender)
                    }
                }
                else {
                    errYouNotPlayer(sender)
                }
            }, arrayOf(PERM_MANAGE_TARGETS)
        ),
        CommandSpecifier(
            arrayOf("removetarget"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING), { sender, args
                -> (args[1] as String).let { targetName
                    -> if(app.targets.remove(targetName) == null) {
                        errTargetDoesNotExist(sender, targetName)
                    }
                    else {
                        sucRemovedTarget(sender, targetName)
                    }
                }
            }, arrayOf(PERM_MANAGE_TARGETS)
        ),
        CommandSpecifier(
            arrayOf("targetall"), arrayOf(CommandArgumentType.STRING), { sender, _ ->
                val targetedPlayers = app.server.onlinePlayers.filter { it.name !in app.targets.keys }.map { it -> it.name }
                targetedPlayers.forEach { name ->
                    app.targets.set(name, TargetListener(name))
                }
                sender.sendMessage(
                    if(targetedPlayers.size > 0) {
                        targetedPlayers.reduce { a, b -> a + ", " + b }.let { "Targeted ${it}" }
                    }
                    else {
                        "Did not target anyone new."
                    }
                )
                true
            }, arrayOf(PERM_MANAGE_TARGETS)
        ),
        CommandSpecifier(
            arrayOf("cleartargets"), arrayOf(CommandArgumentType.STRING), { sender, _ ->
                val targetedPlayers = app.targets.keys.toTypedArray()
                targetedPlayers.forEach { name ->
                    app.targets.remove(name)
                }
                sender.sendMessage(
                    if(targetedPlayers.size > 0) {
                        targetedPlayers.reduce { a, b -> a + ", " + b }.let { "Removed ${it} from targets" }
                    }
                    else {
                        "Did not remove any new targets."
                    }
                )
                true
            }, arrayOf(PERM_MANAGE_TARGETS)
        ),
        CommandSpecifier(
            arrayOf("targetlist"), arrayOf(CommandArgumentType.STRING), { sender, _
                -> sender.sendMessage(
                    if(app.targets.size > 0) {
                        app.targets.keys.reduce { a, b -> a + ", " + b }.let { "Targets: ${it}" }
                    }
                    else {
                        "No targets."
                    }
                )
                true
            }
        ),
        CommandSpecifier(
            arrayOf("give"), arrayOf(CommandArgumentType.STRING), { sender, _ ->
                if(sender is Player) {
                    if(!sender.giveCompass(app)) {
                        errYouHaveCompass(sender)
                    }
                    else {
                        sucGaveCompassSelf(sender)
                    }
                }
                else {
                    errYouNotPlayer(sender)
                }
            }, arrayOf(PERM_GIVE_SELF)
        ),
        CommandSpecifier(
            arrayOf("give"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING), { sender, args ->
                (args[1] as String).let { targetName ->
                    app.server.getPlayerExact(targetName)?.let { player ->
                        if(!player.giveCompass(app)) {
                            errTargetHasCompass(sender, targetName)
                        }
                        else {
                            sucGaveCompass(sender, targetName)
                        }
                    } ?: errPlayerDoesNotExist(sender, targetName)
                }
            }, arrayOf(PERM_GIVE_ANY)
        ),
        CommandSpecifier(
            arrayOf("giveall"), arrayOf(CommandArgumentType.STRING), { sender, _ ->
                val playersToGive = this.app.server.onlinePlayers.filter { player -> player.giveCompass(this.app) }.map { it.name }
                sender.sendMessage("Success: " +
                    if(playersToGive.size > 0) {
                        playersToGive.reduce { a, b -> a + ", " + b }.let { "Gave compasses to ${it}" }
                    }
                    else {
                        "Did not give anyone any compasses."
                    }
                )
                true
            }, arrayOf(PERM_GIVE_ANY)
        ),
        CommandSpecifier(
            arrayOf("environment"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.BOOLEAN), { sender, args
                -> (args[1] as Boolean).let { enabled
                    -> if(sender is Player) {
                        sender.location.world?.environment?.let { env
                            -> app.setEnvironment(env, enabled)
                            sucSetEnvironment(sender, env, enabled)
                        }
                        ?: errIndeterminableEnvironment(sender)
                    }
                    else {
                        errYouNotPlayer(sender)
                    }
                }
            }, arrayOf(PERM_MANAGE_ENVIRONMENT)
        ),
        CommandSpecifier(
            arrayOf("environment"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.BOOLEAN, CommandArgumentType.ENVIRONMENT), { sender, args
                -> (args[1] as Boolean).let { enabled
                    -> (args[2] as World.Environment).let { env
                        -> app.setEnvironment(env, enabled)
                        sucSetEnvironment(sender, env, enabled)
                    }
                }
            }, arrayOf(PERM_MANAGE_ENVIRONMENT)
        ),
        CommandSpecifier(
            arrayOf("environment"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.ENVIRONMENT, CommandArgumentType.BOOLEAN), { sender, args
                -> (args[2] as Boolean).let { enabled
                    -> (args[1] as World.Environment).let { env
                        -> app.setEnvironment(env, enabled)
                        sucSetEnvironment(sender, env, enabled)
                    }
                }
            }, arrayOf(PERM_MANAGE_ENVIRONMENT)
        ),
        CommandSpecifier(
            arrayOf("environmentlist"), arrayOf(CommandArgumentType.STRING), { sender, _
                -> sender.sendMessage(
                    if(app.permittedEnvironments.size > 0) {
                        app.permittedEnvironments.map { " ${it}" }.reduce { a, b -> a + b }.let { "Trackable environments: ${it}" }
                    }
                    else {
                        "No trackable environments."
                    }
                )
                true
            }
        ),
        CommandSpecifier(
            arrayOf("who"), arrayOf(CommandArgumentType.STRING), { sender, _
                -> if(sender is Player) {
                    app.listeners.get(sender.name)?.targetName?.let { targetName
                        -> sender.sendMessage("You are tracking \"${targetName}\".")
                    }
                    ?: sender.sendMessage("You are not tracking anyone.")
                    true
                }
                else {
                    errYouNotPlayer(sender)
                }
            }
        ),
        CommandSpecifier(
            arrayOf("who"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING), { sender, args
                -> (args[1] as String).let { listenerName
                    -> app.listeners.get(listenerName)?.targetName?.let { targetName
                        -> sender.sendMessage("Player \"${listenerName}\" is tracking \"${targetName}\".")
                    }
                    ?: sender.sendMessage("Player \"${listenerName}\" is not tracking anyone.")
                    true
                }
            }
        ),
        CommandSpecifier(
            arrayOf("track"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING), { sender, args
                -> (args[1] as String).let { targetName
                    -> if(sender is Player) {
                        if(targetName in app.targets.keys) {
                            app.setTarget(sender.name, targetName)
                            sucNowTrackingSelf(sender, targetName)
                        }
                        else {
                            errTargetDoesNotExist(sender, targetName)
                        }
                    }
                    else {
                        errTargetDoesNotExist(sender, targetName)
                    }
                }
            }
        ),
        CommandSpecifier(
            arrayOf("track"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.STRING, CommandArgumentType.STRING), { sender, args
                -> (args[1] as String).let { targetName
                    -> (args[2] as String).let { listenerName
                        -> if(app.server.getPlayerExact(listenerName) != null) {
                            if(targetName in app.targets.keys) {
                                app.setTarget(listenerName, targetName)
                                sucNowTracking(sender, targetName, listenerName)
                            }
                            else {
                                errTargetDoesNotExist(sender, targetName)
                            }
                        }
                        else {
                            errPlayerDoesNotExist(sender, listenerName)
                        }
                    }
                }
            }, arrayOf(PERM_MANAGE_TARGETS)
        ),
        CommandSpecifier(
            arrayOf("autogive"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.BOOLEAN), { sender, args
                -> (args[1] as Boolean).let { enabled
                    -> if(enabled) {
                        app.server.onlinePlayers.forEach { it.giveCompass(app) }
                    }
                    app.autoGiveCompass = enabled
                    sucAutoGiveStatus(sender, enabled)
                }
            }, arrayOf(PERM_MANAGE_AUTOGIVE)
        ),
        CommandSpecifier(
            arrayOf("tickrate"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.INTEGER), { sender, args
                -> (args[1] as Int).toLong().let { ticks
                    -> app.runUpdateTimer(ticks)
                    sucChangeTickRate(sender, ticks)
                }
            }, arrayOf(PERM_MANAGE_TICKRATE)
        ),
        CommandSpecifier(
            arrayOf("autotarget"), arrayOf(CommandArgumentType.STRING, CommandArgumentType.BOOLEAN), { sender, args
                -> (args[1] as Boolean).let { enabled
                    -> app.autoTarget = enabled
                    sucAutoTargetStatus(sender, enabled)
                }
            }, arrayOf(PERM_MANAGE_AUTOTARGET)
        )
    )
    override fun onCommand(sender: CommandSender, command: Command, label: String, args: Array<String>): Boolean {
        return inputCommand(this.commands, sender, args, this.cmdName)
    }
    fun playerExists(targetName: String): Boolean {
        return this.app.server.getPlayerExact(targetName) != null
    }
}