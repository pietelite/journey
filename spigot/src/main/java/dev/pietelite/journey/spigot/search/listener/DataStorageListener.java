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

package dev.pietelite.journey.spigot.search.listener;

import dev.pietelite.journey.common.JourneyCommon;
import dev.pietelite.journey.common.data.DataAccessException;
import dev.pietelite.journey.common.navigation.ModeTypeGroup;
import dev.pietelite.journey.common.search.FlexiblePathTrial;
import dev.pietelite.journey.common.search.PathTrial;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.api.search.event.SpigotStopPathSearchEvent;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * A listener for managing data storage as operations throughout
 * this plugin proceed.
 */
public class DataStorageListener implements Listener {

  /**
   * Catch the event that a path trial stopped so that we can save the data of the calculation
   * for the path of that path trial.
   *
   * @param event the event
   */
  @EventHandler
  public void savePathData(SpigotStopPathSearchEvent event) {
    FlexiblePathTrial<LocationCell, World> flexiblePathTrial = event.getSearchEvent().getPathTrial();
    if (flexiblePathTrial instanceof PathTrial) {
      PathTrial<LocationCell, World> pathTrial = (PathTrial<LocationCell, World>) flexiblePathTrial;
      if (pathTrial.getState().isSuccessful()) {
        try {
          JourneyCommon.<LocationCell, World>getDataManager().getPathRecordManager().report(
              pathTrial,
              event.getSearchEvent().getCalculationNodes(),
              ModeTypeGroup.from(event.getSearchEvent().getPathTrial().getModes()),
              event.getSearchEvent().getExecutionTime());
        } catch (DataAccessException e) {
          e.printStackTrace();
        }
      }
    }
  }

}
