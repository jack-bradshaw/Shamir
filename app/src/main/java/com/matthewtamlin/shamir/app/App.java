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