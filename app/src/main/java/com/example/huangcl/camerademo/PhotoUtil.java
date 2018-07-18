package com.example.huangcl.camerademo;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class PhotoUtil {

    public static Bitmap getBitmapFromSd(String path) {
        Bitmap bitmap=null;
        try {
            File file=new File(path);
            if(file.isFile()) {
                InputStream inputStream=new FileInputStream(file);
                BitmapFactory.Options options=new BitmapFactory.Options();
                options.inPreferredConfig=Bitmap.Config.RGB_565;
                options.inPurgeable=true;
                options.inInputShareable=true;
                options.inSampleSize=1;
                bitmap=BitmapFactory.decodeStream(inputStream,null,options);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return bitmap;
    }

//    public static int readPictureDegree(String path) {
//        int degree = 0;
//        try {
//            ExifInterface exifInterface = new ExifInterface(path);
//            int orientation =
//                    exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION,
//                            ExifInterface.ORIENTATION_NORMAL);
//            switch (orientation) {
//                case ExifInterface.ORIENTATION_ROTATE_90:
//                    degree = 90;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_180:
//                    degree = 180;
//                    break;
//                case ExifInterface.ORIENTATION_ROTATE_270:
//                    degree = 270;
//                    break;
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        Log.e("readPictureDegree: ", degree+"");
//        return degree;
//    }

    public static Bitmap rotateToDegrees(int angle, Bitmap bitmap) {
        // 旋转图片 动作
        Matrix matrix = new Matrix();
        matrix.postRotate(angle);

        // 创建新的图片
        Bitmap resizedBitmap = Bitmap.createBitmap(bitmap, 0, 0,
                bitmap.getWidth(), bitmap.getHeight(), matrix, true);
        if (resizedBitmap != bitmap && bitmap != null && !bitmap.isRecycled()) {
            bitmap.recycle();
            bitmap = null;
        }

        matrix.setScale(0.5f, 0.5f);
        Bitmap bm =  Bitmap.createBitmap(resizedBitmap, 0, 0, resizedBitmap.getWidth(),
                resizedBitmap.getHeight(), matrix, true);
        return bm;
    }
}
