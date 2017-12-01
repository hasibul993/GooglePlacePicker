package com.googleplacepicker.MediaPermission;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.location.LocationManager;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.googleplacepicker.PlacePickerActivity;
import com.googleplacepicker.R;
import com.theartofdev.edmodo.cropper.CropImage;


import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import com.googleplacepicker.Utils.GraphicsUtil;
import com.googleplacepicker.Utils.Utility;

/**
 * Created by BookMEds on 20-09-2017.
 */

public class PickMediaActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 0;
    static String imagePath = null;
    private static String TAG = "PickMediaActivity";
    private static int SELECT_PLACE_PICKER = 7;
    private static int SELECT_FILE_GALLERY = 1;
    private static int SELECT_FILE_CAMERA = 2;
    private static int PIC_CROP = 3;
    public SharedPreferences app_preference;
    public static final String MyPREFERENCES = "share_preference_key";
    Uri selectedImageUri, currentImageUri;

    String mCurrentPhotoPath = null, presetTime = "";
    AlertDialog alertDialog;
    int myear, mmonth, mday, mhour, mmin;

    Utility utility = new Utility();

    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri
                .getAuthority());
    }

    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri
                .getAuthority());
    }

    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri
                .getAuthority());
    }

    public void ShowDialogOptionForMidiaPick(final Context context) {

        try {
            final CharSequence[] items = {context.getString(R.string.takeNewPhoto), context.getString(R.string.chooseFromGallery),
                    context.getString(R.string.remove), context.getString(R.string.cancel)};

            AlertDialog.Builder builder = new AlertDialog.Builder(context);

            builder.setItems(items, new DialogInterface.OnClickListener() {

                @Override
                public void onClick(DialogInterface dialog, int item) {
                    Boolean isSDPresent = Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
                    if (item == 0) {
                        if (isSDPresent)
                            getPhotoFromCamera(context);
                        else
                            Toast.makeText(context, context.getString(R.string.SDCardNotAvailable), Toast.LENGTH_SHORT).show();

                        return;
                    } else if (item == 1) {
                        if (isSDPresent)
                            getPhotoFromGallery(context);
                        else
                            Toast.makeText(context, context.getString(R.string.SDCardNotAvailable), Toast.LENGTH_SHORT).show();
                        return;

                    } else if (item == 2) {
                        imagePath = null;
                        return;

                    } else
                        alertDialog.cancel();
                }

            });

            alertDialog = builder.create();
            alertDialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }


    }

  /*  protected void getPhotoFromCamera(Context context) {
        {
            try {
                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                try {
                    currentImageUri = createImageFile();
                    SharedPreferences.Editor editor = context.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE).edit();
                    editor.putString("currentImageUri", String.valueOf(currentImageUri));
                    editor.commit();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, currentImageUri);
                } else {
                    File file = new File(currentImageUri.getPath());
                    Uri photoUri = FileProvider.getUriForFile(context, context.getPackageName() + ".provider", file);
                    intent.putExtra(MediaStore.EXTRA_OUTPUT, photoUri);
                }
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                if (intent.resolveActivity(context.getPackageManager()) != null) {
                    Activity activity = (Activity) context;
                    activity.startActivityForResult(intent, SELECT_FILE_CAMERA);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }*/


    private void getPhotoFromCamera(Context context) {
        try {
            Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            // Ensure that there's a camera activity to handle the
            // intent
            if (takePictureIntent
                    .resolveActivity(context.getPackageManager()) != null) {
                // Create the File where the photo should go
                try {
                    currentImageUri = createImageFile();
                    //Save in shared preference
                    SharedPreferences.Editor editor = context.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE).edit();
                    editor.putString("currentImageUri", String.valueOf(currentImageUri));
                    editor.commit();
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
                // Continue only if the File was successfully
                // created
                if (currentImageUri != null) {
                    Activity activity = (Activity) context;
                    takePictureIntent.putExtra(
                            MediaStore.EXTRA_OUTPUT,
                            currentImageUri);
                    activity.startActivityForResult(takePictureIntent,
                            SELECT_FILE_CAMERA);

                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    protected void getPhotoFromGallery(Context context) {
        try {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            Activity activity = (Activity) context;
            activity.startActivityForResult(Intent.createChooser(intent, context.getString(R.string.selectImageToUpload)), SELECT_FILE_GALLERY);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private Uri createImageFile() throws IOException {
        File image = null;
        try {
            // Create an image file name
            String timeStamp = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            File storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);

            if (storageDir.exists()) {
                image = File.createTempFile(timeStamp, /* prefix */
                        ".jpg", /* suffix */
                        storageDir /* directory */
                );
            } else {
                storageDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM);
                if (storageDir.exists()) {
                    image = File.createTempFile(timeStamp, /* prefix */
                            ".jpg", /* suffix */
                            storageDir /* directory */
                    );
                }
            }
            // Save a file: path for use with ACTION_VIEW intents
            mCurrentPhotoPath = image.getAbsolutePath();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return Uri.fromFile(image);
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    private String getRealPathFromURI(Context context, Uri contentURI) {

        String filePath = "";
        Cursor cursor = null;
        try {
            final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
            String[] column = {MediaStore.Images.Media.DATA};
            if (isKitKat
                    && DocumentsContract.isDocumentUri(context,
                    contentURI)) {
                // ExternalStorageProvider
                if (isExternalStorageDocument(contentURI)) {
                    final String docId = DocumentsContract
                            .getDocumentId(contentURI);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    if ("primary".equalsIgnoreCase(type)) {
                        filePath = Environment.getExternalStorageDirectory()
                                + "/" + split[1];
                    } else {
                        File file = Environment.getExternalStorageDirectory();
                        String fileExtSDPath = System
                                .getenv("SECONDARY_STORAGE");
                        if ((null == fileExtSDPath)
                                || (fileExtSDPath.length() == 0)) {
                            fileExtSDPath = System
                                    .getenv("EXTERNAL_SDCARD_STORAGE");
                        }
                        filePath = file + "/" + split[1];
                    }
                }
                // if (Build.VERSION.SDK_INT >= 19)
                else if (isDownloadsDocument(contentURI)) {
                    String wholeID = DocumentsContract
                            .getDocumentId(contentURI);
                    String[] id = wholeID.split(":");
                    final String type = id[0];
                    final Uri contentUri = ContentUris.withAppendedId(
                            Uri.parse("content://downloads/public_downloads"),
                            Long.valueOf(wholeID));
                    cursor = getContentResolver().query(contentUri, column,
                            null, null, null);
                    int columnIndex = cursor.getColumnIndex(column[0]);
                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                } else if (isMediaDocument(contentURI)) {
                    final String docId = DocumentsContract
                            .getDocumentId(contentURI);
                    final String[] split = docId.split(":");
                    final String type = split[0];
                    String id = docId.split(":")[1];
                    String sel = MediaStore.Images.Media._ID + "=?";
                    Uri contentUri = null;
                    if ("image".equals(type)) {
                        cursor = getContentResolver().query(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                                column, sel, new String[]{id}, null);
                    } else if ("video".equals(type)) {
                        contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                        cursor = getContentResolver().query(contentUri, column,
                                null, null, null);
                    } else if ("audio".equals(type)) {
                        contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                        cursor = getContentResolver().query(contentUri, column,
                                null, null, null);
                    }
                    int columnIndex = cursor.getColumnIndex(column[0]);
                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }
            } else {
                cursor = context.getContentResolver().query(contentURI, column, null, null, null);
                if (cursor == null) { // Source is Dropbox or other similar local file path
                    filePath = contentURI.getPath();
                } else {
                    int columnIndex = cursor.getColumnIndex(column[0]);
                    if (cursor.moveToFirst()) {
                        filePath = cursor.getString(columnIndex);
                    }
                    cursor.close();
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return filePath;
    }

    public void activityResult(Activity context, String screenName, TextView location, int requestCode, int resultCode, Intent data) {
        try {
            Log.i(TAG, "result code" + resultCode);
            Log.i(TAG, "request code" + requestCode);

            if (resultCode == RESULT_OK) {
                Log.i(TAG, "result code ok");
                if (requestCode == SELECT_FILE_GALLERY) {
                    try {
                        selectedImageUri = data.getData();
                        imagePath=getRealPathFromURI(context,selectedImageUri);
                        //decodeFile(context, screenName, imagePath);
                        performCrop(context, screenName, selectedImageUri);
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.i(TAG, "Gallery pick exception" + ex.getMessage());
                        ErrorDialog(context, null, context.getString(R.string.internal_error), null, true);
                    }
                } else if (requestCode == SELECT_FILE_CAMERA) {
                    try {
                        app_preference = context.getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
                        String currentUri = app_preference.getString("currentImageUri", "");
                        currentImageUri = Uri.parse(currentUri);
                        galleryAddPic();
                        if (mCurrentPhotoPath == null) {
                            imagePath = currentImageUri.getPath();
                        } else
                            imagePath = mCurrentPhotoPath;

                        decodeFile(context, screenName, imagePath);
                        /*if (currentImageUri != null) {
                            performCrop(context, screenName, currentImageUri);
                        } else {
                            return;
                        }*/
                    } catch (Exception e) {
                        utility.ShowToast(context, context.getString(R.string.internal_error));
                    }
                } else if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
                    try {
                        CropImage.ActivityResult result = CropImage.getActivityResult(data);
                        if (resultCode == RESULT_OK) {
                            GraphicsUtil graphicUtil = null;
                            Uri resultUri = result.getUri();
                            try {
                                Bitmap thePic = MediaStore.Images.Media.getBitmap(context.getContentResolver(), resultUri);
                                Calendar date = Calendar.getInstance();
                                String filename = date.getTimeInMillis() + ".jpg";
                                imagePath = Environment.getExternalStorageDirectory().toString() + "/Xampr/Xampr Images/" + filename;
                                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                                thePic.compress(Bitmap.CompressFormat.JPEG, 95, stream);
                                utility.SaveImageGallery(thePic, null, filename, null);
                                File file = new File(imagePath);
                                if (file.exists()) {
                                    Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                                    SetIconToReferenceActivity(context, screenName, bitmap, imagePath);
                                } else {
                                    decodeFile(context, screenName, imagePath);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }

                        } else if (resultCode == CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE) {
                            Exception error = result.getError();
                        }
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
                /*else if (requestCode == PIC_CROP) {
                    Bitmap thePic = null;

                    try {
                        // get the returned data
                        Bundle extras = data.getExtras();
                        // get the cropped bitmap`
                        if (extras != null) {
                            thePic = extras.getParcelable("data");
                            String dateformat = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
                            String filename = "XP" + dateformat + ".jpg";
                            imagePath = Environment.getExternalStorageDirectory().toString() + "/Xampr/Xampr Images/" + filename;
                            ByteArrayOutputStream stream = new ByteArrayOutputStream();
                            thePic.compress(Bitmap.CompressFormat.JPEG, 95, stream);
                            mainActivity.SaveImageGallery(thePic, null, filename, null);
                            File file = new File(imagePath);
                            if (file.exists()) {
                                Bitmap bitmap = BitmapFactory.decodeFile(imagePath);
                                SetIconToReferenceActivity(context, screenName, bitmap, imagePath);
                            }
                        } else {
                            decodeFile(context, screenName, imagePath);
                        }


                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Log.i(TAG, "Crop exception" + ex.getMessage());
                    } catch (OutOfMemoryError ex) {
                        ex.printStackTrace();
                        Log.i(TAG, "Crop exception" + ex.getMessage());
                    }
                }*/
                else if (requestCode == SELECT_PLACE_PICKER) {
                    try {
                        final Place place = PlacePicker.getPlace(data, this);
                        final CharSequence name = place.getName();
                        final CharSequence address = place.getAddress();
                        if (address != null && address.length() > 0)
                            location.setText(address + "#" + place.getLatLng());

                        Log.i(TAG, "complete address" + place.getName() + "/" + place.getAddress() + "/" + place.getLatLng());
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }

                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    private void performCrop(Activity context, String screenName, Uri tempUri) {
        try {
            CropImage.activity(tempUri)
                    .setAspectRatio(1, 1)
                    .start(context);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void decodeFile(Context context, String screenName, String filePath) {
        Bitmap bitmap = null;
        float imgRatio = 0;
        try {
            bitmap = null;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            Bitmap bmp = BitmapFactory.decodeFile(filePath, options);

            int actualHeight = options.outHeight;
            int actualWidth = options.outWidth;
            float maxHeight = 816.0f;
            float maxWidth = 612.0f;
            if (actualHeight != 0)
                imgRatio = actualWidth / actualHeight;
            float maxRatio = maxWidth / maxHeight;

            if (actualHeight > maxHeight || actualWidth > maxWidth) {
                if (imgRatio < maxRatio) {
                    imgRatio = maxHeight / actualHeight;
                    actualWidth = (int) (imgRatio * actualWidth);
                    actualHeight = (int) maxHeight;
                } else if (imgRatio > maxRatio) {
                    imgRatio = maxWidth / actualWidth;
                    actualHeight = (int) (imgRatio * actualHeight);
                    actualWidth = (int) maxWidth;
                } else {
                    actualHeight = (int) maxHeight;
                    actualWidth = (int) maxWidth;

                }
            }

            options.inSampleSize = utility.calculateInSampleSize(options, actualWidth, actualHeight);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            options.inPurgeable = true;
            options.inInputShareable = true;
            options.inTempStorage = new byte[16 * 1024];

            try {
                bmp = BitmapFactory.decodeFile(filePath, options);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();

            }
            try {
                bitmap = Bitmap.createBitmap(actualWidth, actualHeight, Bitmap.Config.ARGB_8888);
            } catch (OutOfMemoryError exception) {
                exception.printStackTrace();
            }

            float ratioX = actualWidth / (float) options.outWidth;
            float ratioY = actualHeight / (float) options.outHeight;
            float middleX = actualWidth / 2.0f;
            float middleY = actualHeight / 2.0f;

            Matrix scaleMatrix = new Matrix();
            scaleMatrix.setScale(ratioX, ratioY, middleX, middleY);

            Canvas canvas = new Canvas(bitmap);
            canvas.setMatrix(scaleMatrix);
            canvas.drawBitmap(bmp, middleX - bmp.getWidth() / 2, middleY - bmp.getHeight() / 2, new Paint(Paint.FILTER_BITMAP_FLAG));


            ExifInterface exif;
            try {
                exif = new ExifInterface(filePath);

                int orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 0);
                Log.i("EXIF", "Exif: " + orientation);
                Matrix matrix = new Matrix();
                if (orientation == 6) {
                    matrix.postRotate(90);
                    Log.i("EXIF", "Exif: " + orientation);
                } else if (orientation == 3) {
                    matrix.postRotate(180);
                    Log.i("EXIF", "Exif: " + orientation);
                } else if (orientation == 8) {
                    matrix.postRotate(270);
                    Log.i("EXIF", "Exif: " + orientation);
                }
                bitmap = Bitmap.createBitmap(bitmap, 0, 0, bitmap.getWidth(), bitmap.getHeight(), matrix, true);
            } catch (IOException e) {
                e.printStackTrace();
            }
            FileOutputStream out = null;
            try {
                out = new FileOutputStream(filePath);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, out);

                //GraphicsUtil graphicUtil = new GraphicsUtil();
                //Bitmap cropedBitmap = graphicUtil.getCircleBitmap(bitmap, 14);
                SetIconToReferenceActivity(context, screenName, bitmap, filePath);
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void galleryAddPic() {
        try {
            Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
            mediaScanIntent.setData(currentImageUri);
            this.sendBroadcast(mediaScanIntent);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    @Override
    protected void onStart() {
        super.onStart();
        imagePath = "";
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        try {
            imagePath = "";
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        imagePath = "";
    }


    public void checkPermission(Context context, String[] permissions, boolean isGallery) {
        boolean permissionType = false;
        try {
            PermissionsChecker checker = new PermissionsChecker(context);
            if (isGallery && IsNeverAskAgainPermission(context, context.getString(R.string.cameraNeverAskAgain))) {
                try {
                    Activity activity = (Activity) context;
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", context.getPackageName(), null);
                    intent.setData(uri);
                    activity.startActivity(intent);
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                if (checker.lacksPermissions(permissions)) {
                    requestPermissions(context, permissions);
                } else {
                    ShowDialogOptionForMidiaPick(context);
                    //allPermissionsGranted();
                }
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void requestPermissions(Context context, String... permissions) {
        try {
            Activity activity = (Activity) context;
            ActivityCompat.requestPermissions(activity, permissions, PERMISSION_REQUEST_CODE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void allPermissionsGranted() {
        finish();
    }

    public void activityPermissionsResult(Context context, int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        try {
            if (requestCode == PERMISSION_REQUEST_CODE) {
                //allPermissionsGranted();
                try {
                    for (int i = 0; i < permissions.length; i++) {
                        String permission = permissions[i];
                        int grantResult = grantResults[i];
                        if (grantResult == PackageManager.PERMISSION_DENIED) {
                            Activity activity = (Activity) context;
                            DetectUserChoiceOnPermission(activity, permission);
                        }
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            } else {
                showDeniedResponse(grantResults);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void showDeniedResponse(int[] grantResults) {
        try {
            if (grantResults.length > 1) {
                for (int i = 0; i < grantResults.length; i++) {
                    if (grantResults[i] != 0) {
                        utility.ShowToast(this, getString(R.string.permission_unavailable, permissionFeatures.values()[i]));
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private boolean hasAllPermissionsGranted(@NonNull int[] grantResults) {
        try {
            for (int grantResult : grantResults) {
                if (grantResult == PackageManager.PERMISSION_DENIED) {
                    return false;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return true;
    }

    private void DetectUserChoiceOnPermission(Activity activity, String permission) {
        try {
            boolean showRationale = false;
            switch (permission) {
                case Manifest.permission.CAMERA:
                    showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
                    if (!showRationale) {
                        // user also CHECKED "never ask again"
                        // you can either enable some fall back,
                        // again the permission and directing to
                        // the app setting
                        SetToSharePreference(activity, activity.getString(R.string.cameraNeverAskAgain), true);
                    }
                    break;
                case Manifest.permission.WRITE_EXTERNAL_STORAGE:
                    showRationale = ActivityCompat.shouldShowRequestPermissionRationale(activity, permission);
                    if (!showRationale) {
                        // user also CHECKED "never ask again"
                        // you can either enable some fall back,
                        // again the permission and directing to
                        // the app setting
                        SetToSharePreference(activity, activity.getString(R.string.cameraNeverAskAgain), true);
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }


    public void SetToSharePreference(Activity activity, String key, boolean isBoolean) {
        try {
            SharedPreferences.Editor editor = activity.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE).edit();
            editor.putBoolean(key, isBoolean);
            editor.commit();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private boolean IsNeverAskAgainPermission(Context context, String key) {
        boolean isTrue = false;
        try {
            app_preference = context.getSharedPreferences(MyPREFERENCES, MODE_PRIVATE);
            isTrue = app_preference.getBoolean(key, false);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return isTrue;
    }


    public void ShowLocationMap(Activity activity) {
        try {
            boolean isLive = utility.isInternetConnected(activity);
            LocationManager locationManager = (LocationManager) activity.getSystemService(LOCATION_SERVICE);
            if (isLive) {
                if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                    try {
                        PlacePicker.IntentBuilder intentBuilder = new PlacePicker.IntentBuilder();
                        Intent intent = intentBuilder.build(activity);
                        activity.startActivityForResult(intent, SELECT_PLACE_PICKER);
                    } catch (GooglePlayServicesRepairableException | GooglePlayServicesNotAvailableException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                } else
                    AlertMessageNoGPS(activity);
            } else {
                Toast.makeText(activity, activity.getString(R.string.check_internet), Toast.LENGTH_SHORT).show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void AlertMessageNoGPS(final Activity activity) {
        try {
            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage(activity.getString(R.string.gpsDisableEnableText))
                    .setCancelable(false)
                    .setPositiveButton(activity.getString(R.string.statusYesBtn),
                            new DialogInterface.OnClickListener() {
                                public void onClick(
                                        @SuppressWarnings("unused") final DialogInterface dialog,
                                        @SuppressWarnings("unused") final int id) {
                                    activity.startActivity(new Intent(
                                            Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                                }
                            })
                    .setNegativeButton(activity.getString(R.string.statusNoBtn), new DialogInterface.OnClickListener() {
                        public void onClick(final DialogInterface dialog,
                                            @SuppressWarnings("unused") final int id) {
                            dialog.cancel();
                        }
                    });
            final AlertDialog alert = builder.create();
            alert.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void ShowDatePicker(final Activity activity, final TextView dateTimeTV) {
        String presetDateTime = "", presetDate = "";

        try {

            presetDateTime = dateTimeTV.getText().toString().trim();

            if (!StringUtils.isBlank(presetDateTime)) {
                if (StringUtils.contains(presetDateTime, ",")) {
                    String[] timeDate = presetDateTime.split(",");
                    presetDate = timeDate[0];
                    presetTime = timeDate[1];
                } else if (StringUtils.contains(presetDateTime, "AM") || StringUtils.contains(presetDateTime, "PM")) {
                    presetTime = presetDateTime;
                } else {
                    presetDate = presetDateTime;
                }
            }


            PreSetCalender(presetTime, presetDate, false);

            DatePickerDialog datePickerDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    myear = year;
                    mmonth = monthOfYear;
                    mday = dayOfMonth;
                    updateDate(activity, dateTimeTV, presetTime);
                }
            }, myear, mmonth, mday);
            //datePickerDialog.setTitle("Start Time");
            Calendar calendar = Calendar.getInstance();
            calendar.add(Calendar.DATE, 0); // Add 0 days to Calendar
            Date newDate = calendar.getTime();
            datePickerDialog.getDatePicker().setMinDate(newDate.getTime() - (newDate.getTime() % (24 * 60 * 60 * 1000)));
            datePickerDialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void ShowTimePicker(Activity activity, final String currentSetDate, final TextView dateTimeTV, String existingTime) {
        try {
            PreSetCalender(existingTime, "", true);

            TimePickerDialog mTimePicker = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int mhour, int mmin) {
                    updateTime(dateTimeTV, currentSetDate, mhour, mmin);
                }
            }, mhour, mmin, false);//Yes 24 hour time
            //mTimePicker.setTitle("Start Time");
            mTimePicker.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateDate(Activity activity, TextView dateTimeTV, String existTime) {
        try {

            String currentSetDate = new StringBuilder().append(mday).append("-")
                    .append(mmonth + 1).append("-").append(myear).append(" ").toString().trim();

            ShowTimePicker(activity, currentSetDate, dateTimeTV, existTime);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // TODO Auto-generated method stub
    }

    private void updateTime(TextView dateTimeTV, String currentSetDate, int hours, int mins) {
        String timeSet = "", minutes = "";
        try {
            timeSet = AMorPM(hours);

            if (hours > 12) {
                hours -= 12;
            } else if (hours == 0) {
                hours += 12;
            }

            if (mins < 10)
                minutes = "0" + mins;
            else
                minutes = String.valueOf(mins);

            String time = new StringBuilder().append(hours).append(":")
                    .append(minutes).append(" ").append(timeSet).toString();

            dateTimeTV.setText(currentSetDate + " , " + time);

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public String AMorPM(int hours) {
        String amPM = "";
        try {
            if (hours > 12) {
                amPM = "PM";
            } else if (hours == 0) {
                amPM = "AM";
            } else if (hours == 12)
                amPM = "PM";
            else
                amPM = "AM";
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return amPM;
    }

    public void ShowDatePickerMeeting(final Activity activity, final TextView fromDateTimeTV,
                                      final TextView toDateTimeTV, final boolean isFromDateTime) {
        String presetDateTime = "", presetDate = "";
        try {
            if (isFromDateTime)
                presetDateTime = fromDateTimeTV.getText().toString().trim();
            else
                presetDateTime = toDateTimeTV.getText().toString().trim();

            if (!StringUtils.isBlank(presetDateTime)) {

                if (StringUtils.contains(presetDateTime, ",")) {
                    String[] timeDate = presetDateTime.split(",");
                    presetDate = timeDate[0];
                    presetTime = timeDate[1];
                } else if (StringUtils.contains(presetDateTime, "AM")
                        || StringUtils.contains(presetDateTime, "PM")) {
                    presetTime = presetDateTime;
                } else {
                    presetDate = presetDateTime;
                }
            }


            PreSetCalender(presetTime, presetDate, false);

            DatePickerDialog datePickerDialog = new DatePickerDialog(activity, new DatePickerDialog.OnDateSetListener() {
                @Override
                public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                    myear = year;
                    mmonth = monthOfYear;
                    mday = dayOfMonth;
                    updateDateMeeting(activity, fromDateTimeTV, toDateTimeTV, isFromDateTime, presetTime);
                }
            }, myear, mmonth, mday);
            //datePickerDialog.setTitle("Start Time");
            Calendar calendar = Calendar.getInstance();

            if (!isFromDateTime) {

                String startDate = "", startDateTime;
                startDateTime = fromDateTimeTV.getText().toString().trim();

                if (!StringUtils.isBlank(startDateTime)) {
                    if (StringUtils.contains(startDateTime, ",")) {
                        String[] timeDate = startDateTime.split(",");
                        startDate = timeDate[0];
                    }
                }

                PreSetCalender(presetTime, startDate, false);

                if (!StringUtils.isBlank(startDate)) {
                    try {
                        SimpleDateFormat dformat = new SimpleDateFormat("dd-MM-yyyy");
                        Date date = dformat.parse(startDate);
                        calendar.setTime(date);
                    } catch (ParseException ex) {
                        ex.printStackTrace();
                    } catch (Exception ex) {
                        ex.printStackTrace();
                    }
                }
                calendar.add(Calendar.DATE, 1);// Add 1 days to Calendar will date after current start date
            }


            Date newDate = calendar.getTime();
            datePickerDialog.getDatePicker().setMinDate(newDate.getTime() - (newDate.getTime() % (24 * 60 * 60 * 1000)));
            datePickerDialog.show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateDateMeeting(Activity activity, TextView fromDateTimeTV, TextView toDateTimeTV, boolean isFromDateTime, String existTime) {
        try {

            String currentSetDate = new StringBuilder().append(mday).append("-")
                    .append(mmonth + 1).append("-").append(myear).append(" ").toString().trim();

            ShowTimePickerMeeting(activity, currentSetDate, fromDateTimeTV, toDateTimeTV, isFromDateTime, existTime);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // TODO Auto-generated method stub
    }

    public void ShowTimePickerMeeting(final Activity activity, final String currentSetDate, final TextView fromDateTimeTV, final TextView toDateTimeTV, final boolean isFromDateTime, String existingTime) {
        try {
            PreSetCalender(existingTime, "", true);

            TimePickerDialog mTimePicker = new TimePickerDialog(activity, new TimePickerDialog.OnTimeSetListener() {
                @Override
                public void onTimeSet(TimePicker timePicker, int mhour, int mmin) {
                    updateTimeMeeting(activity, fromDateTimeTV, toDateTimeTV, isFromDateTime, currentSetDate, mhour, mmin);
                }
            }, mhour, mmin, false);//Yes 24 hour time
            //mTimePicker.setTitle("Start Time");
            mTimePicker.show();

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void updateTimeMeeting(Activity activity, TextView fromDateTimeTV, TextView toDateTimeTV, boolean isFromDateTime, String currentSetDate, int hours, int mins) {
        String timeSet = "", minutes = "";
        try {
            timeSet = AMorPM(hours);

            if (hours > 12) {
                hours -= 12;
            } else if (hours == 0) {
                hours += 12;
            }

            if (mins < 10)
                minutes = "0" + mins;
            else
                minutes = String.valueOf(mins);

            String time = new StringBuilder().append(hours).append(":")
                    .append(minutes).append(" ").append(timeSet).toString();

            if (isFromDateTime) {
                fromDateTimeTV.setText(currentSetDate + " , " + time);
            } else {
                ValidationForMeetingDates(activity, fromDateTimeTV, toDateTimeTV, currentSetDate, time);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    private void ValidationForMeetingDates(Activity activity, TextView fromDateTimeTV, TextView toDateTimeTV,
                                           String currentSetDate, String time) {
        try {
            String startDateTime = "";
            startDateTime = fromDateTimeTV.getText().toString().trim();

            if (StringUtils.isBlank(startDateTime)) {
                utility.ShowToast(activity, activity.getString(R.string.check_internet));
            } else {
                SimpleDateFormat dformat = new SimpleDateFormat("dd-MM-yyyy , h:mm a");
                String toDateTime = currentSetDate + " , " + time;

                try {
                    long fromTimeInmills = dformat.parse(startDateTime).getTime();
                    long toTimeInmills = dformat.parse(toDateTime).getTime();

                    if (toTimeInmills < fromTimeInmills) {
                        utility.ShowToast(activity, activity.getString(R.string.check_internet));
                    } else if (toTimeInmills == fromTimeInmills) {
                        utility.ShowToast(activity, activity.getString(R.string.check_internet));
                    } else {
                        toDateTimeTV.setText(toDateTime);
                    }

                } catch (ParseException ex) {
                    ex.printStackTrace();
                } catch (Exception ex) {
                    ex.printStackTrace();
                }

            }

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public void PreSetCalender(String existTime, String existDate, boolean isTime) {
        try {
            Calendar caln = Calendar.getInstance();

            if (isTime) {
                if (!StringUtils.isBlank(existTime)) {
                    SimpleDateFormat tFormat = new SimpleDateFormat("h:mm a");
                    Date date = tFormat.parse(existTime);
                    caln.setTime(date);
                }
            } else {
                if (!StringUtils.isBlank(existDate)) {
                    SimpleDateFormat dFormat = new SimpleDateFormat("dd-MM-yyyy");
                    Date date = dFormat.parse(existDate);
                    caln.setTime(date);
                }
            }

            myear = caln.get(Calendar.YEAR);
            mmonth = caln.get(Calendar.MONTH);
            mday = caln.get(Calendar.DAY_OF_MONTH);
            mhour = caln.get(Calendar.HOUR_OF_DAY);
            mmin = caln.get(Calendar.MINUTE);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        // TODO Auto-generated method stub
    }


    private enum permissionFeatures {
        CAMERA
    }


    public void ErrorDialog(final Context context, final String actID, String toastMessage, final String screenName, boolean isToast) {
        AlertDialog.Builder builder;
        try {
            if (isToast) {
                Toast.makeText(context, toastMessage, Toast.LENGTH_SHORT).show();
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    builder = new android.app.AlertDialog.Builder(context, R.style.DialogStyle);
                } else {
                    builder = new android.app.AlertDialog.Builder(context);
                }
                builder.setTitle("");
                builder.setMessage(toastMessage);
                //builder.setIcon(R.drawable.zibmiicon);


                builder.setPositiveButton(context.getString(R.string.retry),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int id) {
                                try {
                                    dialog.cancel();
                                   /* if (!StringUtils.isBlank(screenName) && screenName.equalsIgnoreCase(context.getString(R.string.activityInformation))) {
                                        ActivityInformation activityInformation = (ActivityInformation) context;
                                        activityInformation.refreshAfterParticipantRemove(false);
                                    }*/
                                } catch (Exception ex) {
                                    ex.printStackTrace();
                                }

                            }

                        });

                builder.setNegativeButton(context.getString(R.string.ok),
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


    private void SetIconToReferenceActivity(Context context, String screenName, Bitmap bitmap, String filePath) {
        try {
            if (!StringUtils.isBlank(screenName) && screenName.equalsIgnoreCase(context.getString(R.string.placePickerActivity))) {
                PlacePickerActivity placePickerActivity = (PlacePickerActivity) context;
                placePickerActivity.SetActivityIcon(bitmap, filePath);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }


}
