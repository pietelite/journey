/*
 * MIT License
 *
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.journey.spigot;

import edu.whimc.journey.common.JourneyCommon;
import edu.whimc.journey.common.cache.PathCache;
import edu.whimc.journey.common.data.DataManager;
import edu.whimc.journey.common.navigation.Mode;
import edu.whimc.journey.common.navigation.ModeType;
import edu.whimc.journey.common.navigation.ModeTypeGroup;
import edu.whimc.journey.common.navigation.Port;
import edu.whimc.journey.common.search.PathTrial;
import edu.whimc.journey.common.search.SearchSession;
import edu.whimc.journey.common.search.event.SearchDispatcher;
import edu.whimc.journey.common.search.event.SearchEvent;
import edu.whimc.journey.spigot.command.JourneyCommand;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.config.SpigotConfigManager;
import edu.whimc.journey.spigot.data.SpigotDataManager;
import edu.whimc.journey.spigot.external.whimcportals.WhimcPortalPort;
import edu.whimc.journey.spigot.manager.DebugManager;
import edu.whimc.journey.spigot.manager.NetherManager;
import edu.whimc.journey.spigot.manager.PlayerSearchManager;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.navigation.mode.ClimbMode;
import edu.whimc.journey.spigot.navigation.mode.DoorMode;
import edu.whimc.journey.spigot.navigation.mode.FlyMode;
import edu.whimc.journey.spigot.navigation.mode.JumpMode;
import edu.whimc.journey.spigot.navigation.mode.SwimMode;
import edu.whimc.journey.spigot.navigation.mode.WalkMode;
import edu.whimc.journey.spigot.search.event.SpigotFoundSolutionEvent;
import edu.whimc.journey.spigot.search.event.SpigotIgnoreCacheSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotModeFailureEvent;
import edu.whimc.journey.spigot.search.event.SpigotModeSuccessEvent;
import edu.whimc.journey.spigot.search.event.SpigotStartItinerarySearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStartPathSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStartSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStepSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStopItinerarySearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStopPathSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotStopSearchEvent;
import edu.whimc.journey.spigot.search.event.SpigotVisitationSearchEvent;
import edu.whimc.journey.spigot.search.listener.AnimationListener;
import edu.whimc.journey.spigot.search.listener.DataStorageListener;
import edu.whimc.journey.spigot.search.listener.PlayerSearchListener;
import edu.whimc.journey.spigot.util.LoggerSpigot;
import edu.whimc.journey.spigot.util.Serialize;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import lombok.Getter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.generator.WorldInfo;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * The base plugin class of the Spigot implementation of Journey.
 * Used in tandem with {@link JourneyCommon} throughout the plugin.
 *
 * @see JourneyCommon
 */
public final class JourneySpigot extends JavaPlugin {

  private static JourneySpigot instance;

  // Caches
  @Getter
  private NetherManager netherManager;
  @Getter
  private DebugManager debugManager;
  @Getter
  private PlayerSearchManager searchManager;
  @Getter
  private boolean initializing = false;
  @Getter
  private float initializedPortion = 0;

  // Database
  @Getter
  private DataManager<LocationCell, World> dataManager;

  /**
   * Get the instance that is currently run on the Spigot server.
   *
   * @return the instance
   */
  public static JourneySpigot getInstance() {
    return instance;
  }

  @Override
  public void onLoad() {
    instance = this;
  }

  @Override
  public void onEnable() {
    getLogger().info("Initializing Journey...");

    if (this.getDataFolder().mkdirs()) {
      getLogger().info("Journey data folder created");
    }

    // Create caches
    JourneyCommon.setLogger(new LoggerSpigot());
    JourneyCommon.setConfigManager(SpigotConfigManager.initialize("config.yml"));
    JourneyCommon.setPathCache(new PathCache<LocationCell, World>());

    this.netherManager = new NetherManager();
    this.debugManager = new DebugManager();
    this.searchManager = new PlayerSearchManager();

    deserializeCaches();

    // Instantiate a SearchDispatcher. Keep registrations alphabetized
    SearchDispatcher<LocationCell, World, Event> dispatcher = new SearchDispatcher<>(event ->
        Bukkit.getServer().getPluginManager().callEvent(event));
    dispatcher.registerEvent(SpigotFoundSolutionEvent::new, SearchEvent.EventType.FOUND_SOLUTION);
    dispatcher.registerEvent(SpigotIgnoreCacheSearchEvent::new, SearchEvent.EventType.IGNORE_CACHE);
    dispatcher.registerEvent(SpigotModeFailureEvent::new, SearchEvent.EventType.MODE_FAILURE);
    dispatcher.registerEvent(SpigotModeSuccessEvent::new, SearchEvent.EventType.MODE_SUCCESS);
    dispatcher.registerEvent(SpigotStartItinerarySearchEvent::new, SearchEvent.EventType.START_ITINERARY);
    dispatcher.registerEvent(SpigotStartPathSearchEvent::new, SearchEvent.EventType.START_PATH);
    dispatcher.registerEvent(SpigotStartSearchEvent::new, SearchEvent.EventType.START);
    dispatcher.registerEvent(SpigotStepSearchEvent::new, SearchEvent.EventType.STEP);
    dispatcher.registerEvent(SpigotStopItinerarySearchEvent::new, SearchEvent.EventType.STOP_ITINERARY);
    dispatcher.registerEvent(SpigotStopPathSearchEvent::new, SearchEvent.EventType.STOP_PATH);
    dispatcher.registerEvent(SpigotStopSearchEvent::new, SearchEvent.EventType.STOP);
    dispatcher.registerEvent(SpigotVisitationSearchEvent::new, SearchEvent.EventType.VISITATION);
    JourneyCommon.setSearchEventDispatcher(dispatcher);

    // Set up data manager
    this.dataManager = new SpigotDataManager();

    // Register command
    CommandNode root = new JourneyCommand();
    PluginCommand command = getCommand(root.getPrimaryAlias());
    if (command == null) {
      throw new NullPointerException("You must register command "
          + root.getPrimaryAlias()
          + " in the plugin.yml");
    }
    command.setExecutor(root);
    command.setTabCompleter(root);
    root.getPermission().map(Permission::getName).ifPresent(command::setPermission);

    // Register listeners
    Bukkit.getPluginManager().registerEvents(netherManager, this);
    Bukkit.getPluginManager().registerEvents(new AnimationListener(), this);
    Bukkit.getPluginManager().registerEvents(new DataStorageListener(), this);
    Bukkit.getPluginManager().registerEvents(new PlayerSearchListener(), this);


    // Start doing a bunch of searches for common use cases
    Bukkit.getScheduler().runTaskAsynchronously(this, () -> {
      // TODO in order for initialization to work, we probably need to use a database because
      //  all of the path data probably cannot all be stored in memory. Untested, though!
//      initializeCacheables();
      initializing = true;
      JourneySpigot.getInstance().getLogger().info("Finished initializing Journey");
    });
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getSearchManager().cancelAllSearches();
    getSearchManager().stopAllJourneys();
    serializeCaches();
  }

  private void deserializeCaches() {
    // Path cache
    Serialize.<PathCache<LocationCell, World>>deserializeCache(this.getDataFolder(),
        PathCache.SERIALIZED_PATH_CACHE_FILENAME,
        JourneyCommon::setPathCache,
        PathCache::new);
    JourneySpigot.getInstance().getLogger().info(JourneyCommon.getPathCache().size() + " paths deserialized");

    // Nether Ports cache
    Serialize.deserializeCache(this.getDataFolder(),
        NetherManager.NETHER_MANAGER_CACHE_FILE_NAME,
        manager -> this.netherManager = manager,
        NetherManager::new);
    JourneySpigot.getInstance().getLogger().info(this.netherManager.size() + " nether ports deserialized");
  }

  private void serializeCaches() {
    // Path cache
    Serialize.<PathCache<LocationCell, World>>serializeCache(this.getDataFolder(),
        PathCache.SERIALIZED_PATH_CACHE_FILENAME,
        JourneyCommon::getPathCache,
        JourneyCommon::setPathCache,
        PathCache::new);
    JourneySpigot.getInstance().getLogger().info(JourneyCommon.getPathCache().size() + " paths serialized");

    // nether Ports cache
    Serialize.serializeCache(this.getDataFolder(),
        NetherManager.NETHER_MANAGER_CACHE_FILE_NAME,
        () -> this.netherManager,
        manager -> this.netherManager = manager,
        NetherManager::new);
    JourneySpigot.getInstance().getLogger().info(this.netherManager.size() + " nether ports serialized");
  }

  private void initializeCacheables() {

    List<Port<LocationCell, World>> allPorts = new LinkedList<>();
    allPorts.addAll(this.netherManager.makePorts());
    allPorts.addAll(WhimcPortalPort.makeAllPorts());

    Set<UUID> allWorlds = Bukkit.getWorlds().stream().map(WorldInfo::getUID).collect(Collectors.toSet());

    Map<UUID, List<Port<LocationCell, World>>> allPortsIntoWorld = new HashMap<>();
    Map<UUID, List<Port<LocationCell, World>>> allPortsOutOfWorld = new HashMap<>();

    // Set up all the lists
    for (UUID uuid : allWorlds) {
      allPortsIntoWorld.put(uuid, new LinkedList<>());
      allPortsOutOfWorld.put(uuid, new LinkedList<>());
    }

    // Add ports
    allPorts.forEach(port -> {
      allPortsIntoWorld.get(port.getDestination().getDomain().getUID()).add(port);
      allPortsOutOfWorld.get(port.getOrigin().getDomain().getUID()).add(port);
    });

    // Find how many calculations we'll be doing
    double calculations = 0;
    for (UUID uuid : allWorlds) {
      calculations += allPortsIntoWorld.get(uuid).size() * allPortsOutOfWorld.get(uuid).size();
    }

    SearchSession<LocationCell, World> session = SearchSession.dummy();
    PathTrial<LocationCell, World> pathTrial;
    ModeTypeGroup modeTypeGroup1 = new ModeTypeGroup(List.of(ModeType.WALK,
        ModeType.JUMP,
        ModeType.SWIM,
        ModeType.DOOR,
        ModeType.CLIMB));
    Collection<Mode<LocationCell, World>> modes1 = List.of(new WalkMode(session, Collections.emptySet()),
        new JumpMode(session, Collections.emptySet()),
        new SwimMode(session, Collections.emptySet()),
        new DoorMode(session, Collections.emptySet()),
        new ClimbMode(session, Collections.emptySet()));
    ModeTypeGroup modeTypeGroup2 = new ModeTypeGroup(List.of(ModeType.WALK,
        ModeType.JUMP,
        ModeType.SWIM,
        ModeType.DOOR,
        ModeType.CLIMB,
        ModeType.FLY));
    Collection<Mode<LocationCell, World>> modes2 = List.of(new FlyMode(session, Collections.emptySet()),
        new WalkMode(session, Collections.emptySet()),
        new JumpMode(session, Collections.emptySet()),
        new SwimMode(session, Collections.emptySet()),
        new DoorMode(session, Collections.emptySet()),
        new ClimbMode(session, Collections.emptySet()));

    final double oneCalculationPortion = 1d / calculations;
    for (UUID uuid : allWorlds) {
      for (Port<LocationCell, World> startingPort : allPortsIntoWorld.get(uuid)) {
        for (Port<LocationCell, World> endingPort : allPortsOutOfWorld.get(uuid)) {
          if (!startingPort.equals(endingPort)) {
            if (!JourneyCommon.<LocationCell, World>getPathCache().contains(startingPort.getDestination(),
                endingPort.getOrigin(),
                modeTypeGroup1)) {
              PathTrial.approximate(SearchSession.dummy(), startingPort.getDestination(), endingPort.getOrigin()).attempt(modes1, false);
            }
            if (!JourneyCommon.<LocationCell, World>getPathCache().contains(startingPort.getDestination(),
                endingPort.getOrigin(),
                modeTypeGroup2)) {
              PathTrial.approximate(SearchSession.dummy(), startingPort.getDestination(), endingPort.getOrigin()).attempt(modes2, false);
            }
          }
          initializedPortion += oneCalculationPortion;
        }
      }
    }
  }

}
