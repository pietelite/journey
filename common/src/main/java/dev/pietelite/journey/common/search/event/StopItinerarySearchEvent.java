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
import dev.pietelite.journey.common.search.ItineraryTrial;
import dev.pietelite.journey.common.search.SearchSession;

/**
 * An event dispatched when an {@link ItineraryTrial} stops searching
 * for an {@link Itinerary}, no matter if
 * the result was successful, a failure, or canceled.
 *
 * @param <T> the location type
 * @param <D> the domain type
 * @see SearchSession
 * @see SearchDispatcher
 */
public class StopItinerarySearchEvent<T extends Cell<T, D>, D> extends SearchEvent<T, D> {

  private final ItineraryTrial<T, D> itineraryTrial;

  /**
   * General constructor.
   *
   * @param session        the session
   * @param itineraryTrial the itinerary trial
   */
  public StopItinerarySearchEvent(SearchSession<T, D> session, ItineraryTrial<T, D> itineraryTrial) {
    super(session);
    this.itineraryTrial = itineraryTrial;
  }

  /**
   * The itinerary trial that caused this event.
   *
   * @return the itinerary trial
   */
  public ItineraryTrial<T, D> getItineraryTrial() {
    return itineraryTrial;
  }

  @Override
  EventType type() {
    return EventType.STOP_ITINERARY;
  }
}