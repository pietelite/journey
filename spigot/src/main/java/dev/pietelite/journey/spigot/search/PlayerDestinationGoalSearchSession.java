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

import dev.pietelite.journey.common.search.DestinationGoalSearchSession;
import dev.pietelite.journey.spigot.JourneySpigot;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.external.whimcportals.WhimcPortalPort;
import dev.pietelite.journey.spigot.navigation.mode.ClimbMode;
import dev.pietelite.journey.spigot.navigation.mode.DoorMode;
import dev.pietelite.journey.spigot.navigation.mode.FlyMode;
import dev.pietelite.journey.spigot.navigation.mode.JumpMode;
import dev.pietelite.journey.spigot.navigation.mode.SwimMode;
import dev.pietelite.journey.spigot.navigation.mode.WalkMode;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * A search session designed to be used for players finding their way to a specific destination.
 */
public class PlayerDestinationGoalSearchSession
    extends DestinationGoalSearchSession<LocationCell, World>
    implements SpigotPlayerSearchSession<PlayerDestinationGoalSearchSession> {

  private final PlayerSessionState sessionState;
  private final AnimationManager animationManager;

  /**
   * General constructor.
   *
   * @param player             the player calling this session
   * @param origin             the origin of the search
   * @param destination        the destination of the search
   * @param animate            true if we should animate
   * @param nofly              true if we should ignore the ability of the player to fly
   * @param nodoor             true if we should ignore the barrier of iron doors
   * @param algorithmStepDelay the millisecond delay to each step of the lowest
   *                           level of decision in the algorithm
   */
  public PlayerDestinationGoalSearchSession(Player player,
                                            LocationCell origin, LocationCell destination,
                                            boolean animate,
                                            boolean nofly,
                                            boolean nodoor,
                                            int algorithmStepDelay) {
    super(player.getUniqueId(), Caller.PLAYER, origin, destination,
        (x, y, z, world) -> new LocationCell(x, y, z, UUID.fromString(world)));
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

    // Ports
    JourneySpigot.getInstance().getNetherManager().makePorts().forEach(this::registerPort);
    WhimcPortalPort.addPortsTo(this, player::hasPermission);
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
  public PlayerDestinationGoalSearchSession getSession() {
    return this;
  }

}
