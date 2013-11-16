package at.ac.tuwien.caa.cvl.imagine.utils;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

public class FileUtils {
	public static String getRealPathFromURI(Context ctx, Uri contentURI) {
		Cursor cursor = ctx.getContentResolver().query(contentURI, null, null, null, null);
		
		if (cursor == null) { // Source is Dropbox or other similar local file path
			return contentURI.getPath();
		} else { 
			cursor.moveToFirst(); 
			int idx = cursor.getColumnIndex(MediaStore.Images.ImageColumns.DATA);
			
			String path = cursor.getString(idx);
			
			// do not forget to close the cursor
			cursor.close();
			
			return path; 
		}
	}
}
