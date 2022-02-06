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
import java.util.ArrayList;
import java.util.Collection;
import org.jetbrains.annotations.NotNull;

/**
 * A series of steps that determine a possible path through a Minecraft world.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public class Path<T extends Cell<T, D>, D> implements Serializable {

  private final T origin;
  private final ArrayList<Step<T, D>> steps;
  private final double length;

  /**
   * General constructor.
   *
   * @param origin the origin of the whole path
   * @param steps  the steps required to get there
   * @param length the length of the path
   */
  public Path(T origin, @NotNull Collection<Step<T, D>> steps, double length) {
    this.origin = origin;
    this.steps = new ArrayList<>(steps);
    this.length = length;
  }

  /**
   * Create an invalid path.
   *
   * @param <A> the location type
   * @param <B> the domain type
   * @return an invalid path (infinite length)
   */
  @NotNull
  public static <A extends Cell<A, B>, B> Path<A, B> invalid() {
    return new Path<>(null, new ArrayList<>(), Double.MAX_VALUE);
  }

  /**
   * Get the origin of the path.
   *
   * @return the origin location
   */
  @NotNull
  public final T getOrigin() {
    return origin;
  }

  /**
   * Get the destination of the path.
   *
   * @return the destination location
   */
  @NotNull
  public final T getDestination() {
    if (steps.isEmpty()) {
      throw new IllegalStateException("There is no destination for a path with no steps.");
    }
    return steps.get(steps.size() - 1).location();
  }

  /**
   * Get a copy of all the steps in this path.
   *
   * @return the steps
   */
  @NotNull
  public final ArrayList<Step<T, D>> getSteps() {
    return new ArrayList<>(steps);
  }

  /**
   * Get the length of the path.
   *
   * @return the path length
   */
  public final double getLength() {
    return length;
  }

  /**
   * Check if the existing at the given location constitutes the completion
   * of this path if traversed by some entity.
   * For example, if a player was walking along a path, they would be considered
   * to have reached the end if this returned true with their current location input.
   *
   * @param location the location to test
   * @return true if the path has been completed
   */
  public boolean completeWith(T location) {
    return location.distanceToSquared(getDestination()) < 4;
  }

  /**
   * Verify if the path is still valid and traversable with the given mode collection.
   *
   * @param modes all modes
   * @return true if the path can be traversed, or false if it is impassable
   */
  public boolean test(Collection<Mode<T, D>> modes) {
    stepLoop:
    for (int i = 0; i < steps.size() - 1; i++) {
      for (Mode<T, D> mode : modes) {
        for (Mode<T, D>.Option option : mode.getDestinations(steps.get(i).location())) {
          if (steps.get(i + 1).location().equals(option.getLocation())) {
            continue stepLoop;  // we found a mode that gave us a fitting option. Continue to the next step.
          }
        }
      }
      // We've exhausted all modes (and each mode's options) for this step, so fail.
      return false;
    }
    return true;
  }

  /**
   * Get domain of this path (there is only one!).
   *
   * @return the domain
   */
  public D getDomain() {
    return origin.getDomain();
  }
}
