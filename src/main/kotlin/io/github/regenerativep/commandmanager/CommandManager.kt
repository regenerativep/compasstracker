package io.github.regenerativep.commandmanager;

import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.World;
import org.bukkit.command.CommandSender;

val POSSIBLE_TRUE_BOOLEAN_VALUES: List<String> = listOf("true", "t", "on", "enable", "yes")
val POSSIBLE_FALSE_BOOLEAN_VALUES: List<String> = listOf("false", "f", "off", "disable", "no")
val NO_PERMISSION: String = "Insufficient permissions. (**reason**)";
fun getFitCommand(commands: List<CommandSpecifier>, args: Array<String>): CommandSpecifier?
{
    for(cmd in commands)
    {
        if(fits(cmd, args))
        {
            return cmd
        }
    }
    return null
}
fun inputCommand(commands: List<CommandSpecifier>, sender: CommandSender, args: Array<String>): Boolean
{
    val cmd = getFitCommand(commands, args)
    if(cmd == null) return false
    for(perm in cmd.perms)
    {
        if(!sender.hasPermission(perm))
        {
            sender.sendMessage(NO_PERMISSION.replace("**reason**", perm))
            return false
        }
    }
    val objArgs: Array<Any> = Array(args.size, { i -> getValue(args[i], cmd.args[i]) } )
    return cmd.func(sender, objArgs)
}
fun fits(cmd: CommandSpecifier, args: Array<String>): Boolean
{
    if(cmd.args.size != args.size) {
        return false
    }
    for(i in 0..(args.size - 1))
    {
        val testVal = args[i]
        val argType = cmd.args[i]
        if(!testValue(testVal, argType))
        {
            return false
        }
    }
    for(i in 0..(cmd.values.size - 1))
    {
        val testVal = args[i]
        val argType = cmd.args[i]
        val testValueObj = getValue(testVal, argType)
        val correctValue = cmd.values[i]
        if(testValueObj != correctValue)
        {
            return false
        }
    }
    return true
}
fun testValue(value: String, type: CommandArgumentType): Boolean
{
    return when(type)
    {
        CommandArgumentType.STRING -> value.length > 0
        CommandArgumentType.INTEGER -> value.toIntOrNull() != null
        CommandArgumentType.FLOAT -> value.toFloatOrNull() != null
        CommandArgumentType.BOOLEAN -> value.toBooleanOrNull() != null
        CommandArgumentType.ENVIRONMENT -> value.toEnvironmentOrNull() != null
    }
}
fun String.toBooleanOrNull(): Boolean?
{
    val text = this.toLowerCase()
    for(testValue in POSSIBLE_TRUE_BOOLEAN_VALUES)
    {
        if(testValue == text)
        {
            return true;
        }
    }
    for(testValue in POSSIBLE_FALSE_BOOLEAN_VALUES)
    {
        if(testValue == text)
        {
            return false;
        }
    }
    return null
}
fun String.toEnvironmentOrNull(): World.Environment?
{
    return when(this.toLowerCase())
    {
        "overworld", "surface", "normal", "default" -> World.Environment.NORMAL
        "nether", "underworld" -> World.Environment.NETHER
        "end", "the end", "the_end", "void" -> World.Environment.THE_END
        else -> null
    }
}
fun getValue(value: String, type: CommandArgumentType): Any
{
    return when(type)
    {
        CommandArgumentType.STRING -> value
        CommandArgumentType.INTEGER -> value.toIntOrNull() ?: -1
        CommandArgumentType.FLOAT -> value.toFloatOrNull() ?: -1
        CommandArgumentType.BOOLEAN -> value.toBooleanOrNull() ?: false
        CommandArgumentType.ENVIRONMENT -> value.toEnvironmentOrNull() ?: World.Environment.NORMAL
    }
}