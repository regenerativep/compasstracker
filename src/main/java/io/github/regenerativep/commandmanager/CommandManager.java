package io.github.regenerativep.commandmanager;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.command.CommandSender;

public class CommandManager
{
    public static final String[] POSSIBLE_TRUE_BOOLEAN_VALUES = { "true", "t", "on", "enable"};
    public static final String[] POSSIBLE_FALSE_BOOLEAN_VALUES = { "false", "f", "off", "disable"};
    private List<CommandSpecifier> commands;
    public CommandManager()
    {
        commands = new ArrayList<CommandSpecifier>();
    }
    public void registerCommand(Object[] values, CommandArgumentType[] args, CommandFunction func)
    {
        commands.add(new CommandSpecifier(values, args, func));
    }
    public CommandSpecifier getFitCommand(String[] args)
    {
        for(int i = 0; i < commands.size(); i++)
        {
            CommandSpecifier cmd = commands.get(i);
            if(cmd.fits(args))
            {
                return cmd;
            }
        }
        return null;
    }
    public boolean inputCommand(CommandSender sender, String[] args)
    {
        CommandSpecifier cmd = getFitCommand(args);
        if(cmd == null) return false;
        Object[] objArgs = new Object[args.length];
        for(int i = 0; i < objArgs.length; i++)
        {
            Object value = getValue(args[i], cmd.argTypes[i]);
            if(value == null) return false;
            objArgs[i] = value;
        }
        return cmd.func.call(sender, objArgs);
    }
    public static boolean testString(String value)
    {
        return value.length() > 0;
    }
    public static boolean testInteger(String value)
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
    public static boolean testFloat(String value)
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
    public static boolean testBoolean(String value)
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
    public static boolean testValue(String value, CommandArgumentType type)
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
    public static Boolean getValueBoolean(String value)
    {
        for(int i = 0; i < POSSIBLE_TRUE_BOOLEAN_VALUES.length; i++)
        {
            if(value.equals(POSSIBLE_TRUE_BOOLEAN_VALUES[i]))
            {
                return true;
            }
        }
        // for(int i = 0; i < POSSIBLE_FALSE_BOOLEAN_VALUES.length; i++)
        // {
        //     if(value.equals(POSSIBLE_FALSE_BOOLEAN_VALUES[i]))
        //     {
        //         return false;
        //     }
        // }
        return false;
    }
    public static Object getValue(String value, CommandArgumentType type)
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