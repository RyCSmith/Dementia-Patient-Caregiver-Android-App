package dementiaapp.com.dementiaapp;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.media.MediaPlayer;
import android.net.Uri;
import android.provider.MediaStore;
import android.widget.Toast;

import com.orhanobut.logger.Logger;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

/**
 * Created by vishwa on 3/3/15.
 */
public class MemAidUtils {

    public static String getAbsolutePathFromUri(Context ctx, Uri contentUri) {
        String[] proj = { MediaStore.Video.Media.DATA };
        Cursor cursor = ctx.getContentResolver().query(contentUri, proj, null, null, null);
        int column_index = cursor.getColumnIndexOrThrow(
                MediaStore.Images.Media.DATA);
        cursor.moveToFirst();
        return cursor.getString(column_index);
    }

    public static Matrix getRotationMatrixForImage(Context ctx, Uri contentUri) {
        String pathToImage = getAbsolutePathFromUri(ctx, contentUri);
        return getRotationMatrixForImage(pathToImage);
    }

    public static Matrix getRotationMatrixForImage(String pathToImage) {
        ExifInterface exif;
        try {
            exif = new ExifInterface(pathToImage);
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
            Matrix matrix = new Matrix();
            if(orientation == ExifInterface.ORIENTATION_ROTATE_90) {
                matrix.postRotate(90);
            }
            else if(orientation == ExifInterface.ORIENTATION_ROTATE_180) {
                matrix.postRotate(180);
            }
            else if(orientation == ExifInterface.ORIENTATION_ROTATE_270) {
                matrix.postRotate(270);
            }
            return matrix;
        } catch (IOException e) {
            return new Matrix();
        }
    }
    public static Bitmap decodeSampledBitmapFromFilePath(String filePath, int reqWidth, int reqHeight) {
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(filePath, options);
    }

    public static int calculateInSampleSize(
            BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            inSampleSize = inSampleSize < 2 ? 2: inSampleSize;
        }
        return inSampleSize;
    }

    public static void playAudio(String filePath) {
        if (filePath == null) {
            Logger.e("Could not play the audio because filePath is null");
            return;
        }
        MediaPlayer mp = new MediaPlayer();

        try {
            mp.setDataSource(filePath);
            mp.prepare();
            mp.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
