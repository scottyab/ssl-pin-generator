SSL Pin Generator
=================

Is a simple Java base util to generate SSL pins based on a certificate's Subject Public Key Info as described on <a href="http://goo.gl/AIx3e5">Adam Langley's Weblog</a> (a.k.a Public Key pinning). Pins are base-64 SHA-1 [default] hashes, consistent with the format Chromium uses for <a href="http://goo.gl/XDh6je">static certificates</a>. See Chromium's <a href="http://goo.gl/4CCnGs">pinsets</a> for hostnames that are pinned in that
browser.
 
I created this mainly to be compatible with [okhttp](https://square.github.io/okhttp/) 2.1+, but later added the option to specific which hashing algorithm can be used to make this compatible with Android's `<network-security-config>`


## Usage

*Warning you should ensure you run this on a trusted network*

Either fork the project and call the `SSLPinGenerator.java` class directly or Download the latest release jar [here](https://github.com/scottyab/ssl-pin-generator/releases/).

Simply pass to hostname with optionally port, and algorithm to the jar. `$ java -jar generatePins.jar <your hostname:port"> algorithm`

### Default

i.e `$ java -jar generatePins.jar publicobject.com`

or 

`$ java -jar generatePins.jar scottyab.com sha-256`

Output:

```
Generating SSL pins for: publicobject.com
sha1/DmxUShsZuNiqPQsX2Oi9uv2sCnw=
sha1/SXxoaOSEzPC6BgGmxAt/EAcsajw=
sha1/blhOM3W9V/bVQhsWAcLYwPU6n24=
sha1/T5x9IXmcrQ7YuQxXnxoCmeeQ84c=
```

Then if you are using okhttp add them to the `com.squareup.okhttp.CertificatePinner` like this (from the [okhttp java docs](https://github.com/square/okhttp/blob/92bf318a70a9e2194e626ff2c2f4266b0bbb09e5/okhttp/src/main/java/com/squareup/okhttp/CertificatePinner.java#L160))

```java
CertificatePinner certificatePinner = new CertificatePinner.Builder()
        .add("publicobject.com", "sha1/DmxUShsZuNiqPQsX2Oi9uv2sCnw=")
        .add("publicobject.com", "sha1/SXxoaOSEzPC6BgGmxAt/EAcsajw=")
        .add("publicobject.com", "sha1/blhOM3W9V/bVQhsWAcLYwPU6n24=")
        .add("publicobject.com", "sha1/T5x9IXmcrQ7YuQxXnxoCmeeQ84c=")
        .build();
```

### Custom Hash,

In this exmaple we use SHA-256 to be compatible with Android's `<network-security-config>`

`$ java -jar generatePins.jar publicobject.com sha-256 debug`

```
Generating SSL pins for: publicobject.com
subject :  CN=publicobject.com, OU=PositiveSSL, OU=Domain Control Validated
sha-256/afwiKY3RxoMmLkuRW1l7QsPZTJPwDS2pdDROQjXw8ig=
subject :  CN=COMODO RSA Domain Validation Secure Server CA, O=COMODO CA Limited, L=Salford, ST=Greater Manchester, C=GB
sha-256/klO23nT2ehFDXCfx3eHTDRESMz3asj1muO+4aIdjiuY=
subject :  CN=COMODO RSA Certification Authority, O=COMODO CA Limited, L=Salford, ST=Greater Manchester, C=GB
sha-256/grX4Ta9HpZx6tSHkmCrvpApTQGo67CYDnvprLg5yRME=
subject :  CN=AddTrust External CA Root, OU=AddTrust External TTP Network, O=AddTrust AB, C=SE
sha-256/lCppFqbkrlJ3EcVFAkeip0+44VaoJUymbnOaEUk7tEU=
```

This also shows the debug option to print out subject name to help identifiy which pin belongs to which cert in the chain.


## Further reading

*Shameless plug alert!*

I wrote about SSL pinning and several other interesting things you can do to make your apps more secure in the [Android Security Cookbook](http://www.amazon.co.uk/Android-Security-Cookbook-Keith-Makan/dp/1782167161)

## License 
The MIT License

Copyright (c) 2014 Scott Alexander-Bown http://scottyab.com
