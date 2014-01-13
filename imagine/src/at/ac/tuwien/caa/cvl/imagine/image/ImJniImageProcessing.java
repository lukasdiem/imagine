package at.ac.tuwien.caa.cvl.imagine.image;

public class ImJniImageProcessing {
	static{
        System.loadLibrary("imageprocessing");
	}
	
	//public static native void brightnessContrast(long srcMat, long dstMat, float contrast, float brightness);
	public static native void cartoonize(long srcMat, long dstMat, int clusterCount, float edgeWeight, int edgeThickness);
	
	//private native void helloJNI();
	
	/*public void brightnessContrastNative(long srcMat, long dstMat, float contrast, float brightness) {
		ImJniImageProcessing.brightnessContrast(srcMat, dstMat, contrast, brightness);
	}
	
	public void hello() {
		helloJNI();
	}*/
}
