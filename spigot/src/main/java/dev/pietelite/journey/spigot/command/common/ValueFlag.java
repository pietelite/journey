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
import java.util.function.BiFunction;
import org.bukkit.entity.Player;

/**
 * A command flag that represents both a key and a value.
 * The data is not stored in the value flag itself but is rather
 * retrieved from a flag set created at the time of command execution.
 *
 * @param <T> the data type of the value
 */
public class ValueFlag<T> extends CommandFlag {

  private final BiFunction<Player, String, T> parser;

  /**
   * General constructor.
   *
   * @param key    the key of the flag
   * @param parser the parser which gets the value from some data string,
   *               based on the player who called the command
   */
  public ValueFlag(String key, BiFunction<Player, String, T> parser) {
    super(key);
    this.parser = parser;
  }

  /**
   * Retrieval method of the data that this value flag represents
   * from a mapping of flags and their values taken from a command execution.
   *
   * @param player  the player who called the command
   * @param flagMap the flags directly from the command
   * @return the data retrieved from the flag map
   */
  public T retrieve(Player player, Map<String, String> flagMap) {
    String value = flagMap.get(getKey());
    if (value == null) {
      return null;
    } else {
      return parser.apply(player, value);
    }
  }

}
