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

package dev.pietelite.journey.common.data.sql;

import dev.pietelite.journey.common.data.DataAccessException;
import dev.pietelite.journey.common.data.PublicEndpointManager;
import dev.pietelite.journey.common.navigation.Cell;
import java.util.Map;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A public endpoint manager implemented for SQL.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public abstract class SqlPublicEndpointManager<T extends Cell<T, D>, D>
    extends SqlEndpointManager<T, D>
    implements PublicEndpointManager<T, D> {

  /**
   * General constructor.
   *
   * @param connectionController the connection controller
   * @param dataAdapter          the data adapter
   */
  public SqlPublicEndpointManager(SqlConnectionController connectionController,
                                  DataAdapter<T, D> dataAdapter) {
    super(connectionController, dataAdapter);
  }

  @Override
  public void addPublicEndpoint(@NotNull T cell, @NotNull String name)
      throws IllegalArgumentException, DataAccessException {
    addEndpoint(null, cell, name);
  }

  @Override
  public void removePublicEndpoint(@NotNull T cell) throws DataAccessException {
    removeEndpoint(null, cell);
  }

  @Override
  public void removePublicEndpoint(@NotNull String name) throws DataAccessException {
    removeEndpoint(null, name);
  }

  @Override
  public @Nullable String getPublicEndpointName(@NotNull T cell) throws DataAccessException {
    return getEndpointName(null, cell);
  }

  @Override
  public @Nullable T getPublicEndpoint(@NotNull String name) throws DataAccessException {
    return getEndpoint(null, name);
  }

  @Override
  public Map<String, T> getPublicEndpoints() throws DataAccessException {
    return getEndpoints(null);
  }
}
