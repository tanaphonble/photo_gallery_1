package ayp.aug.photogallery;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;

/**
 * Created by Tanaphon on 8/22/2016.
 */
public class LargePhotoDialogFragment extends DialogFragment {

    private static final String TAG = "LargePhotoDialogFragment";
    private static final String LARGE_PHOTO = "LARGE_PHOTO";

    ImageView largePhoto;

    public static LargePhotoDialogFragment newInstance(Bitmap bitmap) {

        Bundle args = new Bundle();

        args.putParcelable(LARGE_PHOTO, bitmap);

        LargePhotoDialogFragment fragment = new LargePhotoDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bitmap bitmap = getArguments().getParcelable(LARGE_PHOTO);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        ImageView imageView = new ImageView(getActivity());
        imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
        imageView.setImageDrawable(new BitmapDrawable(getResources(), bitmap));

        builder.setView(imageView);
        builder.setPositiveButton("close", null);
        return builder.create();
    }
}
