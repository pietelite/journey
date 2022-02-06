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

/**
 * A utility class to calculate properties about locations in a grid space.
 */
public final class LocationUtil {

  private LocationUtil() {
  }

  /**
   * Determine if the user can step from the given block to a
   * block one block away in the X or Z direction and some
   * amount of blocks up or down.
   *
   * <p>Does no checks for whether a player can stand at the given
   * origin points.
   *
   * @param cell       the cell to check against
   * @param offY       the offset of the step in y
   * @param stepX      which axis a step is being taken: x if true, z if false
   * @param isPositive which direction a step is being taken:
   *                   positive if true, negative if false
   * @return true if a player can step there
   */
  private static boolean canStep(LocationCell cell, int offY,
                                 boolean stepX, boolean isPositive) {
    if (offY > 1 || offY < -3) {
      return false;  // Too high to jump, too low to fall
    }

    int floorX = cell.getX() + (stepX ? 1 : 0) * (isPositive ? 1 : -1);
    int floorY = cell.getY() - 1 + offY;
    int floorZ = cell.getZ() + (stepX ? 0 : 1) * (isPositive ? 1 : -1);

    if (cell.getDomain().getBlockAt(floorX, floorY, floorZ).isPassable()) {
      return false;  // There is no floor
    }

    for (int columnY = Math.min(floorY, cell.getY()) + 1;
         columnY <= Math.max(floorY, cell.getY()) + 1;
         columnY++) {
      if (!cell.getDomain().getBlockAt(floorX, columnY, floorY).isPassable()) {
        return false;
      }
    }

    return true;
  }

  private static boolean canStepPosX(LocationCell cell, int offY) {
    return canStep(cell, offY, true, true);
  }

  private static boolean canStepNegX(LocationCell cell, int offY) {
    return canStep(cell, offY, true, false);
  }

  private static boolean canStepPosZ(LocationCell cell, int offY) {
    return canStep(cell, offY, false, true);
  }

  private static boolean canStepNegZ(LocationCell cell, int offY) {
    return canStep(cell, offY, false, false);
  }

}
