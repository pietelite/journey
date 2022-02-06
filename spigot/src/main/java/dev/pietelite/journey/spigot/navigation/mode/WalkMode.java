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

import dev.pietelite.journey.common.navigation.Mode;
import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * Implementation of {@link Mode}
 * for normal walking in Minecraft.
 */
public class WalkMode extends SpigotMode {

  /**
   * General constructor.
   *
   * @param session       the session
   * @param forcePassable list of passable materials
   */
  public WalkMode(SearchSession<LocationCell, World> session, Set<Material> forcePassable) {
    super(session, forcePassable);
  }

  @Override
  public void collectDestinations(@NotNull LocationCell origin, @NotNull List<Option> options) {
    LocationCell cell;
    LocationCell cell1;
    LocationCell cell2;
    // Can you drop into an inhabitable block?
    cell = origin.createCellAtOffset(0, -1, 0);
    if (canStandOn(origin.getBlockAtOffset(0, -2, 0)) && isVerticallyPassable(cell.getBlock())) {
      accept(cell, 1.0d, options);
    } else {
      reject(cell);
    }

    // Can we even stand here?
    if (!canStandOn(origin.getBlockAtOffset(0, -1, 0))
        && !canStandIn(origin.getBlockAtOffset(0, 0, 0))) {
      return;
    }

    // 1 block away
    for (int offX = -1; offX <= 1; offX++) {
      outerZ:
      for (int offZ = -1; offZ <= 1; offZ++) {
        // For the diagonal points, check that the path is clear in both
        //  lateral directions and diagonally
        for (int insideOffX = offX * offX /* normalize sign */; insideOffX >= 0; insideOffX--) {
          for (int insideOffZ = offZ * offZ /* normalize sign */; insideOffZ >= 0; insideOffZ--) {
            if (insideOffX == 0 && insideOffZ == 0) {
              continue;
            }
            for (int offY = 0; offY <= 1; offY++) { // Check two blocks tall
              cell = origin.createCellAtOffset(insideOffX * offX /* get sign back */,
                  offY,
                  insideOffZ * offZ /*get sign back */);
              if (!isLaterallyPassable(cell.getBlock())) {
                reject(cell);
                continue outerZ;  // Barrier - invalid move
              }
            }
          }
        }

        // We can move to offX and offY laterally
        cell = origin.createCellAtOffset(offX, 0, offZ);
        if (!isVerticallyPassable(cell.getBlock())) {
          // We can just stand right here (carpets, slabs, etc.)
          accept(cell, origin.distanceTo(cell), options);
        } else {
          reject(cell);
        }

        for (int offY = -1; offY >= -4; offY--) {  // Check for floor anywhere up to a 3 block fall
          cell = origin.createCellAtOffset(offX, offY, offZ);
          cell1 = cell.createCellAtOffset(0, 1, 0);
          if (canStandOn(cell.getBlock())) {
            cell2 = cell.createCellAtOffset(0, 2, 0);
            if (cell2.getBlock().getType().equals(Material.WATER)) {
              reject(cell1); // Water (drowning) - invalid destination
            } else {
              accept(cell1, origin.distanceTo(cell1), options);
            }
            break;
          } else {
            reject(cell1);
          }
        }
      }
    }
  }

  @Override
  public @NotNull ModeType getType() {
    return ModeType.WALK;
  }
}
