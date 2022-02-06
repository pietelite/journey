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

package dev.pietelite.journey.common.util;

import java.util.regex.Pattern;

/**
 * A utility class used to manage static classes that validate input.
 */
public final class Validator {

  private Validator() {
  }

  /**
   * Check if the name is of the valid data name form.
   * It should start with a letter, then be a series letters, numbers, spaces, or dashes,
   * then end with a letter or a number.
   *
   * @param name the name to check
   * @return true if it is valid
   */
  public static boolean isInvalidDataName(String name) {
    if (name.equalsIgnoreCase("help")) {
      return true;
    }
    return !Pattern.matches("^[a-zA-Z][a-zA-Z0-9 -]{1,30}[a-zA-Z0-9]$", name);
  }

}
