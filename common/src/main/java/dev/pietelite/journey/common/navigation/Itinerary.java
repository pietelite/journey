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

import dev.pietelite.journey.common.search.ItineraryTrial;
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.common.tools.AlternatingList;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * A description of all step required to move from some arbitrary origin
 * to some arbitrary destination.
 *
 * <p>The result of a {@link SearchSession} calculation.
 *
 * @param <T> the location type
 * @param <D> the domain type
 * @see SearchSession
 * @see ItineraryTrial
 * @see Journey
 */
public final class Itinerary<T extends Cell<T, D>, D> {

  private final T origin;
  private final ArrayList<Step<T, D>> steps;
  private final AlternatingList<Port<T, D>, Path<T, D>, Path<T, D>> stages;
  private final double length;

  /**
   * General constructor.
   *
   * @param origin the origin of the itinerary
   * @param steps  the steps to get to the destination
   * @param stages the list of stages to complete to traverse this itinerary
   * @param length the length of the entire thing
   */
  public Itinerary(T origin,
                   Collection<Step<T, D>> steps,
                   AlternatingList<Port<T, D>, Path<T, D>, Path<T, D>> stages,
                   double length) {
    this.origin = origin;
    this.steps = new ArrayList<>(steps);
    this.stages = stages;
    this.length = length;
  }

  /**
   * Get the first location of the entire itinerary.
   *
   * @return the origin
   */
  public T getOrigin() {
    return origin;
  }

  /**
   * Get all the steps to complete the itinerary in list form.
   *
   * @return the steps
   */
  public ArrayList<Step<T, D>> getSteps() {
    return new ArrayList<>(steps);
  }

  /**
   * Get the stages to complete the itinerary, separating out paths
   * from their connecting ports.
   *
   * @return the stages
   */
  public AlternatingList<Port<T, D>, Path<T, D>, Path<T, D>> getStages() {
    return stages;
  }

  /**
   * Get the length of the whole itinerary.
   *
   * @return the length
   */
  public double getLength() {
    return length;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Itinerary<?, ?> itinerary = (Itinerary<?, ?>) o;
    return Double.compare(itinerary.length, length) == 0
        && Objects.equals(origin, itinerary.origin)
        && Objects.equals(steps, itinerary.steps);
  }

  @Override
  public int hashCode() {
    return Objects.hash(origin, steps, length);
  }
}
