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

#### Check out project ####
```
mkdir ~/git
cd ~/git
git cone https://github.com/acamilo/CHDK-PTP-Java.git
```
#### Compile ####
```
cd CHDK-PTP-Java
gradle build
gradle eclipse
```

#### Open in eclipse ####
import CHDK-PTP-Java as an eclipse project

#### other problems ####
on some OSes gvfs will grab the PTP device and you'll get this error.
'''
javax.usb.UsbPlatformException: USB error 6: Unable to claim interface: Resource busy
'''
The solutionin ubuntu is this. 
```
gsettings set org.gnome.desktop.media-handling automount false
```

To un do this change false to true. 
