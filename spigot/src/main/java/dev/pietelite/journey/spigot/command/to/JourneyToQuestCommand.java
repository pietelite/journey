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

package dev.pietelite.journey.spigot.command.to;

import dev.pietelite.journey.common.config.Settings;
import dev.pietelite.journey.common.tools.BufferedFunction;
import dev.pietelite.journey.common.util.Extra;
import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import dev.pietelite.journey.spigot.command.common.CommandError;
import dev.pietelite.journey.spigot.command.common.CommandFlags;
import dev.pietelite.journey.spigot.command.common.CommandNode;
import dev.pietelite.journey.spigot.command.common.Parameter;
import dev.pietelite.journey.spigot.command.common.PlayerCommandNode;
import dev.pietelite.journey.spigot.search.PlayerDestinationGoalSearchSession;
import dev.pietelite.journey.spigot.util.Format;
import dev.pietelite.journey.spigot.util.Permissions;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import me.blackvein.quests.Quest;
import me.blackvein.quests.Quests;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A command that allows a player to journey to a destination of a {@link Quest}.
 *
 * @see Quests
 */
public class JourneyToQuestCommand extends PlayerCommandNode {

  private final Quests quests;

  /**
   * General constructor.
   *
   * @param parent the parent command
   * @param quests the quests plugin
   */
  public JourneyToQuestCommand(@Nullable CommandNode parent, @NotNull Quests quests) {
    super(parent, Permissions.JOURNEY_TO_QUEST_USE,
        "Blaze trails to your quest objectives",
        "next");
    this.quests = quests;
    BufferedFunction<Player, List<String>> questFunction = new BufferedFunction<>(player -> {
      Plugin plugin = Bukkit.getPluginManager().getPlugin("Quests");
      if (!(plugin instanceof Quests)) {
        return new LinkedList<>();
      }
      return quests.getQuester(player.getUniqueId())
          .getCurrentQuests()
          .keySet()
          .stream()
          .map(quest -> Extra.quoteStringWithSpaces(quest.getName()))
          .collect(Collectors.toList());
    }, 1000);
    addSubcommand(Parameter.builder()
        .supplier(Parameter.ParameterSupplier.builder()
            .usage("<quest>")
            .allowedEntries((src, list) -> {
              if (src instanceof Player) {
                return questFunction.apply((Player) src);
              } else {
                return new LinkedList<>();
              }
            })
            .strict(false)
            .build())
        .build(), "Blaze trails to your next destination for a quest");
  }

  @Override
  public boolean onWrappedPlayerCommand(@NotNull Player player,
                                        @NotNull Command command,
                                        @NotNull String label,
                                        @NotNull String[] args,
                                        @NotNull Map<String, String> flags) {

    if (args.length == 0) {
      sendCommandUsageError(player, CommandError.FEW_ARGUMENTS);
      return false;
    }

    Quest quest = quests.getQuest(args[0]);
    if (quest == null) {
      player.spigot().sendMessage(Format.error("That quest doesn't exist."));
      return false;
    }

    if (!quests.getQuester(player.getUniqueId()).getCurrentQuests().containsKey(quest)) {
      player.spigot().sendMessage(Format.error("You are not doing that quest."));
      return false;
    }

    LinkedList<Location> locationsToReach = quests.getQuester(player.getUniqueId())
        .getCurrentStage(quest).getLocationsToReach();
    if (locationsToReach.isEmpty()) {
      player.spigot().sendMessage(Format.error("That quest has no destination."));
      return false;
    }

    LocationCell endLocation = new LocationCell(locationsToReach.getFirst());


    int algorithmStepDelay = 0;
    if (CommandFlags.ANIMATE.isIn(flags)) {
      algorithmStepDelay = CommandFlags.ANIMATE.retrieve(player, flags);
    }

    PlayerDestinationGoalSearchSession session = new PlayerDestinationGoalSearchSession(player,
        new LocationCell(player.getLocation()),
        endLocation,
        CommandFlags.ANIMATE.isIn(flags),
        Settings.DEFAULT_NOFLY_FLAG.getValue() != CommandFlags.NOFLY.isIn(flags),
        Settings.DEFAULT_NODOOR_FLAG.getValue() != CommandFlags.NODOOR.isIn(flags),
        algorithmStepDelay);

    int timeout = CommandFlags.TIMEOUT.isIn(flags)
        ? CommandFlags.TIMEOUT.retrieve(player, flags)
        : Settings.DEFAULT_SEARCH_TIMEOUT.getValue();

    session.launchSession(timeout);
    return true;
  }
}