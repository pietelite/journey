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

import java.io.Serializable;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * An enumeration of all possible mode types we allow.
 */
public enum ModeType implements Serializable {

  /**
   * We actually don't move to get here.
   */
  NONE("none", false, 1),  // Origin, for example
  WALK("walk", true, 1 << 1),
  JUMP("jump", true, 1 << 2),
  SWIM("swim", true, 1 << 3),
  FLY("fly", true, 1 << 4),
  /**
   * Travel by boat.
   */
  BOAT("boat", false, 1 << 5),
  /**
   * Travel by mine cart.
   */
  RAIL("rail", false, 1 << 6),
  /**
   * Travel by building somewhere (across or upward, by creating a platform).
   */
  BUILD("build", false, 1 << 7),
  /**
   * Travel by passing through a door.
   */
  DOOR("door", true, 1 << 8),
  /**
   * Travel by climbing something, like a ladder or a vine.
   */
  CLIMB("climb", true, 1 << 9),
  /**
   * Travel through a nether portal.
   */
  NETHER_PORTAL("netherportal", false, 1 << 10),
  /**
   * Travel by typing a command.
   */
  COMMAND("command", true, 1 << 11),
  /**
   * Travel through some generic port.
   */
  PORT("port", false, 1 << 12);

  @Getter
  private final String id;

  @Getter
  private final boolean common;

  @Getter
  private final long accumulationId;

  ModeType(@NotNull String id, boolean common, long accumulationId) {
    this.id = id;
    this.common = common;
    this.accumulationId = accumulationId;
  }

  @Override
  public String toString() {
    return "ModeType{" + "id='"
        + id + '\'' + '}';
  }
}
