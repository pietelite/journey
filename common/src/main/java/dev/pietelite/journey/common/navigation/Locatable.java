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
import org.jetbrains.annotations.NotNull;

/**
 * An abstraction of a location within a Minecraft world.
 *
 * @param <T> This implementation class. This is needed for self-reference.
 * @param <D> The domain, as in a Minecraft world
 */
public interface Locatable<T extends Locatable<T, D>, D> extends Serializable {

  /**
   * Get the cartesian from one locatable to another, ignoring domain.
   * For comparisons between distances, use {@link #distanceToSquared(Locatable)}
   * because it is easier to compute.
   *
   * @param other the other locatable
   * @return the cartesian distance
   */
  default double distanceTo(T other) {
    return Math.sqrt(distanceToSquared(other));
  }

  /**
   * Get the square of the cartesian distance from one locatable to another.
   * This is much easier to compute than the actual distance because
   * you avoid a square root.
   *
   * @param other the other locatable
   * @return the square of the cartesian distance
   */
  double distanceToSquared(T other);

  /**
   * Get the domain, usually the Minecraft world.
   *
   * @return the domain of this locatable
   */
  @NotNull
  D getDomain();

}
