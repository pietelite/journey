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

package dev.pietelite.journey.common.search;

/**
 * An enumeration of possible result states from running processes.
 */
public enum ResultState {

  IDLE("idle"),
  RUNNING("running"),
  RUNNING_SUCCESSFUL("running (successful)"),
  CANCELING_FAILED("canceling (failed)"),
  CANCELING_SUCCESSFUL("canceling (successful)"),
  STOPPED_FAILED("stopped (failed)"),
  STOPPED_SUCCESSFUL("stopped (successful)"),
  STOPPED_CANCELED("stopped (canceled)");

  final String message;

  ResultState(String message) {
    this.message = message;
  }

  /**
   * Determine if the state is implying the process is still operating (running).
   *
   * @return true if running
   */
  public boolean isRunning() {
    return this == RUNNING
        || this == RUNNING_SUCCESSFUL
        || this == CANCELING_FAILED
        || this == CANCELING_SUCCESSFUL;
  }

  /**
   * Determine if this state is implying the process has finished operating
   * or is preparing to finish operating.
   *
   * @return true if finished
   */
  public boolean hasFinished() {
    return this == STOPPED_FAILED
        || this == STOPPED_SUCCESSFUL
        || this == STOPPED_CANCELED
        || this == CANCELING_FAILED
        || this == CANCELING_SUCCESSFUL;
  }

  /**
   * Determine if this state is implying the process had or will have a successful result upon completion.
   *
   * @return true if successful
   */
  public boolean isSuccessful() {
    return this == STOPPED_SUCCESSFUL
        || this == RUNNING_SUCCESSFUL;
  }

  /**
   * Determine if this state is implying the process was canceled/stopped before it was determined successful,
   * whether it would have been successful if it was allowed to continue or not.
   *
   * @return true if canceled and consequently failed
   */
  public boolean isCancelFailed() {
    return this == CANCELING_FAILED
        || this == STOPPED_CANCELED;
  }

  /**
   * Determine if this state was canceled at any point, whether a successful result had been found or not.
   *
   * @return true if canceled (manually stopped)
   */
  public boolean isCanceled() {
    return this == CANCELING_FAILED
        || this == CANCELING_SUCCESSFUL
        || this == STOPPED_CANCELED;
  }

  @Override
  public String toString() {
    return this.message;
  }
}
