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
import dev.pietelite.journey.common.navigation.Step;
import dev.pietelite.journey.common.search.PathTrial;
import dev.pietelite.journey.common.search.SearchSession;

/**
 * An even to be dispatched when the operation of a
 * {@link PathTrial}
 * takes the next-best location in an attempt to calculate the best path.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public class StepSearchEvent<T extends Cell<T, D>, D> extends SearchEvent<T, D> {

  private final Step<T, D> step;

  /**
   * General constructor.
   *
   * @param session the session
   * @param step    the step in the algorithm taken as this event was dispatched
   */
  public StepSearchEvent(SearchSession<T, D> session, Step<T, D> step) {
    super(session);
    this.step = step;
  }

  /**
   * Get the step of this event.
   *
   * @return the step
   */
  public Step<T, D> getStep() {
    return step;
  }

  @Override
  EventType type() {
    return EventType.STEP;
  }
}
