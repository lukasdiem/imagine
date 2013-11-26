package at.ac.tuwien.caa.cvl.imagine.image;

public interface OnImageChangedListener {
	/**
	 * This listener is called, if the underlying image matrix is somehow modified.
	 * Mostly some image manipulation function is called.
	 */
	public void onImageManipulated();
	
	/**
	 * This listener is called if a new image is loaded. 
	 */
	public void onLoadingNewImage();
	
	/**
	 * This listener is called if the image loading task is completed 
	 */
	public void onNewImageLoaded();
}
