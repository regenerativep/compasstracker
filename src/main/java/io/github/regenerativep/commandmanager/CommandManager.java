package io.github.regenerativep.commandmanager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class CommandManager
{
    public static final String[] POSSIBLE_TRUE_BOOLEAN_VALUES = { "true", "t", "on", "enable"};
    public static final String[] POSSIBLE_FALSE_BOOLEAN_VALUES = { "false", "f", "off", "disable"};
    public static final String NO_PERMISSION = "Insufficient permissions. (**reason**)";

    private JavaPlugin plugin;
    public List<CommandSpecifier> commands;
    public CommandManager(JavaPlugin plugin)
    {
        commands = new ArrayList<CommandSpecifier>();
        this.plugin = plugin;
    }
    public CommandSpecifier getFitCommand(String[] args)
    {
        for(int i = 0; i < commands.size(); i++)
        {
            CommandSpecifier cmd = commands.get(i);
            cmd.manager = this;
            if(cmd.fits(args))
            {
                return cmd;
            }
        }
        return null;
    }
    public boolean inputCommand(CommandSender sender, String[] args)
    {
        //get the command
        CommandSpecifier cmd = getFitCommand(args);
        //make sure we got a command
        if(cmd == null) return false;
        cmd.manager = this;
        //check for permissions
        if(cmd.requiredPermissions != null)
        {
            for(int i = 0; i < cmd.requiredPermissions.length; i++)
            {
                String perm = cmd.requiredPermissions[i];
                if(!sender.hasPermission(perm))
                {
                    sender.sendMessage(NO_PERMISSION.replace("**reason**", perm));
                    return false;
                }
            }
        }
        //convert string arguments into the desired types
        Object[] objArgs = new Object[args.length];
        for(int i = 0; i < objArgs.length; i++)
        {
            Object value = getValue(args[i], cmd.argTypes[i]);
            if(value == null) return false;
            objArgs[i] = value;
        }
        //call the command's function and return the result
        return cmd.func.call(sender, objArgs);
    }
    public boolean testString(String value)
    {
        return value.length() > 0;
    }
    public boolean testInteger(String value)
    {
        try
        {
            Integer.parseInt(value);
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }
    public boolean testFloat(String value)
    {
        try
        {
            Float.parseFloat(value);
        }
        catch(Exception e)
        {
            return false;
        }
        return true;
    }
    public boolean testBoolean(String value)
    {
        for(int i = 0; i < POSSIBLE_TRUE_BOOLEAN_VALUES.length; i++)
        {
            if(value.equals(POSSIBLE_TRUE_BOOLEAN_VALUES[i]))
            {
                return true;
            }
        }
        for(int i = 0; i < POSSIBLE_FALSE_BOOLEAN_VALUES.length; i++)
        {
            if(value.equals(POSSIBLE_FALSE_BOOLEAN_VALUES[i]))
            {
                return true;
            }
        }
        return false;
    }
    public boolean testValue(String value, CommandArgumentType type)
    {
        switch(type)
        {
            case STRING:
                return testString(value);
            case INTEGER:
                return testInteger(value);
            case FLOAT:
                return testFloat(value);
            case BOOLEAN:
                return testBoolean(value);
        }
        return false;
    }
    public Boolean getValueBoolean(String value)
    {
        for(int i = 0; i < POSSIBLE_TRUE_BOOLEAN_VALUES.length; i++)
        {
            if(value.equals(POSSIBLE_TRUE_BOOLEAN_VALUES[i]))
            {
                return true;
            }
        }
        return false;
    }
    public Object getValue(String value, CommandArgumentType type)
    {
        switch(type)
        {
            case STRING:
                return value;
            case INTEGER:
                return Integer.parseInt(value);
            case FLOAT:
                return Float.parseFloat(value);
            case BOOLEAN:
                return getValueBoolean(value);
        }
        return null;
    }
}