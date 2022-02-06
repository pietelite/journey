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

package dev.pietelite.journey.spigot;

import dev.pietelite.journey.common.JourneyCommon;
import dev.pietelite.journey.common.search.event.SearchDispatcher;
import dev.pietelite.journey.common.search.event.SearchEvent;
import dev.pietelite.journey.common.util.Serialize;
import dev.pietelite.journey.spigot.api.JourneyService;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.api.search.event.SpigotFoundSolutionEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotIgnoreCacheSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotModeFailureEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotModeSuccessEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStartItinerarySearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStartPathSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStartSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStepSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStopItinerarySearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStopPathSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStopSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotVisitationSearchEvent;
import dev.pietelite.journey.spigot.command.JourneyCommand;
import dev.pietelite.journey.spigot.command.common.CommandNode;
import dev.pietelite.journey.spigot.config.SpigotConfigManager;
import dev.pietelite.journey.spigot.data.SpigotDataManager;
import dev.pietelite.journey.spigot.manager.DebugManager;
import dev.pietelite.journey.spigot.manager.NetherManager;
import dev.pietelite.journey.spigot.manager.PlayerSearchManager;
import dev.pietelite.journey.spigot.search.listener.AnimationListener;
import dev.pietelite.journey.spigot.search.listener.DataStorageListener;
import dev.pietelite.journey.spigot.search.listener.PlayerSearchListener;
import dev.pietelite.journey.spigot.service.JourneyServiceImpl;
import dev.pietelite.journey.spigot.util.LoggerSpigot;
import dev.pietelite.journey.spigot.util.SpigotMinecraftConversions;
import lombok.Getter;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.command.PluginCommand;
import org.bukkit.event.Event;
import org.bukkit.permissions.Permission;
import org.bukkit.plugin.ServicePriority;
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
  private boolean valid = false;

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
    Bukkit.getServer().getServicesManager().register(
        JourneyService.class,
        new JourneyServiceImpl(),
        this,
        ServicePriority.Normal
    );

    if (this.getDataFolder().mkdirs()) {
      getLogger().info("Journey data folder created");
    }

    // Set up Journey Common
    JourneyCommon.setLogger(new LoggerSpigot());
    JourneyCommon.setConfigManager(SpigotConfigManager.initialize("config.yml"));
    JourneyCommon.setConversions(new SpigotMinecraftConversions());

    // Set up caches for Spigot Journey
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
    JourneyCommon.setDataManager(new SpigotDataManager());

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
      // TODO initialize likely-used paths here (link to link)
      valid = true;
      JourneySpigot.getInstance().getLogger().info("Finished initializing Journey");
    });

    // bStats
    Metrics metrics = new Metrics(this, 14192);
  }

  @Override
  public void onDisable() {
    // Plugin shutdown logic
    getSearchManager().cancelAllSearches();
    getSearchManager().stopAllJourneys();
    serializeCaches();
  }

  private void deserializeCaches() {

    // Nether Ports cache
    Serialize.deserializeCache(this.getDataFolder(),
        NetherManager.NETHER_MANAGER_CACHE_FILE_NAME,
        manager -> this.netherManager = manager,
        NetherManager::new);
    JourneySpigot.getInstance().getLogger().info(this.netherManager.size() + " nether ports deserialized");

  }

  private void serializeCaches() {

    // nether Ports cache
    Serialize.serializeCache(this.getDataFolder(),
        NetherManager.NETHER_MANAGER_CACHE_FILE_NAME,
        () -> this.netherManager,
        manager -> this.netherManager = manager,
        NetherManager::new);
    JourneySpigot.getInstance().getLogger().info(this.netherManager.size() + " nether ports serialized");

  }

}
