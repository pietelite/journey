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

package dev.pietelite.journey.spigot.command.to;

import dev.pietelite.journey.common.config.Settings;
import dev.pietelite.journey.common.data.DataAccessException;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.command.common.CommandFlags;
import dev.pietelite.journey.spigot.command.common.CommandNode;
import dev.pietelite.journey.spigot.command.common.PlayerCommandNode;
import dev.pietelite.journey.spigot.search.PlayerSurfaceGoalSearchSession;
import dev.pietelite.journey.spigot.util.Format;
import dev.pietelite.journey.spigot.util.Permissions;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command to allow a user to calculate a path up to the surface of the world,
 * to be used if they are lost underground.
 */
public class JourneyToSurfaceCommand extends PlayerCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyToSurfaceCommand(@Nullable CommandNode parent) {
    super(parent, Permissions.JOURNEY_TO_SURFACE_USE,
        "Journey to the surface, if you are in the overworld",
        "surface");
  }

  @Override
  public boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> commandFlags)
      throws DataAccessException {
    if (!player.getWorld().getEnvironment().equals(World.Environment.NORMAL)) {
      player.spigot().sendMessage(Format.error("You may only use this command in the Overworld!"));
    }

    int algorithmStepDelay = 0;
    if (CommandFlags.ANIMATE.isIn(commandFlags)) {
      algorithmStepDelay = CommandFlags.ANIMATE.retrieve(player, commandFlags);
    }

    LocationCell playerLocationCell = new LocationCell(player.getLocation());
    PlayerSurfaceGoalSearchSession session = new PlayerSurfaceGoalSearchSession(player,
        new LocationCell(player.getLocation()),
        CommandFlags.ANIMATE.isIn(commandFlags),
        Settings.DEFAULT_NOFLY_FLAG.getValue() != CommandFlags.NOFLY.isIn(commandFlags),
        Settings.DEFAULT_NODOOR_FLAG.getValue() != CommandFlags.NODOOR.isIn(commandFlags),
        algorithmStepDelay);

    if (session.reachesGoal(playerLocationCell)) {
      player.spigot().sendMessage(Format.success("You're already at the surface!"));
      return true;
    }

    int timeout = CommandFlags.TIMEOUT.isIn(commandFlags)
        ? CommandFlags.TIMEOUT.retrieve(player, commandFlags)
        : Settings.DEFAULT_SEARCH_TIMEOUT.getValue();

    session.launchSession(timeout);

    return true;
  }
}