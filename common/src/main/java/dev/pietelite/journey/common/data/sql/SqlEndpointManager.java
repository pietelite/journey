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
import dev.pietelite.journey.common.navigation.Cell;
import dev.pietelite.journey.common.util.Validator;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager for storing endpoints of search sessions in SQL.
 *
 * @param <T> the cell type
 * @param <D> the domain type
 */
public abstract class SqlEndpointManager<T extends Cell<T, D>, D> extends SqlManager<T, D> {

  private static final String ENDPOINT_TABLE_NAME = "journey_endpoints";

  /**
   * Default constructor.
   *
   * @param connectionController a controller for connecting to a SQL database
   * @param dataAdapter          a conversion controller to serialize and deserialize data from Journey
   */
  public SqlEndpointManager(SqlConnectionController connectionController, DataAdapter<T, D> dataAdapter) {
    super(connectionController, dataAdapter);
    createTables();
  }

  protected void addEndpoint(@Nullable UUID playerUuid, @NotNull T cell)
      throws IllegalArgumentException, DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      int endpointCount = getEndpoints(playerUuid, connection).size();
      addEndpoint(playerUuid, cell, String.valueOf(endpointCount + 1), connection, true);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }


  protected void addEndpoint(@Nullable UUID playerUuid,
                             @NotNull T cell,
                             @NotNull String name) throws IllegalArgumentException, DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      addEndpoint(playerUuid, cell, name, connection, false);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  private void addEndpoint(@Nullable UUID playerUuid,
                           @NotNull T cell,
                           @NotNull String name,
                           @NotNull Connection connection,
                           boolean forceValidName) throws SQLException {
    if (!forceValidName && Validator.isInvalidDataName(name)) {
      throw new IllegalArgumentException("The given name is not valid: " + name);
    }
    PreparedStatement statement = connection.prepareStatement(String.format(
        "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s) VALUES (?, ?, ?, ?, ?, ?, ?, ?);",
        ENDPOINT_TABLE_NAME,
        "player_uuid",
        "name_id",
        "name",
        "world_uuid",
        "x",
        "y",
        "z",
        "timestamp"));

    statement.setString(1, playerUuid == null ? null : playerUuid.toString());
    statement.setString(2, name.toLowerCase());
    statement.setString(3, name);
    statement.setString(4, getDataAdapter().getDomainIdentifier(cell.getDomain()));
    statement.setInt(5, cell.getX());
    statement.setInt(6, cell.getY());
    statement.setInt(7, cell.getZ());
    statement.setLong(8, System.currentTimeMillis() / 1000);

    statement.execute();

  }

  protected void removeEndpoint(@Nullable UUID playerUuid, @NotNull T cell) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s %s ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          ENDPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "world_uuid",
          "x",
          "y",
          "z"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, getDataAdapter().getDomainIdentifier(cell.getDomain()));
      statement.setInt(3, cell.getX());
      statement.setInt(4, cell.getY());
      statement.setInt(5, cell.getZ());

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  protected void removeEndpoint(@Nullable UUID playerUuid, @NotNull String name) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "DELETE FROM %s WHERE %s %s ? AND %s = ?;",
          ENDPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "name_id"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, name.toLowerCase());

      statement.execute();
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Nullable
  protected T getEndpoint(@Nullable UUID playerUuid, @NotNull String name) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s %s ? AND %s = ?;",
          ENDPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "name_id"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, name.toLowerCase());

      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return getDataAdapter().makeCell(resultSet.getInt("x"),
            resultSet.getInt("y"),
            resultSet.getInt("z"),
            resultSet.getString("world_uuid"));
      } else {
        return null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  @Nullable
  protected String getEndpointName(@Nullable UUID playerUuid, @NotNull T cell) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
          "SELECT * FROM %s WHERE %s %s ? AND %s = ? AND %s = ? AND %s = ? AND %s = ?;",
          ENDPOINT_TABLE_NAME,
          "player_uuid",
          playerUuid == null ? "IS" : "=",
          "world_uuid",
          "x",
          "y",
          "z"));

      statement.setString(1, playerUuid == null ? null : playerUuid.toString());
      statement.setString(2, getDataAdapter().getDomainIdentifier(cell.getDomain()));
      statement.setInt(3, cell.getX());
      statement.setInt(4, cell.getY());
      statement.setInt(5, cell.getZ());

      ResultSet resultSet = statement.executeQuery();
      if (resultSet.next()) {
        return resultSet.getString("name");
      } else {
        return null;
      }
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  protected Map<String, T> getEndpoints(@Nullable UUID playerUuid) throws DataAccessException {
    try (Connection connection = getConnectionController().establishConnection()) {
      return getEndpoints(playerUuid, connection);
    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }
  }

  /**
   * Get endpoints on a pre-established connection to save database connection time.
   *
   * @param playerUuid the player' uuid
   * @param connection the connection object
   * @return the map of endpoints
   * @throws SQLException if sql error occurs
   */
  private Map<String, T> getEndpoints(@Nullable UUID playerUuid,
                                      @NotNull Connection connection) throws SQLException {
    PreparedStatement statement = connection.prepareStatement(String.format(
        "SELECT * FROM %s WHERE %s %s ?;",
        ENDPOINT_TABLE_NAME,
        "player_uuid",
        playerUuid == null ? "IS" : "="));

    statement.setString(1, playerUuid == null ? null : playerUuid.toString());

    ResultSet resultSet = statement.executeQuery();
    Map<String, T> endpoints = new HashMap<>();
    while (resultSet.next()) {
      endpoints.put(resultSet.getString("name"),
          getDataAdapter().makeCell(resultSet.getInt("x"),
              resultSet.getInt("y"),
              resultSet.getInt("z"),
              resultSet.getString("world_uuid")));
    }
    return endpoints;
  }

  protected void createTables() {
    try (Connection connection = getConnectionController().establishConnection()) {
      String tableStatement = "CREATE TABLE IF NOT EXISTS "
          + ENDPOINT_TABLE_NAME + " ("
          + "player_uuid char(36), "
          + "name_id varchar(32) NOT NULL, "
          + "name varchar(32) NOT NULL, "
          + "world_uuid char(36) NOT NULL, "
          + "x int(7) NOT NULL, "
          + "y int(7) NOT NULL, "
          + "z int(7) NOT NULL, "
          + "timestamp integer NOT NULL"
          + ");";
      connection.prepareStatement(tableStatement).execute();

      String indexStatement = "CREATE INDEX IF NOT EXISTS player_uuid_idx ON "
          + ENDPOINT_TABLE_NAME
          + " (player_uuid);";
      connection.prepareStatement(indexStatement).execute();
    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
