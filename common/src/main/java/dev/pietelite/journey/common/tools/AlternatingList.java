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

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Function;
import lombok.Getter;
import org.jetbrains.annotations.NotNull;

/**
 * A generic data structure to store a sequence of items, in which
 * the sequence alternates between items of type A and B (majors and minors).
 * The alternating list starts and ends at an item of type A and, in between,
 * there are items of type B.
 *
 * <p>To allow a sort of "flattening" of items so that the alternating list
 * can maybe be considered a normal list, both A and B are required to extend
 * from C. Therefore, a list of type C is returned when asked to flatten
 * the alternating list into a normal list.
 *
 * <p>The primary purpose of this class is to provide a way to keep track of a
 * path throughout a graph structure, where the majors are the nodes and the
 * minors are the edge.
 *
 * @param <A> the "major" type of item, which start and end the alternating list
 * @param <B> the "minor" type of item, which lie between the majors
 * @param <C> the most recent common ancestor of A and B
 */
@SuppressWarnings("unused")
public final class AlternatingList<A extends C, B extends C, C> {

  /**
   * The items that start and end the list. Minors lie in between them.
   */
  private final ArrayList<A> majors = new ArrayList<>();
  /**
   * The items that lie between the majors.
   */
  private final ArrayList<B> minors = new ArrayList<>();
  @Getter
  double length = 0;

  private AlternatingList(@NotNull List<A> majors, @NotNull List<B> minors) {
    this.majors.addAll(majors);
    this.minors.addAll(minors);
  }

  /**
   * Get a builder for creating an alternating list.
   * This is the only way to create an {@link AlternatingList} because
   * it must be created in a very specific way.
   *
   * @param item the first item of the {@link AlternatingList}
   * @param <A>  the "major" type
   * @param <B>  the "minor" type
   * @param <C>  the most recent common ancestor of A and B
   * @return a builder for an {@link AlternatingList}
   */
  public static <A extends C, B extends C, C> Builder<A, B, C> builder(@NotNull A item) {
    return new Builder<>(item);
  }

  /**
   * Get the first item in the list.
   *
   * @return the first item
   */
  public A getFirst() {
    return majors.get(0);
  }

  /**
   * Get the last item in the list.
   *
   * @return the last item
   */
  public A getLast() {
    return majors.get(majors.size() - 1);
  }

  /**
   * Creates a new {@link Traversal} for this list to traverse, or iterate,
   * through the various items.
   *
   * @return a traversal
   */
  public Traversal<A, B, C> traverse() {
    return new Traversal<>(this);
  }

  /**
   * Creates a new {@link Traversal.TraversalStep} for this list to traverse, or iterate,
   * through the various items.
   * As opposed to {@link #traverse()}, this method returns the individual steps so at every iteration,
   * you know which step, major or minor, you are in.
   *
   * @return the first major step
   */
  public Traversal.MajorTraversalStep<A, B, C> traverseStepwise() {
    return new Traversal.MajorTraversalStep<>(this, 0);
  }

  /**
   * Get all the major items.
   *
   * @return major items
   */
  public Collection<A> getMajors() {
    return new ArrayList<>(majors);
  }

  /**
   * Get all the minor items.
   *
   * @return minor items
   */
  public Collection<B> getMinors() {
    return new ArrayList<>(minors);
  }

  /**
   * Convert an {@link AlternatingList} into another by providing
   * a conversion function for each of the types of items.
   *
   * @param majorConverter the conversion to the new type of major
   * @param minorConverter the conversion to the new type of minor
   * @param <X>            the new major type
   * @param <Y>            the new minor type
   * @param <Z>            the new recent ancestor of the minor and major types
   * @return a new list
   */
  public <X extends Z, Y extends Z, Z> AlternatingList<X, Y, Z> convert(Function<A, X> majorConverter,
                                                                        Function<B, Y> minorConverter) {
    ArrayList<X> newMajors = new ArrayList<>(this.majors.size());
    ArrayList<Y> newMinors = new ArrayList<>(this.minors.size());
    majors.forEach(item -> newMajors.add(majorConverter.apply(item)));
    minors.forEach(item -> newMinors.add(minorConverter.apply(item)));
    return new AlternatingList<>(newMajors, newMinors);
  }

  /**
   * "Flatten" the entire path into a string of single objects
   * by converting every node and edge into that object.
   *
   * @param firstFunction  converting function for firsts
   * @param secondFunction converting function for seconds
   * @param <X>            the type of object to convert into
   * @return the list of new typed objects
   */
  public <X> List<X> flatten(Function<A, X> firstFunction, Function<B, X> secondFunction) {
    List<X> flattened = new LinkedList<>();
    for (int i = 0; i < minors.size(); i++) {
      flattened.add(firstFunction.apply(majors.get(i)));
      flattened.add(secondFunction.apply(minors.get(i)));
    }
    flattened.add(firstFunction.apply(majors.get(majors.size() - 1)));
    return flattened;
  }

  /**
   * A builder to create an alternating list.
   */
  public static final class Builder<A extends C, B extends C, C> {
    /**
     * The items that start and end the list. Minors lie in between them.
     */
    private final LinkedList<A> majors = new LinkedList<>();
    /**
     * The items that lie between the majors.
     */
    private final LinkedList<B> minors = new LinkedList<>();

    /**
     * Basic constructor. It requires at least one item.
     *
     * @param item the first item in the list
     */
    public Builder(@NotNull A item) {
      this.majors.addLast(item);
    }

    /**
     * Add a major and minor item to the beginning of the list.
     *
     * @param major a major item, and the new first item in the list
     * @param minor a minor item, and the new second item in the list
     */
    public void addFirst(@NotNull A major, @NotNull B minor) {
      this.majors.addFirst(major);
      this.minors.addFirst(minor);
    }

    /**
     * Add a major and minor item to the end of the list.
     *
     * @param minor a minor item, and the new penultimate item in the list
     * @param major a major item, and the new final item in the list
     */
    public void addLast(@NotNull B minor, @NotNull A major) {
      this.minors.addLast(minor);
      this.majors.addLast(major);
    }

    /**
     * Build an alternating list that this builder was trying to build.
     *
     * @return the complete alternating list
     */
    public AlternatingList<A, B, C> build() {
      return new AlternatingList<>(majors, minors);
    }
  }

  /**
   * An object meant to traverse across an alternating list.
   * This is the only way to access the items inside an alternating list.
   *
   * @param <A> the "major" type
   * @param <B> the "minor" type
   * @param <C> the ancestor of major and minor types
   */
  public static class Traversal<A extends C, B extends C, C> {
    private TraversalStep<A, B, C, ?, ?> step;

    private Traversal(AlternatingList<A, B, C> list) {
      this.step = new MajorTraversalStep<>(list, 0);
    }

    /**
     * Move the cursor to the next item in the list and return the item.
     *
     * @return the next item
     */
    public C next() {
      if (!hasNext()) {
        throw new NoSuchElementException("There is no next object");
      }
      this.step = this.step.next();
      return this.step.getItem();
    }

    /**
     * True if there is a next item and {@link #next()} can be called.
     *
     * @return true if there is a next item
     */
    public boolean hasNext() {
      return this.step.hasNext();
    }

    /**
     * Get the item at the current location of the cursor.
     *
     * @return the current item
     */
    @NotNull
    public C get() {
      return this.step.getItem();
    }

    /**
     * A single traversal step that wraps around the content of an item in the alternating list.
     *
     * @param <A> the major type
     * @param <B> the minor type
     * @param <C> the ancestor type of the major and minor types
     * @param <T> the major step type
     * @param <N> the minor step type
     */
    public abstract static class TraversalStep<A extends C, B extends C, C,
        T extends TraversalStep<A, B, C, T, N>,
        N extends TraversalStep<A, B, C, N, T>> {

      /**
       * Returns true if there is a next step to traverse to.
       *
       * @return true if there is a next step
       */
      public abstract boolean hasNext();

      /**
       * Move to the next traversal step and then return it.
       *
       * @return the next traversal step
       */
      public abstract N next();

      /**
       * Get the item that this traversal step stores.
       *
       * @return the item held within the traversal step
       */
      public abstract C getItem();
    }

    /**
     * A step along a traversal that represents a stage where the
     * current item in the list is one of the major items.
     */
    public static final class MajorTraversalStep<A extends C, B extends C, C> extends
        TraversalStep<A, B, C, MajorTraversalStep<A, B, C>, MinorTraversalStep<A, B, C>> {
      private final AlternatingList<A, B, C> list;
      private final int index;

      MajorTraversalStep(AlternatingList<A, B, C> list, int index) {
        this.list = list;
        this.index = index;
      }

      @Override
      public A getItem() {
        return list.majors.get(index);
      }

      @Override
      public boolean hasNext() {
        return index < list.minors.size();
      }

      @Override
      public MinorTraversalStep<A, B, C> next() {
        return new MinorTraversalStep<>(list, index);
      }
    }

    /**
     * A step along a traversal that represents a stage where the
     * current item in the list is one of the minor items.
     */
    public static final class MinorTraversalStep<A extends C, B extends C, C>
        extends TraversalStep<A, B, C, MinorTraversalStep<A, B, C>, MajorTraversalStep<A, B, C>> {
      private final AlternatingList<A, B, C> list;
      private final int index;

      MinorTraversalStep(AlternatingList<A, B, C> list, int index) {
        this.list = list;
        this.index = index;
      }

      @Override
      public B getItem() {
        return list.minors.get(index);
      }

      @Override
      public boolean hasNext() {
        return true;
      }

      @Override
      public MajorTraversalStep<A, B, C> next() {
        return new MajorTraversalStep<>(list, index + 1);
      }
    }

  }

}
