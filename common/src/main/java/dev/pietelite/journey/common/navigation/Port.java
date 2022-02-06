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

package dev.pietelite.journey.common.navigation;

import java.util.Collections;
import org.jetbrains.annotations.NotNull;

/**
 * A direct connection between otherwise unrelated locations in any domain.
 *
 * <p>In many cases, there are locations that can be reached from other locations in Minecraft.
 * Examples include teleportation commands and portals. A port is a generic representation for
 * such connections, and they allow the algorithm to calculate accurate solutions leveraging
 * all transportation aspects of a given Minecraft server.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public class Port<T extends Cell<T, D>, D> extends Path<T, D> implements Moded {

  private final ModeType modeType;

  /**
   * General constructor.
   *
   * @param origin      the origin of the port, like the entrance to a portal.
   * @param destination the end of the port, like the endpoint of a portal
   * @param modeType    the mode type that this movement counts as
   * @param length      the length of this movement, for use of calculating efficiency
   */
  public Port(@NotNull T origin, @NotNull T destination,
              @NotNull ModeType modeType, int length) {
    super(origin, Collections.singletonList(new Step<>(destination, 0, modeType)), length);
    this.modeType = modeType;
  }

  /**
   * Get a stationary port, as in, one that doesn't move in any direction
   * and is used as a placeholder to connect paths together.
   *
   * @param location the location, being the origin and destination of this port
   * @param <T>      the location type
   * @param <D>      the domain type
   * @return the port
   */
  public static <T extends Cell<T, D>, D> Port<T, D> stationary(T location) {
    return new Port<>(location, location, ModeType.NONE, 0);
  }

  @Override
  @NotNull
  public ModeType getModeType() {
    return modeType;
  }

}
