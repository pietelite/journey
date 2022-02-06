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

package dev.pietelite.journey.spigot.search;

import dev.pietelite.journey.common.search.DestinationGoalSearchSession;
import dev.pietelite.journey.common.search.LocalUpwardsGoalSearchSession;
import dev.pietelite.journey.common.search.ResultState;
import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.spigot.JourneySpigot;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.util.Format;
import dev.pietelite.journey.spigot.util.TimeUtil;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

/**
 * An interface to tag onto any extension of a {@link SearchSession},
 * like {@link LocalUpwardsGoalSearchSession}
 * or {@link DestinationGoalSearchSession},
 * to allow management of the session, callback to the session,
 * and catching of events relating to sessions that have this type
 * (the type being caused by a player).
 *
 * @param <S> the self-reference to the session type implementing this interface
 */
public interface SpigotPlayerSearchSession<S extends SearchSession<LocationCell, World>> {

  /**
   * Get the player causing this session.
   *
   * @return the player
   */
  @Nullable
  Player getPlayer();

  /**
   * Get the animation manager in charge of animating the session's algorithm.
   *
   * @return the animation manager
   */
  AnimationManager getAnimationManager();

  /**
   * Get the encapsulation of the state of the session.
   * This exists because we want to talk about information relating to the actual session
   * without imposing the player-cause-specific information to a general session.
   *
   * @return the state
   */
  PlayerSessionState getSessionState();

  /**
   * Get the actual session (self-reference).
   *
   * @return the session
   */
  S getSession();

  /**
   * Start the session. This ultimately causes {@link SearchSession#search()},
   * but includes messages to the player and scheduling with the Spigot/Bukkit scheduler.
   *
   * @param timeout how long to try searching before we find a solution and stop naturally or
   *                stop the search manually with {@link SearchSession#stop()}
   */
  default void launchSession(int timeout) {

    Player player = getPlayer();
    if (player == null) {
      return;
    }

    // Set up a "Working..." message if it takes too long
    player.spigot().sendMessage(new ComponentBuilder()
        .append(Format.PREFIX)
        .append(new ComponentBuilder()
            .append("Searching...")
            .color(Format.INFO.asBungee())
            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                new Text(new ComponentBuilder("Search Parameters")
                    .color(Format.THEME.asBungee())
                    .bold(true)
                    .create()),
                new Text(new ComponentBuilder("\nTimeout: ")
                    .bold(false)
                    .color(ChatColor.DARK_GRAY.asBungee())
                    .append(TimeUtil.toSimpleTime(timeout))
                    .color(Format.ACCENT2.asBungee())
                    .create())))
            .create())
        .create());
    // Don't put anything after the search message...
    //    player.spigot().sendMessage(new ComponentBuilder()
    //        .append(Format.PREFIX)
    //        .append(new ComponentBuilder()
    //            .append("  [Search Flags]")
    //            .color(Format.ACCENT2.asBungee())
    //            .italic(true)
    //            .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
    //                new Text(Format.note("Unimplemented"))))
    //            .create())
    //        .create());

    // Set up a search cancellation in case it takes too long
    Bukkit.getScheduler().runTaskLater(JourneySpigot.getInstance(),
        () -> {
          getSession().stop();
          if (getSession().getState() == ResultState.CANCELING_FAILED) {
            player.spigot().sendMessage(Format.error("Time limit surpassed. Canceling search..."));
          }
        },
        (long) timeout * 20 /* ticks per second */);

    // SEARCH
    Bukkit.getScheduler().runTaskAsynchronously(JourneySpigot.getInstance(), () -> {
          // Search... this may take a long time
          getSession().search();
        }
    );
  }

}
