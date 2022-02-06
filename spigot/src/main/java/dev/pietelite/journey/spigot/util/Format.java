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

package dev.pietelite.journey.spigot.util;

import dev.pietelite.journey.spigot.api.navigation.LocationCell;
import java.util.Arrays;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.ChatColor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * A utility class for supplying all general Journey messages
 * specifically for Spigot Minecraft.
 */
public final class Format {

  public static final ChatColor THEME = ChatColor.LIGHT_PURPLE;
  public static final ChatColor SUCCESS = ChatColor.GREEN;
  public static final ChatColor INFO = ChatColor.GOLD;
  public static final ChatColor WARN = ChatColor.YELLOW;
  public static final ChatColor ERROR = ChatColor.RED;
  public static final ChatColor DEBUG = ChatColor.AQUA;
  public static final ChatColor ACCENT = ChatColor.LIGHT_PURPLE;
  public static final ChatColor ACCENT2 = ChatColor.DARK_AQUA;
  public static final ChatColor DEFAULT = ChatColor.WHITE;
  public static final String PREFIX = THEME + "Journey " + ChatColor.DARK_GRAY + "% " + ChatColor.RESET;

  private Format() {
  }

  /**
   * Convert a Spigot text object into a plain string.
   *
   * @param text the text
   * @return the string
   */
  public static String toPlain(BaseComponent[] text) {
    StringBuilder builder = new StringBuilder();
    for (BaseComponent component : text) {
      builder.append(component.toPlainText());
    }
    return builder.toString();
  }

  /**
   * Build Spigot text from a string message.
   *
   * @param single the string
   * @return the text
   */
  public static BaseComponent[] textOf(String single) {
    return new BaseComponent[]{new TextComponent(single)};
  }

  /**
   * Apply a chat color to every message in the sequence and return a
   * Spigot text object.
   *
   * @param color   the color
   * @param message the strings
   * @return the text
   */
  public static BaseComponent[] applyColorToAllOf(ChatColor color, String... message) {
    StringBuilder builder = new StringBuilder();
    for (Object m : message) {
      builder.append(color).append(m);
    }
    return textOf(builder.toString());
  }

  /**
   * Returns text that signifies some successful state.
   *
   * @param message the strings of the message
   * @return the Spigot text
   */
  public static BaseComponent[] success(String... message) {
    return chain(textOf(PREFIX), applyColorToAllOf(SUCCESS, message));
  }

  /**
   * Returns text that signifies the content is solely informative.
   *
   * @param message the strings of the message
   * @return the Spigot text
   */
  public static BaseComponent[] info(String... message) {
    return chain(textOf(PREFIX), applyColorToAllOf(INFO, message));
  }

  /**
   * Returns text that signifies some warning state.
   *
   * @param message the strings of the message
   * @return the Spigot text
   */
  public static BaseComponent[] warn(String... message) {
    return chain(textOf(PREFIX), applyColorToAllOf(WARN, message));
  }

  /**
   * Returns text that signifies some error state.
   *
   * @param message the strings of the message
   * @return the Spigot text
   */
  public static BaseComponent[] error(String... message) {
    return chain(textOf(PREFIX), applyColorToAllOf(ERROR, message));
  }

  /**
   * Returns text that signifies the content is solely informative
   * when debugging.
   *
   * @param message the strings of the message
   * @return the Spigot text-
   */
  public static BaseComponent[] debug(String... message) {
    return chain(textOf(PREFIX), applyColorToAllOf(DEBUG, message));
  }

  /**
   * Creates a Spigot object from a string that is colored the default
   * chat color. This signifies the content to be a small piece of
   * information, like a note.
   *
   * @param message the strings of the message
   * @return the text object
   */
  public static BaseComponent[] note(String... message) {
    return applyColorToAllOf(DEFAULT, message);
  }

  /**
   * Create a formatted Spigot text object from a location.
   *
   * @param cell         the location
   * @param defaultColor the primary color of the characters within the returned text
   * @return the text
   */
  public static BaseComponent[] locationCell(LocationCell cell, ChatColor defaultColor) {
    return applyColorToAllOf(defaultColor,
        "[",
        Format.ACCENT + "" + cell.getX(),
        ", ",
        Format.ACCENT + "" + cell.getY(),
        ", ",
        Format.ACCENT + "" + cell.getZ(),
        " in ",
        Format.ACCENT + cell.getDomain().getName(),
        "]");
  }

  /**
   * Chain otherwise-unrelated Spigot text objects together
   * into one larger text object.
   *
   * @param arrays the text objects
   * @return the final text object
   */
  public static BaseComponent[] chain(BaseComponent[]... arrays) {
    return Arrays.stream(arrays)
        .flatMap(Arrays::stream)
        .toArray(BaseComponent[]::new);
  }

  /**
   * See {@link #command(String, String, String)}, but it uses the command as the label.
   *
   * @param command     the command
   * @param description the description
   * @return the Spigot text object
   */
  public static BaseComponent[] command(@NotNull String command, @Nullable String description) {
    return command(command, command, description);
  }

  /**
   * Create a spigot text object that allows the user to run the command
   * when the text is clicked in chat.
   *
   * @param label       the display name of the text component
   * @param command     the command to run
   * @param description the description of the command and its behavior
   * @return the Spigot text object
   */
  public static BaseComponent[] command(@NotNull String label,
                                        @NotNull String command,
                                        @Nullable String description) {
    return new ComponentBuilder()
        .append(Format.INFO + "[")
        .append(Format.ACCENT + label)
        .italic(true)
        .event(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
        .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
            new Text((description == null || description.isEmpty())
                ? command
                : description + "\n\n" + command)))
        .append(Format.INFO + "]")
        .italic(false)
        .create();
  }

}
