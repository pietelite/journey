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

import edu.whimc.journey.common.search.event.ModeFailureEvent;
import edu.whimc.journey.spigot.navigation.LocationCell;
import org.bukkit.World;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

/**
 * The Spigot implementation of the {@link ModeFailureEvent}.
 */
public class SpigotModeFailureEvent extends SpigotSearchEvent<ModeFailureEvent<LocationCell, World>> {
  private static final HandlerList handlers = new HandlerList();

  /**
   * General constructor.
   *
   * @param event the common event
   */
  public SpigotModeFailureEvent(ModeFailureEvent<LocationCell, World> event) {
    super(event);
  }

  /**
   * Get handler list. Spigot standard.
   *
   * @return the handler list
   */
  public static HandlerList getHandlerList() {
    return handlers;
  }

  @Override
  @NotNull
  public HandlerList getHandlers() {
    return handlers;
  }
}
