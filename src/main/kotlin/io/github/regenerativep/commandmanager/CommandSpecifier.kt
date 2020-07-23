package io.github.regenerativep.commandmanager;

import org.bukkit.command.CommandSender;

enum class CommandArgumentType
{
    STRING, INTEGER, FLOAT, BOOLEAN, ENVIRONMENT
}
data class CommandSpecifier(val values: Array<Any>, val args: Array<CommandArgumentType>, val func: (CommandSender, Array<Any>) -> Boolean, val perms: Array<String> = arrayOf())