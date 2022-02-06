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

import java.util.Collections;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.block.Block;

/**
 * A utility class to handle general odd Spigot Minecraft-related operations.
 */
public final class SpigotUtil {

  public static final double STEVE_HEIGHT = 1.8;
  public static final double STEVE_HEIGHT_SNEAK = 1.5;
  public static final double STEVE_HEIGHT_GLIDE = 0.6;
  public static final double STEVE_HEIGHT_SWIM = 0.6;
  public static final double STEVE_WIDTH = 0.6;

  public static final double STEP_HEIGHT = 0.5;

  /**
   * Return true if the given block can be possibly passed through vertically,
   * like falling through or flying upwards through.
   *
   * @param block the block
   * @return true if it can be passed
   */
  public static boolean isVerticallyPassable(Block block) {
    return isVerticallyPassable(block, Collections.emptySet());
  }

  /**
   * Return true if you can pass vertically through this block.
   *
   * @param block the block
   * @return false if you cannot pass through this block vertically
   */
  public static boolean isVerticallyPassable(Block block, Set<Material> forcePassable) {
    if (isPassable(block, forcePassable)) {
      return true;
    }
    return MaterialGroups.VERTICALLY_PASSABLE.contains(block.getType());
  }

  /**
   * Determine if a block can be laterally passed through, as in,
   * can the entity move in a lateral direction and go through that
   * block location.
   *
   * @param block the block
   * @return true if passable
   */
  public static boolean isLaterallyPassable(Block block) {
    return isVerticallyPassable(block, Collections.emptySet());
  }

  /**
   * Return true if you can pass laterally through this block.
   *
   * @param block the block
   * @return false if you cannot pass through this block laterally
   */
  public static boolean isLaterallyPassable(Block block, Set<Material> forcePassable) {
    if (isPassable(block, forcePassable)) {
      return true;
    }
    return MaterialGroups.LATERALLY_PASSABLE.contains(block.getType());
  }

  /**
   * Determine if this block is generally passable.
   *
   * @param block the block
   * @return true if passable
   */
  public static boolean isPassable(Block block) {
    return isPassable(block, Collections.emptySet());
  }

  /**
   * Return true if you can pass through this block in any direction.
   *
   * @param block the block
   * @return false if you cannot pass through at all
   */
  public static boolean isPassable(Block block, Set<Material> forcePassable) {
    if (forcePassable.contains(block.getType())) {
      return true;
    }
    return block.isPassable() && !MaterialGroups.INVALID_PASSABLE.contains(block.getType());
  }

  /**
   * Can a player be supported by this block below him.
   *
   * @param block the block
   * @return false if a player cannot stand on top of the block
   */
  public static boolean canStandOn(Block block, Set<Material> forcePassable) {
    if (forcePassable.contains(block.getType())) {
      return false;
    } else {
      return (!block.isPassable()/* && block.getBoundingBox().getHeight() >= 1.0*/)
          || MaterialGroups.TALL_SOLIDS.contains(block.getType());
    }
  }

  /**
   * Can a player be supported by this block within the same block as him.
   *
   * @param block the block
   * @return false if a player cannot stand within this block and be supported
   */
  public static boolean canStandIn(Block block, Set<Material> forcePassable) {
    return isLaterallyPassable(block, forcePassable)
        && !isVerticallyPassable(block, forcePassable);
  }

}
