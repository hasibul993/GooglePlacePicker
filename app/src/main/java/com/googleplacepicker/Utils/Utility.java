package com.googleplacepicker.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.googleplacepicker.R;

import org.apache.commons.lang3.StringUtils;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.DecimalFormat;

/**
 * Created by BookMEds on 01-12-2017.
 */

public class Utility {


    public int calculateInSampleSize(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        try {
            if (height > reqHeight || width > reqWidth) {
                final int heightRatio = Math.round((float) height / (float) reqHeight);
                final int widthRatio = Math.round((float) width / (float) reqWidth);
                inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
            }
            final float totalPixels = width * height;
            final float totalReqPixelsCap = reqWidth * reqHeight * 2;

            while (totalPixels / (inSampleSize * inSampleSize) > totalReqPixelsCap) {
                inSampleSize++;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return inSampleSize;
    }


    public boolean isInternetConnected(Context context) {
        boolean isConnected = false;
        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(context.CONNECTIVITY_SERVICE);
        if (connectivityManager != null) {
            NetworkInfo[] info = connectivityManager.getAllNetworkInfo();
            if (info != null) {
                for (int i = 0; i < info.length; i++) {
                    if (info[i].getState() == NetworkInfo.State.CONNECTED) {
                        isConnected = true;
                    }
                }
            }
        }
        return isConnected;
    }


    public static void ShowToast(Context context, String message) {
        try {
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

    }

    public void refreshScreen(Activity activity) {
        try {

            Intent intent = activity.getIntent();
            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
            activity.finish();
            activity.overridePendingTransition(0, 0);
            activity.startActivity(intent);
            activity.overridePendingTransition(0, 0);

        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public Bitmap GetBitmapFromFilePath(String photoPath) {
        Bitmap bitmap = null;
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            //options.inPreferredConfig = Bitmap.Config.ARGB_8888;
            options.inJustDecodeBounds = false;
            options.inPreferredConfig = Bitmap.Config.RGB_565;
            options.inDither = true;
            bitmap = BitmapFactory.decodeFile(photoPath, options);
        } catch (Exception ex) {
            ex.printStackTrace();
        } catch (OutOfMemoryError ex) {
            ex.printStackTrace();
        }
        return bitmap;
    }

    public void redirectToGoogleMap(Context context, String location) {
        Boolean isLive = isInternetConnected(context);
        if (isLive) {

            try {
                Log.i("tag", "result of location" + location);
                String latlang = location.split("#")[1];
                Log.i("latlang", "latlang values" + latlang.split(":")[1].replace(".map", ""));
                String finallatlang = latlang.split(":")[1].replace(".map", "");
                finallatlang = finallatlang.replace("(", "");
                finallatlang = finallatlang.replace(")", "");
                Log.i("final latlang", "final latlang" + finallatlang);
                String mTitle = location.split(",")[0];
                String geoUri = "http://maps.google.com/maps?q=loc:" + finallatlang.trim().split(",")[0] + "," + finallatlang.trim().split(",")[1] + " (" + mTitle + ")";
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(geoUri));
                context.startActivity(intent);
            } catch (Exception e) {
                e.printStackTrace();
            }


        } else
            ShowToast(context, context.getString(R.string.check_internet));
    }


    public double CalculationByDistance(Context context, LatLng StartP, LatLng EndP) {
        int Radius = 6371;// radius of earth in Km
        double c = 0;
        try {
            double lat1 = StartP.latitude;
            double lat2 = EndP.latitude;
            double lon1 = StartP.longitude;
            double lon2 = EndP.longitude;
            double dLat = Math.toRadians(lat2 - lat1);
            double dLon = Math.toRadians(lon2 - lon1);
            double a = Math.sin(dLat / 2) * Math.sin(dLat / 2)
                    + Math.cos(Math.toRadians(lat1))
                    * Math.cos(Math.toRadians(lat2)) * Math.sin(dLon / 2)
                    * Math.sin(dLon / 2);
            c = 2 * Math.asin(Math.sqrt(a));
            double valueResult = Radius * c;
            double km = valueResult / 1;
            DecimalFormat newFormat = new DecimalFormat("####");
            int kmInDec = Integer.valueOf(newFormat.format(km));
            double meter = valueResult % 1000;
            int meterInDec = Integer.valueOf(newFormat.format(meter));
            Log.i("Radius Value", "" + valueResult + "   KM  " + kmInDec
                    + " Meter   " + meterInDec);
        } catch (Exception ex) {
            ex.printStackTrace();
        }


        return Radius * c;
    }


    public String SaveImageGallery(Bitmap bitmap, byte[] byteArrayData, String filename, String imgActualPath) {
        File file = null, videoDirectory, AudioDirectory, galleryDirectory, directory, documentDirectory, profileDirectory;
        String root;
        Bitmap localBitMap;
        FileOutputStream fos;
        boolean isGallery = false, isOthers = false;
        try {
            root = Environment.getExternalStorageDirectory().toString();
            directory = new File(root + "/Xampr");
            if (!directory.exists())
                directory.mkdirs();
            videoDirectory = directory.getParentFile();
            AudioDirectory = directory.getParentFile();
            galleryDirectory = directory.getParentFile();
            videoDirectory = new File(directory + "/Xampr Videos");
            AudioDirectory = new File(directory + "/Xampr Audio");
            galleryDirectory = new File(directory + "/Xampr Images");
            documentDirectory = new File(directory + "/Xampr Documents");
            profileDirectory = new File(directory + "/ProfileImages");

            if (!videoDirectory.exists())
                videoDirectory.mkdirs();
            if (!AudioDirectory.exists())
                AudioDirectory.mkdirs();
            if (!galleryDirectory.exists())
                galleryDirectory.mkdirs();
            if (!documentDirectory.exists())
                documentDirectory.mkdirs();
            if (!profileDirectory.exists())
                profileDirectory.mkdirs();

            if (filename != null) {
                if (filename.toString().endsWith(".jpg")
                        || filename.toString().endsWith(".png")
                        || filename.toString().endsWith(".jpeg")) {

                    if (StringUtils.equalsIgnoreCase(filename, "profileImage.jpg"))
                        file = new File(profileDirectory, filename);
                    else
                        file = new File(galleryDirectory, filename);

                    isGallery = true;

                } else if (filename.toString().endsWith(".pdf")
                        || filename.toString().endsWith(".doc")
                        || filename.toString().endsWith(".docx")
                        || filename.toString().endsWith(".ppt")
                        || filename.toString().endsWith(".pptx")
                        || filename.toString().endsWith(".xls")
                        || filename.toString().endsWith(".xlsx")
                        || filename.toString().endsWith(".txt")) {
                    file = new File(documentDirectory, filename);
                    isOthers = true;
                } else if (filename.toString().endsWith(".mp3")
                        || filename.toString().endsWith(".ogg")
                        || filename.toString().endsWith(".m4a")
                        || filename.toString().endsWith(".amr")
                        || filename.toString().endsWith(".3gpp")) {
                    file = new File(AudioDirectory, filename);
                    isOthers = true;
                } else if (filename.toString().endsWith(".mp4")
                        || filename.toString().endsWith(".3gp")
                        || filename.toString().endsWith(".avi")
                        || filename.toString().endsWith(".mkv")) {
                    file = new File(videoDirectory, filename);
                    isOthers = true;
                }
            }
            if (file.exists())
                file.delete();
            try {
                fos = new FileOutputStream(file, false);
                if (isGallery) {
                    if (imgActualPath != null) {
                        localBitMap = BitmapFactory.decodeFile(imgActualPath);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        localBitMap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        byteArrayData = stream.toByteArray();
                        fos.write(byteArrayData);
                    } else if (byteArrayData != null) {
                        // bitmap = BitmapFactory.decodeFile(filpath);
                        ByteArrayOutputStream stream = new ByteArrayOutputStream();
                        // bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream);
                        // getByteArray = stream.toByteArray();
                        fos = new FileOutputStream(file, false);
                        fos.write(byteArrayData);
                        fos.flush();
                        fos.close();
                    } else
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 95, fos);
                } else if (isOthers) {
                    fos.write(byteArrayData);
                }
                fos.flush();
                fos.close();

                return galleryDirectory + "/" + filename;
            } catch (Exception e) {
                e.printStackTrace();
            } catch (OutOfMemoryError e) {
                e.printStackTrace();
            }
        } catch (Exception e) {
            e.printStackTrace();
            // TODO: handle exception
        }
        return "";
    }

}
