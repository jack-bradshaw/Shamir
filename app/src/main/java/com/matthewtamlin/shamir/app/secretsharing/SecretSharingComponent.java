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

package com.matthewtamlin.shamir.app.secretsharing;

import com.matthewtamlin.shamir.app.AppComponent;
import com.matthewtamlin.shamir.app.secretsharing.creation.CreationModule;
import com.matthewtamlin.shamir.app.secretsharing.encoding.EncodingModule;
import com.matthewtamlin.shamir.app.secretsharing.recovery.RecoveryModule;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.SerialisationModule;
import dagger.Component;
import javafx.scene.Scene;

@Component(
    dependencies = AppComponent.class,
    modules = {
        SecretSharingModule.class,
        CreationModule.class,
        EncodingModule.class,
        RecoveryModule.class,
        SerialisationModule.class})
@SecretSharingScope
public interface SecretSharingComponent {
  public Scene getSecretSharingScene();
  
  public SecretSharingPresenter getSecretSharingPresenter();
  
  @Component.Builder
  public interface Builder {
    public Builder setAppComponent(AppComponent appComponent);
    
    public Builder setCreationModule(CreationModule module);
    
    public Builder setEncodingModule(EncodingModule module);
    
    public Builder setRecoveryModule(RecoveryModule module);
    
    public Builder setSerialisationModule(SerialisationModule module);
    
    public SecretSharingComponent build();
  }
}