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
import dev.pietelite.journey.common.navigation.ModeTypeGroup;
import dev.pietelite.journey.common.navigation.Port;
import dev.pietelite.journey.common.search.graph.WeightedGraph;
import dev.pietelite.journey.common.tools.AlternatingList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import org.jetbrains.annotations.Nullable;

/**
 * An implementation of a weighted graph to be used for the overall search algorithm.
 *
 * @param <T> the location type
 * @param <D> the domain type
 */
public final class SearchGraph<T extends Cell<T, D>, D> extends WeightedGraph<Port<T, D>, PathTrial<T, D>> {

  private final SearchSession<T, D> session;
  private final T origin;
  private final Node originNode;
  private final T destination;
  private final Node destinationNode;
  private final Map<Port<T, D>, Node> portToNode = new HashMap<>();

  private final Cell.CellConstructor<T, D> constructor;

  /**
   * General constructor.
   *
   * @param session     the search session
   * @param origin      the origin of the entire problem
   * @param destination the destination of the entire problem
   * @param ports       the ports
   */
  public SearchGraph(SearchSession<T, D> session, T origin, T destination, Collection<Port<T, D>> ports,
                     Cell.CellConstructor<T, D> constructor) {
    this.session = session;
    this.origin = origin;
    this.originNode = new Node(new Port<>(origin, origin, ModeType.NONE, 0));
    this.destination = destination;
    this.destinationNode = new Node(new Port<>(destination, destination, ModeType.NONE, 0));
    this.constructor = constructor;

    ports.forEach(port -> portToNode.put(port, new Node(port)));
  }

  private Node getOriginNode() {
    return originNode;
  }

  private Node getDestinationNode() {
    return destinationNode;
  }

  private Node getLeapNode(Port<T, D> port) {
    return portToNode.get(port);
  }

  /**
   * Add a path trial to the search graph that supposedly goes
   * directly from the origin to the destination.
   *
   * @param modes the mode types to supposedly get from the origin to the destination
   */
  public void addPathTrialOriginToDestination(Collection<Mode<T, D>> modes) {
    addPathTrial(session, origin, destination, getOriginNode(), getDestinationNode(), modes);
  }

  /**
   * Add a path trial to the search graph that supposedly goes
   * from the origin of the entire search to a port.
   * The endpoint of the path, then, would be the origin of the port.
   *
   * @param end   the end of the path trial
   * @param modes the mode types used to traverse the path
   */
  public void addPathTrialOriginToPort(Port<T, D> end, Collection<Mode<T, D>> modes) {
    addPathTrial(session,
        origin, end.getOrigin(),
        getOriginNode(), getLeapNode(end),
        modes);
  }

  /**
   * Add a path trial to the search graph that supposedly goes
   * from a port to the destination of the entire search
   * The origin of the path, then, would be the destination of the port.
   *
   * @param start the start of the path trial
   * @param modes the mode types used to traverse the path
   */
  public void addPathTrialPortToDestination(Port<T, D> start, Collection<Mode<T, D>> modes) {
    addPathTrial(session,
        start.getDestination(), destination,
        getLeapNode(start), getDestinationNode(),
        modes);
  }

  /**
   * Add a path trial to the search graph that goes from a port to
   * another port.
   * The destination of the start port is the origin of this path trial,
   * and the origin of the end port is the destination of this path trial.
   *
   * @param start the starting port
   * @param end   the ending port
   * @param modes the mode types used to traverse the path
   */
  public void addPathTrialPortToPort(Port<T, D> start, Port<T, D> end, Collection<Mode<T, D>> modes) {
    addPathTrial(session, start.getDestination(), end.getOrigin(),
        getLeapNode(start), getLeapNode(end), modes);
  }

  private void addPathTrial(SearchSession<T, D> session, T origin, T destination,
                            WeightedGraph<Port<T, D>, PathTrial<T, D>>.Node originNode,
                            WeightedGraph<Port<T, D>, PathTrial<T, D>>.Node destinationNode,
                            Collection<Mode<T, D>> modes) {
    // First, try to access a cached path
    ModeTypeGroup modeTypes = ModeTypeGroup.from(modes);
    System.out.println("Seeing whether " + origin + " -> " + destination + " is stored");
    if (JourneyCommon.<T, D>getDataManager()
        .getPathRecordManager()
        .containsRecord(origin, destination, modeTypes)) {
      System.out.println("Yes");
      addPathTrial(PathTrial.cached(session, origin, destination,
              modes,
              JourneyCommon.<T, D>getDataManager()
                  .getPathRecordManager()
                  .getPath(origin, destination, modeTypes, constructor)),
          originNode, destinationNode);
    } else {
      System.out.println("No");
      addPathTrial(PathTrial.approximate(session, origin, destination, modes), originNode, destinationNode);
    }
  }

  private void addPathTrial(PathTrial<T, D> trial, Node start, Node end) {
    addEdge(start, end, trial);
  }

  /**
   * Calculate an itinerary trial using this graph.
   * If none is found, then return null.
   *
   * @return the itinerary trial
   */
  @Nullable
  public ItineraryTrial<T, D> calculate() {
    AlternatingList<Node, PathTrial<T, D>, Object> graphPath = findMinimumPath(originNode, destinationNode);
    if (graphPath == null) {
      return null;
    } else {
      return new ItineraryTrial<>(session, origin,
          graphPath.convert(node -> Objects.requireNonNull(node.getData()),
              edge -> edge));
    }
  }

  @Override
  protected double nodeWeight(Port<T, D> nodeData) {
    return nodeData.getLength();
  }

  @Override
  protected double edgeLength(PathTrial<T, D> edge) {
    return edge.getLength();
  }
}
