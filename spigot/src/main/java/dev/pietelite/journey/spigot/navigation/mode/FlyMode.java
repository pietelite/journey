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
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * A mode to determine which nearby locations are reachable
 * when having the ability to fly.
 */
public class FlyMode extends SpigotMode {

  /**
   * General constructor.
   *
   * @param session       the session
   * @param forcePassable the set of all passable materials
   */
  public FlyMode(SearchSession<LocationCell, World> session, Set<Material> forcePassable) {
    super(session, forcePassable);
  }

  @Override
  public void collectDestinations(@NotNull LocationCell origin, @NotNull List<Option> options) {
    LocationCell cell;
    // Check every block in a 3x3 grid centered around the current location
    for (int offX = -1; offX <= 1; offX++) {
      for (int offY = -1; offY <= 1; offY++) {
        outerZ:
        // Label so we can continue from these main loops when all checks fail
        for (int offZ = -1; offZ <= 1; offZ++) {
          // Checks for the block -- checks between the offset block and the original block,
          //  which would be 1 for just 1 offset variable, 4 for 2 offset variables,
          //  and 8 for 3 offset variables.
          for (int insideOffX = offX * offX /* normalize sign */; insideOffX >= 0; insideOffX--) {
            for (int insideOffY = offY * offY /* normalize sign */; insideOffY >= 0; insideOffY--) {
              for (int insideOffZ = offZ * offZ /* normalize sign */; insideOffZ >= 0; insideOffZ--) {
                // This is the origin, we don't want to move here
                if (insideOffX == 0 && insideOffY == 0 && insideOffZ == 0) {
                  continue;
                }
                // Make sure we get the pillar of y values for the player's body
                cell = origin.createCellAtOffset(// Floor
                    insideOffX * offX /* get sign back */,
                    insideOffY * offY /* get sign back */,
                    insideOffZ * offZ /* get sign back */);
                if (!isLaterallyPassable(cell.getBlock())) {
                  reject(cell);
                  continue outerZ;
                }
                for (int h = 0; h <= insideOffY; h++) {
                  // The rest of the pillar above the floor
                  cell = origin.createCellAtOffset(
                      insideOffX * offX /* get sign back */,
                      ((insideOffY * offY + insideOffY) >> 1) /* 1 for positive, 0 for negative */
                          + h
                          + (1 - insideOffY) /* for if offYIn is 0 */,
                      insideOffZ * offZ /* get sign back */);
                  if (!isPassable(cell.getBlock())) {
                    reject(cell);
                    continue outerZ;
                  }
                }
              }
            }
          }
          LocationCell other = origin.createCellAtOffset(offX, offY, offZ);
          accept(other, origin.distanceTo(other), options);
        }
      }
    }

  }

  @Override
  public @NotNull ModeType getType() {
    return ModeType.FLY;
  }
}
