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

package dev.pietelite.journey.common.navigation;

/**
 * Manage information about the traversal of locatables
 * within the game.
 *
 * @param <T> the type of locatable
 * @param <D> the type of domain
 */
public interface Journey<T extends Cell<T, D>, D> extends Runnable {

  /**
   * Notify this {@link Journey} that the given {@link Locatable}
   * has been visited. This may be called very often, so efficiency
   * is important here.
   *
   * @param locatable the visited locatable
   */
  void visit(T locatable);

  /**
   * Should run when the journey is completed or
   * the journey is otherwise left.
   */
  void stop();

  /**
   * Determine if the caller completed the journey.
   *
   * @return true if complete
   */
  @SuppressWarnings("unused")
  boolean isCompleted();

  /**
   * Run the journey, or restart if it's already been started.
   */
  @Override
  void run();

}
