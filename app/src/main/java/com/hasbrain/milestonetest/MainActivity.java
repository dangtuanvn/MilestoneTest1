package com.hasbrain.milestonetest;

import com.facebook.AccessTokenTracker;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.hasbrain.milestonetest.model.FacebookImage;
import com.hasbrain.milestonetest.model.FacebookPhotoResponse;
import com.hasbrain.milestonetest.model.converter.FacebookImageDeserializer;
import com.hasbrain.milestonetest.model.converter.FacebookPhotoResponseDeserializer;
import com.squareup.picasso.Picasso;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ExifInterface;
import android.os.AsyncTask;
import android.os.Build;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.annotation.StringDef;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import java.io.File;
import java.util.ArrayList;

import java.io.IOException;

import java.util.Collections;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    public static final int MY_REQUEST__CODE = 1;
    public static final String TYPE_UPLOADED = "uploaded";
    public static final String TYPE_TAGGED = "tagged";
    public static final String PUBLISH_ACTIONS_PERMISSION = "publish_actions";
    private static final int REQUEST_IMAGE = 0x1;
    @Bind(R.id.rv_photos)
    RecyclerView rvPhotos;
    @Bind(R.id.swipe_refresh_layout)
    SwipeRefreshLayout swipeRefreshLayout;
    @Bind(R.id.fab)
    FloatingActionButton floatingActionButton;
    @Bind(R.id.my_toolbar)
    Toolbar myToolbar;

    //    @Bind(R.id.bt_fb_logout)
//    LoginButton btFacebookLogout;
    @Bind(R.id.loading_symbol)
    ProgressBar loadingSymbol;
    @Bind(R.id.loading_text)
    TextView loadingText;

    private File cameraOutput;
    private Gson gson;
    private CallbackManager callbackManager;
    private LinearLayoutManager mLayoutManager;
    private String afterpic;
    private List<FacebookImage> facebookImageContainer = new ArrayList<>();
    protected List<FacebookImage> bookmarkList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setTitle("Your facebook photos");
        ButterKnife.bind(this);
        loadingSymbol.setVisibility(View.GONE);
        loadingText.setVisibility(View.GONE);

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
        mLayoutManager = new LinearLayoutManager(this);
        setSupportActionBar(myToolbar);
        rvPhotos.setLayoutManager(mLayoutManager);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openCameraForImage();
            }
        });
        getUserPhotos(TYPE_UPLOADED, null);
        gson = new GsonBuilder()
                .registerTypeAdapter(FacebookImage.class, new FacebookImageDeserializer())
                .registerTypeAdapter(FacebookPhotoResponse.class, new FacebookPhotoResponseDeserializer())
                .create();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
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
        facebookImageContainer.clear();
        getUserPhotos(TYPE_UPLOADED, null);

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (REQUEST_IMAGE == requestCode && resultCode == RESULT_OK) {
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
                if (checkSelfPermission(Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                    requestPermissions(new String[]{Manifest.permission.CAMERA}, MY_REQUEST__CODE);
                }
                if (checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
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
                                    Toast.makeText(MainActivity.this, "Image upload error " + response.getError().getErrorMessage(), Toast.LENGTH_LONG).show();
                                } else {
                                    Toast.makeText(MainActivity.this, "Upload image success", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(MainActivity.this, "No more data to be load", Toast.LENGTH_LONG).show();
                } else {
                    facebookImageContainer.addAll(facebookPhotoResponse.getData());
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
        rvPhotos.setAdapter(new FacebookImageAdapter(getLayoutInflater(), Picasso.with(this), data));
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

    private void goToSplashActivity() {
        Intent startSplashIntent = new Intent(MainActivity.this, SplashActivity.class);
        startSplashIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(startSplashIntent);
        finish();
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

        public void bind(FacebookImage facebookImage) {
            picasso.load(facebookImage.getImageUrl())
                    .resize(1280, 960)
                    .centerCrop()
                    .into(ivFacebookPhoto);
            tvImageName.setText(facebookImage.getName());
            tvImageTime.setText(facebookImage.getCreatedTime());
            //set image view
            tvImageSize.setText("photo height = " + ivFacebookPhoto.getHeight() + ", photo width = " + ivFacebookPhoto.getWidth());
            bookmark.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    action_bookmark();
                }
            });
        }

        public void action_bookmark(){

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
            return new FacebookImageVH(picasso, itemView);
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
