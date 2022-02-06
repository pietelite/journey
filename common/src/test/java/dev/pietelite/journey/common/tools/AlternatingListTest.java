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

import java.util.NoSuchElementException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AlternatingListTest {

  private static class Flub {
    public int id;

    public Flub(int id) {
      this.id = id;
    }

    @Override
    public String toString() {
      return "" + id;
    }
  }

  private static class Foo extends Flub {
    public Foo(int id) {
      super(id);
    }
  }

  private static class Bar extends Flub {
    public Bar(int id) {
      super(id);
    }
  }

  @Test
  public void alternatingListTest() {
    Foo first = new Foo(0);
    AlternatingList.Builder<Foo, Bar, Flub> builder = AlternatingList.builder(first);
    builder.addLast(new Bar(1), new Foo(2));
    builder.addFirst(new Foo(-2), new Bar(-1));
    builder.addLast(new Bar(3), new Foo(4));

    AlternatingList<Foo, Bar, Flub> list = builder.build();
    AlternatingList.Traversal<Foo, Bar, Flub> traversal = list.traverse();

    Assertions.assertTrue(traversal.hasNext());
    Assertions.assertEquals(-2, traversal.get().id);

    Assertions.assertTrue(traversal.hasNext());
    Assertions.assertEquals(-1, traversal.next().id);

    Assertions.assertTrue(traversal.hasNext());
    Assertions.assertEquals(0, traversal.next().id);

    Assertions.assertTrue(traversal.hasNext());
    Assertions.assertEquals(1, traversal.next().id);

    Assertions.assertTrue(traversal.hasNext());
    Assertions.assertEquals(2, traversal.next().id);

    Assertions.assertTrue(traversal.hasNext());
    Assertions.assertEquals(3, traversal.next().id);

    Assertions.assertTrue(traversal.hasNext());
    Assertions.assertEquals(4, traversal.next().id);

    Assertions.assertFalse(traversal.hasNext());

    boolean threwError = false;
    try {
      traversal.next();
    } catch (NoSuchElementException e) {
      threwError = true;
    }
    Assertions.assertTrue(threwError);
  }

}