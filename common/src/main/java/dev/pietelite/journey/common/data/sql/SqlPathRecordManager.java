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

import dev.pietelite.journey.common.JourneyCommon;
import dev.pietelite.journey.common.data.DataAccessException;
import dev.pietelite.journey.common.data.PathRecordManager;
import dev.pietelite.journey.common.navigation.Cell;
import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.navigation.ModeTypeGroup;
import dev.pietelite.journey.common.navigation.Path;
import dev.pietelite.journey.common.navigation.Step;
import dev.pietelite.journey.common.search.FlexiblePathTrial;
import dev.pietelite.journey.common.search.PathTrial;
import dev.pietelite.journey.common.search.ScoringFunction;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Stack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A generic path record manager for SQL storage.
 *
 * @param <T> the cell type
 * @param <D> the domain type
 */
public abstract class SqlPathRecordManager<T extends Cell<T, D>, D>
    extends SqlManager<T, D>
    implements PathRecordManager<T, D> {

  private static final String PATH_RECORD_TABLE_NAME = "path_record";
  private static final String PATH_RECORD_CELL_TABLE_NAME = "path_record_cell";
  private static final String PATH_RECORD_MODE_TABLE_NAME = "path_record_mode";

  /**
   * General constructor.
   *
   * @param connectionController the connection controller
   * @param dataAdapter          the adapter
   */
  public SqlPathRecordManager(SqlConnectionController connectionController, DataAdapter<T, D> dataAdapter) {
    super(connectionController, dataAdapter);
    createTables();
  }

  @Override
  public void report(PathTrial<T, D> trial,
                     Collection<FlexiblePathTrial.Node<T, D>> calculationNodes,
                     ModeTypeGroup modeTypeGroup,
                     long executionTime)
      throws DataAccessException {
    Path<T, D> path = trial.getPath();
    if (path == null) {
      throw new IllegalArgumentException("The path of he input path trial was not valid."
          + " The input path trial must be successful and have a valid path.");
    }

    /* TODO Add back this deletion mechanism
    // Delete previous record if it has the same origin/destination/world and is slower
    ModeTypeGroup modeTypes = ModeTypeGroup.from(trial.getModes());
    if (containsRecord(trial.getOrigin(), trial.getDestination(), modeTypes)) {
      // Let's delete the last one, so we can store the new one
      try (Connection connection = getConnectionController().establishConnection()) {
        connection.prepareStatement("DELETE FROM " + PATH_RECORD_TABLE_NAME
                + " WHERE "
                + "origin_x = " + trial.getOrigin().getX() + " AND "
                + "origin_y = " + trial.getOrigin().getY() + " AND "
                + "origin_z = " + trial.getOrigin().getZ() + " AND "
                + "destination_x = " + trial.getDestination().getX() + " AND "
                + "destination_y = " + trial.getDestination().getY() + " AND "
                + "destination_z = " + trial.getDestination().getZ() + " AND "
                + "world_uuid = '" + trial.getOrigin().getDomainId() + "'")
            .execute();
      } catch (SQLException e) {
        e.printStackTrace();
        throw new DataAccessException();
      }
    }
     */

    long pathReportId = -1;
    try (Connection connection = getConnectionController().establishConnection()) {
      PreparedStatement statement = connection.prepareStatement(String.format(
              "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
              PATH_RECORD_TABLE_NAME,
              "timestamp",
              "duration",
              "path_length",
              "origin_x",
              "origin_y",
              "origin_z",
              "destination_x",
              "destination_y",
              "destination_z",
              "world_uuid",
              "scoring_function"),
          Statement.RETURN_GENERATED_KEYS);

      statement.setLong(1, System.currentTimeMillis() / 1000);
      statement.setInt(2, (int) executionTime);
      statement.setDouble(3, trial.getLength());
      statement.setInt(4, trial.getOrigin().getX());
      statement.setInt(5, trial.getOrigin().getY());
      statement.setInt(6, trial.getOrigin().getZ());
      statement.setInt(7, trial.getDestination().getX());
      statement.setInt(8, trial.getDestination().getY());
      statement.setInt(9, trial.getDestination().getZ());
      statement.setString(10, getDataAdapter().getDomainIdentifier(trial.getDomain()));
      statement.setString(11, trial.getScoringFunction().getType().name());

      statement.execute();

      try (ResultSet generatedKeys = statement.getGeneratedKeys()) {
        if (generatedKeys.next()) {
          pathReportId = generatedKeys.getLong(1);
        }
      }

    } catch (SQLException e) {
      e.printStackTrace();
      throw new DataAccessException();
    }

    if (pathReportId < 0) {
      throw new DataAccessException("No id found from the inserted path record");
    }

    /*
     * Let's calculate the deviation of every node in this trial
     */

    final double totalDistance = trial.getOrigin().distanceTo(path.getDestination());
    final double deviationStepRatio = totalDistance / trial.getLength();
    final Map<T, Double> deviations = new HashMap<>();
    final Map<T, Integer> stepIndexes = new HashMap<>();  // For saving the critical nodes

    // Get all the deviations for everything on the critical path
    // Subtract deviation step to start because the first step is one step forward
    double currentDeviation = totalDistance;
    int i = 0;
    for (Step<T, D> step : path.getSteps()) {

      // Set the index of the critical nodes
      stepIndexes.put(step.location(), i++);

      currentDeviation -= deviationStepRatio * step.length();
      if (currentDeviation < 0.5) {
        currentDeviation = 0; // floor to 0 because we're basically at the end... no need for decimals anymore
      }
      deviations.put(step.location(), currentDeviation);
    }

    // Get all the rest of the deviations
    for (FlexiblePathTrial.Node<T, D> node : calculationNodes) {
      if (deviations.containsKey(node.getData().location())) {
        // We already know this deviation so continue
        continue;
      }

      // We don't know the deviation of this one, so check backwards until we find one that we know,
      // then work forwards again to add the deviations, which will keep increasing.
      // Basically, we will be working our way towards the heart of the tree (the destination),
      // then go back outwards to the leaves once we know how far away some point along the way
      // is from the heart.
      Stack<FlexiblePathTrial.Node<T, D>> stack = new Stack<>();
      FlexiblePathTrial.Node<T, D> current = node;
      while (!deviations.containsKey(current.getData().location())) {
        stack.add(current);
        current = current.getPrevious();
      }
      // We know what current's deviation is. Work forward now
      while (!stack.isEmpty()) {
        deviations.put(stack.peek().getData().location(),
            deviations.get(current.getData().location()) + stack.peek().getData().length());
        stack.pop();
      }
    }
    // We are done setting up the deviations
    assert deviations.size() == calculationNodes.size();

    for (FlexiblePathTrial.Node<T, D> node : calculationNodes) {
      try (Connection connection = getConnectionController().establishConnection()) {
        PreparedStatement statement = connection.prepareStatement(String.format(
            "INSERT INTO %s (%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s) "
                + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);",
            PATH_RECORD_CELL_TABLE_NAME,
            "path_record_id",
            "x", "y", "z",
            "critical",
            "path_index",
            "mode_type",
            "deviation",
            "distance",
            "distance_y",
            "biome",
            "dimension",
            "random"));

        statement.setLong(1, pathReportId);
        statement.setLong(2, node.getData().location().getX());
        statement.setLong(3, node.getData().location().getY());
        statement.setLong(4, node.getData().location().getZ());
        boolean critical = stepIndexes.containsKey(node.getData().location());
        statement.setBoolean(5, critical);
        statement.setObject(6, critical ? stepIndexes.get(node.getData().location()) : null);
        statement.setObject(7, node.getData().getModeType().ordinal());
        statement.setDouble(8, deviations.get(node.getData().location()));
        statement.setDouble(9, node.getData().location().distanceTo(path.getDestination()));
        statement.setInt(10, Math.abs(node.getData().location().getY() - path.getDestination().getY()));
        statement.setInt(11, JourneyCommon.<T, D>getConversions()
            .getBiome(node.getData().location()));
        statement.setInt(12, JourneyCommon.<T, D>getConversions()
            .getDimension(node.getData().location().getDomain()));
        statement.setDouble(13, Math.random());

        statement.execute();
      } catch (SQLException e) {
        e.printStackTrace();
        throw new DataAccessException();
      }
    }

    for (ModeType modeType : modeTypeGroup.getAll()) {
      try (Connection connection = getConnectionController().establishConnection()) {
        PreparedStatement statement = connection.prepareStatement(String.format(
            "INSERT INTO %s (%s, %s) VALUES (?, ?);",
            PATH_RECORD_MODE_TABLE_NAME,
            "path_record_id",
            "mode_type"));

        statement.setLong(1, pathReportId);
        statement.setInt(2, modeType.ordinal());

        statement.execute();
      } catch (SQLException e) {
        e.printStackTrace();
        throw new DataAccessException();
      }
    }

  }

  @Override
  public void clear() {
    // TODO implement
  }

  /**
   * Get all records that have this origin and destination,
   * but do not populate the internal cells, but <b>do</b> populate
   * the modes.
   *
   * @param origin      the origin location
   * @param destination the destination location
   * @return a list of all records
   */
  private List<PathTrialRecord> getRecordsWithoutCells(T origin, T destination) {
    try (Connection connection = getConnectionController().establishConnection()) {
      ResultSet recordResult = connection.prepareStatement("SELECT * FROM "
              + PATH_RECORD_TABLE_NAME
              + " WHERE "
              + "origin_x = " + origin.getX() + " AND "
              + "origin_y = " + origin.getY() + " AND "
              + "origin_z = " + origin.getZ() + " AND "
              + "destination_x = " + destination.getX() + " AND "
              + "destination_y = " + destination.getY() + " AND "
              + "destination_z = " + destination.getZ() + " AND "
              + "world_uuid = '" + origin.getDomainId() + "'")
          .executeQuery();
      List<PathTrialRecord> records = new LinkedList<>();
      while (recordResult.next()) {
        PathTrialRecord record = extractRecord(recordResult);
        ResultSet modeResult = connection.prepareStatement("SELECT * FROM "
            + PATH_RECORD_MODE_TABLE_NAME
            + " WHERE "
            + "path_record_id = " + record.id()).executeQuery();
        while (modeResult.next()) {
          record.modes().add(new PathTrialModeRecord(record,
              ModeType.values()[(modeResult.getInt("mode_type"))]));
        }
        records.add(record);
      }

      return records;
    } catch (SQLException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  @Override
  public @NotNull List<PathTrialRecord> getRecords(T origin, T destination) {
    try (Connection connection = getConnectionController().establishConnection()) {
      List<PathTrialRecord> emptyRecords = getRecordsWithoutCells(origin, destination);

      // Add the subcomponents (modes and cells) to the previously empty records
      for (PathTrialRecord emptyRecord : emptyRecords) {
        ResultSet cellResult = connection.prepareStatement("SELECT * FROM "
            + PATH_RECORD_CELL_TABLE_NAME
            + " WHERE "
            + "path_record_id = " + emptyRecord.id()).executeQuery();
        while (cellResult.next()) {
          emptyRecord.cells().add(extractCell(emptyRecord, cellResult));
        }
      }
      return emptyRecords;
    } catch (SQLException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  @Nullable
  private PathTrialRecord findRecordWithModes(Collection<PathTrialRecord> records, ModeTypeGroup modeTypes) {
    for (PathTrialRecord record : records) {
      if (record.modes()
          .stream()
          .map(PathTrialModeRecord::modeType)
          .allMatch(modeTypes::contains)) {
        return record;
      }
    }
    return null;
  }

  @Override
  public PathTrialRecord getRecord(T origin, T destination, ModeTypeGroup modeTypes) {
    return findRecordWithModes(getRecords(origin, destination), modeTypes);
  }

  private T toClass(PathTrialCellRecord cellRecord,
                    Cell.CellConstructor<T, D> cellConstructor) {
    return cellConstructor.construct(cellRecord.x(),
        cellRecord.y(),
        cellRecord.z(),
        cellRecord.record().worldId());
  }

  @Override
  public Path<T, D> getPath(T origin, T destination, ModeTypeGroup modeTypeGroup,
                            Cell.CellConstructor<T, D> constructor) {
    try (Connection connection = getConnectionController().establishConnection()) {
      PathTrialRecord record = findRecordWithModes(getRecordsWithoutCells(origin, destination),
          modeTypeGroup);

      ResultSet cellResult = connection.prepareStatement("SELECT * FROM "
          + PATH_RECORD_CELL_TABLE_NAME
          + " WHERE "
          + "path_record_id = " + record.id() + " AND "
          + "critical = TRUE").executeQuery();
      while (cellResult.next()) {
        record.cells().add(extractCell(record, cellResult));
      }

      record.cells().sort(Comparator.comparing(PathTrialCellRecord::index));


      LinkedList<Step<T, D>> steps = new LinkedList<>();

      // Add the first one because we don't move to get here
      steps.add(new Step<>(toClass(record.cells().get(0), constructor), 0, record.cells().get(0).modeType()));
      for (int i = 1; i < record.cells().size(); i++) {
        T cell = toClass(record.cells().get(i), constructor);
        steps.add(new Step<>(cell,
            cell.distanceTo(steps.getLast().location()),
            record.cells().get(i).modeType()));
      }

      return new Path<>(steps.getFirst().location(), steps, record.pathLength());
    } catch (SQLException e) {
      e.printStackTrace();
      return null;
    }
  }

  @Override
  public boolean containsRecord(T origin, T destination, ModeTypeGroup modeTypeGroup) {
    try (Connection connection = getConnectionController().establishConnection()) {
      return findRecordWithModes(getRecordsWithoutCells(origin, destination), modeTypeGroup) != null;
    } catch (SQLException e) {
      e.printStackTrace();
      return false;
    }
  }

  @Override
  public @NotNull Collection<PathTrialCellRecord> getAllCells() {
    try (Connection connection = getConnectionController().establishConnection()) {
      ResultSet result = connection.prepareStatement("SELECT * FROM "
          + PATH_RECORD_TABLE_NAME + " "
          + "JOIN " + PATH_RECORD_CELL_TABLE_NAME + " "
          + "ON " + PATH_RECORD_TABLE_NAME + ".id = "
          + PATH_RECORD_CELL_TABLE_NAME + ".path_record_id"
          + ";").executeQuery();
      List<PathTrialCellRecord> cells = new LinkedList<>();
      Map<Long, PathTrialRecord> records = new HashMap<>();
      while (result.next()) {
        PathTrialRecord record = extractRecord(result);
        if (records.containsKey(record.id())) {
          record = records.get(record.id());
        } else {
          records.put(record.id(), record);
        }
        cells.add(extractCell(record, result));
      }
      return cells;
    } catch (SQLException e) {
      e.printStackTrace();
      return Collections.emptyList();
    }
  }

  private PathTrialRecord extractRecord(final ResultSet resultSet) throws SQLException {
    return new PathTrialRecord(
        resultSet.getLong("id"),
        resultSet.getDate("timestamp"),
        resultSet.getLong("duration"),
        resultSet.getLong("path_length"),
        resultSet.getInt("origin_x"),
        resultSet.getInt("origin_y"),
        resultSet.getInt("origin_z"),
        resultSet.getInt("destination_x"),
        resultSet.getInt("destination_y"),
        resultSet.getInt("destination_z"),
        resultSet.getString("world_uuid"),
        ScoringFunction.Type.valueOf(resultSet.getString("scoring_function")),
        new LinkedList<>(),
        new LinkedList<>()
    );
  }

  private PathTrialCellRecord extractCell(final PathTrialRecord record,
                                          final ResultSet resultSet) throws SQLException {
    return new PathTrialCellRecord(
        record,
        resultSet.getInt("x"),
        resultSet.getInt("y"),
        resultSet.getInt("z"),
        resultSet.getBoolean("critical"),
        resultSet.getInt("path_index"),
        ModeType.values()[resultSet.getInt("mode_type")],
        resultSet.getDouble("deviation"),
        resultSet.getDouble("distance"),
        resultSet.getInt("distance_y"),
        resultSet.getInt("biome"),
        resultSet.getInt("dimension")
    );
  }

  protected void createTables() {
    try (Connection connection = getConnectionController().establishConnection()) {

      // Create table of path trials
      connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
          + PATH_RECORD_TABLE_NAME + " ("
          + "id integer PRIMARY KEY AUTOINCREMENT, "
          + "timestamp integer NOT NULL, "
          + "duration integer NOT NULL, "
          + "path_length double(12, 5) NOT NULL, "
          + "origin_x int(7) NOT NULL,"
          + "origin_y int(7) NOT NULL,"
          + "origin_z int(7) NOT NULL,"
          + "destination_x int(7) NOT NULL,"
          + "destination_y int(7) NOT NULL,"
          + "destination_z int(7) NOT NULL,"
          + "world_uuid char(36) NOT NULL, "
          + "scoring_function varchar(32) NOT NULL"
          + ");").execute();

      connection.prepareStatement("CREATE INDEX IF NOT EXISTS path_record_idx ON "
              + PATH_RECORD_TABLE_NAME
              + " (origin_x, origin_y, origin_z, destination_x, destination_y, destination_z, world_uuid);")
          .execute();

      // Create table of nodes within the path trial calculation
      connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
          + PATH_RECORD_CELL_TABLE_NAME + " ("
          + "path_record_id integer NOT NULL, "  // id of saved path trial (indexed)
          + "x int(7) NOT NULL,"  // x coordinate
          + "y int(7) NOT NULL,"  // y coordinate
          + "z int(7) NOT NULL,"  // z coordinate
          + "critical int(1) NOT NULL, "  // is this on the critical path (solution)
          + "path_index int(10), "  // what is the index of this critical node (if not critical, null)
          + "mode_type int(2) NOT NULL, "  // what is the mode type used to get here
          + "deviation double(12, 5) NOT NULL,"
          + "distance double(12, 5) NOT NULL,"  // the euclidean distance
          + "distance_y int(3) NOT NULL,"  // the cartesian distance in the y axis
          + "biome int NOT NULL, "  // the index of the biome
          + "dimension int NOT NULL, "  // the dimension index
          + "random double(12, 12) NOT NULL, "  // a random double for random selection (indexed)
          + "FOREIGN KEY (path_record_id) REFERENCES " + PATH_RECORD_TABLE_NAME + "(id)"
          + " ON DELETE CASCADE"
          + " ON UPDATE CASCADE"
          + ");").execute();

      connection.prepareStatement("CREATE INDEX IF NOT EXISTS cell_path_record_id_idx ON "
          + PATH_RECORD_CELL_TABLE_NAME
          + " (path_record_id);").execute();

      connection.prepareStatement("CREATE INDEX IF NOT EXISTS cell_random_idx ON "
          + PATH_RECORD_CELL_TABLE_NAME
          + " (random);").execute();

      connection.prepareStatement("CREATE TABLE IF NOT EXISTS "
          + PATH_RECORD_MODE_TABLE_NAME + " ("
          + "path_record_id integer NOT NULL, "
          + "mode_type int(2) NOT NULL, "
          + "FOREIGN KEY (path_record_id) REFERENCES " + PATH_RECORD_TABLE_NAME + "(id)"
          + " ON DELETE CASCADE"
          + " ON UPDATE CASCADE, "
          + "UNIQUE(path_record_id, mode_type)"
          + ");").execute();

      connection.prepareStatement("CREATE INDEX IF NOT EXISTS mode_path_record_id_idx ON "
          + PATH_RECORD_MODE_TABLE_NAME
          + " (path_record_id);").execute();

    } catch (SQLException e) {
      e.printStackTrace();
    }
  }

}
