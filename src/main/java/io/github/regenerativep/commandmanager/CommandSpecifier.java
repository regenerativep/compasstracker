package io.github.regenerativep.commandmanager;

public class CommandSpecifier
{
    public Object[] values;
    public CommandArgumentType[] argTypes;
    public CommandFunction func;
    public String[] requiredPermissions;
    public CommandManager manager;
    public CommandSpecifier(Object[] values, CommandArgumentType[] args, CommandFunction func, String[] perms)
    {
        this.values = values;
        argTypes = args;
        this.func = func;
        requiredPermissions = perms;
    }
    public CommandSpecifier(Object[] values, CommandArgumentType[] args, CommandFunction func)
    {
        this(values, args, func, new String[] {} );
    }
    public boolean fits(String[] testValues)
    {
        //arg length must be the same
        if(testValues.length != argTypes.length) return false;
        //types must be the same
        for(int i = 0; i < testValues.length; i++)
        {
            String testValue = testValues[i];
            CommandArgumentType argType = argTypes[i];
            if(!manager.testValue(testValue, argType)) //todo: can store this value for later so that we dont have to recalculate it
            {
                return false;
            }
        }
        //initial values must be the same
        for(int i = 0; i < values.length; i++)
        {
            String testValue = testValues[i];
            CommandArgumentType argType = argTypes[i];
            Object testValueObj = manager.getValue(testValue, argType);
            Object correctValue = values[i];
            if(!testValueObj.equals(correctValue))
            {
                return false;
            }
        }
        return true;
    }
}