package com.qweex.openbooklikes;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v7.app.AlertDialog;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.ImageView;

import com.makeramen.roundedimageview.RoundedImageView;
import com.qweex.openbooklikes.activity.MainActivity;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URLEncoder;

public class DownloadableImageView extends RoundedImageView implements View.OnLongClickListener {
    String title, src;

    public DownloadableImageView(Context context) {
        super(context);
        setOnLongClickListener(this);
    }

    public DownloadableImageView(Context context, AttributeSet attrs) {
        super(context, attrs);
        setOnLongClickListener(this);
    }

    public DownloadableImageView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        setOnLongClickListener(this);
    }


    public void setSource(String title, String url) {
        this.title = title;
        this.src = url;
    }

    @Override
    public boolean onLongClick(View v) {
        if(src==null)
            return false;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle(title);
        builder.setItems(R.array.click_image, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent;
                String filename = title + src.substring(src.lastIndexOf("."));
                switch (which) { //heheheheheheh
                    case 0: // Save image
                        DownloadManager downloadManager = (DownloadManager)getContext().getSystemService(Context.DOWNLOAD_SERVICE);
                        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(src))
                                .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE)
                                .setAllowedOverRoaming(false)
                                .setTitle(filename)
                                .setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
                                .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                        getContext().getResources().getString(R.string.app_name) + "/" + filename
                                )
                                ;
                        long downloadReference = downloadManager.enqueue(request);
                    default:
                        return;
                    case 1: // Open image
                        intent = new Intent(Intent.ACTION_VIEW, Uri.parse(src));
                        break;
                    case 2: // Search Google
                        intent =new Intent(Intent.ACTION_VIEW, Uri.parse("http://www.google.com/searchbyiamge?image_url=" + URLEncoder.encode(src)));
                        break;
                    case 3: // Share
                        File from = new File(MainActivity.imageLoader.getDiskCache().get(src).getAbsolutePath());
                        Intent share = new Intent(Intent.ACTION_SEND);
                        share.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(from));
                        String mimetype = MimeTypeMap.getSingleton().getMimeTypeFromExtension(
                                MimeTypeMap.getFileExtensionFromUrl(src)
                        );
                        share.setType(mimetype);
                        intent = Intent.createChooser(share, "Share Image"); //TODO: String
                        break;
                }
                getContext().startActivity(intent);
            }
        });
        builder.show();
        return false;
    }
}
