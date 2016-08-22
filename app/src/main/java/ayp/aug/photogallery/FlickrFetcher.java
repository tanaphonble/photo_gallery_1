package ayp.aug.photogallery;

import android.net.Uri;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

/**
 * Created by Tanaphon on 8/16/2016.
 */
public class FlickrFetcher {

    private static final String TAG = "FlickrFetcher";

    /**
     * Get url bytes from http connection then return as ByteArray
     *
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public byte[] getUrlBytes(String urlSpec) throws IOException {
        URL url = new URL(urlSpec);

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();

        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            InputStream in = connection.getInputStream();

            if (connection.getResponseCode() != HttpURLConnection.HTTP_OK) {
                throw new IOException(connection.getResponseMessage() + ": with " + urlSpec);
            }

            int bytesRead = 0;

            byte[] buffer = new byte[2048];

            while ((bytesRead = in.read(buffer)) > 0) {
                out.write(buffer, 0, bytesRead);
            }

            out.close();

            return out.toByteArray();
        } finally {
            connection.disconnect();
        }

    }

    /**
     *
     *
     * @param urlSpec
     * @return
     * @throws IOException
     */
    public String getUrlString(String urlSpec) throws IOException {
        return new String(getUrlBytes(urlSpec));
    }

    private static final String FLICKR_URL = "https://api.flickr.com/services/rest/";

    private static final String API_KEY = "6778e513a221969a621392769f02b2a1";

    private static final String METHOD_GET_RECENT = "flickr.photos.getRecent";
    private static final String METHOD_SEARCH = "flickr.photos.search";


    /**
     *
     *
     * @param method
     * @param param
     * @return
     * @throws IOException
     */
    private String buildUri(String method, String... param) throws IOException {
        String jsonString = null;
        Uri baseUrl = Uri.parse(FLICKR_URL);
        Uri.Builder builder = baseUrl.buildUpon();
        builder.appendQueryParameter("method", method)
                .appendQueryParameter("api_key", API_KEY)
                .appendQueryParameter("format", "json")
                .appendQueryParameter("nojsoncallback", "1")
                .appendQueryParameter("extras", "url_s");
        if (METHOD_SEARCH.equalsIgnoreCase(method)) {
            builder.appendQueryParameter("text", param[0]);
        }

        Uri completeUrl = builder.build();
        String url = completeUrl.toString();

        Log.i(TAG, "searchItem: Run URL: " + url);
        return url;
    }

    /**
     * Get JSON from url
     *
     * @param url
     * @return
     * @throws IOException
     */
    String queryItem(String url) throws IOException{
        Log.i(TAG, "searchItem: Run URL: " + url);
        String jsonString = getUrlString(url);

        Log.i(TAG, "Search: Received JSON: " + jsonString);
        return jsonString;
    }

    /**
     * Search photo then put into <b>items</b>
     *
     * @param items array target
     * @param key to search
     */
    public void searchPhotos(List<GalleryItem> items, String key) {
        try {
            String url = buildUri(METHOD_SEARCH, key);
            String jsonStr = queryItem(url);
            if (jsonStr != null)
                parseJSON(items, jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to fetch items", e);
        }
    }

    /**
     *
     * @param items
     */
    public void getRecentPhotos(List<GalleryItem> items) {
        try {
            String url = buildUri(METHOD_GET_RECENT);
            String jsonStr = queryItem(url);
            if (jsonStr != null)
                parseJSON(items, jsonStr);
        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "Failed to fetch items", e);
        }
    }

    /**
     *
     * @param newGalleryItemList
     * @param jsonBodyStr
     * @throws IOException
     * @throws JSONException
     */
    private void parseJSON(List<GalleryItem> newGalleryItemList, String jsonBodyStr)
            throws IOException, JSONException {
        JSONObject jsonBody = new JSONObject(jsonBodyStr);
        JSONObject photosJson = jsonBody.getJSONObject("photos");
        JSONArray photoListJson = photosJson.getJSONArray("photo");

        for (int i = 0; i < photoListJson.length(); i++) {
            JSONObject jsonPhotoItem = photoListJson.getJSONObject(i);

            GalleryItem item = new GalleryItem();

            item.setId(jsonPhotoItem.getString("id"));
            item.setTitle(jsonPhotoItem.getString("title"));

            if (!jsonPhotoItem.has("url_s"))
                continue;

            item.setUrl(jsonPhotoItem.getString("url_s"));

            newGalleryItemList.add(item);
        }
    }
}
