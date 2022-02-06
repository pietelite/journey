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
import dev.pietelite.journey.common.data.PersonalEndpointManager;
import dev.pietelite.journey.common.navigation.Cell;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A combination of an endpoint manager for SQL and a personal endpoint manager.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public abstract class SqlPersonalEndpointManager<T extends Cell<T, D>, D>
    extends SqlEndpointManager<T, D>
    implements PersonalEndpointManager<T, D> {

  /**
   * Default constructor.
   *
   * @param connectionController the connection controller
   * @param dataAdapter          the data adapter
   */
  public SqlPersonalEndpointManager(SqlConnectionController connectionController,
                                    DataAdapter<T, D> dataAdapter) {
    super(connectionController, dataAdapter);
  }

  @Override
  public void addPersonalEndpoint(@NotNull UUID playerUuid, @NotNull T cell)
      throws IllegalArgumentException, DataAccessException {
    this.addEndpoint(playerUuid, cell);
  }

  @Override
  public void addPersonalEndpoint(@NotNull UUID playerUuid, @NotNull T cell, @NotNull String name)
      throws IllegalArgumentException, DataAccessException {
    this.addEndpoint(playerUuid, cell, name);
  }

  @Override
  public void removePersonalEndpoint(@NotNull UUID playerUuid, @NotNull T cell)
      throws DataAccessException {
    this.removeEndpoint(playerUuid, cell);
  }

  @Override
  public void removePersonalEndpoint(@NotNull UUID playerUuid, @NotNull String name)
      throws DataAccessException {
    this.removeEndpoint(playerUuid, name);
  }

  @Override
  public @Nullable String getPersonalEndpointName(@NotNull UUID playerUuid, @NotNull T cell)
      throws DataAccessException {
    return this.getEndpointName(playerUuid, cell);
  }

  @Override
  public @Nullable T getPersonalEndpoint(@NotNull UUID playerUuid, @NotNull String name)
      throws DataAccessException {
    return this.getEndpoint(playerUuid, name);
  }

  @Override
  public Map<String, T> getPersonalEndpoints(@NotNull UUID playerUuid)
      throws DataAccessException {
    return this.getEndpoints(playerUuid);
  }
}
