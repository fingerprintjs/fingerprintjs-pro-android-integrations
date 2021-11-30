<p align="center">
  <a href="https://fingerprintjs.com">
    <img src="https://user-images.githubusercontent.com/10922372/129346814-a4e95dbf-cd27-49aa-ae7c-f23dae63b792.png" alt="FingerprintJS" width="312px" />
  </a>
</p>
<p align="center">
  <a href="https://discord.gg/39EpE2neBg">
    <img src="https://img.shields.io/discord/852099967190433792?style=logo&label=Discord&logo=Discord&logoColor=white" alt="Discord server">
  </a>
</p>

# FingerprintJS Pro Android

An example app and packages demonstrating [FingerprintJS Pro](https://fingerprintjs.com/) capabilities on the Android platform. The repository illustrates how to retrieve a FingerprintJS Pro visitor identifier in a native mobile app. These integrations communicate with the FingerprintJS Pro API and require [browser token](https://dev.fingerprintjs.com/docs). For client-side only Android fingerprinting take a look at [fingerprint-android](https://github.com/fingerprintjs/fingerprint-android) repository instead. If you are interested in the iOS platform, you can also check our [FingerprintJS Pro iOS integrations](https://github.com/fingerprintjs/fingerprintjs-pro-ios-webview).

There are two typical use cases:
- Using our native library to retrieve a FingerprintJS Pro visitor identifier in the native code OR
- Retrieving visitor identifier using our native library in combination with signals from the FingerprintJS Pro browser agent in the webview on the JavaScript level.

## Installation

For both scenarios, you have to add the library to your project first. This is a mandatory prerequisite before continuing with the external library or webview scenario.

#### 1. Add the repository to the gradle.

If your version of Gradle is earlier than 7, add these lines to your `build.gradle`.


```gradle
allprojects {	
  repositories {
  ...
  maven { url 'https://jitpack.io' }	
}}
```

If your version of Gradle is 7 or newer, add these lines to your `settings.gradle`.
```gradle
repositories {
  ...
  maven { url "https://jitpack.io" }
}
```
#### 2. Add a dependency to your `build.gradle` file

```gradle
dependencies {
  implementation 'com.github.fingerprintjs:fingerprintjs-pro-android:v1.2.0-rc2'

  // If you use Java for you project, add also this line
  implementation "org.jetbrains.kotlin:kotlin-stdlib:$kotlin_version"
```

When using Kotlin, also make sure you have specified Kotlin version in your `build.gradle` file:
```gradle
buildscript {
    ext.kotlin_version = 'your-kotlin-version'
    ...
```
*Note: You can find your Kotlin version in Android Studio > File > Settings > Languages & Frameworks > Kotlin.*

Sync gradle settings.


## Using the external library to retrieve a FingerprintJS Pro visitor identifier
This integration approach uses the external library [fingerprint-android](https://github.com/fingerprintjs/fingerprint-android). It collects various signals from the Android system, sends them to the FingerprintJS Pro API for processing, and retrieves an accurate visitor identifier.

*Note: The library depends on [kotlin-stdlib](https://kotlinlang.org/api/latest/jvm/stdlib/). If your application is written in Java, add `kotlin-stdlib` dependency first (it's lightweight and has excellent backward and forward compatibility).*

#### 3. Get the visitor identifier

Retrieve the visitor identifier using browser token. You can find your [browser token](https://dev.fingerprintjs.com/docs) in your [dashboard](https://dashboard.fingerprintjs.com/subscriptions/).


##### 3.1 Kotlin example

```kotlin
import com.fingerprintjs.android.fpjs_pro.Configuration
import com.fingerprintjs.android.fpjs_pro.FPJSProFactory
...

// Initialization
val factory = FPJSProFactory(applicationContext)
val configuration = Configuration(
    apiToken = "BROWSER_TOKEN"
  )
 
val fpjsClient = factory.createInstance(
    configuration
)

// Usage
fpjsClient.getVisitorId { visitorId ->
    // Use visitorId
}
```


##### 3.2 Java example

```java
import com.fingerprintjs.android.fpjs_pro.Configuration;
import com.fingerprintjs.android.fpjs_pro.FPJSProClient;
import com.fingerprintjs.android.fpjs_pro.FPJSProFactory;
...

FPJSProFactory factory = new FPJSProFactory(this.getApplicationContext());
Configuration configuration = new Configuration(
    "BROWSER_TOKEN"
    ); 

FPJSProClient fpjsClient = factory.createInstance(
    configuration
);

fpjsClient.getVisitorId(new Function1<String, Unit>() {
    @Override
    public Unit invoke(String visitorId) {
        // Use visitorId
        return null;
    }
});
```

*❗ Important: Due to WebView limitations the initialization of the client is performed on the UI-thread, consider call `getVisitorId()` while the screen is static.*

## Full public API

Full public API is following

```kotlin

interface FPJSProClient {
    fun getVisitorId(listener: (String) -> Unit)
    fun getVisitorId(listener: (String) -> Unit, errorListener: (String) -> (Unit))
    fun getVisitorId(tags: Map<String, Any>, listener: (String) -> Unit, errorListener: (String) -> (Unit))
}

```

### Error handling

```kotlin
fpjsClient.getVisitorId(
          listener = { visitorId ->
            // Handle ID
          },
          errorListener = { error ->
            // Handle error
          })
```

### [Tags](https://dev.fingerprintjs.com/v2/docs/js-agent#tag) support

```kotlin
 fpjsClient.getVisitorId(
      tags = mapOf("sessionId" to sessionId),
      listener = { visitorId ->
          // Handle ID
      },
      errorListener = { error ->
          // Handle error
      })
```



## Using inside a webview with JavaScript

This approach uses signals from [FingerprintJS Pro browser agent](https://dev.fingerprintjs.com/docs/quick-start-guide#js-agent) together with signals provided by [fingerprint-android](https://github.com/fingerprintjs/fingerprint-android). The identifier collected by [fingerprint-android](https://github.com/fingerprintjs/fingerprint-android) is added to the [`tag` field](https://dev.fingerprintjs.com/docs#tagging-your-requests) in the given format. FingerprintJS Pro browser agent adds an additional set of signals and sends them to the FingerprintJS Pro API. Eventually, the API returns an accurate visitor identifier.

#### 4. Add a JavaScript interface to your webview

##### 4.1 Kotlin example

```kotlin
import com.fingerprintjs.android.fpjs_pro.Configuration
import com.fingerprintjs.android.fpjs_pro.FPJSProFactory
...

val myWebView: WebView = findViewById(R.id.webview)

// Init interface
val factory = FPJSProFactory(myWebView.context.applicationContext)
val configuration = Configuration(
    apiToken = "BROWSER_TOKEN",
    region = Configuration.Region.US, // optional
    endpointUrl = "https://endpoint.url" // optional
)
val fpjsInterface = factory.createInterface(configuration)

// Add interface to the webview
myWebView.addJavascriptInterface(
    fpjsInterface,
    "fpjs-pro-android"
)

// Use embedded webview in the app instead of the default new app
myWebView.setWebViewClient(WebViewClient())

// Enable javascript inside the webview
val webSettings: WebSettings = myWebView.getSettings()
webSettings.javaScriptEnabled = true

// Load url with the injected and configured FingerprintJS Pro agent
myWebView.loadUrl("https://site-with-injected-agent.com")
```

##### 4.2 Java example
```java
import com.fingerprintjs.android.fpjs_pro.Configuration;
import com.fingerprintjs.android.fpjs_pro.FPJSProFactory;
import com.fingerprintjs.android.fpjs_pro.FPJSProInterface;
...

WebView myWebView = findViewById(R.id.webview);

// Init interface
FPJSProFactory factory = new FPJSProFactory(this.getApplicationContext());
Configuration configuration = new Configuration(
  "BROWSER_TOKEN"
  );

FPJSProInterface fpjsInterface = factory.createInterface(configuration);

// Add interface to the webview
myWebView.addJavascriptInterface(
    fpjsInterface,
    "fpjs-pro-android"
    );

// Use embedded webview in the app instead of the default new app
myWebView.setWebViewClient(new WebViewClient());

// Enable javascript inside the webview
WebSettings webSettings = myWebView.getSettings();
webSettings.setJavaScriptEnabled(true);

// Load url with the injected and configured FingerprintJS Pro agent
myWebView.loadUrl("https://site-with-injected-agent.com");
```

### 4. Setup the JavaScript FingerprintJS Pro integration in your webview

```js
function initFingerprintJS() {
  // Initialize an agent at application startup
  const fpPromise = FingerprintJS.load({
    token: 'your-browser-token',
    endpoint: 'your-endpoint', // optional
    region: 'your-region' // optional
  });
  
  var androidDeviceId = window['fpjs-pro-android'].getDeviceId();

  // Get the visitor identifier when you need it
  fpPromise
    .then(fp => fp.get({
     environment: {
      deviceId: androidDeviceId,
      type: 'android',
     }
    }))
    .then(result => console.log(result.visitorId));
}
```
You can find your [browser token](https://dev.fingerprintjs.com/docs) in your [dashboard](https://dashboard.fingerprintjs.com/subscriptions/).

## Additional Resources
[FingerprintJS Pro documentation](https://dev.fingerprintjs.com/docs)

## License
This library is MIT licensed.
