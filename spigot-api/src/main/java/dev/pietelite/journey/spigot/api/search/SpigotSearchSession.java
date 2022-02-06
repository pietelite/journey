/*
 * MIT License
 *
 * Copyright 2022 Pieter Svenson
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

package dev.pietelite.journey.spigot.api.search;

import dev.pietelite.journey.common.search.DestinationGoalSearchSession;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import java.util.Objects;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;

/**
 * A search session for the use of plugins using the Journey API.
 */
public abstract class SpigotSearchSession extends DestinationGoalSearchSession<LocationCell, World> {

  /**
   * General constructor.
   *
   * @param callerId    the identifier for the caller
   * @param callerType  the type of caller
   * @param origin      origination location
   * @param destination destination location
   */
  public SpigotSearchSession(UUID callerId, Caller callerType,
                             Location origin, Location destination) {
    super(callerId, callerType,
        new LocationCell(origin), new LocationCell(destination),
        (x, y, z, domainId) -> new LocationCell(x, y, z, Objects.requireNonNull(Bukkit.getWorld(domainId))));
  }
}
