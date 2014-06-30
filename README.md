CHDK-PTP-Java
=============

Pure Java (jsr80 usb) interface to the Cannon cameras running CHDK PTP

Interface reference https://github.com/c10ud/CHDK/blob/master/core/ptp.h

### Example Usage ###

``` java
	    cam = CameraFactory.getCamera(SupportedCamera.SX160IS);
	    cam.connect();
	    cam.setRecordingMode();
	    cam.setManualFocusMode();
	    int i = 0;
	    BufferedImagePanel d = new BufferedImagePanel(cam.getView()); // displays live view
	    Random random = new Random();
	    while (true) {
		d.setImage(cam.getView());
		++i;
		cam.setZoom(i % 100);
		if (i % 40 == 0) {
		    cam.setAutoFocusMode();
		    cam.setZoom(random.nextInt(100));
		    cam.setManualFocusMode();
		}

		if (i % 8 == 0)
			cam.setZoom(random.nextInt(100));
		    cam.setFocus(random.nextInt(1000) + 100);
	    }

```

### Instalation ###
#### Download gradle ####
``` 
cd ~/
wget https://services.gradle.org/distributions/gradle-1.12-bin.zip
unzip gradle-1.12-bin.zip
echo "export PATH=\$PATH:$HOME/gradle-1.12/bin" >> ~/.bashrc
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
The solutionin ubuntu is this. 
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