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

/**
 * A utility class to manage utility methods pertaining to displaying time quantities.
 */
public final class TimeUtil {

  private TimeUtil() {
  }

  /**
   * Convert some number of seconds to a nicely formatted string.
   * This works best for periods of time less than a day.
   *
   * @param seconds the number of seconds
   * @return a nicely formatting string to send to the user
   */
  public static String toSimpleTime(long seconds) {

    if (seconds <= 0) {
      return "instantly";
    }

    if (seconds < 60) {
      return seconds + " secs";
    }

    long mins = seconds / 60;
    long secs = seconds % 60;

    if (mins < 60) {
      return mins + " mins"
          + (secs > 0 ? ", " + secs + " secs" : "");
    }

    long hours = mins / 60;
    mins = mins % 60;

    if (hours < 24) {
      return hours + " hours"
          + (mins > 0 ? ", " + mins + " mins" : "")
          + (secs > 0 ? ", " + secs + " secs" : "");
    }

    return "More than a day";

  }

}