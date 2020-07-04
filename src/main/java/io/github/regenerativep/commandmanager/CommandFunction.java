package io.github.regenerativep.commandmanager;

import org.bukkit.command.CommandSender;

public interface CommandFunction
{
    boolean call(CommandSender sender, Object[] args);
}