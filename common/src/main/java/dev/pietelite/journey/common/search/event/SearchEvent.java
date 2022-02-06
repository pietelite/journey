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
import dev.pietelite.journey.common.search.SearchSession;
import java.util.Date;

/**
 * A search event. These are the events that are dispatched from the
 * execution of a {@link SearchSession} using the {@link SearchDispatcher}.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public abstract class SearchEvent<T extends Cell<T, D>, D> {

  public static int ID = 4;
  private final SearchSession<T, D> session;
  private final Date date = new Date();

  /**
   * General constructor.
   *
   * @param session the session that caused this event
   */
  public SearchEvent(SearchSession<T, D> session) {
    this.session = session;
  }

  /**
   * Get the session causing this event.
   *
   * @return the search session
   */
  public SearchSession<T, D> getSession() {
    return session;
  }

  /**
   * Get the date the event was created and dispatched. This is useful for data storage.
   *
   * @return the date
   */
  public Date getDate() {
    return date;
  }

  abstract EventType type();

  /**
   * An enumeration of all possible event types.
   * Every search event must have a unique type, found in this enumeration.
   * This value is used for keying purposes upon registration.
   */
  public enum EventType {
    FOUND_SOLUTION,
    IGNORE_CACHE,
    MODE_FAILURE,
    MODE_SUCCESS,
    START_ITINERARY,
    START_PATH,
    START,
    STEP,
    STOP_ITINERARY,
    STOP_PATH,
    STOP,
    VISITATION
  }

}
