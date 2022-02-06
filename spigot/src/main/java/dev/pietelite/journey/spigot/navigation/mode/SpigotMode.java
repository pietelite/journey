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
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.util.SpigotUtil;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

/**
 * A general implementation of modes used in Spigot Minecraft.
 */
public abstract class SpigotMode extends Mode<LocationCell, World> {

  private final Set<Material> forcePassable;

  /**
   * General constructor.
   *
   * @param session       the session
   * @param forcePassable the list of passable materials
   */
  public SpigotMode(SearchSession<LocationCell, World> session, Set<Material> forcePassable) {
    super(session);
    this.forcePassable = forcePassable;
  }

  // TODO move all of these methods into a static file
  //  and possibly get it from Baritone
  protected boolean isVerticallyPassable(Block block) {
    return SpigotUtil.isVerticallyPassable(block, forcePassable);
  }

  protected boolean isLaterallyPassable(Block block) {
    return SpigotUtil.isLaterallyPassable(block, forcePassable);
  }

  protected boolean isPassable(Block block) {
    return SpigotUtil.isPassable(block, forcePassable);
  }

  protected boolean canStandOn(Block block) {
    return SpigotUtil.canStandOn(block, forcePassable);
  }

  protected boolean canStandIn(Block block) {
    return SpigotUtil.canStandIn(block, forcePassable);
  }

}
