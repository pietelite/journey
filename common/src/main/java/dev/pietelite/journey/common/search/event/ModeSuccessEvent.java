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
import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.search.SearchSession;

/**
 * An event dispatched by the search process when a mode is found to be successful
 * in stepping a new location.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public class ModeSuccessEvent<T extends Cell<T, D>, D> extends SearchEvent<T, D> {

  private final T cell;
  private final ModeType modeType;

  /**
   * General constructor.
   *
   * @param session  the search session
   * @param cell     the location
   * @param modeType the type of mode
   */
  public ModeSuccessEvent(SearchSession<T, D> session, T cell, ModeType modeType) {
    super(session);
    this.cell = cell;
    this.modeType = modeType;
  }

  /**
   * Get the cell that we can reach.
   *
   * @return the cell
   */
  public T getCell() {
    return cell;
  }

  /**
   * Get the type of mode.
   *
   * @return the mode type
   */
  public ModeType getModeType() {
    return modeType;
  }

  @Override
  EventType type() {
    return EventType.MODE_SUCCESS;
  }
}
