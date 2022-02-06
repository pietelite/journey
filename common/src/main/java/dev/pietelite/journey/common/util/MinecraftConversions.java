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

package dev.pietelite.journey.common.util;

import dev.pietelite.journey.common.navigation.Cell;

/**
 * A template to convert various types to values understood by a database.
 *
 * @param <T> the cell type
 * @param <D> the domain type
 * @deprecated This was mostly useful when trying to test the waters with some
 *             deep net work for trying to calculate things using machine learning principles.
 *             This is likely no longer needed.
 */
public interface MinecraftConversions<T extends Cell<T, D>, D> {

  /**
   * Get the ordinal type for a Minecraft biome.
   *
   * @param cell the cell
   * @return the representation for a biome
   */
  int getBiome(T cell);

  /**
   * Get the ordinal type for a Minecraft dimension.
   *
   * @param domain the domain
   * @return the representation for a dimension
   */
  int getDimension(D domain);

}
