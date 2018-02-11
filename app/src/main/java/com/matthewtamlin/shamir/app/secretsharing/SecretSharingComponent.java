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