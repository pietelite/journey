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

package dev.pietelite.journey.spigot.command.list;

import dev.pietelite.journey.common.JourneyCommon;
import dev.pietelite.journey.common.data.DataAccessException;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.command.common.CommandNode;
import dev.pietelite.journey.spigot.command.common.Parameter;
import dev.pietelite.journey.spigot.command.common.PlayerCommandNode;
import dev.pietelite.journey.spigot.util.Format;
import dev.pietelite.journey.spigot.util.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command to list personal search endpoints.
 */
public class JourneyListMineCommand extends PlayerCommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyListMineCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.JOURNEY_TO_CUSTOM_USE,
        "List saved personal destinations",
        "mine");
    addSubcommand(Parameter.builder()
            .supplier(Parameter.ParameterSupplier.builder()
                .strict(false)
                .usage("[page]")
                .build())
            .build(),
        "View saved custom locations");
  }

  @Override
  public boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) throws DataAccessException {
    int pageNumber;
    if (args.length > 0) {
      try {
        pageNumber = Integer.parseInt(args[0]);

        if (pageNumber < 0) {
          player.spigot().sendMessage(Format.error("The page number may not be negative!"));
          return false;
        }
      } catch (NumberFormatException e) {
        player.spigot().sendMessage(Format.error("The page number must be an integer."));
        return false;
      }
    } else {
      pageNumber = 1;
    }

    Map<String, LocationCell> cells = JourneyCommon.<LocationCell, World>getDataManager()
        .getPersonalEndpointManager()
        .getPersonalEndpoints(player.getUniqueId());

    if (cells.isEmpty()) {
      player.spigot().sendMessage(Format.warn("You have no saved custom locations yet!"));
      return true;
    }

    List<Map.Entry<String, LocationCell>> sortedEntryList = new ArrayList<>(cells.entrySet());
    sortedEntryList.sort(Map.Entry.comparingByKey());

    StringBuilder builder = new StringBuilder();
    sortedEntryList.forEach(entry -> builder
        .append(Format.ACCENT2)
        .append(entry.getKey())
        .append(Format.DEFAULT)
        .append(" > ")
        .append(Format.toPlain(Format.locationCell(entry.getValue(), Format.DEFAULT)))
        .append("\n"));
    ChatPaginator.ChatPage chatPage = ChatPaginator.paginate(builder.toString(),
        pageNumber,
        ChatPaginator.GUARANTEED_NO_WRAP_CHAT_PAGE_WIDTH,
        ChatPaginator.CLOSED_CHAT_PAGE_HEIGHT - 1);

    pageNumber = Math.min(pageNumber, chatPage.getTotalPages());

    player.spigot().sendMessage(Format.success("Custom Locations - Page ",
        Format.toPlain(Format.note(Integer.toString(pageNumber))),
        " of ",
        Format.toPlain(Format.note(Integer.toString(chatPage.getTotalPages())))));
    Arrays.stream(chatPage.getLines()).forEach(player::sendMessage);

    return true;
  }

}