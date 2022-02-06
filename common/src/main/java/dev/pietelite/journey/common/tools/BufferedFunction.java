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

package dev.pietelite.journey.common.tools;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import org.jetbrains.annotations.NotNull;

/**
 * A function that only calculates a new value after a certain amount of time has passed.
 * This class is useful for saving time in calculations that do not need precise results
 * from a function all the time in situations where the function has a very long operation time.
 *
 * @param <T> the input type
 * @param <R> the return type
 * @see BufferedSupplier
 */
public class BufferedFunction<T, R> implements Function<T, R> {

  private final Function<T, R> function;
  private final long delayMillis;
  private final Map<T, R> dataMap = new ConcurrentHashMap<>();
  private long latestQueryTime = 0;

  /**
   * Default constructor.
   *
   * @param function    the underlying function to get new values from inputs
   * @param delayMillis the delay in milliseconds between getting new cached values
   */
  public BufferedFunction(@NotNull Function<T, R> function, long delayMillis) {
    this.function = function;
    this.delayMillis = delayMillis;
  }

  @Override
  public R apply(T input) {
    long currentTime = System.currentTimeMillis();
    R value;
    if (currentTime >= latestQueryTime + delayMillis || !dataMap.containsKey(input)) {
      latestQueryTime = currentTime;
      value = function.apply(input);
      dataMap.put(input, value);
    } else {
      value = dataMap.get(input);
    }
    return value;
  }

}
