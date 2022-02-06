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

package dev.pietelite.journey.spigot.command.common;

import dev.pietelite.journey.common.data.DataAccessException;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An abstract class describing a command node that must be invoked by an in-game player.
 */
public abstract class PlayerCommandNode extends CommandNode {

  /**
   * General constructor.
   *
   * @param parent       the parent command
   * @param permission   the permission
   * @param description  the description
   * @param primaryAlias primary alias of this command
   */
  public PlayerCommandNode(@Nullable CommandNode parent,
                           @Nullable Permission permission,
                           @NotNull String description,
                           @NotNull String primaryAlias) {
    super(parent, permission, description, primaryAlias);
  }

  @Override
  public final boolean onWrappedCommand(@NotNull CommandSender sender,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) throws DataAccessException {
    Player player;
    if (!(sender instanceof Player)) {
      sendCommandUsageError(sender, CommandError.ONLY_PLAYER);
      return false;
    }
    player = (Player) sender;
    return onWrappedPlayerCommand(player, command, label, args, flags);
  }

  /**
   * The same as {@link #onWrappedCommand(CommandSender, Command, String, String[], Map)},
   * but gives the player because this command must be executed by a player.
   *
   * @param player  the player
   * @param command the command
   * @param label   the label (root command)
   * @param args    the arguments after the root command
   * @param flags   the flags (arguments starting with "-")
   * @return true if successful
   * @throws DataAccessException a data access exception
   */
  public abstract boolean onWrappedPlayerCommand(@NotNull Player player,
                                                 @NotNull Command command,
                                                 @NotNull String label,
                                                 @NotNull String[] args,
                                                 @NotNull Map<String, String> flags)
      throws DataAccessException;
}
