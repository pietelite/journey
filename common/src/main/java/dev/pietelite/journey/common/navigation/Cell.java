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

package dev.pietelite.journey.common.navigation;

import java.io.Serializable;
import java.util.Objects;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * A generic unit of a 3-dimensional grid (domain).
 *
 * @param <T> the cell type, for self-reference purposes
 * @param <D> the domain type
 */
public abstract class Cell<T extends Cell<T, D>, D> implements Locatable<T, D>, Serializable {

  protected final int coordinateX;
  protected final int coordinateY;
  protected final int coordinateZ;
  protected final String domainId;
  protected final Function<String, D> domainFunction;
  private transient D domain;

  /**
   * General constructor.
   *
   * @param x              the X coordinate
   * @param y              the Y coordinate
   * @param z              the Z coordinate
   * @param domainId       the identifier for the domain
   * @param domainFunction the function that converts an identifier into a domain object
   */
  public Cell(int x, int y, int z, @NotNull String domainId, @NotNull Function<String, D> domainFunction) {
    this.coordinateX = x;
    this.coordinateY = y;
    this.coordinateZ = z;
    this.domainId = Objects.requireNonNull(domainId);
    this.domainFunction = Objects.requireNonNull(domainFunction);
  }

  @Override
  public abstract double distanceToSquared(T other);

  /**
   * Get X coordinate.
   *
   * @return x coordinate
   */
  public final int getX() {
    return coordinateX;
  }

  /**
   * Get Y coordinate.
   *
   * @return y coordinate
   */
  public final int getY() {
    return coordinateY;
  }

  /**
   * Get Z coordinate.
   *
   * @return z coordinate
   */
  public final int getZ() {
    return coordinateZ;
  }

  @Override
  @NotNull
  public D getDomain() {
    if (domain == null) {
      domain = Objects.requireNonNull(this.domainFunction.apply(domainId));
    }
    return domain;
  }

  /**
   * Get the id of the domain.
   *
   * @return the domain id
   */
  public final String getDomainId() {
    return domainId;
  }

  /**
   * An object that can create a type of cell.
   *
   * @param <T> the type of cell
   * @param <D> the type of domain
   */
  @FunctionalInterface
  public interface CellConstructor<T extends Cell<T, D>, D> {

    /**
     * Create a new cell.
     *
     * @param x        the x location
     * @param y        the y location
     * @param z        the z location
     * @param domainId the id of the domain
     * @return the new cell
     */
    T construct(int x, int y, int z, String domainId);

  }

}
