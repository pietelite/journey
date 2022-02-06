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

import dev.pietelite.journey.common.navigation.Itinerary;
import dev.pietelite.journey.common.navigation.Journey;
import dev.pietelite.journey.common.search.ResultState;
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.spigot.JourneySpigot;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.api.search.event.SpigotFoundSolutionEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotIgnoreCacheSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStartItinerarySearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStartPathSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStartSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStopItinerarySearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStopPathSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStopSearchEvent;
import dev.pietelite.journey.spigot.navigation.PlayerJourney;
import dev.pietelite.journey.spigot.search.PlayerDestinationGoalSearchSession;
import dev.pietelite.journey.spigot.search.PlayerSessionState;
import dev.pietelite.journey.spigot.search.SpigotPlayerSearchSession;
import dev.pietelite.journey.spigot.util.Format;
import dev.pietelite.journey.spigot.util.TimeUtil;
import java.util.Optional;
import java.util.UUID;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

/**
 * A listener with a series of event handlers to perform general management operations
 * of running {@link PlayerDestinationGoalSearchSession}s.
 */
public class PlayerSearchListener implements Listener {

  private static final double STEVE_RUNNING_SPEED = 5.621;  // blocks per second

  /**
   * Handle the start of search sessions.
   * The instance must be saved and the last one running has to be stopped.
   *
   * @param event the event
   */
  @EventHandler
  public void startSearchEvent(SpigotStartSearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(event.getSearchEvent().getSession().getCallerId());
      if (player != null) {

        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug("Started a search for player "
            + Format.ACCENT + player.getName()));
        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug(
            "Modes: " + ChatColor.DARK_GRAY + search.getSession().modes().size()));
        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug(
            "Ports: " + ChatColor.DARK_GRAY + search.getSession().ports().size()));

        SearchSession<LocationCell, World> oldSession = JourneySpigot.getInstance()
            .getSearchManager()
            .getSearch(player.getUniqueId());

        if (oldSession != null && oldSession.getState() == ResultState.RUNNING) {
          player.spigot().sendMessage(Format.info("Canceling other search..."));
        }

        JourneySpigot.getInstance().getSearchManager().putSearch(player.getUniqueId(), search.getSession());
      }
    });
  }

  /**
   * Handle the event fired when a new solution is found.
   * Send the found {@link Itinerary} to the running {@link Journey}
   * as a prospective itinerary in case the player wants to use it.
   *
   * @param event the event
   */
  @EventHandler
  public void foundSolutionEvent(SpigotFoundSolutionEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      // Need to update the session state
      PlayerSessionState playerSessionState = search.getSessionState();

      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player == null) {
        return;
      }

      JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug(
          "Found a solution to a search for player " + Format.ACCENT + player.getName()));

      Itinerary<LocationCell, World> itinerary = event.getSearchEvent().getItinerary();
      if (playerSessionState.wasSolutionPresented()) {
        PlayerJourney journey = JourneySpigot.getInstance()
            .getSearchManager()
            .getJourney(player.getUniqueId());
        if (journey != null) {
          journey.setProspectiveItinerary(itinerary);
          player.spigot().sendMessage(Format.info("A faster path to your destination "
              + "was found from your original location."));
          player.spigot().sendMessage(Format.chain(Format.info("Run "),
              Format.command("/journey accept", "Accept an incoming trail request"),
              Format.textOf(Format.INFO + " to accept.")));
          return;
        }
      }

      if (playerSessionState.wasSolved()) {
        Bukkit.getScheduler().cancelTask(playerSessionState.getSuccessNotificationTaskId());
      }
      playerSessionState.setSolved(true);

      // Create a journey that is completed when the player reaches within 3 blocks of the endpoint
      PlayerJourney journey = new PlayerJourney(player.getUniqueId(), search.getSession(), itinerary);
      journey.run();

      // Save the journey
      JourneySpigot.getInstance().getSearchManager().putJourney(player.getUniqueId(), journey);

      // Set up a success notification that will be cancelled if a better one is found in some amount of time
      playerSessionState.setSuccessNotificationTaskId(Bukkit.getScheduler()
          .runTaskLater(JourneySpigot.getInstance(),
              () -> {
                player.spigot().sendMessage(new ComponentBuilder()
                    .append(Format.PREFIX)
                    .append(new ComponentBuilder()
                        .append("Success! Please follow the path.")
                        .color(Format.SUCCESS.asBungee())
                        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                            new Text(new ComponentBuilder("Search Statistics")
                                .color(Format.THEME.asBungee())
                                .bold(true)
                                .create()),
                            new Text(new ComponentBuilder("\nWalk Time: ")
                                .bold(false)
                                .color(ChatColor.DARK_GRAY.asBungee())
                                .append(TimeUtil.toSimpleTime(
                                    Math.round(event.getSearchEvent().getItinerary().getLength()
                                        / STEVE_RUNNING_SPEED)))
                                .color(Format.ACCENT2.asBungee())
                                .create()),
                            new Text(new ComponentBuilder("\nDistance: ")
                                .bold(false)
                                .color(ChatColor.DARK_GRAY.asBungee())
                                .append(Math.round(event.getSearchEvent().getItinerary().getLength())
                                    + " blocks")
                                .color(Format.ACCENT2.asBungee())
                                .create()),
                            new Text(new ComponentBuilder("\nSearch Time: ")
                                .bold(false)
                                .color(ChatColor.DARK_GRAY.asBungee())
                                .append(TimeUtil.toSimpleTime(
                                    Math.round((double) event.getSearchEvent().getExecutionTime() / 1000)))
                                .color(Format.ACCENT2.asBungee())
                                .create())))
                        .create())
                    .create());
                playerSessionState.setSolutionPresented(true);
              },
              20 /* one second (20 ticks) */)
          .getTaskId());
    });
  }

  /**
   * Handle the stop search event.
   * We need to remove the instance from memory storage and stop any running tasks.
   *
   * @param event the event
   */
  @EventHandler
  public void stopSearchEvent(SpigotStopSearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      // Send failure message if we finished unsuccessfully
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {

        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug("Stopping a search for player "
            + Format.ACCENT + player.getName()));
        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug("Status: "
            + ChatColor.DARK_GRAY + search.getSession().getState()));

        switch (search.getSession().getState()) {
          case STOPPED_FAILED:
            player.spigot().sendMessage(
                Format.error("Search ended. Either there's no path to it, or it's too far away!"));
            break;
          case STOPPED_CANCELED:
            player.spigot().sendMessage(
                Format.info("Search canceled."));
            break;
          case STOPPED_SUCCESSFUL:
            /* Don't say anything. They were already notified of the successful solutions. */
            break;
          default:
            Bukkit.getLogger().warning("A player search session stopped while in the "
                + search.getSession().getState() + " state");
        }
        // Remove from the searching set, if the saved session is the one that is currently stopping here.
        //  This check is necessary because another session could have been started while this one was running

        SearchSession<LocationCell, World> savedSession = JourneySpigot.getInstance().getSearchManager()
            .getSearch(player.getUniqueId());
        if (savedSession != null && savedSession.getUuid().equals(search.getSession().getUuid())) {
          JourneySpigot.getInstance().getSearchManager().removeSearch(player.getUniqueId());
        }
      }
    });
  }

  /**
   * Handle the event when an itinerary search begins for those that were caused by a player.
   *
   * @param event the event
   */
  @EventHandler
  public void startItinerarySearchEvent(SpigotStartItinerarySearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {
        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug(
            "Started an itinerary search for player " + Format.ACCENT + player.getName()));
      }
    });
  }

  /**
   * Handle the event when an itinerary search ends for those that were caused by a player.
   *
   * @param event the event
   */
  @EventHandler
  public void stopItinerarySearchEvent(SpigotStopItinerarySearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {
        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug(
            "Stopped an itinerary search for player " + Format.ACCENT + player.getName()));
        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug("Status: "
            + ChatColor.DARK_GRAY + event.getSearchEvent().getItineraryTrial().getState()));
      }
    });
  }

  /**
   * Handle the event when a path search begins for those that were caused by a player.
   *
   * @param event the event
   */
  @EventHandler
  public void startPathSearchEvent(SpigotStartPathSearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {
        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug(
            "Started a path search for player " + Format.ACCENT + player.getName()));
      }
    });
  }

  /**
   * Handle the event when a path search ends for those that were caused by a player.
   *
   * @param event the event
   */
  @EventHandler
  public void stopPathSearchEvent(SpigotStopPathSearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {
        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug(
            "Stopped a path search for player " + Format.ACCENT + player.getName()));
        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug("Status: "
            + ChatColor.DARK_GRAY + event.getSearchEvent().getPathTrial().getState()));
      }
    });
  }

  /**
   * Handle the event when a search stops considering cached paths for those that were caused by a player.
   *
   * @param event the event
   */
  @EventHandler
  public void ignoreCacheSearchEvent(SpigotIgnoreCacheSearchEvent event) {
    getPlayerSearch(event).ifPresent(search -> {
      Player player = Bukkit.getPlayer(search.getSession().getCallerId());
      if (player != null) {
        if (!search.getSession().getState().isSuccessful()) {
          player.spigot().sendMessage(Format.warn("There probably isn't a solution, "
              + "but the search will continue, just in case!"));
        }
        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug(
            "Ignoring cache in search for " + Format.ACCENT + player.getName()));
        JourneySpigot.getInstance().getDebugManager().broadcast(Format.debug("Status: "
            + ChatColor.DARK_GRAY + event.getSearchEvent().getSession().getState()));
      }
    });
  }


  /**
   * Handler for when players move throughout the world.
   * This allows us to update the last known location so player journeys
   * know which particles to show.
   *
   * @param event the event
   */
  @EventHandler
  public void onPlayerMove(PlayerMoveEvent event) {

    if (event.getTo() == null) {
      return;
    }
    LocationCell cell = new LocationCell(event.getTo());
    UUID playerUuid = event.getPlayer().getUniqueId();
    PlayerJourney playerJourney = JourneySpigot.getInstance().getSearchManager().getJourney(playerUuid);

    if (playerJourney == null) {
      // We don't care about a player moving unless there's a journey happening
      return;
    }

    LocationCell currentLocation = JourneySpigot.getInstance().getSearchManager().getLocation(playerUuid);
    if (currentLocation == null) {
      JourneySpigot.getInstance().getSearchManager().putLocation(playerUuid, cell);
      return;
    }

    if (currentLocation.equals(cell)) {
      return;
    }

    JourneySpigot.getInstance().getSearchManager().putLocation(playerUuid, cell);
    playerJourney.visit(cell);
  }

  /**
   * Handle the player quit event.
   *
   * @param event the event
   */
  @EventHandler
  public void onQuit(PlayerQuitEvent event) {
    // Perform quit logic. Currently, nothing.
  }

  @SuppressWarnings("unchecked")
  private Optional<SpigotPlayerSearchSession<SearchSession<LocationCell, World>>> getPlayerSearch(
      SpigotSearchEvent<?> event) {
    if (event.getSearchEvent().getSession() instanceof SpigotPlayerSearchSession) {
      return Optional.of((SpigotPlayerSearchSession<SearchSession<LocationCell, World>>)
          event.getSearchEvent().getSession());
    } else {
      return Optional.empty();
    }
  }

}
