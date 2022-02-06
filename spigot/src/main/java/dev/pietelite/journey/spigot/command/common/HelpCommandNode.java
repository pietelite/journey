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

package dev.pietelite.journey.spigot.command.common;

import dev.pietelite.journey.spigot.util.Format;
import java.util.Map;
import java.util.Objects;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

final class HelpCommandNode extends CommandNode {

  public HelpCommandNode(@NotNull CommandNode parent) {
    super(parent,
        null,
        "Get help for this command",
        "help",
        false);
    Objects.requireNonNull(parent);
    addAliases("?");
  }

  @Override
  public boolean onWrappedCommand(@NotNull CommandSender sender,
                                  @NotNull Command command,
                                  @NotNull String label,
                                  @NotNull String[] args,
                                  @NotNull Map<String, String> flags) {
    CommandNode parent = Objects.requireNonNull(this.getParent());
    sender.spigot().sendMessage(Format.success(
        "Command: ["
            + ChatColor.GRAY
            + parent.getFullCommand() + "]"));
    sender.spigot().sendMessage(Format.success(
        "Description: "
        + ChatColor.GRAY
        + parent.getDescription()));
    for (CommandNode node : parent.getChildren()) {
      if (node.getPermission().map(sender::hasPermission).orElse(true)) {
        ComponentBuilder builder = new ComponentBuilder();
        builder.append(ChatColor.GRAY + "> " + parent.getPrimaryAlias() + " ")
            .append(ChatColor.AQUA + node.getPrimaryAlias());
        if (node.getChildren().size() > 1 || !node.getSubcommands().isEmpty()) {
          builder.append(" [ . . . ]");
          if (node.getHelpCommand() != null) {
            builder.event(new ClickEvent(ClickEvent.Action.RUN_COMMAND,
                    "/" + node.getHelpCommand().getFullCommand()))
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                    new Text(node.getDescription().isEmpty()
                        ? node.getFullCommand()
                        : node.getDescription() + "\n\n" + "/" + node.getHelpCommand().getFullCommand())));
          }
        }
        builder.append(ChatColor.WHITE + " " + node.getDescription());
        sender.spigot().sendMessage(builder.create());
      }
    }
    for (Parameter parameter : parent.getSubcommands()) {
      if (parameter.getPermission().map(sender::hasPermission).orElse(true)) {
        parameter.getFullUsage(sender).ifPresent(usage -> {
          StringBuilder builder = new StringBuilder();
          builder.append(ChatColor.GRAY)
              .append("> ")
              .append(parent.getPrimaryAlias())
              .append(" ")
              .append(ChatColor.AQUA)
              .append(usage)
              .append(ChatColor.GRAY);
          parameter.getFlags().forEach(flag -> builder.append(" ")
              .append(ChatColor.DARK_GRAY)
              .append("[")
              .append(ChatColor.GOLD)
              .append("-")
              .append(flag)
              .append(ChatColor.DARK_GRAY)
              .append("]"));
          builder.append("  ")
              .append(ChatColor.WHITE)
              .append(parent.getSubcommandDescription(parameter));

          sender.sendMessage(builder.toString());
        });
      }
    }
    return true;

  }

}
