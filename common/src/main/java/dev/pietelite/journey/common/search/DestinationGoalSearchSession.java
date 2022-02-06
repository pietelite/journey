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
import dev.pietelite.journey.common.navigation.Port;
import dev.pietelite.journey.common.search.event.FoundSolutionEvent;
import dev.pietelite.journey.common.search.event.IgnoreCacheSearchEvent;
import dev.pietelite.journey.common.search.event.StartSearchEvent;
import dev.pietelite.journey.common.search.event.StopSearchEvent;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * An implementation of the {@link SearchSession} that uses a "reverse"
 * and predictive method to calculated ideal Itineraries.
 *
 * <p>First, all possible paths will be collected into a series of {@link PathTrial}s.
 *
 * @param <T> the cell type
 * @param <D> the domain type
 */
public abstract class DestinationGoalSearchSession<T extends Cell<T, D>, D> extends SearchSession<T, D> {

  private final T origin;
  private final T destination;
  private final Cell.CellConstructor<T, D> constructor;
  private long executionStartTime = -1;

  /**
   * General constructor.
   *
   * @param callerId   the identifier for the caller
   * @param callerType the type of caller
   * @param origin origination location
   * @param destination destination location
   * @param constructor a constructor for the cell type
   */
  public DestinationGoalSearchSession(UUID callerId, Caller callerType,
                                      T origin, T destination,
                                      Cell.CellConstructor<T, D> constructor) {
    super(callerId, callerType);
    this.origin = origin;
    this.destination = destination;
    this.constructor = constructor;
  }

  @Override
  public final void search() {

    executionStartTime = System.currentTimeMillis();
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StartSearchEvent<>(this));

    state = ResultState.RUNNING;

    Set<D> allDomains = new HashSet<>();

    for (Port<T, D> port : this.ports) {
      allDomains.add(port.getOrigin().getDomain());
      allDomains.add(port.getDestination().getDomain());
    }

    Map<D, List<Port<T, D>>> leapsByOriginDomain = new HashMap<>();
    Map<D, List<Port<T, D>>> leapsByDestinationDomain = new HashMap<>();
    // Prepare leap maps
    for (D domain : allDomains) {
      leapsByOriginDomain.put(domain, new LinkedList<>());
      leapsByDestinationDomain.put(domain, new LinkedList<>());
    }

    // Fill leap maps
    for (Port<T, D> port : this.ports) {
      leapsByOriginDomain.get(port.getOrigin().getDomain()).add(port);
      leapsByDestinationDomain.get(port.getDestination().getDomain()).add(port);
    }

    SearchGraph<T, D> graph = new SearchGraph<>(this, origin, destination, this.ports, constructor);

    // Collect path trials
    if (origin.getDomain().equals(destination.getDomain())) {
      graph.addPathTrialOriginToDestination(this.modes);
    }

    for (D domain : allDomains) {
      for (Port<T, D> pathTrialOriginPort : leapsByDestinationDomain.get(domain)) {
        for (Port<T, D> pathTrialDestinationPort : leapsByOriginDomain.get(domain)) {
          graph.addPathTrialPortToPort(
              pathTrialOriginPort,
              pathTrialDestinationPort,
              this.modes);
          if (domain.equals(origin.getDomain())) {
            graph.addPathTrialOriginToPort(pathTrialDestinationPort, this.modes);
          }
          if (domain.equals(destination.getDomain())) {
            graph.addPathTrialPortToDestination(pathTrialOriginPort, this.modes);
          }
        }
      }
    }

    Itinerary<T, D> bestItinerary = null;
    boolean usingCache = true;
    while (!this.state.isCanceled()) {

      ItineraryTrial<T, D> itineraryTrial = graph.calculate();
      if (itineraryTrial == null) {
        // There is no possible solution to the entire problem
        state = ResultState.STOPPED_FAILED;
        JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopSearchEvent<>(this));
        return;
      } else {
        // We have an overall solution with individual paths that aren't verified/calculated
        ItineraryTrial.TrialResult<T, D> trialResult = itineraryTrial.attempt(usingCache);
        if (trialResult.itinerary().isPresent()) {
          // There is an itinerary solution!

          if (bestItinerary == null
              || trialResult.itinerary().get().getLength() < bestItinerary.getLength()) {
            bestItinerary = trialResult.itinerary().get();
            this.state = ResultState.RUNNING_SUCCESSFUL;
            JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(
                new FoundSolutionEvent<>(this, trialResult.itinerary().get()));
          }
        }

        // Do a quick check to see if the search was canceled before we try to return a value
        if (this.state.isCanceled()) {
          break;
        }

        if (!trialResult.changedProblem()) {
          // This result did not change the problem.
          // If run again, then, we would get the same solution to the graph.
          if (usingCache) {
            // Turn off the use of the cache. Maybe we can find better solution by re-solving some paths.
            JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new IgnoreCacheSearchEvent<>(this));
            usingCache = false;
            // continue...
          } else {
            // The problem hasn't changed, and we aren't using the cache,
            // so no better solutions are possible.
            if (state.isSuccessful()) {
              state = ResultState.STOPPED_SUCCESSFUL;  // in case we had the running-successful state
            } else {
              state = ResultState.STOPPED_FAILED;
            }
            JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopSearchEvent<>(this));
            return;
          }
        }
      }
    }

    // Canceling...
    if (state.isSuccessful()) {
      state = ResultState.STOPPED_SUCCESSFUL;  // in case we had the running-successful state
    } else {
      state = ResultState.STOPPED_CANCELED;  // must have been canceled
    }
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopSearchEvent<>(this));
  }

  @Override
  public long executionTime() {
    if (executionStartTime < 0) {
      return -1;
    }
    return System.currentTimeMillis() - executionStartTime;
  }

}
