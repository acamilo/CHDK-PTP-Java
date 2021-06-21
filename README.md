# CHDK-PTP-Java

Pure Java (jsr80 usb) interface to the Canon cameras running CHDK PTP

Interface reference https://github.com/c10ud/CHDK/blob/master/core/ptp.h
(not found anymore, so this may be the same?: <https://github.com/DIYBookScanner/chdk/blob/master/chdk/core/ptp.h>)

## Introduction

Forked from <https://github.com/acamilo/CHDK-PTP-Java> on Wednesday, 18 November 2020.

Values added:

* Migrating it to a Maven project.
* Adding Code formatting plugin
* fixed typos
* Adding Canon A2200 camera (LiveViewDemo working)


### Using in other project ###

This code is has mirrored public repository available under:
https://git.man.poznan.pl/stash/projects/KWE/repos/chdk-ptp-java/browse

#### Artifacts and maven repository ####

The codes found there are automatically tested and built as artifacts using Jenkins CI server and uploaded to maven repository:
https://maven.man.poznan.pl/repository/webapp/browserepo.html?pathId=kiwi-libs-snapshots:org/chdk/ptp/java/CHDK-PTP-Java

Which can be later used in gradle project by simply defining the repository maven address:

```
repositories
{
    maven() { url 'https://maven.man.poznan.pl/repository/kiwi-repo' } // virtual repo
}
```

and adding appropriate entry in build.gradle or relevant in pom.xml

```
dependencies
{
	compile 'org.chdk.ptp.java:CHDK-PTP-Java:+'
}	
```

#### Versioning ####

For the sake of usability, rapid development and usage sanity we use semantic versioning: http://semver.org/

### Example Usage ###

Try to follow chdk.ptp.java.standalone.LiveViewApiDemo.java but part of the example is below:

``` java
	    cam = CameraFactory.getCamera(SupportedCamera.SX160IS);
	    cam.connect();
	    cam.setOperaionMode(CameraMode.RECORD);
	    int i = 0;
	    BufferedImagePanel d = new BufferedImagePanel(cam.getView()); // displays live view
	    Random random = new Random();
	    while (true) {
		d.setImage(cam.getView());
		++i;
		cam.setZoom(i % 100);
		if (i % 40 == 0) {
		    cam.setZoom(random.nextInt(100));
		}

		if (i % 8 == 0) {
			cam.setZoom(random.nextInt(100));
			cam.setFocus(random.nextInt(1000) + 100);
		}
		cam.setFocusMode(FocusMode.AUTO);
	    }

```

### Installation ###

#### Download gradle ####

``` 
cd ~/
wget https://services.gradle.org/distributions/gradle-2.2-bin.zip
unzip gradle-2.2-bin.zip
echo "export PATH=\$PATH:$HOME/gradle-2.2/bin" >> ~/.bashrc
source ~/.bashrc
```

#### Set-up gradle ####

Artifactory plugin requires a file

```
$HOME\.gradle
```

to be created and filled with the following two lines:

```
USER=wont_be_used
PASSWORD=but_needs_to_be_here
```

#### Eclipse integration ####

Spring offers Gradle integration which can be acquired from:
https://github.com/spring-projects/eclipse-integration-gradle

#### Check out project ####

Use console git:

```
mkdir ~/git
cd ~/git
git clone https://github.com/acamilo/CHDK-PTP-Java.git
```

or Eclipse plugin.

#### Build ####

```
cd CHDK-PTP-Java
gradle build
```

#### Open in eclipse ####

Import Gradle Project from within Eclipse.

#### other problems ####

I.E Afternoon wasters...

on some OSes gvfs will grab the PTP device and you'll get this error.

```
javax.usb.UsbPlatformException: USB error 6: Unable to claim interface: Resource busy
```

The solution in ubuntu is this. 

```
gsettings set org.gnome.desktop.media-handling automount false
```

To un do this change false to true. 

Sometimes, cam.setRecordingMode() will fail but the camera will otherise be responsive.

see thread:
http://chdk.setepontos.com/index.php?topic=10664.10

solition is to either kill gvfs-gphoto2-volume-monitor or to make it perminent, change it's name so it doesn't start.

### Other OSes ##

This uses libusb4java so it probably works in windows and mac. I've never tried it though.

### USB IDs

<http://www.linux-usb.org/usb.ids>

04a9  Canon, Inc. - 322a  PowerShot A2200