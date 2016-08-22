package ayp.aug.photogallery;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Message;
import android.util.Log;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Created by Tanaphon on 8/18/2016.
 */
public class ThumbnailDownloader<T> extends HandlerThread {
    private static final String TAG = "ThumnailDownloader";

    private Handler mRequestHandler;
    private final ConcurrentMap<T, String> mRequestUrlMap = new ConcurrentHashMap<>();
    private static final int DOWNLOAD_FILE = 2018;

//    private final LruCache mCache = new LruCache(60);

    private Handler mResponseHandler;
    private ThumbnailDownloaderListener<T> mThumbnailDownloaderListener;



    interface ThumbnailDownloaderListener<T> {
        void onThumbnailDownloaded(T target, Bitmap thumbnail, String url);
    }

    public void setmThumbnailDownloaderListener(ThumbnailDownloaderListener<T> mThumbnailDownloaderListener) {
        this.mThumbnailDownloaderListener = mThumbnailDownloaderListener;
    }

    public ThumbnailDownloader(Handler mUIHandler) {
        super(TAG);
        mResponseHandler = mUIHandler;
    }

    @Override
    protected void onLooperPrepared() {
        mRequestHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                // /work in the queue
                if (msg.what == DOWNLOAD_FILE) {
                    T target = (T) msg.obj;

                    String url = mRequestUrlMap.get(target);
                    Log.i(TAG, "handleMessage: Got message from queue: please download this URL: " + url);

                    handleRequestDownload(target, url);
                }
            }
        };
    }

    public void clearQueue() {
        mRequestHandler.removeMessages(DOWNLOAD_FILE);
    }

    private void handleRequestDownload(final T target, final String url) {
        try {
            if (url == null) {
                return;
            }

            byte[] bitMapBytes = new FlickrFetcher().getUrlBytes(url);
            final Bitmap bitmap = BitmapFactory.decodeByteArray(bitMapBytes, 0, bitMapBytes.length);

            mResponseHandler.post(new Runnable() {
                @Override
                public void run() {
                    String currentUrl = mRequestUrlMap.get(target);
                    if (currentUrl != null && !currentUrl.equals(url)) {
                        return;
                    }

                    // url is ok (the same one)
                    mRequestUrlMap.remove(target);
                    mThumbnailDownloaderListener.onThumbnailDownloaded(target, bitmap, url);
                }
            });

            Log.i(TAG, "handleRequestDownload: Bitmap URL is downloaded: ");
        } catch (IOException ioe) {
            Log.e(TAG, "handleRequestDownload: Error downloading:", ioe);
        }
    }

    public void queueThumbnailDownload(T target, String url) {
        Log.i(TAG, "queueThumbnailDownload: Got url : " + url);

        if (null == url) {
            mRequestUrlMap.remove(target);
        } else {
            mRequestUrlMap.put(target, url);

            Message msg = mRequestHandler.obtainMessage(DOWNLOAD_FILE, target); // get message from handler
            msg.sendToTarget(); // send to handler
        }

    }
}