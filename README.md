# Shamir
Shamir's Secret Sharing in three forms:
- A GUI app
- A standard Java API
- A reactive Java API

All forms use finite field arithmetic to prevent geometric attacks.

Having at least a cursory understanding of Shamir's Secret Sharing is beneficial before using the APIs. The [Wikipedia entry](https://en.wikipedia.org/wiki/Shamir%27s_Secret_Sharing) is recommended as a good starting point.

## GUI app
The GUI app provides a simple way to use Shamir's Secret Sharing.

<img src="https://i.imgur.com/qt8NAGU.png" width="500">

### Download
Native packages are available for MacOS and Windows, and a JAR is available for all other cases.

Latest releases:
- [MacOS](https://github.com/MatthewTamlin/Shamir/releases/tag/mac-os-app-v1.0.1).
- [Windows](https://github.com/MatthewTamlin/Shamir/releases/tag/windows-app-v1.0.1).
- [JAR](https://github.com/MatthewTamlin/Shamir/releases/tag/generic-app-v1.0.0)

Each releases is PGP signed for security, and the signer's public key can be found [here](https://pgp.mit.edu/pks/lookup?op=vindex&search=0xA1A2F30F3885AE92).

### Building from source
You can also build the releases from the source for added security.

Start by getting a copy of the master branch. You can download it from [Github](https://github.com/MatthewTamlin/Shamir/tree/master) or clone it by running:
```shell
git clone -b develop https://github.com/MatthewTamlin/Shamir
```

The native releases for MacOS and Windows come bundled with a minified JRE, however for technical and legal reasons the JREs aren't included in this repository. To install the JREs:
- Download the MacOS and Windows TAR releases from [here](http://www.oracle.com/technetwork/java/javase/downloads/jre9-downloads-3848532.html)
- Unzip the MacOS JRE and copy the contents of the unzipped `Home` directory to `/app/deployment/JREs/macos`.
- Unzip the windows JRE to `/app/deployment/JREs/windows`.

Next open the command line in the repository's root directory and run the following commands:
```shell
# On Unix style command lines
chmod +x gradlew
./gradlew cleanAllModules buildAllModules :app:buildAllReleases

# On Windows
gradlew.bat cleanAllModules buildAllModules :app:buildAllReleases
```

If you just want to build a specific release, replace `:app:buildAllReleases` with `:app:buildMacOsRelease` or `:app:buildWindowsRelease`.

The subdirectories of `/app/build/` contain the release artifacts.

### Limitations
A 4096 bit prime is used as the basis of the finite field, therefore the GUI can only be used to share files which are at most 510 bytes long (two bytes are reserved for safety and encoding). To share larger files, first use a symmetric encryption protocol to encrypt the payload, and then use the GUI app to convert the key into shares. If you use a well-known protocol such as AES, then it should be safe to distribute the encrypted payload to each shareholder.

### Future work
The next steps for the GUI app are:
- Randomise the prime per installation.
- Provide better descriptions when files are selected.

## Standard Java API
The standard Java API provides Shamir's Secret Sharing using standard Java patterns.

### Dependency
To add the standard API to your project, add the following to you gradle build file:
```java
repositories {
	jcenter()
}

dependencies {
	implementation 'com.matthew-tamlin:shamir:2.0.1'
}
```

Older versions are available in [the Maven repo](https://bintray.com/matthewtamlin/maven/Shamir).

### Usage
This example demonstrates how to use the standard API to share and recover a secret. The example uses a K=3 N=5 scheme, meaning that 5 shares are created in total and a minimum of 3 are needed to reconstruct the secret.

For the example let the secret be `973490247382347`. 

The sharing/recovery operations are provided by the `Shamir` class. To instantiate the class:
```java
Shamir shamir = new Shamir(new SecureRandom());
```

Alternatively:
```java
Shamir shamir = Shamir.create(new SecureRandom());
```

To share the secret:
```java
BigInteger secret = new BigInteger("973490247382347");

// The prime must be greater than the secret and the total number of shares
BigInteger prime = new BigInteger("2305843009213693951");

CreationScheme creationScheme = CreationScheme
		.builder()
		.setRequiredShareCount(3)
		.setTotalShareCount(5)
		.setPrime(prime)
		.build();

Set<Share> shares = shamir.createShares(secret, creationScheme);
```

Each share contains an index and a value. The example yields the following shares:
- index = 1, value = 1007431061686543935
- index = 2, value = 1805108382619357109
- index = 3, value = 88162443832127918
- index = 4, value = 468279263752244264
- index = 5, value = 639615833166012196

To recover the secret:
```
// Must contain the same values as the creation scheme (excluding the total share count)
RecoveryScheme recoveryScheme = RecoveryScheme
		.builder()
		.setRequiredShareCount(3)
		.setPrime(prime)
		.build();

// For brevity, assume there's some method that takes the first three shares
Set<Share> threeShares = takeFirstThree(shares);

BigInteger recoveredSecret = shamir.recoverSecret(threeShares, recoveryScheme);
```

The example yields a recovered secret of `973490247382347` which matches the original secret.

### Compatibility
The standard API is compatible with Java 1.8 and up.

## Reactive Java API
The reactive API is functionally the same as the standard API, but the interface has been changed to use reactive types from [RxJava](https://github.com/ReactiveX/RxJava).

### Dependency
To add the reactive API to your project, add the following to you gradle build file:
```java
repositories {
	jcenter()
}

dependencies {
	implementation 'com.matthew-tamlin:rxshamir:2.0.1'
}
```

Older versions are available in [the Maven repo](https://bintray.com/matthewtamlin/maven/RxShamir).

### Usage
This example demonstrates how to use the reactive API to share and recover a secret. For brevity, the example will use the definitions for the secret, the prime, the creation scheme and the recovery scheme from the previous example.

The sharing/recovery operations are provided by the `RxShamir` class. To instantiate the class:
```java
RxShamir shamir = new RxShamir(new SecureRandom());
```

Alternatively:
```java
RxShamir shamir = RxShamir.create(new SecureRandom());
```

To share the secret:
```java
Observable<Share> shares = rxShamir.createShares(secret, creationScheme);
```

Each share contains an index and a value. The example yields an observable which emits the following shares:
- index = 1, value = 1007431061686543935
- index = 2, value = 1805108382619357109
- index = 3, value = 88162443832127918
- index = 4, value = 468279263752244264
- index = 5, value = 639615833166012196

To recover the secret:
```java
Single<BigInteger> recoveredSecret = shares
		.take(3)
		.collectInto(new HashSet<Share>(), Set::add)
		.flatMap(threeShares -> rxShamir.recoverSecret(threeShares, recoveryScheme));
```

The example yields a single that emits `973490247382347`. Thus the recovered secret is equal to the original secret.

### Compatibility
The reactive API is compatible with Java 1.8 and up.

## Compatibility between APIs
The standard API and the reactive API produce the same results given the same inputs, therefore the APIs can be used interchangably without migration/conversion.

## Licensing
The GUI app and the APIs are licensed under the Apache v2.0 licence. Have a look at [the license](LICENSE) for details.