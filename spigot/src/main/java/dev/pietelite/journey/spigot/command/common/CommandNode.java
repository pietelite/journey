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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import dev.pietelite.journey.common.data.DataAccessException;
import dev.pietelite.journey.common.util.Extra;
import dev.pietelite.journey.spigot.JourneySpigot;
import dev.pietelite.journey.spigot.util.Format;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.permissions.Permission;
import org.bukkit.util.StringUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A node within the Minecraft command tree structure.
 */
public abstract class CommandNode implements CommandExecutor, TabCompleter {

  public static final int ARG_MAX_LENGTH = 32;

  @Getter
  private final CommandNode parent;
  @Getter
  private final CommandNode helpCommand;
  private final Permission permission;
  private final String description;
  private final List<String> aliases = Lists.newLinkedList();
  private final List<CommandNode> children = Lists.newLinkedList();
  private final Map<Parameter, String> parameters = Maps.newLinkedHashMap();

  /**
   * A flag to say whether this command can ignore the regular barrier of waiting for
   * initialization to complete.
   */
  @Setter
  @Getter
  private boolean canBypassInvalid = false;

  /**
   * Simple constructor.
   *
   * @param parent       parent node, null if none
   * @param permission   permission allowing this command
   * @param description  describes the function of this command
   * @param primaryAlias the primary label used to call this command
   */
  public CommandNode(@Nullable CommandNode parent,
                     @Nullable Permission permission,
                     @NotNull String description,
                     @NotNull String primaryAlias) {
    this(parent, permission, description, primaryAlias, true);
  }

  /**
   * Full constructor.
   *
   * @param parent       parent node, null if none
   * @param permission   permission allowing this command
   * @param description  describes the function of this command
   * @param primaryAlias the primary label used to call this command
   * @param addHelp      whether a help sub-command is generated
   */
  public CommandNode(@Nullable CommandNode parent,
                     @Nullable Permission permission,
                     @NotNull String description,
                     @NotNull String primaryAlias,
                     boolean addHelp) {
    Objects.requireNonNull(description);
    Objects.requireNonNull(primaryAlias);
    this.parent = parent;
    this.permission = permission;
    this.description = description;
    this.aliases.add(primaryAlias);
    if (addHelp) {
      this.helpCommand = new HelpCommandNode(this);
      addChildren(this.helpCommand);
    } else {
      this.helpCommand = null;
    }
  }

  // Getters and Setters

  /**
   * Return an optional of the permission.
   * If the permission is empty, that means that anyone can use it.
   *
   * @return the permission
   */
  @NotNull
  public final Optional<Permission> getPermission() {
    return Optional.ofNullable(permission);
  }

  /**
   * Get the description of the command.
   *
   * @return the description
   */
  @NotNull
  public final String getDescription() {
    return description;
  }

  /**
   * Get the primary alias of the command.
   *
   * @return the alias
   */
  @NotNull
  public final String getPrimaryAlias() {
    return aliases.get(0);
  }

  /**
   * Return all the aliases used for the command.
   *
   * @return all aliases
   */
  @NotNull
  @SuppressWarnings("unused")
  public final List<String> getAliases() {
    return aliases;
  }

  /**
   * Add any amount of new aliases to this command.
   *
   * @param aliases the aliases
   */
  public final void addAliases(@NotNull String... aliases) {
    this.aliases.addAll(Arrays.asList(aliases));
  }

  /**
   * Get the {@link Parameter}s that can be used on top of this command as a subcommand.
   *
   * @return a parameter
   */
  @NotNull
  public final List<Parameter> getSubcommands() {
    return Lists.newLinkedList(parameters.keySet());
  }

  /**
   * Get the description for a subcommand.
   *
   * @param parameter a subcommand
   * @return the description
   */
  @NotNull
  public final String getSubcommandDescription(@NotNull Parameter parameter) {
    return parameters.get(parameter);
  }

  /**
   * Add a subcommand to perform some extra functionality with this command.
   * This is similar to a child command, but is considered simply a "flavor"
   * of the base command rather than an entirely different command.
   *
   * @param parameter   the subcommand
   * @param description the description of the subcommand
   */
  @SuppressWarnings("SameParameterValue")
  protected final void addSubcommand(@NotNull Parameter parameter, @NotNull String description) {
    this.parameters.put(parameter, description);
  }

  /**
   * Get the full command string. This is useful for explaining how to run
   * this command. It doesn't include the leading forward slash.
   *
   * @return the command
   */
  @NotNull
  public final String getFullCommand() {
    StringBuilder command = new StringBuilder(getPrimaryAlias());
    CommandNode cur = this;
    while (!cur.isRoot()) {
      command.insert(0, cur.parent.getPrimaryAlias() + " ");
      cur = cur.parent;
    }
    return command.toString();
  }

  /**
   * Add some number of children commands. The children commands
   * will be runnable by following this command with any of the child's
   * aliases.
   *
   * @param nodes the children
   */
  protected final void addChildren(CommandNode... nodes) {
    this.children.addAll(Arrays.asList(nodes));
  }

  /**
   * Get all the saved children nodes.
   *
   * @return all children
   */
  @NotNull
  public final List<CommandNode> getChildren() {
    return children;
  }

  /**
   * Determine if this command is a root command. In other words,
   * true if this command has no parent.
   *
   * @return true if root
   */
  public final boolean isRoot() {
    return parent == null;
  }

  /**
   * Send a command error to the sender. This is best used
   * when the sender uses the command incorrectly, because
   * the returned message includes a clickable help command option.
   *
   * @param sender the command sender
   * @param error  the error message
   */
  public final void sendCommandUsageError(CommandSender sender, String error) {
    sender.spigot().sendMessage(Format.error(error));
    // TODO figure out why this method has no hover/click event
    sender.spigot().sendMessage(Format.chain(Format.textOf(Format.PREFIX),
        Format.command("/" + getFullCommand() + " help",
            Format.DEFAULT + "Run help command")));
  }

  /**
   * See {@link #sendCommandUsageError(CommandSender, String)},
   * but use a {@link CommandError} instead of a string message.
   *
   * @param sender the sender
   * @param error  the error
   */
  public final void sendCommandUsageError(CommandSender sender, CommandError error) {
    sendCommandUsageError(sender, error.getMessage());
  }

  @Override
  public final boolean onCommand(@NotNull CommandSender sender,
                                 @NotNull Command command,
                                 @NotNull String label,
                                 @NotNull String[] args) {

    if (!JourneySpigot.getInstance().isValid() && !canBypassInvalid) {
      sender.spigot().sendMessage(Format.warn("The Journey plugin is still initializing..."));
      return false;
    }

    String[] argsCombined = Extra.combineQuotedArguments(args);

    // Adds support for quotations around space-delimited arguments
    List<String> actualArgsList = new LinkedList<>();
    Map<String, String> flags = new HashMap<>();
    String[] flagSplit;
    for (String arg : argsCombined) {
      if (arg.isEmpty()) {
        continue;
      }
      if (arg.charAt(0) == '-') {
        flagSplit = arg.substring(1).split(":", 2);
        if (flagSplit.length == 0) {
          continue;
        }
        if (!Extra.isNumber(flagSplit[0])) {
          if (flagSplit.length > 1) {
            flags.put(flagSplit[0].toLowerCase(), flagSplit[1]);
          } else {
            flags.put(flagSplit[0].toLowerCase(), "");
          }
        }
      } else {
        actualArgsList.add(arg);
      }
    }

    if (isRoot()) {
      for (String arg : actualArgsList) {
        if (arg.length() > ARG_MAX_LENGTH) {
          sender.spigot().sendMessage(Format.error("Arguments cannot exceed "
              + ARG_MAX_LENGTH
              + " characters!"));
          return false;
        }
      }
    }
    String[] actualArgs = actualArgsList.toArray(new String[0]);
    return onCommand(sender, command, label, actualArgs, flags);
  }

  private boolean onCommand(@NotNull CommandSender sender,
                            @NotNull Command command,
                            @NotNull String label,
                            @NotNull String[] actualArgs,
                            @NotNull Map<String, String> flags) {
    if (this.permission != null && !sender.hasPermission(this.permission)) {
      sender.spigot().sendMessage(Format.error("You don't have permission to do this!"));
      return false;
    }
    if (actualArgs.length != 0) {
      for (CommandNode child : children) {
        for (String alias : child.aliases) {
          if (alias.equalsIgnoreCase(actualArgs[0])) {
            return child.onCommand(sender,
                command,
                child.getPrimaryAlias(),
                Arrays.copyOfRange(actualArgs, 1, actualArgs.length),
                flags);
          }
        }
      }
    }
    try {
      return onWrappedCommand(sender, command, label, actualArgs, flags);
    } catch (DataAccessException e) {
      sender.spigot().sendMessage(Format.error("An error occurred. Please contact an administrator."));
      return false;
    }
  }

  /**
   * Executes the given command, returning its success.
   * This command already filters for permission status and
   * traverses the command tree to give just the elements of the subcommand,
   * if it is not the root.
   *
   * <p>If false is returned, then the "usage" plugin.yml entry for this command
   * (if defined) will be sent to the player.
   *
   * @param sender  the command sender
   * @param command the originally executed command
   * @param label   the alias of the command or subcommand used
   * @param args    the arguments of this command or subcommand
   * @return true if the command succeeded, false if failed
   */
  public abstract boolean onWrappedCommand(@NotNull CommandSender sender,
                                           @NotNull Command command,
                                           @NotNull String label,
                                           @NotNull String[] args,
                                           @NotNull Map<String, String> flags) throws DataAccessException;

  @Override
  public final List<String> onTabComplete(@NotNull CommandSender sender,
                                          @NotNull Command command,
                                          @NotNull String label,
                                          @NotNull String[] args) {
    List<String> allPossible = Lists.newLinkedList();
    if (this.permission != null && !sender.hasPermission(this.permission)) {
      return allPossible; // empty
    }
    if (args.length == 0) {
      return allPossible; // empty
    }
    for (CommandNode child : children) {
      for (int i = 0; i < child.aliases.size(); i++) {
        String alias = child.aliases.get(i);

        // If any alias matches from this child, then bump us up to its children
        if (alias.equalsIgnoreCase(args[0])) {
          return child.onTabComplete(sender,
              command,
              child.getPrimaryAlias(),
              Arrays.copyOfRange(args, 1, args.length));
        }

        // Only if we're on the last arg of the recursion and we're at the primary alias,
        // and we have permission to the command, add it
        if (args.length == 1 && i == 0) {
          if (child.permission == null || sender.hasPermission(child.permission)) {
            allPossible.add(alias);
          }
        }
      }
    }

    for (Parameter param : parameters.keySet()) {
      allPossible.addAll(param.nextAllowedInputs(sender, Arrays.copyOfRange(args, 0, args.length - 1)));
    }

    List<String> out = Lists.newLinkedList();
    StringUtil.copyPartialMatches(args[args.length - 1], allPossible, out);
    Collections.sort(out);
    return out;
  }

}
