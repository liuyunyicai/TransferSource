package imageloader;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import java.io.FileDescriptor;


/**
 * Created by admin on 2015/11/30.
 */
public class ImageHelper {

    // 计算图片压缩比例
    private static int calculateInSampleSize(BitmapFactory.Options options, int picWidth, int picHeight) {
        if (picWidth == 0 || picHeight == 0) {
            return 1;
        }

        final int height = options.outHeight;
        final int width  = options.outWidth;

        int inSampleSize = 1;

        if (height > picHeight || width > picWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            // 保证取样率为2的幂
            while ((halfHeight / inSampleSize) >= picWidth
                    && (halfWidth / inSampleSize) >= picHeight) {
                inSampleSize *= 2;
            }
        }
        return inSampleSize;
    }

    // ======= 压缩图片 ====== //
    // 根据图片的路径String来压缩图片
    public static Bitmap decodeSampledBitmap(String imgFilePath, int picWidth, int picHeight) {
        // 第一次解析将inJustDecodeBounds设置为true，来获取图片大小,此时返回的bitmap为null
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds= true;
        Bitmap bitmap = BitmapFactory.decodeFile(imgFilePath, options);

        // 获得压缩比
        options.inSampleSize = calculateInSampleSize(options, picWidth, picHeight);

        // 解析获得图片
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFile(imgFilePath, options);
    }

    //
    public static Bitmap decodeSampledBitmapFromResource(Resources res, int resId, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeResource(res, resId, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeResource(res, resId, options);
    }

    public static Bitmap decodeSampledBitmapFromFileDescriptor(FileDescriptor fd, int reqWidth, int reqHeight) {
        // First decode with inJustDecodeBounds=true to check dimensions
        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFileDescriptor(fd, null, options);

        // Calculate inSampleSize
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight);

        // Decode bitmap with inSampleSize set
        options.inJustDecodeBounds = false;
        return BitmapFactory.decodeFileDescriptor(fd, null, options);
    }

}
