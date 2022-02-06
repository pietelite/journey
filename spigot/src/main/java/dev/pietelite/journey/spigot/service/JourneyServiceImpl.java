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

package dev.pietelite.journey.spigot.service;

import dev.pietelite.journey.common.JourneyCommon;
import dev.pietelite.journey.common.data.DataManager;
import dev.pietelite.journey.common.navigation.Mode;
import dev.pietelite.journey.common.navigation.ModeType;
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.common.search.event.SearchDispatcher;
import dev.pietelite.journey.spigot.api.JourneyService;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.navigation.mode.ClimbMode;
import dev.pietelite.journey.spigot.navigation.mode.DoorMode;
import dev.pietelite.journey.spigot.navigation.mode.FlyMode;
import dev.pietelite.journey.spigot.navigation.mode.JumpMode;
import dev.pietelite.journey.spigot.navigation.mode.SwimMode;
import dev.pietelite.journey.spigot.navigation.mode.WalkMode;
import java.util.List;
import java.util.Set;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.event.Event;

/**
 * Implementation for the Journey Service.
 */
public class JourneyServiceImpl implements JourneyService {
  @Override
  public SearchDispatcher<LocationCell, World, Event> getSearchEventDispatcher() {
    return (SearchDispatcher<LocationCell, World, Event>)
        JourneyCommon.<LocationCell, World>getSearchEventDispatcher();
  }

  @Override
  public DataManager<LocationCell, World> getDataManager() {
    return JourneyCommon.getDataManager();
  }

  @Override
  public Mode<LocationCell, World> mode(ModeType modeType,
                                        SearchSession<LocationCell, World> session,
                                        Set<Material> forcePassable) {
    switch (modeType) {
      case CLIMB:
        return new ClimbMode(session, forcePassable);
      case DOOR:
        return new DoorMode(session, forcePassable);
      case FLY:
        return new FlyMode(session, forcePassable);
      case JUMP:
        return new JumpMode(session, forcePassable);
      case SWIM:
        return new SwimMode(session, forcePassable);
      case WALK:
        return new WalkMode(session, forcePassable);
      default:
        throw new IllegalArgumentException("Mode type unsupported: " + modeType.getId());
    }
  }

  @Override
  public List<Mode<LocationCell, World>> modesFor(Entity entity) {
    // TODO implement
    return null;
  }
}
