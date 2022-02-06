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

package dev.pietelite.journey.common.config;

import dev.pietelite.journey.common.data.StorageMethod;

/**
 * An enumeration of all {@link Setting}s. No need to register anywhere, that's done dynamically.
 */
public final class Settings {

  public static final Setting<Integer> DEFAULT_SEARCH_TIMEOUT
      = new IntegerSetting("search.default-timeout", 30);

  public static final Setting<Boolean> DEFAULT_NOFLY_FLAG
      = new BooleanSetting("search.default-nofly-flag", false);

  public static final Setting<Boolean> DEFAULT_NODOOR_FLAG
      = new BooleanSetting("search.default-nodoor-flag", false);

  public static final Setting<String> STORAGE_ADDRESS
      = new StringSetting("storage.auth.address", "my.address");

  public static final Setting<String> STORAGE_DATABASE
      = new StringSetting("storage.auth.database", "my_database");

  public static final Setting<String> STORAGE_USERNAME
      = new StringSetting("storage.auth.username", "username");

  public static final Setting<String> STORAGE_PASSWORD
      = new StringSetting("storage.auth.password", "p@ssword");

  public static final Setting<StorageMethod> CUSTOM_ENDPOINT_STORAGE_TYPE
      = new EnumSetting<>("storage.custom_endpoint_type", StorageMethod.SQLITE, StorageMethod.class);

  public static final Setting<StorageMethod> SERVER_ENDPOINT_STORAGE_TYPE
      = new EnumSetting<>("storage.server_endpoint_type", StorageMethod.SQLITE, StorageMethod.class);

  private Settings() {
  }

}
