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