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

package dev.pietelite.journey.spigot.navigation.mode;

import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.util.MaterialGroups;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.type.Door;
import org.jetbrains.annotations.NotNull;

/**
 * The movement mode to handle if players can move through doors.
 *
 * @see SearchSession
 */
public final class DoorMode extends SpigotMode {

  /**
   * Default constructor.
   *
   * @param forcePassable set of materials that we can always pass through
   */
  public DoorMode(SearchSession<LocationCell, World> session, Set<Material> forcePassable) {
    super(session, forcePassable);
  }

  @Override
  public void collectDestinations(@NotNull LocationCell origin, @NotNull List<Option> options) {
    // TODO check if there are buttons or levers nearby that may open the door

    LocationCell cell;
    Block block;
    // Pos X - East
    cell = origin.createCellAtOffset(1, 0, 0);
    block = cell.getBlock();
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(origin.getBlockAtOffset(1, -1, 0))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.NORTH)
              || doorBlock.getFacing().equals(BlockFace.SOUTH)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.createCellAtOffset(1, 0, 0), 1.0d, options);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              accept(origin.createCellAtOffset(1, 0, 0), 1.0d, options);
            } else {
              reject(origin.createCellAtOffset(1, 0, 0));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.createCellAtOffset(1, 0, 0), 1.0d, options);
        }
      } else {
        reject(origin.createCellAtOffset(1, -1, 0));
      }
    } else {
      reject(new LocationCell(block.getLocation()));
    }

    // Pos Z - North
    cell = origin.createCellAtOffset(0, 0, 1);
    block = cell.getBlock();
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(origin.getBlockAtOffset(0, -1, 1))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.EAST)
              || doorBlock.getFacing().equals(BlockFace.WEST)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.createCellAtOffset(0, 0, 1), 1.0d, options);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              accept(origin.createCellAtOffset(0, 0, 1), 1.0d, options);
            } else {
              reject(origin.createCellAtOffset(0, 0, 1));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.createCellAtOffset(0, 0, 1), 1.0d, options);
        }
      } else {
        reject(origin.createCellAtOffset(0, -1, 1));
      }
    } else {
      reject(new LocationCell(block.getLocation()));
    }

    // Neg X - West
    cell = origin.createCellAtOffset(-1, 0, 0);
    block = cell.getBlock();
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(origin.getBlockAtOffset(-1, -1, 0))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.NORTH)
              || doorBlock.getFacing().equals(BlockFace.SOUTH)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.createCellAtOffset(-1, 0, 0), 1.0d, options);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              accept(origin.createCellAtOffset(-1, 0, 0), 1.0d, options);
            } else {
              reject(origin.createCellAtOffset(-1, 0, 0));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.createCellAtOffset(-1, 0, 0), 1.0d, options);
        }
      } else {
        reject(origin.createCellAtOffset(-1, -1, 0));
      }
    } else {
      reject(new LocationCell(block.getLocation()));
    }

    // Neg Z - South
    cell = origin.createCellAtOffset(0, 0, -1);
    block = cell.getBlock();
    // Check if we found a door
    if (block.getBlockData() instanceof Door) {
      // Check it's a solid floor
      if (!isVerticallyPassable(origin.getBlockAtOffset(0, -1, -1))) {
        Door doorBlock = (Door) block.getBlockData();
        if (block.getType().equals(Material.IRON_DOOR)) {
          // Need to check if the door is blocking
          if (doorBlock.getFacing().equals(BlockFace.EAST)
              || doorBlock.getFacing().equals(BlockFace.WEST)
              || doorBlock.isOpen()) {
            // Nothing blocking
            accept(origin.createCellAtOffset(0, 0, -1), 1.0d, options);
          } else {
            // We need to be able to open the door
            if (MaterialGroups.PRESSURE_PLATES.contains(origin.getBlock().getType())) {
              // We can step on a pressure plate to open it
              accept(origin.createCellAtOffset(0, 0, -1), 1.0d, options);
            } else {
              reject(origin.createCellAtOffset(0, 0, -1));
            }
          }
          //  If it is blocking, then see if you can open with a switch or something
        } else {
          // It's not iron, so its passable
          accept(origin.createCellAtOffset(0, 0, -1), 1.0d, options);
        }
      } else {
        reject(origin.createCellAtOffset(0, -1, -1));
      }
    } else {
      reject(new LocationCell(block.getLocation()));
    }

  }

  @Override
  public @NotNull ModeType getType() {
    return ModeType.DOOR;
  }
}
