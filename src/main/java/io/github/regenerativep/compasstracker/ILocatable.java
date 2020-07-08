package io.github.regenerativep.compasstracker;

import org.bukkit.Location;

public interface ILocatable
{
    Location getLocation();
    String getLocationDescription();
    String getName();
}