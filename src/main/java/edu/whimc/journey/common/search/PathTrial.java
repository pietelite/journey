/*
 * MIT License
 *
 * Copyright 2021 Pieter Svenson
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
 *
 */

package edu.whimc.journey.common.search;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.cache.PathCache;
import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.navigation.Mode;
import edu.whimc.journey.common.navigation.ModeTypeGroup;
import edu.whimc.journey.common.navigation.Path;
import java.util.Collection;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * An extension of {@link FlexiblePathTrial} where the goal of the trial is to find a path to
 * a specific predefined destination.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public class PathTrial<T extends Cell<T, D>, D> extends FlexiblePathTrial<T, D> {

  public static final double SUFFICIENT_COMPLETION_DISTANCE_SQUARED = 0;
  @Getter
  private final T destination;

  private PathTrial(SearchSession<T, D> session,
                    T origin,
                    T destination,
                    double length,
                    Path<T, D> path,
                    ResultState state,
                    boolean fromCache) {
    super(session, origin,
        node -> -node.getData().location().distanceToSquared(destination),
        node -> node.getData().location().distanceToSquared(destination)
            <= SUFFICIENT_COMPLETION_DISTANCE_SQUARED,
        length,
        path,
        state,
        fromCache);
    this.destination = destination;
  }

  /**
   * Get a path trial that is already determined to be successful.
   * Any attempts will result in success.
   *
   * @param session     the session
   * @param origin      the origin of the path
   * @param destination the destination of the path
   * @param path        the path
   * @param <T>         the location type
   * @param <D>         the domain type
   * @return the successful path trial
   */
  public static <T extends Cell<T, D>, D> PathTrial<T, D> successful(SearchSession<T, D> session,
                                                                     T origin, T destination,
                                                                     Path<T, D> path) {
    return new PathTrial<>(session, origin, destination,
        path.getLength(), path,
        ResultState.STOPPED_SUCCESSFUL, false);
  }

  /**
   * Get a path trial that is already determined to have failed.
   * Any attempts will result in failure.
   *
   * @param session     the session
   * @param origin      the origin of the path
   * @param destination the destination of the path
   * @param <T>         the location type
   * @param <D>         the domain type
   * @return the path trial
   */
  public static <T extends Cell<T, D>, D> PathTrial<T, D> failed(SearchSession<T, D> session,
                                                                 T origin, T destination) {
    return new PathTrial<>(session, origin, destination,
        Double.MAX_VALUE, null,
        ResultState.STOPPED_FAILED, false);
  }

  /**
   * Get a path trial that has not yet been calculated.
   * We will approximate the result as the cartesian distance between the
   * origin and destination,
   * but a more precise answer will be calculated the first time it is attempted.
   *
   * @param session     the session
   * @param origin      the origin
   * @param destination the destination
   * @param <T>         the location type
   * @param <D>         the domain type
   * @return the path trial
   */
  public static <T extends Cell<T, D>, D> PathTrial<T, D> approximate(SearchSession<T, D> session,
                                                                      T origin, T destination) {
    return new PathTrial<>(session, origin, destination,
        origin.distanceTo(destination), null,
        ResultState.IDLE, false);
  }

  /**
   * Get a path trial that has some result determined from the cache.
   *
   * @param session     the session
   * @param origin      the origin
   * @param destination the destination
   * @param path        the path
   * @param <T>         the location type
   * @param <D>         the domain type
   * @return the path trial
   */
  public static <T extends Cell<T, D>, D> PathTrial<T, D> cached(SearchSession<T, D> session,
                                                                 T origin, T destination,
                                                                 Path<T, D> path) {
    return new PathTrial<>(session, origin, destination,
        path == null ? Double.MAX_VALUE : path.getLength(), path,
        path == null ? ResultState.STOPPED_FAILED : ResultState.STOPPED_SUCCESSFUL,
        true);
  }

  @Override
  public @NotNull TrialResult<T, D> attempt(Collection<Mode<T, D>> modes, boolean useCacheIfPossible) {
    TrialResult<T, D> result = super.attempt(modes, useCacheIfPossible);

    // We might need to cache this result
    switch (this.getState()) {
      case STOPPED_SUCCESSFUL -> JourneyCommon.<T, D>getPathCache().put(this.getOrigin(),
          this.destination,
          ModeTypeGroup.from(modes),
          PathCache.CachedPath.of(result.path().orElseThrow(() ->
              new RuntimeException("A Path to Destination Trial has a successful state "
                  + "but returned an empty path Optional."))));
      case STOPPED_FAILED -> JourneyCommon.<T, D>getPathCache().put(this.getOrigin(),
          this.destination, ModeTypeGroup.from(modes), PathCache.CachedPath.empty());
      default -> {
        /* Do nothing if its canceled */
      }
    }
    return result;
  }
}