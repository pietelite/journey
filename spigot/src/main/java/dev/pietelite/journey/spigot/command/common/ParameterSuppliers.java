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
import java.util.List;
import java.util.stream.Collectors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.World;
import org.bukkit.entity.Player;

/**
 * An enumeration utility class to enumerate out common
 * {@link Parameter.ParameterSupplier}s.
 */
public final class ParameterSuppliers {

  public static final Parameter.ParameterSupplier NONE = Parameter.ParameterSupplier.builder()
      .allowedEntries((src, prev) -> Lists.newLinkedList())
      .usage("")
      .build();
  public static final Parameter.ParameterSupplier ONLINE_PLAYER = Parameter.ParameterSupplier.builder()
      .allowedEntries((src, prev) ->
          Bukkit.getOnlinePlayers().stream().map(Player::getName).collect(Collectors.toList()))
      .usage("<player>")
      .strict(false)
      .build();
  public static final Parameter.ParameterSupplier WORLD = Parameter.ParameterSupplier.builder()
      .allowedEntries((src, prev) ->
          Bukkit.getWorlds().stream().map(World::getName).collect(Collectors.toList()))
      .usage("<world>")
      .strict(true)
      .build();
  public static final Parameter.ParameterSupplier BOOLEAN = Parameter.ParameterSupplier.builder()
      .allowedEntries((src, prev) ->
          Lists.newArrayList("true", "false", "t", "f"))
      .usage("true|false")
      .build();
  public static final Parameter.ParameterSupplier INTEGER = Parameter.ParameterSupplier.builder()
      .usage("<integer>")
      .build();
  public static final Parameter.ParameterSupplier ITEM = Parameter.ParameterSupplier.builder()
      .allowedEntries((src, prev) -> {
        List<String> out = Lists.newLinkedList();
        Lists.newLinkedList(Registry.MATERIAL)
            .stream()
            .filter(Material::isItem)
            .forEach(material -> {
              out.add(material.toString().toLowerCase());
              out.add(material.getKey().toString());
            });
        return out;
      })
      .usage("<item>")
      .build();

  private ParameterSuppliers() {
  }

}
