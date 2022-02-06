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

/**
 * An event dispatched when a {@link SearchSession} stops running.
 * This happens when the search event stops naturally because of a
 * successful or failed result, or if the session was canceled before
 * completion.
 *
 * @param <T> the location type
 * @param <D> the destination type
 */
public class StopSearchEvent<T extends Cell<T, D>, D> extends SearchEvent<T, D> {

  /**
   * General constructor.
   *
   * @param session the session
   */
  public StopSearchEvent(SearchSession<T, D> session) {
    super(session);
  }

  @Override
  EventType type() {
    return EventType.STOP;
  }

}
