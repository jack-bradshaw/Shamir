# Shamir
Shamir's Secret Sharing( in three forms:
- A standard Java API
- A reactive Java API
- A GUI (still in development)

Having at least a cursory understanding of Shamir's Secret Sharing is beneficial before using the APIs. The [Wikipedia entry](https://en.wikipedia.org/wiki/Shamir%27s_Secret_Sharing) is recommended as a good starting point.

## Standard Java API
The standard Java API provides Shamir's Secret Sharing using standard Java patterns.

### Dependency
To add the standard API to your project, add the following to you gradle build file:
```java
repositories {
	jcenter()
}

dependencies {
	implementation 'com.matthew-tamlin:shamir:1.0.1'
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
	implementation 'com.matthew-tamlin:rxshamir:1.0.1'
}
```

Older versions are available in [the Maven repo](https://bintray.com/matthewtamlin/maven/RxShamir).

### Usage
This example demonstrates how to use the reactive API to share and recovery a secret. For brevity, the example will use the definitions for the secret, the creation scheme and the recovery scheme from the previous example.

The sharing/recovery operations are provided by the `RxShamir` class. To instantiate the class:
```java
Shamir shamir = new Shamir(new SecureRandom());
RxShamir rxShamir = RxShamir.from(shamir);
```

To share the secret:
```java
Single<Set<Share>> shares = rxShamir.createShares(secret, creationScheme);
```

To recover the secret:
```java
Single<Set<Share>> threeShares = shares
		.flatMapObservable(Observable::fromIterable)
		.take(3)
		.collectInto(new HashSet<Share>(), Set::add);

Single<BigInteger> recoveredSecret = threeShares.flatMap(
	threeSharesVal -> rxShamir.recoverSecret(threeSharesVal, recoveryScheme));
```

Subscribing to the `recoveredSecret` single yields `973490247382347` which matches the original secret.

### Compatibility with the standard Java API
The standard Java API and the reactive API produce the same results, therefore the APIs can be used interchangably without migration/conversion.

### Compatibility
The reactive API is compatible with Java 1.8 and up.

## GUI
Currently in development.