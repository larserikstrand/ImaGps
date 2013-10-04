package no.hig.imt3662.imagps;

import java.io.File;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.provider.Settings;


/**
 * Utility class that holds methods that are activity independent
 * and possible to use by multiple activities.
 * @author LarsErik
 *
 */
public final class Utility {
	
	
	
	/**
	 * Creates an alert dialog to ask if the user wants to enable GPS.
	 * @param activity - The activity in which to create the dialog.
	 */
	public static void enableGPSDialog(final Activity activity) {
		final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		final String action = Settings.ACTION_LOCATION_SOURCE_SETTINGS;
		final String message = activity.getString(R.string.GPS_alert);
		
		builder.setMessage(message);
		builder.setPositiveButton(activity.getString(R.string.enable),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						activity.startActivity(new Intent(action));
						dialog.dismiss();
					}
				});
		builder.setNegativeButton(activity.getString(R.string.cancel),
				new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialog, int which) {
						dialog.cancel();
					}
				});
		builder.create().show();
	}
	
	
	
	/**
	 * Returns a bitmap from the given file, scaled to use the given
	 * width and height.
	 * @param file - File where the image is held
	 * @param reqWidth - The width of the output bitmap
	 * @param reqHeight - The height of the output bitmap
	 * @return - A scaled Bitmap
	 */
	public static Bitmap decodeSampledBitmapFromFile(File file,
			int reqWidth, int reqHeight) {
		BitmapFactory.Options options = new BitmapFactory.Options();
		
		// Avoid memory allocation.
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(file.getPath(), options);
		
		// Calculate sample size, allow memory allocation.
		options.inSampleSize = Utility.calculateInSampleSize(
				options, reqWidth, reqHeight);
		options.inJustDecodeBounds = false;
		Bitmap bitmap = BitmapFactory.decodeFile(file.getPath(), options);
		
		/*
		 * Create a matrix for the bitmap in order to check the orientation,
		 * and rotate the matrix if necessary.
		 */
		try {
            ExifInterface exif = new ExifInterface(file.getPath());
            int orientation = exif.getAttributeInt(
            		ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if (orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            }
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            }
            else if (orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(),
            		bitmap.getHeight(), matrix, true);
        }
        catch (Exception e) {
        	e.printStackTrace();
        }
		
		return bitmap;
	}
	
	
	
	/**
	 * Calculates a sample size to be used in scaling an image, based on
	 * the width and height of the original compared to the wanted
	 * width and height.
	 * @param options - The BitmapFactory Options that hold the dimensions
	 * of the original.
	 * @param reqWidth - The preferred output width.
	 * @param reqHeight - The preferred output height.
	 * @return - An integer of the sample size
	 */
	public static int calculateInSampleSize(
			BitmapFactory.Options options, int reqWidth, int reqHeight) {
		// Height and width of the original image.
		final int height = options.outHeight;
		final int width = options.outWidth;
		int inSampleSize = 1;
		
		if (height > reqHeight || width > reqWidth) {
			final int heightRatio = Math.round(
					(float) height / (float) reqHeight);
			final int widthRatio = Math.round(
					(float) width / (float) reqWidth);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		
		return inSampleSize;
	}
	
}
