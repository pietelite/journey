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

package dev.pietelite.journey.spigot.navigation;

import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.navigation.Port;
import dev.pietelite.journey.common.tools.Verifiable;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.util.NetherUtil;
import java.util.Objects;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * A port that represents a Minecraft Nether Portal.
 */
public final class NetherPort extends Port<LocationCell, World> implements Verifiable {

  public static final int NETHER_PORT_LENGTH = 16;
  private final LocationCell origin;
  private final LocationCell destination;

  /**
   * General constructor.
   *
   * @param origin      the origin of the portal
   * @param destination the destination of the portal
   */
  public NetherPort(@NotNull final LocationCell origin, @NotNull final LocationCell destination) {
    super(origin, destination, ModeType.NETHER_PORTAL, NETHER_PORT_LENGTH);
    this.origin = origin;
    this.destination = destination;
  }

  @Override
  public boolean verify() {
    return NetherUtil.locateAll(origin, 1).size() > 0
        && NetherUtil.locateAll(destination, 1).size() > 0;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    NetherPort that = (NetherPort) o;
    return getOrigin().equals(that.getOrigin()) && getDestination().equals(that.getDestination());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getOrigin(), getDestination());
  }

  @Override
  public String toString() {
    return "NetherLink: "
        + origin + " -> "
        + destination;
  }

}
