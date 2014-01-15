package at.ac.tuwien.caa.cvl.imagine.image;

public class ImJniImageProcessing {
	static{
        System.loadLibrary("imageprocessing");
	}
	
	public static native void cartoonize(long srcMat, long dstMat, int clusterCount, float edgeWeight, int edgeThickness);
	
	public static native void edgeEffect(long srcMat, long dstMat, int edgeThickness);
	
	public static native void sepiaEffect(long srcMat, long dstMat);
	
	public static native void loadImage(String path, long dstMat, int width, int height, float rotation);
	
}
