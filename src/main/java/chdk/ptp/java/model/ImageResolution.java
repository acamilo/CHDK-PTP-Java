package chdk.ptp.java.model;

/**
 * Image resolution
 * 
 * @author Daniel Finger Tavares
 *
 */
public enum ImageResolution {
	L(0),M1(1),M2(2),S(4);
	
	int value;
	
	ImageResolution(int value){
		this.value = value;
	}
	
	public int getValue(){
		return value;
	}
	
	public static ImageResolution valueOf(int resolution) {
		for(ImageResolution i: values()){
			if(i.value == resolution){
				return i;
			}
		}
		return null;
	}
}
