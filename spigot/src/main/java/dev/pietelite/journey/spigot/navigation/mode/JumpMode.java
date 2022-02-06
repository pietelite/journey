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
 * to check the places a player may jump to in Minecraft.
 */
public class JumpMode extends SpigotMode {

  /**
   * General constructor.
   *
   * @param session       the search session
   * @param forcePassable the set of passable materials
   */
  public JumpMode(SearchSession<LocationCell, World> session, Set<Material> forcePassable) {
    super(session, forcePassable);
  }

  @Override
  public void collectDestinations(@NotNull LocationCell origin, @NotNull List<Option> options) {
    LocationCell cell;

    cell = origin.createCellAtOffset(0, -1, 0);
    if (isVerticallyPassable(cell.getBlock())) {
      // Nothing to jump off of
      reject(cell);
      return;
    }

    cell = origin.createCellAtOffset(0, 2, 0);
    if (!isVerticallyPassable(cell.getBlock())) {
      // No room to jump
      reject(cell);
      return;
    }
    // 1 block up
    accept(origin.createCellAtOffset(0, 1, 0), 1.0d, options);

    // 1 block away and up
    for (int offX = -1; offX <= 1; offX++) {
      outerZ:
      for (int offZ = -1; offZ <= 1; offZ++) {
        for (int insideOffX = offX * offX /* normalize sign */; insideOffX >= 0; insideOffX--) {
          for (int insideOffZ = offZ * offZ /* normalize sign */; insideOffZ >= 0; insideOffZ--) {
            if (insideOffX == 0 && insideOffZ == 0) {
              continue;
            }
            // Check two blocks tall
            cell = origin.createCellAtOffset(
                insideOffX * offX /* get sign back */,
                1,
                insideOffZ * offZ /* get sign back */);
            if (!isLaterallyPassable(cell.getBlock())) {
              reject(cell);
              continue outerZ;
            }
            cell = origin.createCellAtOffset(
                insideOffX * offX /* get sign back */,
                2,
                insideOffZ * offZ /* get sign back */);
            if (!isPassable(cell.getBlock())) {
              reject(cell);
              continue outerZ;
            }
          }
        }
        double jumpDistance = (origin.getBlockAtOffset(offX, 1, offZ).getBoundingBox().getMaxY()
            + 1.0
            - (origin.getBlockAtOffset(0, 0, 0).isPassable()
            ? origin.getBlockAtOffset(0, -1, 0).getBoundingBox().getMaxY() - 1
            : origin.getBlockAtOffset(0, 0, 0).getBoundingBox().getMaxY()));
        LocationCell other = origin.createCellAtOffset(offX, 1, offZ);
        if (!isVerticallyPassable(origin.getBlockAtOffset(offX, 0, offZ))
            && jumpDistance <= 1.2) {
          // Can stand here
          accept(other, origin.distanceTo(other), options);
        } else {
          reject(other);
        }
      }
    }
  }

  @Override
  public @NotNull ModeType getType() {
    return ModeType.JUMP;
  }
}
