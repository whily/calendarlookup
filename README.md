Calendar Lookup
===============

Calendar Lookup is an Android app to convert between historical Chinese
calendar and Julian/Gregorian calendar.

Android app can be downloaded from Goolge Play:
  <https://play.google.com/store/apps/details?id=net.whily.android.calendarlookup>

Features of the app can be found in the above link.

Latest version of APK can be also downloaded from:
  <https://github.com/whily/calendarlookup/releases/download/v0.2.1/calendarlookup-v0.2.1.apk>

For more information about Calendar Lookup, please go to
  <https://github.com/whily/calendarlookup>

Wiki pages can be found at
  <https://wiki.github.com/whily/calendarlookup>

Development
-----------

The following tools are needed to build Calendar Lookup from source:

* JDK version 8 from <http://www.java.com> if Java is not available.
  Note that JDK is preinstalled on Mac OS X and available via package manager
  on many Linux systems.
* Android SDK r25.2.5.
* Scala (2.11.11)
* sbt (0.13.16)
* [Inkscape](http://inkscape.org) and [ImageMagick](http://www.imagemagick.org)
  to generate icons.

### Generate the icons

In project directory, run following command:

        $ ./genart

### Build the code

The library dependencies include
[scasci](https://github.com/whily/scasci),
[scaland](https://github.com/whily/scaland), and
[chinesecalendar](https://github.com/whily/chinesecalendar).  Please
follow the steps discussed in those libraries on how to use them.

To compile/run the code, run the following command to build the
   app and start it in a connected device:

        $ sbt android:run

To build a release version and start it in a connected device:

        $ sbt android:set-release android:run
