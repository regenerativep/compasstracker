package io.github.regenerativep.commandmanager;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

val POSSIBLE_TRUE_BOOLEAN_VALUES: List<String> = listOf("true", "t", "on", "enable", "yes", "allow")
val POSSIBLE_FALSE_BOOLEAN_VALUES: List<String> = listOf("false", "f", "off", "disable", "no", "disallow")
const val NO_PERMISSION: String = "Insufficient permissions. (**reason**)";

enum class CommandArgumentType
{
    STRING, INTEGER, FLOAT, BOOLEAN, ENVIRONMENT
}
data class CommandSpecifier(val values: Array<Any>, val args: Array<CommandArgumentType>, val func: (CommandSender, Array<Any>) -> Boolean, val perms: Array<String> = arrayOf()) {
    fun fits(inpArgs: Array<String>): Boolean {
        return this.args.size == inpArgs.size
            && inpArgs.mapIndexed { ind, value
                -> Pair(ind, value)
            }
            .find { (ind, value)
                -> !testValue(value, this.args[ind])
            } == null
            && this.values.mapIndexed { ind, value
                -> Pair(ind, value)
            }
            .find { (ind, value)
                -> getValue(inpArgs[ind], this.args[ind]) != value
            } == null
    }
}

fun inputCommand(commands: List<CommandSpecifier>, sender: CommandSender, args: Array<String>): Boolean {
    return commands.find { it.fits(args) }?.let { cmd
        -> cmd.perms.find { !sender.hasPermission(it) }?.let {
            sender.sendMessage(NO_PERMISSION.replace("**reason**", it))
            false
        }
        ?: cmd.func(sender, Array(args.size, { i -> getValue(args[i], cmd.args[i]) } ))
    } ?: false
}
fun testValue(value: String, type: CommandArgumentType): Boolean {
    return when(type) {
        CommandArgumentType.STRING -> value.length > 0
        CommandArgumentType.INTEGER -> value.toIntOrNull() != null
        CommandArgumentType.FLOAT -> value.toFloatOrNull() != null
        CommandArgumentType.BOOLEAN -> value.toBooleanOrNull() != null
        CommandArgumentType.ENVIRONMENT -> value.toEnvironmentOrNull() != null
    }
}
fun String.toBooleanOrNull(): Boolean? {
    return this.toLowerCase().let { text
        -> if(text in POSSIBLE_TRUE_BOOLEAN_VALUES) {
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
fun String.toEnvironmentOrNull(): World.Environment? {
    return when(this.toLowerCase().split(":").last()) {
        "overworld", "surface", "normal", "default" -> World.Environment.NORMAL
        "nether", "underworld" -> World.Environment.NETHER
        "end", "the end", "the_end", "void" -> World.Environment.THE_END
        else -> null
    }
}
fun getValue(value: String, type: CommandArgumentType): Any {
    return when(type) {
        CommandArgumentType.STRING -> value
        CommandArgumentType.INTEGER -> value.toIntOrNull() ?: -1
        CommandArgumentType.FLOAT -> value.toFloatOrNull() ?: -1
        CommandArgumentType.BOOLEAN -> value.toBooleanOrNull() ?: false
        CommandArgumentType.ENVIRONMENT -> value.toEnvironmentOrNull() ?: World.Environment.NORMAL
    }
}