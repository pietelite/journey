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

package dev.pietelite.journey.spigot.search;

import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * The place where all animation operations and memory are stored for any single
 * {@link PlayerDestinationGoalSearchSession}.
 */
public class AnimationManager {

  private final Set<LocationCell> successfulLocations = ConcurrentHashMap.newKeySet();
  private final SpigotPlayerSearchSession<?> session;
  private LocationCell lastFailure;
  private boolean animating;

  /**
   * General constructor.
   *
   * @param session the session
   */
  public AnimationManager(SpigotPlayerSearchSession<?> session) {
    this.session = session;
  }

  /**
   * Show the result of an algorithm step to the user.
   *
   * @param cell     the location
   * @param success  whether the step was a successful move
   * @param modeType the mode used to get there
   * @return true if it showed the result correctly, false if it failed for some reason
   */
  public boolean showResult(LocationCell cell, boolean success, ModeType modeType) {
    if (!animating) {
      return false;
    }
    Player player = Bukkit.getPlayer(session.getSession().getCallerId());
    if (player == null) {
      return false;
    }

    if (this.successfulLocations.contains(cell)) {
      return false;
    }

    if (player.getLocation().equals(cell.getBlock().getLocation())
        || player.getLocation().add(0, 1, 0).equals(cell.getBlock().getLocation())) {
      return false;
    }

    if (success) {
      BlockData blockData;
      switch (modeType) {
        case WALK:
          blockData = Material.LIME_STAINED_GLASS.createBlockData();
          break;
        case JUMP:
          blockData = Material.MAGENTA_STAINED_GLASS.createBlockData();
          break;
        case FLY:
          blockData = Material.WHITE_STAINED_GLASS.createBlockData();
          break;
        default:
          blockData = Material.COBWEB.createBlockData();
          break;
      }
      return showBlock(cell, blockData);
    } else {
      if (lastFailure != null) {
        hideResult(lastFailure);
      }
      lastFailure = cell;
      return showBlock(cell, Material.GLOWSTONE.createBlockData());
    }
  }

  private void hideResult(LocationCell cell) {
    Player player = Bukkit.getPlayer(session.getSession().getCallerId());
    if (player == null) {
      return;
    }
    player.sendBlockChange(cell.getBlock().getLocation(), cell.getBlock().getBlockData());
    successfulLocations.remove(cell);
  }

  /**
   * Show the location of a step in an algorithm to the user.
   *
   * @param cell the location
   * @return true if it displayed correctly, false if it failed for some reason
   */
  public boolean showStep(LocationCell cell) {
    return showBlock(cell, Material.OBSIDIAN.createBlockData());
  }

  private boolean showBlock(LocationCell cell, BlockData blockData) {
    if (!animating) {
      return false;
    }
    Player player = getPlayer();
    if (player == null) {
      return false;
    }
    if (cell.getDomain() != player.getWorld()
        || cell.distanceToSquared(new LocationCell(player.getLocation())) > 10000) {
      return false;
    }
    player.sendBlockChange(cell.getBlock().getLocation(), blockData);
    successfulLocations.add(cell);
    return true;
  }

  /**
   * Undo all the block changes displayed to the user so everything looks as it did before animating.
   */
  public void undoAnimation() {
    Player player = Bukkit.getPlayer(session.getSession().getCallerId());
    if (player == null) {
      return;
    }

    successfulLocations.forEach(cell -> showBlock(cell, cell.getBlock().getBlockData()));

    if (lastFailure != null) {
      showBlock(lastFailure, lastFailure.getBlock().getBlockData());
    }
  }

  @Nullable
  private Player getPlayer() {
    return Bukkit.getPlayer(session.getSession().getCallerId());
  }

  /**
   * Whether this manager should be creating new animations.
   * When set to false, the animations will still be reset and
   * cleaned up normally.
   *
   * @return true if animating
   */
  public boolean isAnimating() {
    return animating;
  }

  /**
   * Set whether this manager should be creating new animations.
   * When set to false, the animations will still be reset and
   * cleaned up normally.
   *
   * @param animating true to animate
   */
  public void setAnimating(boolean animating) {
    this.animating = animating;
  }
}
