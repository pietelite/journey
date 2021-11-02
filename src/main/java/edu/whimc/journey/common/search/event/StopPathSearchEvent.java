/*
 * MIT License
 *
 * Copyright 2021 Pieter Svenson
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
 *
 */

package edu.whimc.journey.common.search.event;

import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.search.FlexiblePathTrial;
import edu.whimc.journey.common.search.PathTrial;
import edu.whimc.journey.common.search.SearchSession;

/**
 * An event dispatched when a {@link PathTrial} stops searching
 * for a {@link edu.whimc.journey.common.navigation.Path}.
 *
 * @param <T> the location type
 * @param <D> the domain type
 * @see SearchDispatcher
 * @see SearchSession
 */
public class StopPathSearchEvent<T extends Cell<T, D>, D> extends SearchEvent<T, D> {

  private final FlexiblePathTrial<T, D> pathTrial;

  /**
   * General constructor.
   *
   * @param session   the session
   * @param pathTrial the path trial causing this event
   */
  public StopPathSearchEvent(SearchSession<T, D> session, FlexiblePathTrial<T, D> pathTrial) {
    super(session);
    this.pathTrial = pathTrial;
  }

  /**
   * The path trial being run when this event was dispatched.
   *
   * @return the path trial
   */
  public FlexiblePathTrial<T, D> getPathTrial() {
    return pathTrial;
  }

  @Override
  EventType type() {
    return EventType.STOP_PATH;
  }
}
