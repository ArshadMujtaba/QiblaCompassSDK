# **Qibla Compass SDK**

A standalone Jetpack Compose library to easily add Qibla direction features to any Android app.

## **Features**

* 🧭 Accurate Qibla calculation based on device location.  
* 🎨 Fully customizable UI with Jetpack Compose.  
* 📱 Sensor-based smooth compass animation.  
* 🔌 Plug and Play implementation.

## **Get Started**
**Step 1\. Add the JitPack repository to your build file**
Add it in your root settings.gradle.kts (or settings.gradle):
```
dependencyResolutionManagement {  
    repositories {  
        google()  
        mavenCentral()  
        maven { url = uri("https://jitpack.io") }  
    }  
}
```

**Step 2\. Add the dependency**
Add it in your app module's build.gradle.kts:
```
dependencies {  
    implementation("com.github.ArshadMujtaba:QiblaCompassSDK:1.0.0")  
}
```

## **Usage**
Simply call the QiblaScreen composable in your activity or any screen:
```
setContent {  
    QiblaScreen()  
}
```

## **Credits & Acknowledgements 🙏**
This SDK contains logic and code adapted from the open-source project [**hj-qibla-compass**](https://github.com/hassaanjamil/hj-qibla-compass) by **Hassaan Jamil**.
We are grateful for their contribution to the open-source community.

## **License**
MIT License 

Copyright (c) 2026 \[Arshad Mujtaba/AMK APPS HUB\]

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
