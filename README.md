# calendarlookup


Calendar Lookup
===============

World Metro is An Android app to convert between historical Chinese
calendar and Julian/Gregorian calendar.  It is currently under
development and is expected to have following features:

* Years supported: CE 1-232.
* Accurate to date.
* Support both traditional/simplified Chinese input/display

For more information about Calendar Lookup, please go to
  <https://github.com/whily/calendarlookup>

Wiki pages can be found at
  <https://wiki.github.com/whily/calendarlookup>

Development
-----------

The following tools are needed to build Calendar Lookup from source:

* JDK version 6/7 from <http://www.java.com> if Java is not available. 
  Note that JDK is preinstalled on Mac OS X and available via package manager
  on many Linux systems. 
* Android SDK r23.0.5 (since CardView is used, make sure to install b
  oth *Android Support Repository* and *Android Support Library*)
* Scala (2.11.0)
* sbt (0.13.5)
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



