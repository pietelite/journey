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

import dev.pietelite.journey.common.JourneyCommon;
import java.util.Objects;
import org.jetbrains.annotations.NotNull;

/**
 * A setting, generally used as a key-value pair from a configuration file.
 * These are enumerated in {@link Settings} and they shouldn't be created elsewhere.
 *
 * @param <T> the data type stored in the setting
 */
public abstract class Setting<T> {

  protected final Class<T> clazz;
  private final String path;
  private final T defaultValue;
  boolean initialized = false;
  private T value;

  Setting(@NotNull String path, @NotNull T defaultValue, @NotNull Class<T> clazz) {
    if (!clazz.isInstance(defaultValue)) {
      throw new IllegalArgumentException("The value must match the class type");
    }
    this.path = Objects.requireNonNull(path);
    this.defaultValue = Objects.requireNonNull(defaultValue);
    this.clazz = clazz;
  }

  /**
   * Return the configuration path of the setting.
   *
   * @return the path
   */
  @NotNull
  public String getPath() {
    return path;
  }

  /**
   * Get the default value stored in this setting.
   *
   * @return the default value
   */
  @NotNull
  public T getDefaultValue() {
    return clazz.cast(defaultValue);
  }

  /**
   * Get the value stored in this setting.
   *
   * @return the setting value
   */
  @NotNull
  public T getValue() {
    if (!initialized) {
      JourneyCommon.getLogger().info("Using default value for config setting at " + path);
      value = getDefaultValue();
      initialized = true;
    }
    return value;
  }

  /**
   * Set the value for this setting.
   *
   * @param value the value
   */
  public void setValue(@NotNull T value) {
    this.value = Objects.requireNonNull(value);
    this.initialized = true;
  }

  /**
   * Parse a string into a value accepted by this setting.
   *
   * @param string the serialized value string
   * @return the value
   */
  public abstract T parseValue(@NotNull String string);

  /**
   * Print the value stored in this setting into a serialized format.
   *
   * @return the string (serialized) form of the value
   */
  @NotNull
  public abstract String printValue();

}
