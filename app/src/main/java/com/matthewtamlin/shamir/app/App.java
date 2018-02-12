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

import com.matthewtamlin.shamir.app.secretsharing.DaggerSecretSharingComponent;
import com.matthewtamlin.shamir.app.secretsharing.SecretSharingComponent;
import javafx.application.Application;
import javafx.stage.Stage;

public class App extends Application {
  private AppComponent appComponent;
  
  private SecretSharingComponent secretSharingComponent;
  
  @Override
  public void start(final Stage primaryStage) {
    appComponent = DaggerAppComponent
        .builder()
        .setWindow(primaryStage)
        .build();
    
    secretSharingComponent = DaggerSecretSharingComponent
        .builder()
        .setAppComponent(appComponent)
        .build();
    
    primaryStage.setTitle(appComponent.getResources().getString("appName").blockingGet());
    primaryStage.setResizable(false);
    primaryStage.setScene(secretSharingComponent.getSecretSharingScene());
    primaryStage.show();
    
    secretSharingComponent.getSecretSharingPresenter().startPresenting().blockingGet();
  }
  
  @Override
  public void stop() throws Exception {
    super.stop();
    
    secretSharingComponent.getSecretSharingPresenter().stopPresenting().blockingGet();
  }
  
  public static void main(final String[] args) {
    launch(args);
  }
}