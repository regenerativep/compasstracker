package io.github.regenerativep.compasstracker;

import org.bukkit.scheduler.BukkitRunnable;

public class RegularUpdate extends BukkitRunnable
{
  private App app;
  public RegularUpdate(App app)
  {
    this.app = app;
  }
  @Override
  public void run()
  {
    app.updateListeners();
  }
}