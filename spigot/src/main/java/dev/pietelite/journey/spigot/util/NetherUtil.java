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

package dev.pietelite.journey.spigot.util;

import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

/**
 * A utility class for static operations involving the Nether.
 */
public final class NetherUtil {

  private static final BlockFace[] BLOCK_FACES = new BlockFace[]{
      BlockFace.EAST,
      BlockFace.WEST,
      BlockFace.NORTH,
      BlockFace.SOUTH,
      BlockFace.UP,
      BlockFace.DOWN};

  private NetherUtil() {
  }

  /**
   * * Gets the nearest Nether portal within the specified radius in relation to the given cell.
   *
   * @param origin    Center of the search radius
   * @param radius    The search radius
   * @param minHeight Minimum height of search
   * @param maxHeight Maximum height of search
   * @return Returns cell in the bottom center of the nearest nether portal if found. Otherwise, returns null.
   */
  public static Collection<PortalGroup> locateAll(LocationCell origin,
                                                  int radius,
                                                  int minHeight,
                                                  int maxHeight) {
    Set<PortalGroup> portals = new HashSet<>();  // All PortalGroups found
    Set<LocationCell> stored = new HashSet<>();  // All Portal blocks found in the PortalGroups
    World world = origin.getDomain();

    int startY = Math.max(origin.getY() - radius, minHeight);
    int endY = Math.min(origin.getY() + radius, maxHeight);

    for (int x = origin.getX() - radius; x <= origin.getX() + radius; x++) {
      for (int y = startY; y <= endY; y += 2) {
        for (int z = origin.getZ() - radius; z <= origin.getZ() + radius; z++) {
          if (x + z % 2 == 0) {
            break;  // Check only in checkerboard pattern
          }
          //Location being iterated over.
          LocationCell cell = new LocationCell(x, y, z, world);
          //Don't do anything if the Portal block is already stored.
          if (!stored.contains(cell)) {
            PortalGroup pg = getPortalBlocks(cell);
            //Do nothing if there are no Portal blocks
            if (pg != null) {
              //If the PortalGroup was added, store the Portal blocks in the Collection
              if (portals.add(pg)) {
                stored.addAll(pg.getBlocks());
              }
            }
          }
        }
      }
    }
    return portals;
  }

  /**
   * Locate all portals near the given cell within the given radius.
   *
   * @param cell   the central cell
   * @param radius the radius around the cell
   * @return the portals
   */
  public static Collection<PortalGroup> locateAll(LocationCell cell, int radius) {
    return locateAll(cell, radius, cell.getY() - radius, cell.getY() + radius);
  }

  /**
   * Gets the Portal blocks that is part of the Nether portal.
   * Will return null if there are no portals or the portal that has been found
   * has too few portal blocks (<6).
   *
   * @param cell - Location to start the getting the portal blocks.
   * @return A PortalGroup of all the found Portal blocks. Otherwise, returns null.
   */
  public static PortalGroup getPortalBlocks(LocationCell cell) {
    if (cell.getBlock().getType() != Material.NETHER_PORTAL) {
      return null;
    }

    PortalGroup group = portalBlock(new PortalGroup(cell.getDomain()), cell);
    return group.size() > 5 ? group : null;
  }

  private static PortalGroup portalBlock(PortalGroup group, LocationCell cell) {
    for (BlockFace face : BLOCK_FACES) {
      Block relative = cell.getBlock().getRelative(face);
      LocationCell relLoc = new LocationCell(relative.getLocation());
      if (group.add(relLoc)) {
        portalBlock(group, relLoc);
      }
    }
    return group;
  }

  /**
   * Represents a Nether portal. This actually consists of some number of blocks
   */
  public static class PortalGroup {
    private final Set<LocationCell> portal = new HashSet<>();
    private final HashMap<Integer, Set<LocationCell>> blockY = new HashMap<>();
    private final World world;
    private LocationCell teleportTo;
    private int bottom = Integer.MAX_VALUE;

    /**
     * A group of Portal block for a Nether portal.
     *
     * @param world The world the Portal blocks resides in.
     */
    public PortalGroup(World world) {
      this.world = world;
    }

    /**
     * Adds the Location to the PortalGroup.
     *
     * @param cell Vector to add
     * @return if the Location was added. Otherwise, false.
     */
    public boolean add(LocationCell cell) {
      // Check to see if the block is a Portal block.
      if (cell.getBlock().getType() != Material.NETHER_PORTAL) {
        return false;
      }

      boolean added = portal.add(cell);
      // If the cell was added, do more actions.
      if (added) {
        int y = cell.getY();
        if (y < bottom) {
          //The bottom of the Nether portal
          bottom = cell.getY();
        }
        //Put the Location in a Map sorted by Y value.
        Set<LocationCell> set = blockY.computeIfAbsent(y, k -> new HashSet<>());
        set.add(cell);
        //Reset the teleport cell and distance squared since a new block was added.
        teleportTo = null;
      }
      return added;
    }

    /**
     * Get the number of blocks in the portal.
     *
     * @return the size
     */
    public int size() {
      return portal.size();
    }

    /**
     * Get all the blocks in the portal.
     *
     * @return all portal blocks
     */
    public Collection<LocationCell> getBlocks() {
      return Collections.unmodifiableCollection(portal);
    }

    /**
     * Gets the place to teleport an entity to the Nether portal.
     *
     * @return The cell at the bottom center of the Nether portal.
     */
    public LocationCell port() {
      if (teleportTo != null) {
        return teleportTo;
      }

      if (portal.size() == 0) {
        return null;
      }

      Set<LocationCell> bottomY = blockY.get(bottom);
      int minX = Collections.min(bottomY, Comparator.comparingInt(LocationCell::getX)).getX();
      int maxX = Collections.max(bottomY, Comparator.comparingInt(LocationCell::getX)).getX();
      int minZ = Collections.min(bottomY, Comparator.comparingInt(LocationCell::getZ)).getZ();
      int maxZ = Collections.max(bottomY, Comparator.comparingInt(LocationCell::getZ)).getZ();

      return new LocationCell((maxX + minX) / 2, bottom, (maxZ + minZ) / 2, world);
    }

    @Override
    public int hashCode() {
      return portal.hashCode();
    }

    @Override
    public boolean equals(Object o) {
      if (o instanceof PortalGroup) {
        PortalGroup pg = (PortalGroup) o;
        return portal.equals(pg.portal);
      }
      return portal.equals(o);
    }

  }
}