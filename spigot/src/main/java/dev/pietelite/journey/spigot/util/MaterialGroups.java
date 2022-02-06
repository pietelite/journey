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

import com.google.common.collect.Sets;
import java.util.Set;
import org.bukkit.Material;

/**
 * A utility class to enumerate out groups of materials for classification purposes.
 */
public final class MaterialGroups {

  /**
   * All pressure plates.
   */
  public static final Set<Material> PRESSURE_PLATES = Sets.newHashSet(
      Material.ACACIA_PRESSURE_PLATE,
      Material.BIRCH_PRESSURE_PLATE,
      Material.CRIMSON_PRESSURE_PLATE,
      Material.DARK_OAK_PRESSURE_PLATE,
      Material.HEAVY_WEIGHTED_PRESSURE_PLATE,
      Material.JUNGLE_PRESSURE_PLATE,
      Material.LIGHT_WEIGHTED_PRESSURE_PLATE,
      Material.OAK_PRESSURE_PLATE,
      Material.POLISHED_BLACKSTONE_PRESSURE_PLATE,
      Material.SPRUCE_PRESSURE_PLATE,
      Material.STONE_PRESSURE_PLATE,
      Material.WARPED_PRESSURE_PLATE
  );
  /**
   * Anything that a player, under any circumstance, is not allowed to touch.
   */
  public static final Set<Material> INVALID_PASSABLE = Sets.newHashSet(
      Material.LAVA,
      Material.END_PORTAL);
  /**
   * Anything that a player can squeeze through vertically, other than the obvious.
   */
  public static final Set<Material> VERTICALLY_PASSABLE = Sets.newHashSet(
      Material.LADDER);
  /**
   * Anything that a player can squeeze through laterally, other than the obvious.
   */
  public static final Set<Material> LATERALLY_PASSABLE = Sets.newHashSet(
      Material.WHITE_CARPET,
      Material.ORANGE_CARPET,
      Material.MAGENTA_CARPET,
      Material.LIGHT_BLUE_CARPET,
      Material.YELLOW_CARPET,
      Material.LIME_CARPET,
      Material.PINK_CARPET,
      Material.GRAY_CARPET,
      Material.LIGHT_GRAY_CARPET,
      Material.CYAN_CARPET,
      Material.PURPLE_CARPET,
      Material.BLUE_CARPET,
      Material.BROWN_CARPET,
      Material.GREEN_CARPET,
      Material.RED_CARPET,
      Material.BLACK_CARPET/*,
      SpigotUtil.setSlabType(Material.OAK_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SPRUCE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.BIRCH_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.JUNGLE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.ACACIA_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.DARK_OAK_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.STONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SANDSTONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.PETRIFIED_OAK_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.COBBLESTONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.BRICK_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.STONE_BRICK_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.NETHER_BRICK_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.QUARTZ_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.RED_SANDSTONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.PURPUR_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.PRISMARINE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.PRISMARINE_BRICK_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.DARK_PRISMARINE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SMOOTH_QUARTZ_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SMOOTH_STONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.CUT_SANDSTONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.CUT_RED_SANDSTONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.POLISHED_GRANITE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SMOOTH_RED_SANDSTONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.MOSSY_STONE_BRICK_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.POLISHED_DIORITE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.MOSSY_COBBLESTONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.END_STONE_BRICK_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SMOOTH_SANDSTONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.SMOOTH_QUARTZ_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.GRANITE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.ANDESITE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.RED_NETHER_BRICK_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.POLISHED_ANDESITE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.DIORITE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.CRIMSON_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.WARPED_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.BLACKSTONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.POLISHED_BLACKSTONE_SLAB, Slab.Type.BOTTOM),
      SpigotUtil.setSlabType(Material.POLISHED_BLACKSTONE_BRICK_SLAB, Slab.Type.BOTTOM)*/);
  /**
   * Anything that a player can stand on, other than the obvious.
   */
  public static final Set<Material> TALL_SOLIDS = Sets.newHashSet(
      Material.LADDER);

  private MaterialGroups() {
  }

}
