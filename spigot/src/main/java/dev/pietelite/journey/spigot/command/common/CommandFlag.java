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
import lombok.Getter;

/**
 * A flag that can be passed when a Minecraft command is run. Enumerated in {@link CommandFlags}.
 */
public class CommandFlag {

  @Getter
  private final String key;

  /**
   * General constructor.
   *
   * @param key the identifier for this flag. Must be unique!
   */
  public CommandFlag(String key) {
    this.key = key;
  }

  /**
   * Determine whether a command flag is inside a map of flags.
   * The map should come from the command handling system.
   *
   * @param flagMap all flags
   * @return true if it is present
   */
  public boolean isIn(Map<String, String> flagMap) {
    return flagMap.containsKey(key);
  }

}
