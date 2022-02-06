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
import dev.pietelite.journey.common.navigation.Mode;
import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.navigation.Path;
import dev.pietelite.journey.common.navigation.Step;
import dev.pietelite.journey.common.search.event.StartPathSearchEvent;
import dev.pietelite.journey.common.search.event.StepSearchEvent;
import dev.pietelite.journey.common.search.event.StopPathSearchEvent;
import dev.pietelite.journey.common.search.event.VisitationSearchEvent;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.PriorityQueue;
import java.util.Queue;
import java.util.function.Predicate;
import lombok.Getter;
import lombok.Setter;
import lombok.Value;
import lombok.experimental.Accessors;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * An attempt to calculate a {@link Path} encapsulated into an object.
 *
 * @param <T> the location type
 * @param <D> the domain type
 * @see Path
 * @see SearchSession
 * @see ItineraryTrial
 */
public class FlexiblePathTrial<T extends Cell<T, D>, D> implements Resulted {

  // TODO bring this max size up to something larger like a million but
  //  create a way to identify whether a long-running operation
  //  is likely to be a failure a different way than maxing out memory.
  //  For example, keep track of the best distance so far and if no
  //  significant improvement has been made in a while, then assume it will never.
  public static final int MAX_SIZE = 10000;

  private final SearchSession<T, D> session;
  @Getter
  private final T origin;
  @Getter
  private final D domain;
  @Getter
  private final ScoringFunction<T, D> scoringFunction;
  private final Completer<T, D> completer;
  @Getter
  private final List<Mode<T, D>> modes = new LinkedList<>();
  @Getter
  private double length;
  @Getter
  private Path<T, D> path;
  @Getter
  private ResultState state;
  @Getter
  private boolean fromCache;
  private long startExecutionTime = -1;

  /**
   * General constructor.
   *
   * @param session         the session requesting this path trial run
   * @param origin          the origin
   * @param scoringFunction the object to score various possibilities when stepping to new locations
   *                        throughout the algorithm
   * @param completer       the object to determine whether the path algorithm is complete and
   *                        the goal has been reached
   */
  public FlexiblePathTrial(SearchSession<T, D> session,
                           T origin,
                           Collection<Mode<T, D>> modes,
                           ScoringFunction<T, D> scoringFunction,
                           Completer<T, D> completer) {
    this.session = session;
    this.origin = origin;
    this.domain = origin.getDomain();
    this.modes.addAll(modes);
    this.scoringFunction = scoringFunction;
    this.completer = completer;
  }

  protected FlexiblePathTrial(SearchSession<T, D> session,
                              T origin,
                              Collection<Mode<T, D>> modes,
                              ScoringFunction<T, D> scoringFunction,
                              Completer<T, D> completer,
                              double length,
                              @Nullable Path<T, D> path,
                              ResultState state,
                              boolean fromCache) {
    this.session = session;
    this.origin = origin;
    this.domain = origin.getDomain();
    this.modes.addAll(modes);
    this.scoringFunction = scoringFunction;
    this.completer = completer;
    this.length = length;
    this.path = path;
    this.state = state;
    this.fromCache = fromCache;
  }

  private FlexiblePathTrial.TrialResult<T, D> resultFail() {
    this.state = ResultState.STOPPED_FAILED;
    this.length = Double.MAX_VALUE;
    this.fromCache = false;
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopPathSearchEvent<>(session,
        this,
        Collections.emptySet(),
        System.currentTimeMillis() - startExecutionTime));
    return new TrialResult<>(Optional.empty(), true);
  }

  private FlexiblePathTrial.TrialResult<T, D> resultSucceed(double length,
                                                            List<Step<T, D>> steps,
                                                            Collection<Node<T, D>> calculationNodes) {
    this.state = ResultState.STOPPED_SUCCESSFUL;
    this.length = length;
    this.path = new Path<>(origin, new ArrayList<>(steps), length);
    this.fromCache = false;
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopPathSearchEvent<>(session,
        this,
        calculationNodes,
        System.currentTimeMillis() - startExecutionTime));
    return new TrialResult<>(Optional.of(this.path), true);
  }

  private FlexiblePathTrial.TrialResult<T, D> resultCancel() {
    this.state = ResultState.STOPPED_CANCELED;
    this.length = Double.MAX_VALUE;
    this.fromCache = false;
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StopPathSearchEvent<>(session,
        this,
        Collections.emptySet(),
        System.currentTimeMillis() - startExecutionTime));
    return new TrialResult<>(Optional.empty(), true);
  }

  /**
   * Attempt to calculate a path given some modes of transportation.
   *
   * @param useCacheIfPossible whether the cache should be used for retrieving previous results
   * @return a result object
   */
  @NotNull
  public TrialResult<T, D> attempt(boolean useCacheIfPossible) {

    // Return the saved states, but only if we want that result.
    //  If we don't want to use the cache, but this result is from the cache,
    //  then don't return this.
    if (!this.fromCache || useCacheIfPossible) {
      if (this.state == ResultState.STOPPED_SUCCESSFUL) {
        if (path.test(modes)) {
          return new TrialResult<>(Optional.of(path), false);
        }
      } else if (this.state == ResultState.STOPPED_FAILED) {
        return new TrialResult<>(Optional.empty(), false);
      }
    }

    // Dispatch a starting event
    JourneyCommon.<T, D>getSearchEventDispatcher().dispatch(new StartPathSearchEvent<>(session, this));
    startExecutionTime = System.currentTimeMillis();

    Queue<Node<T, D>> upcoming = new PriorityQueue<>(Comparator.comparingDouble(node ->
        -scoringFunction.apply(node)));
    Map<T, Node<T, D>> visited = new HashMap<>();

    Node<T, D> originNode = new Node<>(new Step<>(origin, 0, ModeType.NONE),
        null, 0);
    upcoming.add(originNode);
    visited.put(origin, originNode);
    JourneyCommon.<T, D>getSearchEventDispatcher()
        .dispatch(new VisitationSearchEvent<>(session, originNode.getData()));

    Node<T, D> current;
    while (!upcoming.isEmpty()) {
      if (session.state.isCancelFailed()) {
        // Canceled! Fail here, but don't cache it because it's not the true solution for this path.
        return resultCancel();
      }

      if (visited.size() > MAX_SIZE) {
        // We ran out of allocated memory. Let's just call it here and say we failed and cache the failure.
        return resultFail();
      }

      current = upcoming.poll();
      assert current != null;
      JourneyCommon.<T, D>getSearchEventDispatcher()
          .dispatch(new StepSearchEvent<>(session, originNode.getData()));

      if (completer.test(current)) {
        // We found it!
        double length = current.getScore();
        LinkedList<Step<T, D>> steps = new LinkedList<>();
        do {
          steps.addFirst(current.getData());
          current = current.getPrevious();
        } while (current != null);
        return resultSucceed(length, steps, visited.values());
      }

      // Need to keep going
      for (Mode<T, D> mode : modes) {
        for (Mode<T, D>.Option option : mode.getDestinations(current.getData().location())) {
          if (visited.containsKey(option.getLocation())) {
            // Already visited, but see if it is better to come from this new direction
            Node<T, D> that = visited.get(option.getLocation());
            if (current.getScore() + option.getDistance() < that.getScore()) {
              that.setPrevious(current);
              that.setScore(current.getScore() + option.getDistance());
              that.setData(new Step<>(that.getData().location(),
                  option.getDistance(),
                  mode.getType()));
            }
          } else {
            // Not visited. Set up node, give it a score, and add it to the system
            Node<T, D> nextNode = new Node<>(
                new Step<>(option.getLocation(),
                    option.getDistance(),
                    mode.getType()),
                current,
                current.getScore() + option.getDistance());
            upcoming.add(nextNode);
            visited.put(option.getLocation(), nextNode);
            JourneyCommon.<T, D>getSearchEventDispatcher()
                .dispatch(new VisitationSearchEvent<>(session, nextNode.getData()));
          }
        }
      }
    }

    // We've exhausted all possibilities. Fail.
    return resultFail();
  }

  /**
   * An interface to represent when a node is considered successful and therefore
   * the end of a successful path.
   * At this point in the algorithm, the path up until and through this node is returned.
   *
   * @param <T> the location type
   * @param <D> the domain type
   */
  @FunctionalInterface
  public interface Completer<T extends Cell<T, D>, D> extends Predicate<Node<T, D>> {
  }

  /**
   * A result object to return the result of the {@link #attempt} method.
   *
   * @param <T> the location type
   * @param <D> the domain type
   */
  @Value
  @Accessors(fluent = true)
  public static class TrialResult<T extends Cell<T, D>, D> {
    Optional<Path<T, D>> path;
    boolean changedProblem;
  }

  /**
   * A single node representing a possible movement during traversal.
   *
   * @param <T> the cell type
   * @param <D> the domain type
   */
  public static class Node<T extends Cell<T, D>, D> {
    @Getter
    @Setter
    private Step<T, D> data;
    @Getter
    @Setter
    private Node<T, D> previous;
    /**
     * The value to store how far away this node is from the original node.
     * So, how far it is to traverse the space from the origin until this node is reached.
     */
    @Getter
    @Setter
    private double score;

    /**
     * General constructor.
     *
     * @param data     the step
     * @param previous the previous node that we came from to get here
     * @param score    our score so far throughout the pathfinding algorithm
     */
    public Node(@NotNull Step<T, D> data, Node<T, D> previous, double score) {
      this.data = data;
      this.previous = previous;
      this.score = score;
    }

  }

}