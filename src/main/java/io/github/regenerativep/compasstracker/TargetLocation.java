package io.github.regenerativep.compasstracker;

import org.bukkit.Location;

public class TargetLocation implements ILocatable
{
    private Location location;
    public TargetLocation(Location loc)
    {
        this.location = loc;
    }
    @Override
    public Location getLocation()
    {
        return location;
    }

    @Override
    public String getLocationDescription()
    {
        return "location XZ: " + location.getBlockX() + ", " + location.getBlockZ();
    }

    @Override
    public String getName()
    {
        return "";
    }
    
}