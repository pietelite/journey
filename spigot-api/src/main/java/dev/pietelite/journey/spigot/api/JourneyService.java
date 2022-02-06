/*
 * MIT License
 *
 * Copyright 2022 Pieter Svenson
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

package dev.pietelite.journey.spigot.api;

import dev.pietelite.journey.common.data.DataManager;
import dev.pietelite.journey.common.navigation.Mode;
import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.common.search.event.SearchDispatcher;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

/**
 * A service to provide to Bukkit/Spigot based plugins.
 */
public interface JourneyService {

  /**
   * Get the running instance of the search event dispatcher
   * for firing events during a search session.
   *
   * @return the dispatcher
   */
  SearchDispatcher<LocationCell, World, Event> getSearchEventDispatcher();

  /**
   * Get the data manager for handling saved data throughout Journey.
   *
   * @return the manager
   */
  DataManager<LocationCell, World> getDataManager();

  /**
   * Get the mode implementation for this mode type.
   *
   * @param modeType the mode type
   * @return the mode
   */
  // TODO this should only take mode type.
  //  The other stuff can be removed by moving functionality elsewhere:
  //  - the session should be given in the methods used by mode type
  //    instead of the constructor of the mode, I just moved it into the constructor
  //    a while ago because it was less coding to inject it in every method,
  //    but I think it's technically wrong here
  //  - the force passable should really only be in static helper methods. No reason
  //    to have them here.
  Mode<LocationCell, World> mode(ModeType modeType,
                                 SearchSession<LocationCell, World> session,
                                 Set<Material> forcePassable);

  /**
   * Get the mode implementations for a specific entity.
   *
   * @param entity the entity that needs to move
   * @return the list of modes
   */
  List<Mode<LocationCell, World>> modesFor(Entity entity);

}
