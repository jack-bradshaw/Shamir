package com.matthewtamlin.shamir.app;

import com.matthewtamlin.shamir.app.files.FilesModule;
import com.matthewtamlin.shamir.app.files.RxFilePicker;
import com.matthewtamlin.shamir.app.files.RxFiles;
import com.matthewtamlin.shamir.app.resources.Resources;
import com.matthewtamlin.shamir.app.resources.ResourcesModule;
import dagger.BindsInstance;
import dagger.Component;
import javafx.stage.Window;

@Component(modules = {
    ResourcesModule.class,
    FilesModule.class})
@AppScope
public interface AppComponent {
  public Resources getResources();
  
  public RxFiles getRxFiles();
  
  public RxFilePicker getRxFilePicker();
  
  @Component.Builder
  public interface Builder {
    @BindsInstance
    public Builder setWindow(Window window);
    
    public Builder setResourcesModule(ResourcesModule module);
    
    public Builder setRxUtilitiesModule(FilesModule module);
    
    public AppComponent build();
  }
}