package ayp.aug.photogallery;

/**
 * Created by Tanaphon on 8/16/2016.
 */
public class GalleryItem {
    private String mId;
    private String mTitle;
    private String mUrl;
    private String bigSizeUrl;

    public void setId(String id) {
        mId = id;
    }

    public String getId() {
        return mId;
    }


    public void setTitle(String title) {
        mTitle = title;
    }

    public void setUrl(String url) {
        mUrl = url;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getName(){
        return getTitle();
    }

    public void setName(String name){
        setTitle(name);
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof GalleryItem){
            // is GalleryItem too !!
            GalleryItem that = (GalleryItem) obj;
            return that.mId != null && this.mId != null && that.mId.equals(mId);
        }
        return false;
    }

    public void setBigSizeUrl(String url) {
        this.bigSizeUrl = url;
    }

    public String getBigSizeUrl () {
        return bigSizeUrl;
    }
}
