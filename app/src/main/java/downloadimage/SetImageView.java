package downloadimage;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.widget.ImageView;

public class SetImageView {

	private AsyncImageLoader loader;

	public SetImageView(Context context) {
		loader = new AsyncImageLoader(context);
		// 将图片缓存至外部文件中
		loader.setCache2File(true); // false
		// 设置外部缓存文件夹
		loader.setCachedDir(context.getCacheDir().getAbsolutePath());
	}

	public void setview(final ImageView view, String image_url) {
		// 下载图片，第二个参数是否缓存至内存中
		loader.downloadImage(image_url, true,
				new AsyncImageLoader.ImageCallback() {
					@SuppressLint("NewApi")
					@Override
					public void onImageLoaded(Bitmap bitmap, String imageUrl,
											  boolean flag) {

						if (bitmap != null) {
							view.setScaleType(ImageView.ScaleType.FIT_XY);
							view.setImageBitmap(bitmap);
						} else {
							// 下载失败，设置默认图片

						}
					}
				});
	}

	public void setview_src(final ImageView view, String image_url) {
		// 下载图片，第二个参数是否缓存至内存中
		loader.downloadImage(image_url, true,
				new AsyncImageLoader.ImageCallback() {
					@SuppressLint("NewApi")
					@Override
					public void onImageLoaded(Bitmap bitmap, String imageUrl,
											  boolean flag) {

						if (bitmap != null) {
							view.setScaleType(ImageView.ScaleType.FIT_XY);
							view.setImageBitmap(bitmap);
						} else {
							// 下载失败，设置默认图片

						}
					}
				});
	}

}
