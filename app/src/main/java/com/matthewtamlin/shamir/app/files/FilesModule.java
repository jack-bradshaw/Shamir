package com.matthewtamlin.shamir.app.files;

import com.matthewtamlin.shamir.app.AppScope;
import dagger.Module;
import dagger.Provides;
import javafx.stage.Window;

@Module
public class FilesModule {
  @Provides
  @AppScope
  public RxFiles provideRxFiles() {
    return new RxFiles();
  }
  
  @Provides
  @AppScope
  public RxFilePicker provideRxFilePicker(final Window window) {
    return new RxFilePicker(window);
  }
}