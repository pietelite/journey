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

import java.util.LinkedList;

/**
 * A utility class to perform "extra" miscellaneous tasks.
 */
public final class Extra {

  private Extra() {
  }

  /**
   * Determine if a string is a number.
   *
   * @param s the string
   * @return true if it is a number
   */
  public static boolean isNumber(String s) {
    for (int c : s.toCharArray()) {
      if (c > '9' || c < '0') {
        return false;
      }
    }
    return true;
  }

  /**
   * Combine an array of single-word inputs into another array where the words
   * surrounded by quotes are combined into singular strings within the array.
   *
   * @param input the inputs
   * @return the combined inputs
   */
  public static String[] combineQuotedArguments(String[] input) {
    String full = String.join(" ", input);
    LinkedList<String> out = new LinkedList<>();
    boolean inEncloser = false;
    StringBuilder arg = new StringBuilder();
    for (char c : full.toCharArray()) {
      if (c == ' ') {
        if (inEncloser) {
          arg.append(c);
        } else {
          out.add(arg.toString());
          arg = new StringBuilder();
        }
      } else if (c == '"') {
        inEncloser = !inEncloser;
        if (!arg.toString().isEmpty()) {
          out.add(arg.toString());
        }
        arg = new StringBuilder();
      } else {
        arg.append(c);
      }
    }
    if (!arg.toString().isEmpty()) {
      out.add(arg.toString());
    }
    return out.toArray(new String[0]);
  }

  /**
   * Place quotes around the input if there is a space in it.
   *
   * @param input the input string
   * @return the new string
   */
  public static String quoteStringWithSpaces(String input) {
    if (input.contains(" ")) {
      return "\"" + input + "\"";
    } else {
      return input;
    }
  }

}
