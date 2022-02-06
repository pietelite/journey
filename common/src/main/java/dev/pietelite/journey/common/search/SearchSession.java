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

import dev.pietelite.journey.common.navigation.Cell;
import dev.pietelite.journey.common.navigation.Itinerary;
import dev.pietelite.journey.common.navigation.Mode;
import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.navigation.Port;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * A session to handle a pathfinding search.
 * Every time a person wants to find an {@link Itinerary}
 * from some origin to some destination, they can run a search from this session.
 *
 * <p>This class must be thread-safe because it will be called asynchronously
 *
 * @param <T> the cell type
 * @param <D> the domain type
 */
public abstract class SearchSession<T extends Cell<T, D>, D> implements Resulted {

  protected final List<Port<T, D>> ports = new LinkedList<>();
  protected final List<Mode<T, D>> modes = new LinkedList<>();
  private final UUID callerId;
  private final UUID uuid = UUID.randomUUID();
  private final Caller callerType;
  protected ResultState state = ResultState.IDLE;
  private int algorithmStepDelay = 0;

  protected SearchSession(UUID callerId, Caller callerType) {
    this.callerId = callerId;
    this.callerType = callerType;
  }

  /**
   * Perform the titular search operation.
   */
  public abstract void search();

  /**
   * Terminate the search operation. It is up to the implementation
   * of this search session object to implement the actual cancellation behavior;
   * this method only tells the class that it should be canceling itself.
   *
   * @return true if the state was changed
   */
  public final boolean stop() {
    if (state == ResultState.IDLE
        || state == ResultState.RUNNING) {
      this.state = ResultState.CANCELING_FAILED;
      return true;
    } else if (state == ResultState.RUNNING_SUCCESSFUL) {
      this.state = ResultState.CANCELING_SUCCESSFUL;
      return true;
    }
    return false;
  }

  @Override
  public final ResultState getState() {
    return state;
  }

  /**
   * Register a {@link Port} to use in the pathfinding system.
   * The ports are what allow the algorithm to jump directly around the different worlds,
   * so all possible avenues to do so should be registered before running the search.
   *
   * @param port the port
   */
  public final void registerPort(Port<T, D> port) {
    this.ports.add(port);
  }

  /**
   * Register a {@link Mode} of transportation in the pathfinding system.
   * Modes are how the algorithm determines whether the moving entity can
   * get from place to adjacent place.
   *
   * @param mode the mode
   */
  public final void registerMode(Mode<T, D> mode) {
    this.modes.add(mode);
  }

  /**
   * Get a copy of all the registered ports on this session.
   *
   * @return the ports
   */
  public final Collection<Port<T, D>> ports() {
    List<Port<T, D>> copy = new ArrayList<>(ports.size());
    copy.addAll(ports);
    return copy;
  }

  /**
   * Get a copy of all the registered modes on this session.
   *
   * @return the modes
   */
  public final Collection<Mode<T, D>> modes() {
    List<Mode<T, D>> copy = new ArrayList<>(modes.size());
    copy.addAll(modes);
    return copy;
  }

  /**
   * Get a set of all mode types that are registered on this session.
   *
   * @return the mode types
   */
  public final Set<ModeType> modeTypes() {
    return modes.stream().map(Mode::getType).collect(Collectors.toSet());
  }

  /**
   * Get the identifier of the caller, for indexing and retrieval purposes.
   *
   * @return the id
   */
  public final UUID getCallerId() {
    return callerId;
  }

  /**
   * Get the unique identifier of this session.
   *
   * @return the id
   */
  public final UUID getUuid() {
    return uuid;
  }

  /**
   * Get the type of caller, for classification purposes.
   *
   * @return the type of caller.
   */
  public Caller getCallerType() {
    return callerType;
  }

  /**
   * Get the step delay time for the search operation (milliseconds).
   * This value is primarily used in animation so that each step of the way
   * is delayed and can therefore be seen visually by the user.
   *
   * @return the step delay (milliseconds)
   */
  public int getAlgorithmStepDelay() {
    return algorithmStepDelay;
  }

  protected void setAlgorithmStepDelay(int delay) {
    this.algorithmStepDelay = delay;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    SearchSession<?, ?> that = (SearchSession<?, ?>) o;
    return uuid.equals(that.uuid);
  }

  @Override
  public int hashCode() {
    return Objects.hash(uuid);
  }

  /**
   * Get the time since the search operation execution, in milliseconds.
   * It will return a negative number if the method is called before the search operation.
   *
   * @return the search execution time, in milliseconds
   */
  public abstract long executionTime();

  /**
   * The caller type. A search session may be created for multiple types of entities,
   * but generally they are players.
   * In some scenarios, we may want an NPC of some sort to follow the path,
   * so in that case, the caller would be something other than Player.
   */
  public enum Caller {
    PLAYER,
    OTHER
  }
}
