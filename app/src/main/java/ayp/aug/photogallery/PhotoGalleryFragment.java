package ayp.aug.photogallery;

import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v4.util.LruCache;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Tanaphon on 8/16/2016.
 */
public class PhotoGalleryFragment extends Fragment {
    private static final String TAG = "PhotoGalleryFragment";

    public static PhotoGalleryFragment newInstance() {

        Bundle args = new Bundle();

        PhotoGalleryFragment fragment = new PhotoGalleryFragment();
        fragment.setArguments(args);
        return fragment;
    }

    private RecyclerView mRecyclerView;
    private FlickrFetcher mFlickrFetcher;
    private PhotoGalleryAdapter mAdapter;
    private ThumbnailDownloader<PhotoHolder> mThumbnailDownloaderThread;
    private FetcherTask mFetcherTask;
    private List<GalleryItem> mItems;
    private LruCache<String, Bitmap> mMemoryCache;
    private String mSearchKey;
    final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);

    // Use 1/8th of the available memory for this memory cache.
    final int cacheSize = maxMemory / 8;


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);

        MenuItem menuItem = menu.findItem(R.id.mnu_search);
        final SearchView searchView = (SearchView) menuItem.getActionView();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                mSearchKey = query;
                loadPhotos();
                Log.d(TAG, "onQueryTextSubmit: " + query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                Log.d(TAG, "onQueryTextChange: " + newText);
                return false;
            }
        });

        searchView.setOnSearchClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                searchView.setQuery(mSearchKey, false);
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.mnu_reload:
                loadPhotos();
                return true;
            case R.id.mnu_clear_search:
                mSearchKey = null;
                loadPhotos();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void loadPhotos() {
        if (mFlickrFetcher == null || !mFetcherTask.isRunning()) {
            mFetcherTask = new FetcherTask();

            if (mSearchKey != null)
                mFetcherTask.execute(mSearchKey);
            else
                mFetcherTask.execute();
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mFlickrFetcher = new FlickrFetcher();

        setHasOptionsMenu(true);
        setRetainInstance(true);

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // The cache size will be measured in kilobytes rather than
                // number of items.
                return bitmap.getByteCount() / 1024;
            }
        };

        mFetcherTask = new FetcherTask();

        Handler responseUIHandler = new Handler();

        ThumbnailDownloader.ThumbnailDownloaderListener listener =
                new ThumbnailDownloader.ThumbnailDownloaderListener<PhotoHolder>() {
                    @Override
                    public void onThumbnailDownloaded(PhotoHolder target, Bitmap thumbnail, String url) {
                        if (null == mMemoryCache.get(url)) {
                            mMemoryCache.put(url, thumbnail);
                        }

                        Drawable drawable = new BitmapDrawable(getResources(), thumbnail);
                        target.bindDrawable(drawable);
                    }
                };

        mThumbnailDownloaderThread = new ThumbnailDownloader<>(responseUIHandler);
        mThumbnailDownloaderThread.setmThumbnailDownloaderListener(listener);
        mThumbnailDownloaderThread.start();
        mThumbnailDownloaderThread.getLooper();

        Log.i(TAG, "onCreate: Start background thread");
    }


    @Override
    public void onResume() {
        super.onResume();
        String searchKey = PhotoGalleryPreference.getStoreSearchKey(getActivity());
        if(searchKey != null)
            mSearchKey = searchKey;
    }

    @Override
    public void onPause() {
        super.onPause();

        PhotoGalleryPreference.setStoredSearchKey(getActivity(), mSearchKey);
    }



    @Override
    public void onDestroy() {
        super.onDestroy();

        mThumbnailDownloaderThread.quit();
        Log.i(TAG, "onDestroy: Stop background thread");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        mThumbnailDownloaderThread.clearQueue();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_photo_gallery, container, false);

        mRecyclerView = (RecyclerView) v.findViewById(R.id.photo_gallery_recycle_view);
        mRecyclerView.setLayoutManager(new GridLayoutManager(getActivity(), 3));
        List<GalleryItem> mGalleryItems = new ArrayList<>();
        mAdapter = new PhotoGalleryAdapter(mGalleryItems);
        mItems = new ArrayList<>();
        mRecyclerView.setAdapter(new PhotoGalleryAdapter(mItems));
        loadPhotos();

        return v;
    }

    class PhotoHolder extends RecyclerView.ViewHolder {

        ImageView mPhoto;

        public PhotoHolder(View itemView) {
            super(itemView);

            mPhoto = (ImageView) itemView.findViewById(R.id.image_photo);
        }

        public void bindDrawable(@NonNull Drawable drawable) {
            mPhoto.setImageDrawable(drawable);
        }


    }

    class PhotoGalleryAdapter extends RecyclerView.Adapter<PhotoHolder> {

        List<GalleryItem> mGalleryItemList;

        PhotoGalleryAdapter(List<GalleryItem> galleryItems) {
            mGalleryItemList = galleryItems;
        }

        @Override
        public PhotoHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(getActivity()).inflate(R.layout.item_photo, parent, false);
            return new PhotoHolder(v);
        }

        @Override
        public void onBindViewHolder(PhotoHolder holder, int position) {
            GalleryItem galleryItem = mGalleryItemList.get(position);

            Drawable pukingRainbowsDrawable =
                    ResourcesCompat.getDrawable(getResources(), R.drawable.puking_rainbows, null);

            Log.d(TAG, "onBindViewHolder: bind position #" + position + ", url: " + galleryItem.getUrl());
            holder.bindDrawable(pukingRainbowsDrawable);

            if (mMemoryCache.get(galleryItem.getUrl()) != null) {
                Bitmap bitmap = mMemoryCache.get(galleryItem.getUrl());
                holder.bindDrawable(new BitmapDrawable(getResources(), bitmap));
            } else {
                //
                mThumbnailDownloaderThread.queueThumbnailDownload(holder, galleryItem.getUrl());
            }

        }

        @Override
        public int getItemCount() {
            return mGalleryItemList.size();

        }
    }

    class FetcherTask extends AsyncTask<String, Void, List<GalleryItem>> {

        boolean running = false;

        @Override
        protected List<GalleryItem> doInBackground(String... params) {
            synchronized (this) {
                running = true;
            }
            try {

                List<GalleryItem> itemList = new ArrayList<>();
                FlickrFetcher flickrFetcher = new FlickrFetcher();

                if (params.length > 0) {
                    flickrFetcher.
                            searchPhotos(itemList, params[0]);
                } else {
                    flickrFetcher.getRecentPhotos(itemList);
                }
                return itemList;

            } finally {
                synchronized (this) {
                    running = false;
                }
            }
        }

        boolean isRunning() {
            return running;
        }

        @Override
        protected void onProgressUpdate(Void... values) {
            super.onProgressUpdate(values);

            String formatString = getResources().getString(R.string.photo_progress_loaded);
            Snackbar.make(mRecyclerView, formatString, Snackbar.LENGTH_SHORT).show();
        }

        @Override
        protected void onPostExecute(List<GalleryItem> galleryItems) {
            mRecyclerView.setAdapter(new PhotoGalleryAdapter(galleryItems));
        }
    }
}
