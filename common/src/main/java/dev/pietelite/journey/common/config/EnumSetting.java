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

package dev.pietelite.journey.common.config;

import java.util.HashMap;
import java.util.Map;
import org.jetbrains.annotations.NotNull;

/**
 * A setting that holds an enum value.
 *
 * @param <E> the enum type
 */
public class EnumSetting<E extends Enum<E>> extends Setting<E> {

  final Map<String, E> capitalNameToEnum;

  EnumSetting(@NotNull String path, @NotNull E defaultValue, @NotNull Class<E> clazz) {
    super(path, defaultValue, clazz);
    HashMap<String, E> nameMap = new HashMap<>();
    for (E enumConstant : clazz.getEnumConstants()) {
      nameMap.put(enumConstant.name().toUpperCase(), enumConstant);
    }
    capitalNameToEnum = nameMap;
  }

  @Override
  public E parseValue(@NotNull String string) {
    return capitalNameToEnum.get(string.toUpperCase());
  }

  @Override
  @NotNull
  public String printValue() {
    return getValue().name().toLowerCase();
  }
}
