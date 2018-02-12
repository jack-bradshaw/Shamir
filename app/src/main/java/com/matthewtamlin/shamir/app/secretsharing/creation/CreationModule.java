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

package com.matthewtamlin.shamir.app.secretsharing.creation;

import com.matthewtamlin.shamir.app.files.RxFilePicker;
import com.matthewtamlin.shamir.app.files.RxFiles;
import com.matthewtamlin.shamir.app.resources.Resources;
import com.matthewtamlin.shamir.app.secretsharing.SecretSharingScope;
import com.matthewtamlin.shamir.app.secretsharing.encoding.SecretEncoder;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.RecoverySchemeSerialiser;
import com.matthewtamlin.shamir.app.secretsharing.serialisation.ShareSerialiser;
import com.matthewtamlin.shamir.reactivejavaapi.crypto.RxShamir;
import dagger.Module;
import dagger.Provides;
import io.reactivex.Scheduler;
import io.reactivex.rxjavafx.schedulers.JavaFxScheduler;
import io.reactivex.schedulers.Schedulers;

import javax.inject.Named;
import java.math.BigInteger;

@Module
public class CreationModule {
  @Provides
  @SecretSharingScope
  public CreationView provideCreationView(final Resources resources, final RxFilePicker rxFilePicker) {
    return new CreationView(resources, rxFilePicker);
  }
  
  @Provides
  @SecretSharingScope
  @Named("creationPresentationScheduler")
  public Scheduler providePresentationScheduler() {
    return Schedulers.newThread();
  }
  
  @Provides
  @SecretSharingScope
  @Named("creationViewScheduler")
  public Scheduler provideViewScheduler() {
    return JavaFxScheduler.platform();
  }
  
  @Provides
  @SecretSharingScope
  public PersistenceOperations providePersistenceOperations(
      final ShareSerialiser shareSerialiser,
      final RecoverySchemeSerialiser recoverySchemeSerialiser,
      final RxFiles rxFiles) {
    
    return new PersistenceOperations(shareSerialiser, recoverySchemeSerialiser, rxFiles);
  }
  
  @Provides
  @SecretSharingScope
  public CryptoConstants provideCryptoConstants() {
    return CryptoConstants
        .builder()
        .setMaxFileSizeBytes(510)
        .setPrime(
            new BigInteger("831426846410154605386922901726808670880783103996883772230399966068386871907041816489060005" +
                "27799288728855880111103377007922967366161011597498072387315104504855641800875825141399655351374242211" +
                "02640655880680115748995410017045951348022716465674400664240682989080942842040331300731868850831823212" +
                "97176247422905505876644242586681485091495201732838184079148765801093563549632370787548898672178085941" +
                "48931910213313072454114172034721028874167039083069192484620215858208438664130153913817224353255594640" +
                "39059904225725188761608908718490445613910044707300816838520494549724473799877085897105174794579281260" +
                "89055308747182507684423837176668262585656715744728730335208351405109459528795218416475284753503568524" +
                "53009173059514706648174101429185314963398066638155251028889786831567575124977146088137897074060865756" +
                "50084719656546172913917915730123417159335007310843071961069197533573160243595281569561571985956991252" +
                "63379118887495039169420827396890031942183480242704937111215690501814534768777771783720131912867854978" +
                "76526331081457059933987997910714583229222312327587635958740461519260782362303706434716587523748907865" +
                "20735904007315131394404101606911181117420995892696727406577923082177709569281057231604329943024228305" +
                "41364818201423822419315834566813"))
        .build();
  }
  
  @Provides
  @SecretSharingScope
  public CreationPresenter provideCreationPresenter(
      final CreationView creationView,
      @Named("creationPresentationScheduler") final Scheduler presentationScheduler,
      @Named("creationViewScheduler") final Scheduler viewScheduler,
      final RxShamir rxShamir,
      final CryptoConstants cryptoConstants,
      final SecretEncoder secretEncoder,
      final PersistenceOperations persistenceOperations,
      final RxFiles rxFiles) {
    
    return new CreationPresenter(
        creationView,
        presentationScheduler,
        viewScheduler,
        rxShamir,
        cryptoConstants,
        secretEncoder,
        persistenceOperations,
        rxFiles);
  }
}