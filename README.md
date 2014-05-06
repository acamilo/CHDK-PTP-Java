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
todo

* install gradle
* Check out project
* Compile
* Open in eclipse
