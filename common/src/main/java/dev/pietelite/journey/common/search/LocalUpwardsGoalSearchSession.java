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

package dev.pietelite.journey.common.search;

import dev.pietelite.journey.common.JourneyCommon;
import dev.pietelite.journey.common.navigation.Cell;
import dev.pietelite.journey.common.navigation.Itinerary;
import dev.pietelite.journey.common.navigation.Path;
import dev.pietelite.journey.common.navigation.Port;
import dev.pietelite.journey.common.search.event.FoundSolutionEvent;
import dev.pietelite.journey.common.search.event.StartSearchEvent;
import dev.pietelite.journey.common.search.event.StopSearchEvent;
import dev.pietelite.journey.common.tools.AlternatingList;
import java.util.UUID;

/**
 * A search session that's purpose is to run a search, trying to find
 * a location that reaches some criteria by moving upwards as much as possible.
 *
 * @param <T> the location type
 * @param <D> the destination type
 */
public abstract class LocalUpwardsGoalSearchSession<T extends Cell<T, D>, D> extends SearchSession<T, D> {

  private final T origin;
  private long executionStartTime = -1;

  protected LocalUpwardsGoalSearchSession(UUID callerId, Caller callerType, T origin) {
    super(callerId, callerType);
    this.origin = origin;
  }

  @Override
  public void search() {

    executionStartTime = System.currentTimeMillis();
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StartSearchEvent<>(this));
    state = ResultState.RUNNING;

    FlexiblePathTrial<T, D> pathTrial = new FlexiblePathTrial<>(
        this,
        origin,
        this.modes,
        new ScoringFunction<>(node -> (double) node.getData().location().getY(), ScoringFunction.Type.HEIGHT),
        node -> reachesGoal(node.getData().location())
    );

    PathTrial.TrialResult<T, D> result = pathTrial.attempt(false);

    if (result.path().isPresent()) {
      AlternatingList.Builder<Port<T, D>, Path<T, D>, Path<T, D>> stages
          = AlternatingList.builder(Port.stationary(origin));
      stages.addLast(result.path().get(), Port.stationary(result.path().get().getDestination()));

      JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(
          new FoundSolutionEvent<>(
              this,
              new Itinerary<>(this.origin,
                  result.path().get().getSteps(),
                  stages.build(),
                  result.path().get().getLength())));
    }

    state = pathTrial.getState();
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopSearchEvent<>(this));

  }

  @Override
  public final long executionTime() {
    if (executionStartTime < 0) {
      return -1;
    }
    return System.currentTimeMillis() - executionStartTime;
  }

  protected abstract boolean reachesGoal(T cell);

}