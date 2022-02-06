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

package dev.pietelite.journey.spigot.search.listener;

import dev.pietelite.journey.common.search.SearchSession;
import dev.pietelite.journey.common.search.event.StepSearchEvent;
import dev.pietelite.journey.common.search.event.StopPathSearchEvent;
import dev.pietelite.journey.common.search.event.StopSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotModeFailureEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotModeSuccessEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStepSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStopPathSearchEvent;
import dev.pietelite.journey.spigot.api.search.event.SpigotStopSearchEvent;
import dev.pietelite.journey.spigot.search.AnimationManager;
import dev.pietelite.journey.spigot.search.SpigotPlayerSearchSession;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * A class to listen for Spigot events originating from
 * {@link SearchSession#search()}
 * and allowing for the animating of the algorithm in realtime.
 */
public class AnimationListener implements Listener {

  /**
   * A handler animating the case where the algorithm finds that a certain block is accessible
   * during algorithm execution.
   *
   * @param event the event
   */
  @EventHandler
  public void successModeEvent(SpigotModeSuccessEvent event) {
    AnimationManager manager = getAnimationManager(event);
    if (manager != null) {
      manager.showResult(event.getSearchEvent().getCell(), true, event.getSearchEvent().getModeType());
    }
  }

  /**
   * A handler animating the case where the algorithm finds that a certain block is inaccessible
   * during algorithm execution.
   *
   * @param event the event
   */
  @EventHandler
  public void failModeEvent(SpigotModeFailureEvent event) {
    AnimationManager manager = getAnimationManager(event);
    if (manager != null) {
      manager.showResult(event.getSearchEvent().getCell(), false, event.getSearchEvent().getModeType());
    }
  }

  /**
   * Handle the {@link StepSearchEvent}
   * by showing blocks to the player that called for the search.
   * This is part of the algorithm-animation process.
   *
   * @param event the event
   */
  @EventHandler
  public void stepSearchEvent(SpigotStepSearchEvent event) {
    AnimationManager manager = getAnimationManager(event);
    if (manager != null) {
      manager.showStep(event.getSearchEvent().getStep().location());
    }
  }

  /**
   * Handle the {@link StopPathSearchEvent}
   * by cleaning up the pieces of the animation for that specific path and calling
   * {@link AnimationManager#undoAnimation()}.
   *
   * @param event the event
   */
  @EventHandler
  public void stopPathSearchEvent(SpigotStopPathSearchEvent event) {
    AnimationManager manager = getAnimationManager(event);
    if (manager != null) {
      manager.undoAnimation();
    }
  }

  /**
   * Handle the {@link StopSearchEvent}
   * by cleaning up whatever is needed to be cleaned up by the running of the
   * animation.
   *
   * <p>{@link #stopPathSearchEvent} is the one that normally cleans up the animations
   * but if the animation is cancelled before it finishes, then this is the last resort
   * to fix the user's environment.
   *
   * @param event the event
   */
  @EventHandler
  public void stopSearchEvent(SpigotStopSearchEvent event) {
    AnimationManager manager = getAnimationManager(event);
    if (manager != null) {
      manager.undoAnimation();
    }
  }

  private AnimationManager getAnimationManager(SpigotSearchEvent<?> event) {
    if (event.getSearchEvent().getSession() instanceof SpigotPlayerSearchSession<?>) {
      AnimationManager animationManager = ((SpigotPlayerSearchSession<?>) event.getSearchEvent().getSession())
          .getAnimationManager();
      if (animationManager.isAnimating()) {
        return animationManager;
      }
    }
    return null;
  }

}
