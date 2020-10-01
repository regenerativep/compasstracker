package io.github.regenerativep.commandmanager

import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.World
import org.bukkit.command.CommandSender

/**
 * A list of strings that map to true
 */
val POSSIBLE_TRUE_BOOLEAN_VALUES: List<String> = listOf("true", "t", "on", "enable", "y", "yes", "allow")
/**
 * A list of strings that map to false
 */
val POSSIBLE_FALSE_BOOLEAN_VALUES: List<String> = listOf("false", "f", "off", "disable", "n", "no", "disallow")
/**
 * Message for when the command caller does not have permission.
 */
const val NO_PERMISSION: String = "Insufficient permissions. (**reason**)";

/**
 * Types of command arguments
 */
enum class CommandArgumentType {
    STRING, INTEGER, FLOAT, BOOLEAN, ENVIRONMENT
}
/**
 * Data to recognize and call command
 * 
 * @property values Initial values required to trigger the function
 * @property args Argument types required to trigger the function
 * @property func The function to call when the command is triggered
 * @property perms A list of permissions required to run the command
 */
data class CommandSpecifier(
    val values: Array<Any>,
    val args: Array<CommandArgumentType>,
    val func: (CommandSender, Array<Any>) -> Boolean,
    val perms: Array<String> = arrayOf()
) {
    /**
     * Checks if the given input arguments fits this command
     * 
     * @param inpArgs List of inputs to compare against
     * @return If the given input arguments fits this command
     */
    fun fits(inpArgs: Array<String>): Boolean {
        return this.args.size == inpArgs.size
            && inpArgs.mapIndexed { ind, value -> Pair(ind, value) }
                .find { (ind, value) -> !testValue(value, this.args[ind]) } == null
            && this.values.mapIndexed { ind, value -> Pair(ind, value) }
                .find { (ind, value) -> getValue(inpArgs[ind], this.args[ind]) != value } == null
    }
}
/**
 * Gets a string describing the usage of a command
 * 
 * @param cmd The command
 * @param cmdName The initial command to trigger the given sub-command
 * @return A string describing the usage of the given command
 */
fun getUsage(cmd: CommandSpecifier, cmdName: String): String {
    return "/${cmdName} " + cmd.args.mapIndexed { i, cmdType -> 
        if(i < cmd.values.size) {
            cmd.values.get(i).toString()
        }
        else {
            "[" + cmdType.toString() + "]"
        }
    }.reduce { a, b -> a + " " + b }
}
/**
 * Inputs a command into the given command list
 * 
 * @param commands List of commands to check from
 * @param sender The sender of the command
 * @param args The list of args sent by the sender
 * @param cmdName The initial command used to trigger this
 * @return If the command was found
 */
fun inputCommand(commands: List<CommandSpecifier>, sender: CommandSender, args: Array<String>, cmdName: String): Boolean {
    return (commands + CommandSpecifier(
        arrayOf("help"), arrayOf(CommandArgumentType.STRING), { sender, _ ->
            sender.sendMessage(commands.map { cmd -> getUsage(cmd, cmdName) }.reduce { a, b -> a + "\n" + b})
            true
        }
    )).find { it.fits(args) }?.let { cmd ->
        cmd.perms.find { !sender.hasPermission(it) }?.let {
            sender.sendMessage(NO_PERMISSION.replace("**reason**", it))
            false
        }
        ?: cmd.func(sender, Array(args.size, { i -> getValue(args[i], cmd.args[i]) } )).let {
            if(!it) {
                sender.sendMessage("Usage: " + getUsage(cmd, cmdName))
            }
            true
        }
    } ?: false
}
/**
 * Tests if an argument value matches the given argument type
 * 
 * @param value The value to test
 * @param type The type to test for
 * @return If the given argument value is the given type
 */
fun testValue(value: String, type: CommandArgumentType): Boolean {
    return when(type) {
        CommandArgumentType.STRING -> value.length > 0
        CommandArgumentType.INTEGER -> value.toIntOrNull() != null
        CommandArgumentType.FLOAT -> value.toFloatOrNull() != null
        CommandArgumentType.BOOLEAN -> value.toBooleanOrNull() != null
        CommandArgumentType.ENVIRONMENT -> value.toEnvironmentOrNull() != null
    }
}
/**
 * Converts a string into a boolean
 * 
 * @return A boolean value, or null if not detected to be a boolean value
 */
fun String.toBooleanOrNull(): Boolean? {
    return this.toLowerCase().let { text ->
        if(text in POSSIBLE_TRUE_BOOLEAN_VALUES) {
            true
        }
        else if (text in POSSIBLE_FALSE_BOOLEAN_VALUES) {
            false
        }
        else {
            null
        }
    }
}
/**
 * Converts a string into an environment
 * 
 * @return A World.Environment value, or null if not detected to be one
 */
fun String.toEnvironmentOrNull(): World.Environment? {
    return when(this.toLowerCase().split(":").last()) {
        "overworld", "surface", "normal", "default" -> World.Environment.NORMAL
        "nether", "underworld" -> World.Environment.NETHER
        "end", "the end", "the_end", "void" -> World.Environment.THE_END
        else -> null
    }
}
/**
 * Converts the given argument value into the desired type
 * 
 * @param value The value to convert
 * @param type The type to convert the value to
 * @return The converted value
 */
fun getValue(value: String, type: CommandArgumentType): Any {
    return when(type) {
        CommandArgumentType.STRING -> value
        CommandArgumentType.INTEGER -> value.toIntOrNull() ?: -1
        CommandArgumentType.FLOAT -> value.toFloatOrNull() ?: -1
        CommandArgumentType.BOOLEAN -> value.toBooleanOrNull() ?: false
        CommandArgumentType.ENVIRONMENT -> value.toEnvironmentOrNull() ?: World.Environment.NORMAL
    }
}