package test;

public class JniTest {	
	public void brightnessContrastNative(int contrast, int brightness) {
		this.brightnessContrast(contrast, brightness);
	}
	
	public void hello() {
		helloJNI();
	}
	
	private native void brightnessContrast(int contrast, int brightness);
	
	private native void helloJNI();
	
	static{
        System.loadLibrary("imageprocessing");
	}
}
