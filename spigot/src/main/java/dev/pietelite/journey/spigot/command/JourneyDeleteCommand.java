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

package dev.pietelite.journey.spigot.command;

import dev.pietelite.journey.spigot.command.common.CommandNode;
import dev.pietelite.journey.spigot.command.common.FunctionlessCommandNode;
import dev.pietelite.journey.spigot.command.delete.JourneyDeleteMyCommand;
import dev.pietelite.journey.spigot.command.delete.JourneyDeletePublicCommand;
import org.jetbrains.annotations.Nullable;

/**
 * A command to host various subcommands that allow deletions of search endpoints.
 */
public class JourneyDeleteCommand extends FunctionlessCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyDeleteCommand(@Nullable CommandNode parent) {
    super(parent, null,
        "Delete a path destination",
        "delete");
    addChildren(new JourneyDeletePublicCommand(this));
    addChildren(new JourneyDeleteMyCommand(this));
  }
}
