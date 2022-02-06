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

import dev.pietelite.journey.common.search.LocalUpwardsGoalSearchSession;
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.navigation.mode.ClimbMode;
import dev.pietelite.journey.spigot.navigation.mode.DoorMode;
import dev.pietelite.journey.spigot.navigation.mode.FlyMode;
import dev.pietelite.journey.spigot.navigation.mode.JumpMode;
import dev.pietelite.journey.spigot.navigation.mode.SwimMode;
import dev.pietelite.journey.spigot.navigation.mode.WalkMode;
import java.util.HashSet;
import java.util.Set;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * A combination of a {@link LocalUpwardsGoalSearchSession} and a {@link SpigotPlayerSearchSession},
 * where the former determines the best way to go upwards and potentially reach the surface of the overworld,
 * and the latter allows us to manage and call back to the former session when we catch the event
 * in event handlers.
 */
public class PlayerSurfaceGoalSearchSession
    extends LocalUpwardsGoalSearchSession<LocationCell, World>
    implements SpigotPlayerSearchSession<PlayerSurfaceGoalSearchSession> {

  /**
   * The height of the space filled with air to be considered the surface of the world.
   */
  private static final int AT_SURFACE_HEIGHT = 64;

  private final PlayerSessionState sessionState;
  private final AnimationManager animationManager;

  /**
   * General constructor.
   *
   * <p>The algorithm step delay is primarily used for animation.
   *
   * @param player             the player
   * @param algorithmStepDelay how long to wait between every search step
   */
  public PlayerSurfaceGoalSearchSession(Player player, LocationCell origin,
                                        boolean animate,
                                        boolean nofly,
                                        boolean nodoor,
                                        int algorithmStepDelay) {
    super(player.getUniqueId(), SearchSession.Caller.PLAYER, origin);
    this.sessionState = new PlayerSessionState();
    this.animationManager = new AnimationManager(this);
    animationManager.setAnimating(animate);
    setAlgorithmStepDelay(algorithmStepDelay);

    // Modes
    Set<Material> passableBlocks = new HashSet<>();
    if (nodoor) {
      passableBlocks.add(Material.IRON_DOOR);
    }

    // Register modes in order of preference
    if (player.getAllowFlight() && !nofly) {
      registerMode(new FlyMode(this, passableBlocks));
    } else {
      registerMode(new WalkMode(this, passableBlocks));
      registerMode(new JumpMode(this, passableBlocks));
      registerMode(new SwimMode(this, passableBlocks));
    }
    registerMode(new DoorMode(this, passableBlocks));
    registerMode(new ClimbMode(this, passableBlocks));

    // We don't need any ports for this!
  }


  @Override
  public @Nullable Player getPlayer() {
    return Bukkit.getPlayer(getCallerId());
  }

  @Override
  public AnimationManager getAnimationManager() {
    return animationManager;
  }

  @Override
  public PlayerSessionState getSessionState() {
    return sessionState;
  }

  @Override
  public PlayerSurfaceGoalSearchSession getSession() {
    return this;
  }

  @Override
  public boolean reachesGoal(LocationCell cell) {
    int x = cell.getX();
    int z = cell.getZ();
    World world = cell.getDomain();
    for (int y = cell.getY() + 1; y <= Math.min(256, cell.getY() + AT_SURFACE_HEIGHT); y++) {
      if (world.getBlockAt(x, y, z).getType() != Material.AIR) {
        return false;
      }
    }
    return true;
  }
}
