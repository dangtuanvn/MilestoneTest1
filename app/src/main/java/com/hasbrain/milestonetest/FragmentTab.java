package com.hasbrain.milestonetest;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.StringDef;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.AccessTokenTracker;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.hasbrain.milestonetest.model.FacebookImage;
import com.hasbrain.milestonetest.model.FacebookPhotoResponse;
import com.hasbrain.milestonetest.model.converter.FacebookImageDeserializer;
import com.hasbrain.milestonetest.model.converter.FacebookPhotoResponseDeserializer;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


/**
 * Created by sinhhx on 10/25/16.
 */
public class FragmentTab extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    public static final int MY_REQUEST__CODE = 1;
    public static final String TYPE_UPLOADED = "uploaded";
    public static final String TYPE_TAGGED = "tagged";
    public static final String PUBLISH_ACTIONS_PERMISSION = "publish_actions";
    private static final int REQUEST_IMAGE = 0x1;

    RecyclerView rvPhotos;

    SwipeRefreshLayout swipeRefreshLayout;

    FloatingActionButton floatingActionButton;

    Toolbar myToolbar;

    ProgressBar loadingSymbol;

    TextView loadingText;

    private File cameraOutput;
    private Gson gson;
    private CallbackManager callbackManager;
    private LinearLayoutManager mLayoutManager;
    private String afterpic;
    private List<FacebookImage> facebookImageContainer = new ArrayList<>();
    protected static List<FacebookImage> bookmarkList;
    private boolean first_start = true;
    public static final String ARG_PAGE = "ARG_PAGE";
    String url;

    private int mPage;

    public static FragmentTab newInstance(int page) {
        Bundle args = new Bundle();
        args.putInt(ARG_PAGE, page);
        FragmentTab fragment = new FragmentTab();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mPage = getArguments().getInt(ARG_PAGE);
        setHasOptionsMenu(true);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View view = inflater.inflate(R.layout.activity_main, container, false);

        rvPhotos = (RecyclerView) view.findViewById(R.id.rv_photos);
        swipeRefreshLayout = (SwipeRefreshLayout) view.findViewById(R.id.swipe_refresh_layout);
       floatingActionButton = (FloatingActionButton) view.findViewById(R.id.fab);
        myToolbar = (Toolbar) view.findViewById(R.id.my_toolbar);

        loadingSymbol = (ProgressBar) view.findViewById(R.id.loading_symbol);
        loadingText = (TextView) view.findViewById(R.id.loading_text);

        getActivity().setTitle("Your facebook photos");
//        ButterKnife.bind(getActivity());
        loadingSymbol.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);

        if(first_start){
            first_start = false;
            readDataFromExternalStorage();
        }

        AccessTokenTracker accessTokenTracker = new AccessTokenTracker() {
            @Override
            protected void onCurrentAccessTokenChanged(
                    AccessToken oldAccessToken,
                    AccessToken currentAccessToken) {
                if (currentAccessToken == null) {
                    goToSplashActivity();
                }
            }
        };

        swipeRefreshLayout.setOnRefreshListener(this);
        mLayoutManager = new LinearLayoutManager(getActivity());

        ((AppCompatActivity)getActivity()).setSupportActionBar(myToolbar);
        rvPhotos.setLayoutManager(mLayoutManager);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCameraForImage();
            }
        });
        if(mPage==1){
            getUserPhotos(TYPE_UPLOADED, null);
        }
        if(mPage==2){
           displayPhotos(bookmarkList);
        }

        gson = new GsonBuilder()
                .registerTypeAdapter(FacebookImage.class, new FacebookImageDeserializer())
                .registerTypeAdapter(FacebookPhotoResponse.class, new FacebookPhotoResponseDeserializer())
                .create();



        return view;
    }

//    @Override
//    public void onResume(){
//        super.onResume();
//        if(mPage==1){
//            getUserPhotos(TYPE_UPLOADED, null);
//        }
//        if(mPage==2){
//            displayPhotos(bookmarkList);
//        }
//    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_main, menu);
        super.onCreateOptionsMenu(menu,inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {


            case R.id.action_logout:
                AccessToken.setCurrentAccessToken(null);
                return true;

            default:
                // If we got here, the user's action was not recognized.
                // Invoke the superclass to handle it.
                return super.onOptionsItemSelected(item);

        }
    }


    @Override
    public void onRefresh() {

        if(mPage==1){
            facebookImageContainer.clear();
            getUserPhotos(TYPE_UPLOADED, null);
        }
        if(mPage==2){
            displayPhotos(bookmarkList);
            swipeRefreshLayout.setRefreshing(false);
        }

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_IMAGE == requestCode && resultCode == getActivity().RESULT_OK) {
//            Bitmap bitmapData = data.getParcelableExtra("data");

            String photoPath = Uri.fromFile(cameraOutput).getPath();
            int targetW = 1280, targetH = 960;

            BitmapFactory.Options bmOptions = new BitmapFactory.Options();
            bmOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(photoPath, bmOptions);
            int photoW = bmOptions.outWidth;
            int photoH = bmOptions.outHeight;

            // Determine how much to scale down the image
            int scaleFactor = Math.min(photoW / targetW, photoH / targetH);

            // Decode the image file into a Bitmap sized to fill the View
            bmOptions.inJustDecodeBounds = false;
            bmOptions.inSampleSize = scaleFactor;
            bmOptions.inPurgeable = true;

            Bitmap bitmapData = BitmapFactory.decodeFile(photoPath, bmOptions);

            // Detect image's orientation
            ExifInterface exif = null;
            try {
                exif = new ExifInterface(photoPath);
            } catch (IOException e) {
                e.printStackTrace();
            }
            int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_UNDEFINED);

            // Rotate the image
            bitmapData = rotateBitmap(bitmapData, orientation);
            if (bitmapData != null) {
                uploadPhotoToFacebook(bitmapData);
            }
        } else {
            // callbackManager.onActivityResult(requestCode, resultCode, data);
        }
    }

    public static Bitmap rotateBitmap(Bitmap bitmap, int orientation) {
        Matrix matrix = new Matrix();
        switch (orientation) {
            case ExifInterface.ORIENTATION_NORMAL:
                return bitmap;
            case ExifInterface.ORIENTATION_FLIP_HORIZONTAL:
                matrix.setScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_180:
                matrix.setRotate(180);
                break;
            case ExifInterface.ORIENTATION_FLIP_VERTICAL:
                matrix.setRotate(180);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_TRANSPOSE:
                matrix.setRotate(90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_90:
                matrix.setRotate(90);
                break;
            case ExifInterface.ORIENTATION_TRANSVERSE:
                matrix.setRotate(-90);
                matrix.postScale(-1, 1);
                break;
            case ExifInterface.ORIENTATION_ROTATE_270:
                matrix.setRotate(-90);
                break;
            default:
                return bitmap;
        }

        try {
            Bitmap bmRotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            bitmap.recycle();
            return bmRotated;
        } catch (OutOfMemoryError e) {
            e.printStackTrace();
            return null;
        }
    }


    private void openCameraForImage() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (getActivity().checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_REQUEST__CODE);
            }
            if (getActivity().checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST__CODE);
            }
        }

        Intent openCameraForImageIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        File dir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);

        cameraOutput = new File(dir, "Sample_image.jpeg");
        openCameraForImageIntent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(cameraOutput));
        startActivityForResult(openCameraForImageIntent, REQUEST_IMAGE);

    }


    private void uploadPhotoToFacebook(final Bitmap imageBitmap) {
        AccessToken currentAccessToken = AccessToken.getCurrentAccessToken();
        if (currentAccessToken.getPermissions().contains(PUBLISH_ACTIONS_PERMISSION)) {
            doUploadPhotoToFacebook(imageBitmap, currentAccessToken);
        } else {
            callbackManager = CallbackManager.Factory.create();
            LoginManager loginManager = LoginManager.getInstance();
            loginManager.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
                @Override
                public void onSuccess(LoginResult loginResult) {
                    if (loginResult.getRecentlyGrantedPermissions().contains(PUBLISH_ACTIONS_PERMISSION)) {
                        doUploadPhotoToFacebook(imageBitmap, loginResult.getAccessToken());
                    }
                }

                @Override
                public void onCancel() {

                }

                @Override
                public void onError(FacebookException error) {

                }
            });
            loginManager.logInWithPublishPermissions(this, Collections.singletonList(PUBLISH_ACTIONS_PERMISSION));

        }
    }

    private void doUploadPhotoToFacebook(Bitmap imageFile, AccessToken currentAccessToken) {
        loadingSymbol.setVisibility(View.VISIBLE);
        loadingText.setVisibility(View.VISIBLE);
        GraphRequest graphRequest = GraphRequest
                .newUploadPhotoRequest(currentAccessToken, "me/photos", imageFile,
                        "Upload from hasBrain Milestone test", null, new GraphRequest.Callback() {
                            @Override
                            public void onCompleted(GraphResponse response) {
                                if (response.getError() != null) {
                                    Toast.makeText(getActivity(), "Image upload error " + response.getError().getErrorMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(getActivity(), "Upload image success", Toast.LENGTH_LONG).show();
                                    getUserPhotos(TYPE_UPLOADED, null);
                                }
                                loadingSymbol.setVisibility(View.GONE);
                                loadingText.setVisibility(View.GONE);
                            }
                        });
        graphRequest.executeAsync();
    }


    private void getUserPhotos(@PHOTO_TYPE String photoType, final String after) {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        Bundle parameters = new Bundle();
        parameters.putString("fields", "id,name,images,picture,created_time");
        parameters.putString("type", photoType);
        if (after != null) {
            parameters.putString("after", after);
        }
        GraphRequest graphRequest = new GraphRequest(accessToken, "me/photos", parameters, HttpMethod.GET);
        graphRequest.setCallback(new GraphRequest.Callback() {
            @Override
            public void onCompleted(GraphResponse response) {
                Log.d("hasBrain", "Graph response " + response.toString());
                FacebookPhotoResponse facebookPhotoResponse = gson
                        .fromJson(response.getRawResponse(), FacebookPhotoResponse.class);
                if (facebookPhotoResponse.getData() == null) {
                    Toast.makeText(getActivity(), "No more data to be load", Toast.LENGTH_LONG).show();
                } else {
//                    facebookImageContainer.addAll(facebookPhotoResponse.getData());
                    for(int i = 0; i < facebookPhotoResponse.getData().size(); i++){
                        FacebookImage image = facebookPhotoResponse.getData().get(i);
//                        Log.i("IMAGE ID", "" + image.getId());
//                        Log.i("IMAGE NAME", "" + image.getName());
//                        Log.i("IMAGE URL", "" + image.getImageUrl());
//                        Log.i("IMAGE THUMBNAIL URL", "" + image.getThumbnailUrl());
                        for(int j = 0; j < bookmarkList.size(); j++){
                            if(image.getId().equals(bookmarkList.get(j).getId())){
                                image.setBookmark(true);
                                break;
                            }
                        }
                        facebookImageContainer.add(image);
                    }
                    displayPhotos(facebookImageContainer);
                    afterpic = facebookPhotoResponse.getAfter();
                }
                //remove loading indicator
                swipeRefreshLayout.setRefreshing(false);
            }
        });
        graphRequest.executeAsync();
    }

    private void displayPhotos(List<FacebookImage> data) {
        rvPhotos.setAdapter(new FacebookImageAdapter(getActivity().getLayoutInflater(), Picasso.with(getActivity()), data));
        rvPhotos.setAdapter(new FacebookImageAdapter(getActivity().getLayoutInflater(), Picasso.with(getActivity()), data));

        if(mPage == 1) {
            rvPhotos.addOnScrollListener(new RecyclerView.OnScrollListener() {
                @Override
                public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                    super.onScrolled(recyclerView, dx, dy);
                    int pastVisiblesItems, visibleItemCount, totalItemCount;

                    if (dy > 0) //check for scroll down
                    {
                        visibleItemCount = mLayoutManager.getChildCount();
                        totalItemCount = mLayoutManager.getItemCount();
                        pastVisiblesItems = mLayoutManager.findFirstVisibleItemPosition();


                        if ((visibleItemCount + pastVisiblesItems) >= totalItemCount) {
                            getUserPhotos(TYPE_UPLOADED, afterpic);
                        }

                    }
                }
            });
        }
    }

    private void goToSplashActivity() {
        Intent startSplashIntent = new Intent(getActivity(), SplashActivity.class);
        startSplashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startSplashIntent);
        getActivity().finish();
    }

    @StringDef({TYPE_UPLOADED, TYPE_TAGGED})
    public @interface PHOTO_TYPE {

    }

    static class FacebookImageVH extends RecyclerView.ViewHolder {
        @Bind(R.id.iv_facebook_photo)
        ImageView ivFacebookPhoto;
        @Bind(R.id.tv_image_name)
        TextView tvImageName;
        @Bind(R.id.tv_image_time)
        TextView tvImageTime;
        @Bind(R.id.tv_image_size)
        TextView tvImageSize;
        private Picasso picasso;
        @Bind(R.id.bookmark)
        ImageView bookmark;

        public FacebookImageVH(Picasso picasso, View itemView) {
            super(itemView);
            this.picasso = picasso;
            ButterKnife.bind(this, itemView);
        }

        public void bind(final FacebookImage facebookImage) {
            picasso.load(facebookImage.getImageUrl())
                    .resize(1280, 960)
                    .centerCrop()
                    .into(ivFacebookPhoto);

            tvImageSize.setText("photo height = " + ivFacebookPhoto.getHeight() + ", photo width = " + ivFacebookPhoto.getWidth());;


            tvImageName.setText(facebookImage.getName());
            tvImageTime.setText(facebookImage.getCreatedTime());

            if(facebookImage.isBookmark()){
                bookmark.setImageResource(R.drawable.ic_bookmark_star_selected);
                bookmark.invalidate();
            }
            else{
                bookmark.setImageResource(R.drawable.ic_bookmark_star_unselected);
                bookmark.invalidate();
            }

            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    action_bookmark(facebookImage);
                }
            });
        }

        public void action_bookmark(FacebookImage facebookImage){
            if(facebookImage.isBookmark()){
                facebookImage.setBookmark(false);
                for(int i = 0; i < bookmarkList.size(); i++){
                    if(bookmarkList.get(i).getId().equals(facebookImage.getId())){
                        bookmarkList.remove(i);
                        break;
                    }
                }
                bookmark.setImageResource(R.drawable.ic_bookmark_star_unselected);
                bookmark.invalidate();
            }
            else {
                facebookImage.setBookmark(true);
                bookmarkList.add(facebookImage);
                bookmark.setImageResource(R.drawable.ic_bookmark_star_selected);
                bookmark.invalidate();
            }
            saveDataToExternalStorage();
        }
    }

    public static void saveDataToExternalStorage(){
        // get the path to sdcard
        File sdcard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        File dir = new File(sdcard.getAbsolutePath() + "/bookmark/");
        // create this directory if not already created
        if(!dir.exists()) {
            dir.mkdir();
        }
        // create the file in which we will write the contents

        try {
            FileOutputStream file = new FileOutputStream(new File(dir, "bookmark_list.txt"));
            ObjectOutputStream oos = new ObjectOutputStream(file);
            oos.writeObject(bookmarkList);
            oos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.i("WRITE OUPUT","File didn't write");
        }
    }

    public void readDataFromExternalStorage(){
        // get the path to sdcard
        File sdcard = Environment.getExternalStorageDirectory();
        // to this path add a new directory path
        File dir = new File(sdcard.getAbsolutePath() + "/bookmark/");
        // create this directory if not already created
        dir.mkdir();
        // create the file in which we will write the contents

        FileInputStream fis;
        try {
            fis = new FileInputStream(new File(dir, "bookmark_list.txt"));
            ObjectInputStream ois = new ObjectInputStream(fis);
            bookmarkList = (ArrayList<FacebookImage>) ois.readObject();
            ois.close();

        } catch (Exception e) {
            e.printStackTrace();
            bookmarkList = new ArrayList<>();
        }
    }

    private static class FacebookImageAdapter extends RecyclerView.Adapter<FacebookImageVH> {
        private LayoutInflater layoutInflater;
        private Picasso picasso;
        private List<FacebookImage> facebookImages;

        public FacebookImageAdapter(LayoutInflater layoutInflater, Picasso picasso,
                                    List<FacebookImage> facebookImages) {
            this.layoutInflater = layoutInflater;
            this.picasso = picasso;
            this.facebookImages = facebookImages;
        }

        @Override
        public FacebookImageVH onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = layoutInflater.inflate(R.layout.item_facebook_photo, parent, false);
            return new FacebookImageVH(picasso, itemView );
        }

        @Override
        public void onBindViewHolder(FacebookImageVH holder, int position) {
            holder.bind(facebookImages.get(position));
        }

        @Override
        public int getItemCount() {
            return facebookImages != null ? facebookImages.size() : 0;
        }
    }
}
class SampleFragmentPagerAdapter extends FragmentPagerAdapter {
    private final int PAGE_COUNT = 2;
    private String tabTitles[] = new String[] { "STANDARD", "BOOKMARK"};
    private Context context;

    public SampleFragmentPagerAdapter(FragmentManager fm, Context context) {
        super(fm);
        this.context = context;
    }

    @Override
    public int getCount() {
        return PAGE_COUNT;
    }

    @Override
    public Fragment getItem(int position) {
        return FragmentTab.newInstance(position + 1);
    }

    @Override
    public CharSequence getPageTitle(int position) {
        // Generate title based on item position
        return tabTitles[position];
    }

}
