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

package dev.pietelite.journey.spigot.api.navigation;

import dev.pietelite.journey.common.navigation.Cell;
import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Function;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.jetbrains.annotations.NotNull;

/**
 * The Spigot Minecraft implementation of a {@link Cell}.
 * This, along with {@link World} as the domain,
 * allows the general spigot implementation to use all the generified
 * common Journey packages.
 */
public class LocationCell extends Cell<LocationCell, World> {

  /**
   * The offset above the block location (for solid inhabitable blocks).
   */
  @Getter
  private final double heightOffset;

  /**
   * Constructor using all necessary raw data with a given domain object (world).
   *
   * @param x     the x coordinate
   * @param y     the y coordinate
   * @param z     the z coordinate
   * @param world the domain
   */
  public LocationCell(int x, double y, int z, @NotNull World world) {
    this(x, y, z, world.getUID());
  }

  /**
   * Constructor using a Spigot location.
   *
   * @param location the location
   */
  public LocationCell(Location location) {
    this(location.getBlockX(),
        location.getBlockY(),
        location.getBlockZ(),
        Objects.requireNonNull(location.getWorld()));
  }

  /**
   * Constructor using all necessary raw data with a given identifier of the domain (world).
   *
   * @param x         the x coordinate
   * @param y         the y coordinate
   * @param z         the z coordinate
   * @param worldUuid the identifier of the world
   */
  public LocationCell(int x, double y, int z, @NotNull UUID worldUuid) {
    super(x, (int) y, z, worldUuid.toString(), new UuidToWorld());
    this.heightOffset = y - this.getY();
  }

  @Override
  public double distanceToSquared(LocationCell other) {
    return vectorSizeSquared(this.coordinateX - other.coordinateX,
        this.coordinateY - other.coordinateY,
        this.coordinateZ - other.coordinateZ);
  }

  /**
   * Get the Minecraft block at this location.
   *
   * @return the block
   */
  public Block getBlock() {
    return this.getDomain().getBlockAt(this.coordinateX, this.coordinateY, this.coordinateZ);
  }

  /**
   * Make a Spigot Minecraft location, built from the raw data stored in this cell.
   *
   * @return the location
   */
  public Location toLocation() {
    return new Location(this.getDomain(),
        this.coordinateX,
        this.coordinateY,
        this.coordinateZ);
  }

  /**
   * Get the Minecraft block at some location offset from this location.
   *
   * @param x the x coordinate offset
   * @param y the y coordinate offset
   * @param z the z coordinate offset
   * @return the block
   */
  public Block getBlockAtOffset(int x, int y, int z) {
    return this.getDomain().getBlockAt(this.coordinateX + x, this.coordinateY + y, this.coordinateZ + z);
  }

  /**
   * Create another location cell at an offset.
   *
   * @param x the x coordinate offset
   * @param y the y coordinate offset
   * @param z the z coordinate offset
   * @return the offset location cell
   */
  public LocationCell createCellAtOffset(int x, double y, int z) {
    return new LocationCell(this.coordinateX + x,
        this.coordinateY + heightOffset + y,
        this.coordinateZ + z,
        this.getDomain());
  }

  private double vectorSizeSquared(int distX, int distY, int distZ) {
    return distX * distX + distY * distY + distZ * distZ;
  }

  @Override
  public String toString() {
    return String.format("(%d, %d, %d) in %s",
        this.coordinateX, this.coordinateY, this.coordinateZ,
        this.getDomain().getName());
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    LocationCell that = (LocationCell) o;
    return this.coordinateX == that.coordinateX
        && this.coordinateY == that.coordinateY
        && this.coordinateZ == that.coordinateZ
        && this.getDomain().equals(that.getDomain());
  }

  @Override
  public int hashCode() {
    return Objects.hash(this.coordinateX, this.coordinateY, this.coordinateZ, this.getDomain());
  }

  /**
   * A serializable class that computes a string version of a Spigot World identifier
   * to a Spigot World java object.
   */
  public static class UuidToWorld implements Function<String, World>, Serializable {

    @Override
    public World apply(String s) {
      World world = Bukkit.getWorld(UUID.fromString(s));
      if (world == null) {
        throw new IllegalStateException("There is no world with UUID " + s);
      }
      return world;
    }

  }

}
