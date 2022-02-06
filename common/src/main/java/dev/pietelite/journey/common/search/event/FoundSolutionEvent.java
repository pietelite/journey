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

package dev.pietelite.journey.common.search.event;

import dev.pietelite.journey.common.navigation.Cell;
import dev.pietelite.journey.common.navigation.Itinerary;
import dev.pietelite.journey.common.search.SearchSession;

/**
 * An event that is dispatched when a new solution is found during a path search.
 *
 * @param <T> the location type
 * @param <D> the domain type
 * @see SearchSession
 * @see SearchDispatcher
 */
public class FoundSolutionEvent<T extends Cell<T, D>, D> extends SearchEvent<T, D> {

  private final Itinerary<T, D> itinerary;
  private final long executionTime;

  /**
   * General constructor.
   *
   * @param session   the session
   * @param itinerary the solution to the search
   */
  public FoundSolutionEvent(SearchSession<T, D> session, Itinerary<T, D> itinerary) {
    super(session);
    this.itinerary = itinerary;
    this.executionTime = this.getSession().executionTime();
  }

  /**
   * Get the solution to the search.
   *
   * @return the solution
   */
  public Itinerary<T, D> getItinerary() {
    return this.itinerary;
  }

  /**
   * Get how long the session had been searching up until this event was dispatched.
   *
   * @return the length of time of execution
   */
  public final long getExecutionTime() {
    return executionTime;
  }

  @Override
  EventType type() {
    return EventType.FOUND_SOLUTION;
  }

}
