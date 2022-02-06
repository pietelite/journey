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

package dev.pietelite.journey.spigot.manager;

import dev.pietelite.journey.spigot.JourneySpigot;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.navigation.NetherPort;
import dev.pietelite.journey.spigot.util.Format;
import dev.pietelite.journey.spigot.util.NetherUtil;
import java.io.Serializable;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPortalEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.world.PortalCreateEvent;

/**
 * A manager for all nether portals.
 */
public class NetherManager implements Listener, Serializable {

  public static final String NETHER_MANAGER_CACHE_FILE_NAME = "netherports.ser";
  private final Map<LocationCell, LocationCell> portalConnections = new ConcurrentHashMap<>();

  /**
   * Create ports specifically representing all nether portals in the world.
   *
   * @return all nether ports
   */
  public Collection<NetherPort> makePorts() {
    List<NetherPort> linksUnverified = portalConnections.entrySet().stream()
        .map(entry -> new NetherPort(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
    List<NetherPort> linksVerified = new LinkedList<>();
    for (NetherPort port : linksUnverified) {
      if (port.verify()) {
        linksVerified.add(port);
      } else {
        portalConnections.remove(port.getOrigin(), port.getDestination());
      }
    }
    return linksVerified;
  }

  /**
   * An event handler for when portals are created.
   * Links should be cached if the calculator determines a match has been found.
   *
   * @param e the event
   */
  @EventHandler(priority = EventPriority.LOW)
  public void onPortalCreate(PortalCreateEvent e) {
    // TODO implement a way to create port here
  }

  /**
   * An event handler for when an entity goes through a portal.
   * In this case, we know for sure how a portal is linked, and it can be saved.
   *
   * @param e the event
   */
  @EventHandler(priority = EventPriority.LOW)
  public void onEntityPortal(EntityPortalEvent e) {
    onPortal(e.getFrom(), e.getTo(), e.getEntity());
  }

  /**
   * An event handler for when a player goes through a portal.
   * In this case, we know for sure how a portal is linked, and it can be saved.
   *
   * @param e the event
   */
  @EventHandler(priority = EventPriority.LOW)
  public void onPlayerPortal(PlayerPortalEvent e) {
    onPortal(e.getFrom(), e.getTo(), e.getPlayer());
  }

  private void onPortal(Location from, Location to, Entity entity) {
    if (to == null) {
      return;
    }

    LocationCell origin = new LocationCell(from);

    // Wait 1 second, and check for a portal around where the player is now
    Bukkit.getScheduler().runTaskLater(JourneySpigot.getInstance(), () -> {
      Location loc = entity.getLocation();

      // Origin portal
      Optional<NetherUtil.PortalGroup> originGroup = NetherUtil
          .locateAll(origin,
              8,
              origin.getY() - 8,
              origin.getY() + 8)
          .stream()
          .min(Comparator.comparingDouble(group ->
              group.port().distanceToSquared(new LocationCell(from))));
      if (!originGroup.isPresent()) {
        return;  // We can't find the origin portal
      }

      // Destination Portal
      Optional<NetherUtil.PortalGroup> destinationGroup = NetherUtil
          .locateAll(new LocationCell(loc),
              16,
              loc.getBlockY() - 16,
              loc.getBlockY() + 16)
          .stream()
          .min(Comparator.comparingDouble(group ->
              group.port().distanceToSquared(new LocationCell(to))));
      if (!destinationGroup.isPresent()) {
        return;  // We can't find the destination portal
      }

      List<LocationCell> linkedOrigins = new LinkedList<>();
      for (LocationCell originCell : originGroup.get().getBlocks()) {
        for (LocationCell destinationCell : destinationGroup.get().getBlocks()) {
          if (!portalConnections.containsKey(originCell)) {
            continue;
          }
          linkedOrigins.add(originCell);
          if (portalConnections.get(originCell).equals(destinationCell)) {
            return;  // We already have this portal link set up
          }
        }
      }

      /* We don't have this portal link set up yet */

      // Remove any connections with this origin (the portal link changed)
      linkedOrigins.forEach(old -> {
        JourneySpigot.getInstance().getDebugManager()
            .broadcast(Format.debug("Removed nether port:"));
        JourneySpigot.getInstance().getDebugManager()
            .broadcast(Format.debug(old + " -> "));
        JourneySpigot.getInstance().getDebugManager()
            .broadcast(Format.debug(portalConnections.get(old).toString()));

        portalConnections.remove(old);
      });

      // Add the portal
      LocationCell previous = portalConnections.put(originGroup.get().port(), destinationGroup.get().port());
      if (previous == null) {
        JourneySpigot.getInstance().getDebugManager()
            .broadcast(Format.debug("Added nether port:"));
        JourneySpigot.getInstance().getDebugManager()
            .broadcast(Format.debug(originGroup.get().port() + " -> "));
        JourneySpigot.getInstance().getDebugManager()
            .broadcast(Format.debug(destinationGroup.get().port().toString()));
      }
    }, 20);
  }

  /**
   * Get the number of portal connections that are registered.
   *
   * @return the portal connection count
   */
  public int size() {
    return portalConnections.size();
  }
}