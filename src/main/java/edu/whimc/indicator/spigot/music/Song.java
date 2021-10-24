/*
 * Copyright 2021 Pieter Svenson
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

package edu.whimc.indicator.spigot.music;

import edu.whimc.indicator.spigot.IndicatorSpigot;
import java.util.LinkedList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class Song {

  public static final Song SUCCESS_CHORD = new Song(List.of(
      new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.6f, 1),
      new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.75f, 3),
      new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 0.9f, 5),
      new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.1f, 7),
      new Note(Sound.BLOCK_NOTE_BLOCK_PLING, 1.33f, 9)
      ));

  private final List<Note> notes = new LinkedList<>();

  public Song() {

  }

  public Song(List<Note> notes) {
    this.notes.addAll(notes);
  }

  public void addNode(Note note) {
    this.notes.add(note);
  }

  public void play(Player player) {
    for (int i = 0; i < notes.size(); i++) {
      playNote(player, i);
    }
  }

  private void playNote(Player player, int index) {
    Bukkit.getScheduler().scheduleSyncDelayedTask(IndicatorSpigot.getInstance(), () ->
        player.playSound(player.getLocation(), this.notes.get(index).sound,
            1, this.notes.get(index).pitch),
        this.notes.get(index).delay);
  }

  public record Note(Sound sound, float pitch, int delay) { }

}
