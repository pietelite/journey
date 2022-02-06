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

import org.bukkit.permissions.Permission;

/**
 * Utility class to enumerate {@link Permission}s.
 */
public final class Permissions {

  public static final Permission ADMIN = new Permission("journey.command.admin");
  public static final Permission JOURNEY_USE = new Permission("journey.command.use");
  public static final Permission JOURNEY_TO_CUSTOM_USE = new Permission("journey.command.to.custom.use");
  public static final Permission JOURNEY_TO_SURFACE_USE = new Permission("journey.command.to.surface.use");
  public static final Permission JOURNEY_TO_QUEST_USE = new Permission("journey.command.to.quest.use");
  public static final Permission JOURNEY_TO_PUBLIC_USE = new Permission("journey.command.to.public.use");
  public static final Permission JOURNEY_TO_PUBLIC_EDIT = new Permission("journey.command.to.public.edit");

  private Permissions() {
  }

}
