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

package dev.pietelite.journey.common.data;

import dev.pietelite.journey.common.navigation.Cell;

/**
 * An interface for describing what is needed to store the state for this application.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public interface DataManager<T extends Cell<T, D>, D> {

  /**
   * Get the implementation for the endpoint manager
   * specifically for personal endpoints in the search algorithm.
   *
   * @return the personal endpoint manager
   */
  PersonalEndpointManager<T, D> getPersonalEndpointManager();

  /**
   * Get the implementation for the endpoint manager
   * specifically for public endpoints in the search algorithm.
   *
   * @return the public endpoint manager
   */
  PublicEndpointManager<T, D> getPublicEndpointManager();

  /**
   * Get the implementation for the storage of
   * {@link PathRecordManager.PathTrialRecord}s.
   *
   * @return the manager
   */
  PathRecordManager<T, D> getPathRecordManager();

}
