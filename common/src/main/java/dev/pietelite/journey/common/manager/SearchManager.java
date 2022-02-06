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

package dev.pietelite.journey.common.manager;

import dev.pietelite.journey.common.navigation.Cell;
import dev.pietelite.journey.common.navigation.Journey;
import dev.pietelite.journey.common.search.SearchSession;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A manager to handle search sessions and their corresponding journeys.
 *
 * @param <T> the location type
 * @param <D> the domain type
 * @param <S> the session type
 * @param <J> the journey type
 */
public class SearchManager<T extends Cell<T, D>, D, S extends SearchSession<T, D>, J extends Journey<T, D>> {

  protected final Map<UUID, J> playerJourneys = new ConcurrentHashMap<>();
  protected final Map<UUID, T> playerLocations = new ConcurrentHashMap<>();
  protected final Map<UUID, S> playerSearches = new ConcurrentHashMap<>();

  /**
   * Store a journey. Stops the previously running journey if there was one.
   *
   * @param callerId the caller id
   * @param journey  the journey
   * @return the old journey
   */
  public J putJourney(@NotNull UUID callerId, J journey) {
    J oldJourney = this.playerJourneys.put(callerId, journey);
    if (oldJourney != null) {
      oldJourney.stop();
    }
    return oldJourney;
  }

  /**
   * Remove a journey from storage and stop the journey.
   *
   * @param callerId the caller id
   * @return the removed journey
   */
  @Nullable
  public J removeJourney(@NotNull UUID callerId) {
    J oldJourney = playerJourneys.remove(callerId);
    if (oldJourney != null) {
      oldJourney.stop();
    }
    return oldJourney;
  }

  /**
   * Whether there is a journey stored.
   *
   * @param callerId the caller id
   * @return true if there is a journey
   */
  public boolean hasJourney(@NotNull UUID callerId) {
    return playerJourneys.containsKey(callerId);
  }

  /**
   * Get the journey.
   *
   * @param callerId the caller id
   * @return the journey, or null if it doesn't exist
   */
  @Nullable
  public J getJourney(@NotNull UUID callerId) {
    return playerJourneys.get(callerId);
  }

  /**
   * Store a new search. Cancel the previous search if there was one.
   *
   * @param callerId the caller id
   * @param search   the search
   * @return the old search
   */
  @Nullable
  public S putSearch(@NotNull UUID callerId, S search) {
    S oldSearch = playerSearches.put(callerId, search);
    if (oldSearch != null) {
      oldSearch.stop();
    }
    return oldSearch;
  }

  /**
   * Remove the stored search and cancel it.
   *
   * @param callerId the caller id
   * @return the previous search, or null if there was none
   */
  @Nullable
  public S removeSearch(@NotNull UUID callerId) {
    S oldSearch = playerSearches.remove(callerId);
    if (oldSearch != null) {
      oldSearch.stop();
    }
    return oldSearch;
  }

  /**
   * Get whether there is a search stored for a caller.
   *
   * @param callerId the caller id
   * @return whether there is a search stored
   */
  public boolean isSearching(@NotNull UUID callerId) {
    return playerSearches.containsKey(callerId);
  }

  /**
   * Get the stored search.
   *
   * @param callerId the caller id
   * @return the stored search, or null if there is none
   */
  @Nullable
  public S getSearch(@NotNull UUID callerId) {
    return playerSearches.get(callerId);
  }

  /**
   * Update the last known location of a certain caller.
   *
   * @param callerId the caller id
   * @param location the location
   * @return the previous location, or null if there was none
   */
  @Nullable
  public T putLocation(@NotNull UUID callerId, T location) {
    return playerLocations.put(callerId, location);
  }

  /**
   * Get the last known location of a certain caller.
   * This is the last one updated with {@link #putLocation}.
   *
   * @param callerId the caller id
   * @return the location
   */
  @Nullable
  public T getLocation(@Nullable UUID callerId) {
    return playerLocations.get(callerId);
  }

  /**
   * Cancel all the saved running searches.
   */
  public void cancelAllSearches() {
    playerSearches.values().forEach(SearchSession::stop);
  }

  /**
   * Stop all the saved running journeys.
   */
  public void stopAllJourneys() {
    playerJourneys.values().forEach(Journey::stop);
  }

}
