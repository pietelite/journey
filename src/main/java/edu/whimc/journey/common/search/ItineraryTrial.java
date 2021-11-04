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
import edu.whimc.journey.common.navigation.Cell;
import edu.whimc.journey.common.navigation.Itinerary;
import edu.whimc.journey.common.navigation.Mode;
import edu.whimc.journey.common.navigation.Port;
import edu.whimc.journey.common.navigation.Step;
import edu.whimc.journey.common.search.event.StartItinerarySearchEvent;
import edu.whimc.journey.common.search.event.StopItinerarySearchEvent;
import edu.whimc.journey.common.tools.AlternatingList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import org.jetbrains.annotations.NotNull;

/**
 * An attempt to calculate an {@link Itinerary} encapsulated into an object.
 *
 * @param <T> the location type
 * @param <D> the domain type
 * @see Itinerary
 * @see SearchSession
 * @see PathTrial
 * @see #attempt(Collection, boolean)
 */
public class ItineraryTrial<T extends Cell<T, D>, D> implements Resulted {

  private final SearchSession<T, D> session;
  private final T origin;
  private final AlternatingList<Port<T, D>, PathTrial<T, D>, Object> alternatingList;
  private ResultState state;

  /**
   * General constructor.
   *
   * @param session         the session
   * @param origin          the origin of the entire itinerary
   * @param alternatingList the list of stages
   */
  public ItineraryTrial(SearchSession<T, D> session, T origin,
                        AlternatingList<Port<T, D>, PathTrial<T, D>, Object> alternatingList) {
    this.session = session;
    this.origin = origin;
    this.alternatingList = alternatingList;
    this.state = ResultState.IDLE;
  }

  /**
   * Attempt to calculate an itinerary given some modes of transportation.
   *
   * @param modes              the modes allowed for the caller
   * @param useCacheIfPossible whether the cache should be used for retrieving previous results
   * @return a result object
   */
  @NotNull
  public TrialResult<T, D> attempt(Collection<Mode<T, D>> modes, boolean useCacheIfPossible) {
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StartItinerarySearchEvent<>(session, this));

    state = ResultState.RUNNING;
    boolean failed = false;
    boolean changedProblem = false;
    for (PathTrial<T, D> pathTrial : alternatingList.getMinors()) {
      if (session.state.isCanceled()) {
        state = ResultState.STOPPED_CANCELED;
        JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(
            new StopItinerarySearchEvent<>(session, this));
        return new TrialResult<>(Optional.empty(), true);  // doesn't really matter if changed problem
      }

      PathTrial.TrialResult<T, D> pathTrialResult = pathTrial.attempt(modes, useCacheIfPossible);
      if (pathTrialResult.changedProblem()) {
        changedProblem = true;
      }
      if (pathTrialResult.path().isEmpty()) {
        failed = true;
      }
    }
    if (failed) {
      state = ResultState.STOPPED_FAILED;
      JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopItinerarySearchEvent<>(session, this));
      return new TrialResult<>(Optional.empty(), changedProblem);
    }

    // accumulate length
    double length = 0;
    for (Port<T, D> port : alternatingList.getMajors()) {
      if (port != null) {
        length += port.getLength();
      }
    }
    for (PathTrial<T, D> pathTrial : alternatingList.getMinors()) {
      length += pathTrial.getLength();
    }

    List<List<Step<T, D>>> flattenedList = alternatingList.flatten(leap -> {
      if (leap == null) {
        return null;
      } else {
        return leap.getSteps();
      }
    }, trial -> trial.getPath().getSteps() /* Path must exist because we didn't fail */);
    List<Step<T, D>> allSteps = new LinkedList<>();
    for (List<Step<T, D>> list : flattenedList) {
      if (list != null) {
        allSteps.addAll(list);
      }
    }
    state = ResultState.STOPPED_SUCCESSFUL;
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopItinerarySearchEvent<>(session, this));
    return new TrialResult<>(Optional.of(new Itinerary<>(origin,
        allSteps,
        alternatingList.convert(leap -> leap, pathTrial -> Objects.requireNonNull(pathTrial.getPath())),
        length)), changedProblem);
  }

  @Override
  public ResultState getState() {
    return this.state;
  }

  /**
   * A result of a trial attempt.
   *
   * @param <T> the location type
   * @param <D> the domain type
   * @see #attempt(Collection, boolean)
   */
  public static final record TrialResult<T extends Cell<T, D>, D>(
      @NotNull Optional<Itinerary<T, D>> itinerary,
      boolean changedProblem) {
  }

}
