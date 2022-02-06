/*
 * MIT License
 *
 * Copyright (c) Pieter Svenson
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do
 * so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
 * INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A
 * PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN
 * AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package dev.pietelite.journey.spigot.external.whimcportals;

import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.navigation.Port;
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.common.tools.Verifiable;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.util.SpigotUtil;
import edu.whimc.portals.Main;
import edu.whimc.portals.Portal;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Predicate;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.Nullable;

/**
 * A {@link Port} representing a {@link Portal} in the Portals plugin,
 * managed by {@link edu.whimc.portals.Main}.
 */
public class WhimcPortalPort extends Port<LocationCell, World> implements Verifiable {

  private final String portalName;

  private WhimcPortalPort(String name, LocationCell origin, LocationCell destination) {
    super(origin, destination, ModeType.PORT, 5);
    this.portalName = name;
  }

  /**
   * Static constructor, to create a port directly from a WHIMC portal.
   *
   * @param portal the portal
   * @return the generated port
   */
  private static WhimcPortalPort from(Portal portal) {
    if (portal.getWorld() == null) {
      throw new IllegalStateException("Error with portal: " + portal.getName()
          + "A Portal Link may only be created with portals that have a world.");
    }
    if (portal.getDestination() == null) {
      throw new IllegalStateException("Error with portal: " + portal.getName()
          + "A Portal Link may only be created with portals that have a destination.");
    }
    if (portal.getDestination().getLocation().getWorld() == null) {
      throw new IllegalStateException("Error with portal: " + portal.getName()
          + ". A Portal Link may only be created with portals"
          + " that have a world associated with its destination.");
    }

    LocationCell origin = getOriginOf(portal);
    if (origin == null) {
      throw new IllegalStateException("Error with portal: " + portal.getName()
          + ". A reachable central location could not be identified.");
    }

    LocationCell destination = getDestinationOf(portal);

    return new WhimcPortalPort(portal.getName(), origin, destination);
  }

  @Nullable
  private static LocationCell getOriginOf(Portal portal) {
    // Start by trying to use the center of the portal.
    int locX = (portal.getPos1().getBlockX() + portal.getPos2().getBlockX()) / 2;  // center of portal
    int locY = Math.min(portal.getPos1().getBlockY(), portal.getPos2().getBlockY());  // bottom of portal
    int locZ = (portal.getPos1().getBlockZ() + portal.getPos2().getBlockZ()) / 2;
    while (!SpigotUtil.isLaterallyPassable(portal.getWorld().getBlockAt(locX, locY, locZ))
        || !SpigotUtil.isPassable(portal.getWorld().getBlockAt(locX, locY + 1, locZ))) {
      locY++;
      if (locY > Math.max(portal.getPos1().getBlockY(), portal.getPos2().getBlockY())) {
        // There is no y value that works for the center of this portal.
        // Try every other point and see what sticks (this does not repeat)
        for (locX = portal.getPos1().getBlockX(); locX <= portal.getPos2().getBlockX(); locX++) {
          for (locY = portal.getPos1().getBlockY(); locY < portal.getPos2().getBlockY(); locY++) {
            for (locZ = portal.getPos1().getBlockZ(); locZ <= portal.getPos2().getBlockZ(); locZ++) {
              if (SpigotUtil.isLaterallyPassable(portal.getWorld().getBlockAt(locX, locY, locZ))
                  && SpigotUtil.isPassable(portal.getWorld().getBlockAt(locX, locY + 1, locZ))) {
                return new LocationCell(locX, locY, locZ, portal.getWorld());
              }
            }
          }
        }
        // Nothing at all found
        return null;
      }
    }
    // We found one at the center of the portal!
    return new LocationCell(locX, locY, locZ, portal.getWorld());
  }

  private static LocationCell getDestinationOf(Portal portal) {
    return new LocationCell(portal.getDestination().getLocation());
  }

  /**
   * Add all possible {@link WhimcPortalPort}s to a session.
   *
   * @param session          the session
   * @param permissionAccess the predicate that determines whether the cause of the session has permission
   *                         to use the port, which is determined by passing in the permission required
   *                         for the port to this "permissionAccess" predicate to test
   */
  public static void addPortsTo(SearchSession<LocationCell, World> session,
                                Predicate<String> permissionAccess) {
    Plugin plugin = Bukkit.getPluginManager().getPlugin("WHIMC-Portals");
    if (plugin instanceof Main) {
      Portal.getPortals().stream()
          .filter(portal -> portal.getDestination() != null)
          .filter(portal -> portal.getWorld() != null)
          .filter(portal -> portal.getDestination().getLocation().getWorld() != null)
          .filter(portal -> Optional.ofNullable(portal.getPermission()).map(perm ->
              permissionAccess.test(perm.getName())).orElse(true))
          .map(portal -> {
            try {
              return WhimcPortalPort.from(portal);
            } catch (Exception e) {
              return null;
            }
          })
          .filter(Objects::nonNull)
          .forEach(session::registerPort);
    }
  }

  @Override
  public boolean verify() {
    Portal portal = Portal.getPortal(portalName);
    if (portal == null) {
      return false;
    }
    return getOrigin().equals(getOriginOf(portal)) && getDestination().equals(getDestinationOf(portal));
  }

  @Override
  public String toString() {
    return "PortalLink{portalName='" + portalName + "'}";
  }

  @Override
  public boolean completeWith(LocationCell location) {
    Portal portal = Portal.getPortal(this.portalName);
    return Math.min(portal.getPos1().getBlockX(), portal.getPos2().getBlockX()) <= location.getX()
        && location.getX() <= Math.max(portal.getPos1().getBlockX(), portal.getPos2().getBlockX())
        && Math.min(portal.getPos1().getBlockY(), portal.getPos2().getBlockY()) <= location.getY()
        && location.getY() <= Math.max(portal.getPos1().getBlockY(), portal.getPos2().getBlockY())
        && Math.min(portal.getPos1().getBlockZ(), portal.getPos2().getBlockZ()) <= location.getZ()
        && location.getZ() <= Math.max(portal.getPos1().getBlockZ(), portal.getPos2().getBlockZ());
  }

}
