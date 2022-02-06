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

package dev.pietelite.journey.common;

import dev.pietelite.journey.common.config.ConfigManager;
import dev.pietelite.journey.common.data.DataManager;
import dev.pietelite.journey.common.navigation.Cell;
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.common.search.event.SearchDispatcher;
import dev.pietelite.journey.common.util.LoggerCommon;
import dev.pietelite.journey.common.util.MinecraftConversions;
import lombok.Getter;
import lombok.Setter;

/**
 * The central utility class to provide methods for all platform
 * implementations of the Journey plugin.
 */
public final class JourneyCommon {

  // Database
  private static DataManager<?, ?> dataManager;

  private static SearchDispatcher<?, ?, ?> searchEventDispatcher;
  @Getter
  @Setter
  private static ConfigManager configManager;
  private static LoggerCommon logger;

  private static MinecraftConversions<?, ?> conversions;

  /**
   * Get the event dispatcher used in a {@link SearchSession}.
   * It is up to the caller of this method to use the same generics used when
   * instantiating the event dispatcher at start-up.
   *
   * @param <T> the location type
   * @param <D> the domain type
   * @return the dispatcher
   */
  @SuppressWarnings("unchecked")
  public static <T extends Cell<T, D>, D> SearchDispatcher<T, D, ?> getSearchEventDispatcher() {
    if (searchEventDispatcher == null) {
      throw new IllegalStateException("No search dispatcher! Did you forget to initialize it?");
    }
    return (SearchDispatcher<T, D, ?>) searchEventDispatcher;
  }

  /**
   * Set the search dispatcher used in a {@link SearchSession}.
   *
   * @param dispatcher the dispatcher
   * @param <T>        the location type
   * @param <D>        the domain type
   * @param <E>        the event type ultimately used to catch the events.
   */
  public static <T extends Cell<T, D>, D, E> void setSearchEventDispatcher(
      SearchDispatcher<T, D, E> dispatcher) {
    JourneyCommon.searchEventDispatcher = dispatcher;
  }

  /**
   * Get the cache of all memoized paths.
   *
   * @param <T> the location type
   * @param <D> the domain type
   * @return the cache
   */
  @SuppressWarnings("unchecked")
  public static <T extends Cell<T, D>, D> DataManager<T, D> getDataManager() {
    if (dataManager == null) {
      throw new IllegalStateException("No path cache! Did you forget to initialize it?");
    }
    return (DataManager<T, D>) dataManager;
  }

  /**
   * Set the path cache.
   *
   * @param dataManager the path manager
   * @param <T>         the location type
   * @param <D>         the domain type
   */
  public static <T extends Cell<T, D>, D> void setDataManager(DataManager<T, D> dataManager) {
    JourneyCommon.dataManager = dataManager;
  }

  /**
   * Get a simple logger, which can be used anywhere in common files.
   *
   * @return the logger
   */
  public static LoggerCommon getLogger() {
    return logger;
  }

  /**
   * Set the simple logger, implemented using the implementation platform's logger.
   *
   * @param logger the logger
   */
  public static void setLogger(LoggerCommon logger) {
    JourneyCommon.logger = logger;
  }

  /**
   * Get the {@link MinecraftConversions} object.
   *
   * @param <T> the cell type
   * @param <D> the domain type
   * @return the conversions for a specific implementation of a modding platform
   */
  @SuppressWarnings("unchecked")
  public static <T extends Cell<T, D>, D> MinecraftConversions<T, D> getConversions() {
    if (conversions == null) {
      throw new IllegalStateException("No Minecraft conversions! Did you forget to initialize it?");
    }
    return (MinecraftConversions<T, D>) conversions;
  }

  /**
   * Set the path cache.
   *
   * @param conversions the path cache
   * @param <T>         the location type
   * @param <D>         the domain type
   */
  public static <T extends Cell<T, D>, D> void setConversions(MinecraftConversions<T, D> conversions) {
    JourneyCommon.conversions = conversions;
  }

}
