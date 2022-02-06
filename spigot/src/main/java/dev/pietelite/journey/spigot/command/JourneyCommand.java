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

package dev.pietelite.journey.spigot.command;

import dev.pietelite.journey.common.JourneyCommon;
import dev.pietelite.journey.common.data.DataAccessException;
import dev.pietelite.journey.common.tools.BufferedFunction;
import dev.pietelite.journey.common.tools.BufferedSupplier;
import dev.pietelite.journey.common.util.Extra;
import dev.pietelite.journey.spigot.JourneySpigot;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.command.common.CommandNode;
import dev.pietelite.journey.spigot.util.Format;
import dev.pietelite.journey.spigot.util.Permissions;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.ConsoleCommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Root command of all commands for the Journey plugin.
 */
public class JourneyCommand extends CommandNode {

  /**
   * General constructor.
   */
  public JourneyCommand() {
    super(null, Permissions.JOURNEY_USE,
        "The root for all journey commands",
        "journey");
    addChildren(new JourneyAcceptCommand(this));
    addChildren(new JourneyAdminCommand(this));
    addChildren(new JourneyCancelCommand(this));
    addChildren(new JourneyDeleteCommand(this));
    addChildren(new JourneyListCommand(this));
    addChildren(new JourneySaveCommand(this));
    addChildren(new JourneyToCommand(this));
    setCanBypassInvalid(true);
  }

  /**
   * Get a buffered supplier of a list of the names of public endpoints,
   * quoted as needed for multi-word names.
   *
   * @return the supplier
   */
  @NotNull
  public static BufferedSupplier<List<String>> bufferedPublicEndpointSupplier() {
    return new BufferedSupplier<>(() -> {
      try {
        return JourneyCommon.<LocationCell, World>getDataManager()
            .getPublicEndpointManager()
            .getPublicEndpoints().keySet()
            .stream().map(Extra::quoteStringWithSpaces).collect(Collectors.toList());
      } catch (DataAccessException e) {
        return new LinkedList<>();
      }
    }, 1000);
  }

  /**
   * Get a buffered supplier of a map of players to the names of their personal endpoints,
   * quoted as needed for multi-word names.
   *
   * @return the supplier
   */
  @NotNull
  public static BufferedFunction<Player, List<String>> bufferedPersonalEndpointFunction() {
    return new BufferedFunction<>(player -> {
      try {
        return JourneyCommon.<LocationCell, World>getDataManager()
            .getPersonalEndpointManager()
            .getPersonalEndpoints(player.getUniqueId()).keySet()
            .stream().map(Extra::quoteStringWithSpaces).collect(Collectors.toList());
      } catch (DataAccessException e) {
        return new LinkedList<>();
      }
    }, 1000);
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args,
                                  @NotNull Map<String, String> flags) {
    if (sender instanceof ConsoleCommandSender) {
      sender.spigot().sendMessage(new ComponentBuilder()
          .append("%%%%%%%%   ").color(ChatColor.DARK_GRAY)
          .append("Journey").color(Format.THEME.asBungee())
          .append("   %%%%%%%%%").reset().color(ChatColor.DARK_GRAY)
          .create());
      sender.spigot().sendMessage(new ComponentBuilder()
          .append("         ")
          .append("v" + JourneySpigot.getInstance().getDescription().getVersion()).color(ChatColor.DARK_GRAY)
          .create());
      sender.spigot().sendMessage(new ComponentBuilder()
          .append("        ")
          .append("by  ").color(ChatColor.GRAY)
          .append("PietElite").color(ChatColor.DARK_AQUA).bold(true)
          .create());
      sender.spigot().sendMessage(new ComponentBuilder()
          .append(" ")
          .append("github.com/pietelite/journey").color(ChatColor.GOLD).event(
              new ClickEvent(ClickEvent.Action.OPEN_URL,
                  "https://github.com/pietelite/journey")).italic(true).underlined(true)
          .create());
      sender.spigot().sendMessage(new ComponentBuilder()
          .append("%%%%%%%%%%%%%%%%%%%%%%%%%%%%%%").color(ChatColor.DARK_GRAY)
          .create());
    } else {
      sender.spigot().sendMessage(new ComponentBuilder()
          .append("%%%%%%%%  ").color(ChatColor.DARK_GRAY)
          .append(" Journey ").color(Format.THEME.asBungee())
          .append("  %%%%%%%%").reset().color(ChatColor.DARK_GRAY)
          .create());
      sender.spigot().sendMessage(new ComponentBuilder()
          .append("             ")
          .append("v" + JourneySpigot.getInstance().getDescription().getVersion()).color(ChatColor.DARK_GRAY)
          .create());
      sender.spigot().sendMessage(new ComponentBuilder()
          .append("            ")
          .append("by ").color(ChatColor.GRAY)
          .append("PietElite").color(ChatColor.DARK_AQUA).bold(true)
          .create());
      sender.spigot().sendMessage(new ComponentBuilder()
          .append("  ")
          .append("github.com/pietelite/journey").color(ChatColor.GOLD).event(
              new ClickEvent(ClickEvent.Action.OPEN_URL,
                  "https://github.com/pietelite/journey")).italic(true).underlined(true)
          .create());
      sender.spigot().sendMessage(new ComponentBuilder()
          .append("%%%%%%%%%%%%%%%%%%%%%%%%%%%").color(ChatColor.DARK_GRAY)
          .create());
    }
    return true;
  }
}
