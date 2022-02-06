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

import dev.pietelite.journey.common.JourneyCommon;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Paths;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * A utility class to handle methods involving serialization.
 */
public final class Serialize {

  private Serialize() {
  }

  /**
   * Serialize a cacheable object. If serialization fails, replace the original object in memory
   * with a new one with the supplied "new getter" so that the object in memory reflects the
   * cached state for continuity. To not replace it, have the setter do nothing in its consumption.
   *
   * @param dataFolder  the data folder in which to serialize
   * @param fileName    the file name of the serialized data
   * @param getter      the getter for the object to serialize
   * @param setter      the setter for the object that we want serialized.
   *                    In the case of failure, this will be used to replace the original.
   * @param constructor the new getter
   * @param <T>         the type of data we are serializing
   */
  public static <T extends Serializable> void serializeCache(File dataFolder,
                                                             String fileName,
                                                             Supplier<T> getter,
                                                             Consumer<T> setter,
                                                             Supplier<T> constructor) {
    File file = Paths.get(dataFolder.toPath().toString(), fileName).toFile();
    try {
      //noinspection ResultOfMethodCallIgnored
      dataFolder.mkdirs();
      if (file.createNewFile()) {
        JourneyCommon.getLogger().info("Created serialized file: " + fileName);
      }
    } catch (IOException e) {
      JourneyCommon.getLogger().error("Could not create serialized file: " + fileName);
      setter.accept(constructor.get());
      return;
    }

    try (FileOutputStream fileStream = new FileOutputStream(Paths.get(
        dataFolder.toPath().toString(),
        fileName).toFile());
         ObjectOutputStream out = new ObjectOutputStream(fileStream)) {

      out.writeObject(getter.get());

    } catch (IOException e) {
      JourneyCommon.getLogger().error("Could not serialize " + fileName);
      setter.accept(constructor.get());
      e.printStackTrace();
    }
  }

  /**
   * Deserialize a cacheable object. If deserialization fails, use the constructor.
   *
   * @param dataFolder  the folder in which to store the data
   * @param fileName    the name of the file in which the serialized data is stored
   * @param setter      the setter of the object in memory
   * @param constructor a constructor for the object, in case deserialization fails
   * @param <T>         the type of serializable object
   */
  @SuppressWarnings("unchecked")
  public static <T extends Serializable> void deserializeCache(File dataFolder,
                                                               String fileName,
                                                               Consumer<T> setter,
                                                               Supplier<T> constructor) {
    File file = Paths.get(dataFolder.toPath().toString(), fileName).toFile();
    if (!file.exists()) {
      setter.accept(constructor.get());
      return;
    }
    try (FileInputStream fileStream = new FileInputStream(file);
         ObjectInputStream in = new ObjectInputStream(fileStream)) {

      Object read = in.readObject();
      if (read == null) {
        setter.accept(constructor.get());
      } else {
        setter.accept((T) read);
      }

    } catch (IOException | ClassNotFoundException e) {
      JourneyCommon.getLogger().error("Could not deserialize " + fileName);
      setter.accept(constructor.get());
    }
  }

}
