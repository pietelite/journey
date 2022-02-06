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
import dev.pietelite.journey.common.navigation.Mode;
import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.navigation.ModeTypeGroup;
import dev.pietelite.journey.common.navigation.Path;
import dev.pietelite.journey.common.search.FlexiblePathTrial;
import dev.pietelite.journey.common.search.PathTrial;
import dev.pietelite.journey.common.search.ScoringFunction;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager to record paths during searches.
 *
 * @param <T> the cell type
 * @param <D> the domain type
 */
public interface PathRecordManager<T extends Cell<T, D>, D> {

  /**
   * Record a path trial.
   *
   * @param trial            the trial
   * @param calculationNodes get all the nodes used for this calculation
   * @param modeTypeGroup    group of modes used to calculate this path trial
   * @param executionTime    the time it took to execute
   * @throws DataAccessException when data is accessed incorrectly
   */
  void report(PathTrial<T, D> trial,
              Collection<FlexiblePathTrial.Node<T, D>> calculationNodes,
              ModeTypeGroup modeTypeGroup,
              long executionTime) throws DataAccessException;

  /**
   * Clear all records. <b>Dangerous!</b>
   */
  void clear();

  /**
   * Ger any records matching a start and end.
   *
   * @param origin      the original cell
   * @param destination the destination cell
   * @return any records
   */
  @NotNull
  List<PathTrialRecord> getRecords(T origin, T destination);

  /**
   * Get a specific record, if it exists.
   *
   * @param origin      the original cell
   * @param destination the destination cell
   * @param modeTypes   the mode types used to traverse to the destination
   * @return the record, if it exists
   */
  @Nullable
  PathTrialRecord getRecord(T origin, T destination, ModeTypeGroup modeTypes);

  /**
   * Get a specific path, using a constructor for cells.
   *
   * @param origin        the original cell
   * @param destination   the destination cell
   * @param modeTypeGroup the mode types used to traverse to the destination
   * @param constructor   the constructor to provide new cells
   * @return the new path
   */
  Path<T, D> getPath(T origin, T destination, ModeTypeGroup modeTypeGroup,
                     Cell.CellConstructor<T, D> constructor);

  /**
   * Return whether a record exists with the given critera.
   *
   * @param origin        the original cell
   * @param destination   the destination cell
   * @param modeTypeGroup the mode types used to traverse to the destination
   * @return whether it exists
   */
  boolean containsRecord(T origin, T destination, ModeTypeGroup modeTypeGroup);

  /**
   * Get all the cells. Dangerous if the database is large!
   *
   * @return all cells
   */
  @NotNull Collection<PathTrialCellRecord> getAllCells();

  /**
   * A record that represents a saved {@link PathTrial}.
   */
  @Value
  @Accessors(fluent = true)
  @SuppressWarnings("checkstyle:MethodName")
  class PathTrialRecord {
    long id;
    Date date;
    long duration;
    double pathLength;
    int originX;
    int originY;
    int originZ;
    int destinationX;
    int destinationY;
    int destinationZ;
    String worldId;
    ScoringFunction.Type scoringFunctionType;
    List<PathTrialCellRecord> cells;
    Collection<PathTrialModeRecord> modes;

  }

  /**
   * A record that represents a saved {@link Cell} within a {@link PathTrial}.
   */
  @Value
  @Accessors(fluent = true)
  @SuppressWarnings("checkstyle:MemberName")
  class PathTrialCellRecord {
    PathTrialRecord record;
    int x;
    int y;
    int z;
    boolean critical;
    Integer index;
    ModeType modeType;
    double deviation;
    double distance;
    int distanceY;
    int biome;
    int dimension;
  }

  /**
   * A record to store a {@link Mode}.
   */
  @Value
  @Accessors(fluent = true)
  class PathTrialModeRecord {
    PathTrialRecord record;
    ModeType modeType;
  }

}
