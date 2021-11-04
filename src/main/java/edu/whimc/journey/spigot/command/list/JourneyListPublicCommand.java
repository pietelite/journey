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

package edu.whimc.journey.spigot.command.list;

import edu.whimc.journey.common.data.DataAccessException;
import edu.whimc.journey.spigot.JourneySpigot;
import edu.whimc.journey.spigot.command.common.CommandNode;
import edu.whimc.journey.spigot.command.common.Parameter;
import edu.whimc.journey.spigot.navigation.LocationCell;
import edu.whimc.journey.spigot.util.Format;
import edu.whimc.journey.spigot.util.Permissions;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.util.ChatPaginator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command to list public search endpoints.
 */
public class JourneyListPublicCommand extends CommandNode {

  /**
   * General constructor.
   *
   * @param parent the parent command
   */
  public JourneyListPublicCommand(@Nullable CommandNode parent) {
    super(parent,
        Permissions.JOURNEY_TO_PUBLIC_USE,
        "List saved public destinations",
        "public");
    addSubcommand(Parameter.builder()
            .supplier(Parameter.ParameterSupplier.builder()
                .strict(false)
                .usage("[page]")
                .build())
            .build(),
        "View saved public locations with a given page");
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args,
                                  @NotNull Map<String, String> flags) throws DataAccessException {
    int pageNumber;
    if (args.length > 0) {
      try {
        pageNumber = Integer.parseInt(args[0]);

        if (pageNumber < 0) {
          sender.spigot().sendMessage(Format.error("The page number may not be negative!"));
          return false;
        }
      } catch (NumberFormatException e) {
        sender.spigot().sendMessage(Format.error("The page number must be an integer."));
        return false;
      }
    } else {
      pageNumber = 1;
    }

    Map<String, LocationCell> cells = JourneySpigot.getInstance()
        .getDataManager()
        .getPublicEndpointManager()
        .getPublicEndpoints();

    if (cells.isEmpty()) {
      sender.spigot().sendMessage(Format.warn("There are no saved server locations yet!"));
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

    sender.spigot().sendMessage(Format.success("Server Locations - Page ",
        Format.toPlain(Format.note(Integer.toString(pageNumber))),
        " of ",
        Format.toPlain(Format.note(Integer.toString(chatPage.getTotalPages())))));
    Arrays.stream(chatPage.getLines()).forEach(sender::sendMessage);

    return true;
  }

}