/*
 * Copyright 2018 Matthew Tamlin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.matthewtamlin.shamir.app.files;

import io.reactivex.Single;
import javafx.stage.DirectoryChooser;
import javafx.stage.FileChooser;
import javafx.stage.Window;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.matthewtamlin.java_utilities.checkers.NullChecker.checkNotNull;

public class RxFilePicker {
  private final Window window;
  
  public RxFilePicker(@Nonnull final Window window) {
    this.window = checkNotNull(window, "\'window\' must not be null.");
  }
  
  @Nonnull
  public Single<Optional<File>> pickFile(@Nullable final String title) {
    return Single.fromCallable(() -> {
      final FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle(title);
      
      return Optional.ofNullable(fileChooser.showOpenDialog(window));
    });
  }
  
  @Nonnull
  public Single<Optional<Set<File>>> pickFiles(@Nullable final String title) {
    return Single.fromCallable(() -> {
      final FileChooser fileChooser = new FileChooser();
      fileChooser.setTitle(title);
      
      final List<File> files = fileChooser.showOpenMultipleDialog(window);
      
      return files == null ? Optional.empty() : Optional.of(new HashSet<>(files));
    });
  }
  
  @Nonnull
  public Single<Optional<File>> pickDirectory(@Nullable final String title) {
    return Single.fromCallable(() -> {
      final DirectoryChooser fileChooser = new DirectoryChooser();
      fileChooser.setTitle(title);
      
      return Optional.ofNullable(fileChooser.showDialog(window));
    });
  }
}