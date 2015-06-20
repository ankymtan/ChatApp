package com.ankymtan.couplechat;

import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

/**
 * Created by An on 1/6/2015.
 */
public class Themer {

    final String PACKAGE_NAME = "ankymtan.com.myservice";
    private Resources resources;

    public Themer(Context context){
        try {
            PackageManager packageManager = context.getPackageManager();
            Context uContext = context.createPackageContext(PACKAGE_NAME, Context.CONTEXT_IGNORE_SECURITY);
            resources = uContext.getResources();
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Drawable getDrawable(String drawableName){
        int drawableId = resources.getIdentifier(drawableName, "drawable", PACKAGE_NAME);
        Drawable drawable = resources.getDrawable(drawableId);

        return drawable;
    }

    public Bitmap getBitmap (String drawableName) {
        Drawable drawable = getDrawable(drawableName);
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }
}
