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

import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.permissions.Permission;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command node within the command tree structure which has no unique behavior of its own.
 * This is almost always used as a command node to simply provide a list of children commands.
 */
public abstract class FunctionlessCommandNode extends CommandNode {

  /**
   * General constructor.
   *
   * @param parent       the parent command
   * @param permission   the permission
   * @param description  the description of the command
   * @param primaryAlias the primary alias used to access this command
   */
  public FunctionlessCommandNode(@Nullable CommandNode parent,
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
                                        @NotNull Map<String, String> flags) {
    sendCommandUsageError(sender, "Too few arguments or invalid arguments!");
    return false;
  }
}
