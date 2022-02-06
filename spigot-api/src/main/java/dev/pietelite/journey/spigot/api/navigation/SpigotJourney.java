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

package dev.pietelite.journey.spigot.api.navigation;

import dev.pietelite.journey.common.navigation.Itinerary;
import dev.pietelite.journey.common.navigation.Journey;
import dev.pietelite.journey.common.navigation.Path;
import dev.pietelite.journey.common.navigation.Port;
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.common.tools.AlternatingList;
import org.bukkit.World;
import org.jetbrains.annotations.NotNull;

/**
 * A general implementation of a {@link Journey} specifically for Spigot purposes.
 */
public abstract class SpigotJourney implements Journey<LocationCell, World> {

  private final SearchSession<LocationCell, World> session;
  private final Itinerary<LocationCell, World> itinerary;
  private AlternatingList.Traversal<Port<LocationCell, World>,
      Path<LocationCell, World>,
      Path<LocationCell, World>> traversal;
  /**
   * Itinerary that is queued as a better one, in case the player wants to use it.
   */
  private Itinerary<LocationCell, World> prospectiveItinerary;

  /**
   * General constructor.
   *
   * @param session   the session ultimately causing this journey
   * @param itinerary the itinerary that acts a roadmap for this journey
   */
  public SpigotJourney(@NotNull SearchSession<LocationCell, World> session,
                       @NotNull Itinerary<LocationCell, World> itinerary) {
    this.session = session;
    this.itinerary = itinerary;
    this.traversal = itinerary.getStages().traverse();
  }

  /**
   * Get the player search session used to calculate this journey.
   *
   * @return the session
   */
  public SearchSession<LocationCell, World> getSession() {
    return session;
  }

  /**
   * Get the itinerary that originally determined the directions
   * and acts as a roadmap for this journey.
   *
   * @return the itinerary
   */
  public Itinerary<LocationCell, World> getItinerary() {
    return this.itinerary;
  }

  /**
   * Get the prospective itinerary.
   * This (better) itinerary was calculated by the session after this journey was already prepared
   * for the user. The user will have the option of using this itinerary if he/she
   * so chooses and create another journey.
   *
   * @return the itinerary
   */
  public Itinerary<LocationCell, World> getProspectiveItinerary() {
    return prospectiveItinerary;
  }

  /**
   * Get the prospective itinerary.
   * This (better) itinerary was calculated by the session after this journey was already prepared
   * for the user. The user will have the option of using this itinerary if he/she
   * so chooses and create another journey.
   *
   * @param prospectiveItinerary the prospective itinerary
   */
  public void setProspectiveItinerary(Itinerary<LocationCell, World> prospectiveItinerary) {
    this.prospectiveItinerary = prospectiveItinerary;
  }

  /**
   * Get the destination of the current path being traversed.
   *
   * @return the destination location
   */
  public final LocationCell currentPathDestination() {
    return traversal.get().getDestination();
  }

  protected final AlternatingList.Traversal<Port<LocationCell, World>,
      Path<LocationCell, World>,
      Path<LocationCell, World>> traversal() {
    return traversal;
  }

  protected final void resetTraversal() {
    this.traversal = getItinerary().getStages().traverse();
  }

}
