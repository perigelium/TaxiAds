# inrideads

## Development

The `inrideads` Android application is a standard Android Java application built with the Android Studio tools.

### Development Environment ###

Android Studio and the integrated gradle build system handles all package management for the project so there are no additional steps required to build the application. Run `Build -> Make Project` to execute a complete build with Android Studio.

Requirements:
- Android Studio 3.0.1

## Deploy

Release builds are created via the Android Studio Tools (`Build -> Generate Sign APKs`). Signing configuration details can be find in `app/build.gradle`

## Third Party Libraries

The following third-party packages/SDKs are included in the application build:

- io.fabric.tools:gradle v1.25.1
- com.squareup.okhttp3:okhttp v3.8.0
- com.squareup.retrofit2:retrofit v2.3.0
- com.squareup.retrofit2:converter-gson v2.3.0
- com.squareup.picasso:picasso v2.5.2
- com.google.code.gson:gson v2.8.2
- com.android.support:design v27.0.2
- io.reactivex:rxjava v1.1.6
- io.reactivex:rxandroid v1.2.1
- com.google.android.gms:play-services-vision v11.6.0
- com.google.android.gms:play-services-location v11.6.0
- com.google.android.gms:play-services-maps v11.6.0
- com.github.kenglxn.QRGen:android v2.3.0
- com.aol.one.publishers.android:sdk v2.17
- com.crashlytics.sdk.android:crashlytics v2.8.0

## Misc
