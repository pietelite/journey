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

package edu.whimc.journey.spigot.search.event;

import edu.whimc.journey.common.search.event.SearchEvent;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;
import org.bukkit.event.Event;

/**
 * The general Spigot implementation of a {@link SearchEvent}.
 *
 * @param <S> the type of common event that an instantiation of this class
 *            would encapsulate
 */
public abstract class SpigotSearchEvent<S extends SearchEvent<LocationCell, World>> extends Event {

  private final S searchEvent;

  /**
   * General constructor.
   *
   * @param event the common event
   */
  public SpigotSearchEvent(S event) {
    super(true);
    this.searchEvent = event;
  }

  /**
   * Get the common search event for this event.
   *
   * @return the common event
   */
  public S getSearchEvent() {
    return searchEvent;
  }

}
