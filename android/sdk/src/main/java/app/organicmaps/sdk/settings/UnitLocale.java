package app.organicmaps.sdk.settings;

import app.organicmaps.sdk.util.Config;

public class UnitLocale
{
  // This constants should be equal with platform/settings.hpp
  public static final int UNITS_UNDEFINED = -1;
  public static final int UNITS_METRIC = 0;
  public static final int UNITS_FOOT = 1;

  // Route 404 defaults to miles regardless of locale.
  private static int getDefaultUnits()
  {
    return UNITS_FOOT;
  }

  private static native int getCurrentUnits();

  private static native void setCurrentUnits(int u);

  public static int getUnits()
  {
    return getCurrentUnits();
  }

  public static void setUnits(int units)
  {
    setCurrentUnits(units);
  }

  public static void initializeCurrentUnits()
  {
    // One-time switch to miles for installs that predate the Route 404 default.
    if (!Config.isImperialDefaultApplied())
    {
      setCurrentUnits(getDefaultUnits());
      Config.setImperialDefaultApplied();
      return;
    }
    final int u = getCurrentUnits();
    setCurrentUnits(u == UNITS_UNDEFINED ? getDefaultUnits() : u);
  }
}
