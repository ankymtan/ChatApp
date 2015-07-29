package com.ankymtan.couplechat.entity;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.Display;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.util.Random;

import com.ankymtan.couplechat.framework.Themer;
import com.ankymtan.couplechat.framework.UserLocal;

/**
 * Created by An on 27/5/2015.
 */
public class Backgrounder extends SurfaceView implements Runnable {

    private final int EFFECT_TIME = 1000;
    private Bitmap background, heart1, heart2, heart3, heart4;
    private Bitmap resizedHeart2, resizedHeart3, resizedHeart4, resizedHeart1;
    private int previousNum = 0, currentNum = 0;
    private Random rand = new Random();
    private Matrix matrix = new Matrix();
    private int[] posXs = new int[100];
    private int[] posYs = new int[100];
    private int[] angles = new int[100];
    private int[] indice = new int[100];
    public float alpha = 0;
    private float startTime;
    private Themer themer;
    private Paint paint = new Paint();
    private volatile boolean running = false, isEnableBackground = true, isEnableAnimation = true;
    Thread thread = null;
    SurfaceHolder surfaceHolder;
    private UserLocal userLocal;

    @Override
    public void run() {

        surfaceHolder = getHolder();
        while (running) {
            if (alpha > 254) {
                continue;
            }
            if (surfaceHolder.getSurface().isValid()) {
                Canvas canvas = surfaceHolder.lockCanvas();
                //... actual drawing on canvas

                alpha += (System.nanoTime() / 1000000 - startTime) * 255 / EFFECT_TIME;
                startTime = System.nanoTime() / 1000000;
                if (alpha > 255) {
                    alpha = 255;
                }
                canvas.drawColor(Color.WHITE);


                if (isEnableBackground) {
                    canvas.drawBitmap(background, 0, 0, null);

                    if (isEnableAnimation) {
                        if (previousNum == currentNum) {
                            for (int i = 0; i < previousNum; i++) {
                                canvas.drawBitmap(getBitmap(indice[i]), posXs[i], posYs[i], null);
                            }
                            alpha = 255;
                        } else if (previousNum < currentNum) {
                            paint.setAlpha((int) alpha);

                            for (int i = 0; i < previousNum; i++) {
                                canvas.drawBitmap(getBitmap(indice[i]), posXs[i], posYs[i], null);
                            }

                            for (int i = previousNum; i < currentNum; i++) {
                                canvas.drawBitmap(getBitmap(indice[i]), posXs[i], posYs[i], paint);
                            }
                        } else if (previousNum > currentNum) {
                            paint.setAlpha((int) (255 - alpha));

                            for (int i = 0; i < currentNum; i++) {
                                canvas.drawBitmap(getBitmap(indice[i]), posXs[i], posYs[i], null);
                            }

                            for (int i = currentNum; i < previousNum; i++) {
                                canvas.drawBitmap(getBitmap(indice[i]), posXs[i], posYs[i], paint);
                            }
                        }
                    }
                }
                surfaceHolder.unlockCanvasAndPost(canvas);
            }
        }

    }

    public void onResumeMySurfaceView() {
        if (running) return;
        running = true;
        thread = new Thread(this);
        thread.start();
    }


    public void onPauseMySurfaceView() {
        if (!running) return;
        boolean retry = true;
        running = false;
        while (retry) {
            try {
                thread.join();
                retry = false;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public Backgrounder(Context context) {
        super(context);
    }

    public Backgrounder(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public Backgrounder(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void init(Activity activity) {

        userLocal = new UserLocal(activity);

        themer = new Themer(activity);
        background = themer.getBitmap("tree");
        heart1 = themer.getBitmap("heart1");
        heart2 = themer.getBitmap("heart2");
        heart3 = themer.getBitmap("heart3");
        heart4 = themer.getBitmap("heart4");
        //get width, height
        WindowManager wm = (WindowManager) activity.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int x = size.x;
        int y = size.y;

        resizedHeart1 = Bitmap.createScaledBitmap(heart1, x / 10, x / 10, false);
        resizedHeart2 = Bitmap.createScaledBitmap(heart2, x / 10, x / 10, false);
        resizedHeart3 = Bitmap.createScaledBitmap(heart3, x / 10, x / 10, false);
        resizedHeart4 = Bitmap.createScaledBitmap(heart4, x / 10, x / 10, false);
        //initialize positions of the Heart;
        for (int i = 0; i < 100; i++) {
            posXs[i] = x / 5 + rand.nextInt(x / 5 * 2);
            posYs[i] = y / 5 + rand.nextInt(y / 6);
            angles[i] = rand.nextInt(360);
            indice[i] = 1 + rand.nextInt(4);
        }
        startTime = System.nanoTime() / 1000000;
    }

    private Bitmap getBitmap(int heartIndex) {
        switch (heartIndex) {
            case 1:
                return resizedHeart1;
            case 2:
                return resizedHeart2;
            case 3:
                return resizedHeart3;
            case 4:
                return resizedHeart4;
            default:
                return resizedHeart1;
        }
    }

    private Bitmap rotatedBitmap(int angle, int heartIndex) {
        matrix.postRotate(angle);
        Bitmap rotatedBitmap;
        switch (heartIndex) {
            case 1:
                rotatedBitmap = Bitmap.createBitmap(resizedHeart1, 0, 0, resizedHeart1.getWidth(), resizedHeart1.getHeight(), matrix, true);
                matrix.postRotate(-angle);
                Bitmap.createBitmap(resizedHeart1, 0, 0, resizedHeart1.getWidth(), resizedHeart1.getHeight(), matrix, true);
                break;
            case 2:
                rotatedBitmap = Bitmap.createBitmap(resizedHeart2, 0, 0, resizedHeart2.getWidth(), resizedHeart2.getHeight(), matrix, true);
                matrix.postRotate(-angle);
                Bitmap.createBitmap(resizedHeart2, 0, 0, resizedHeart2.getWidth(), resizedHeart2.getHeight(), matrix, true);
                break;
            case 3:
                rotatedBitmap = Bitmap.createBitmap(resizedHeart3, 0, 0, resizedHeart3.getWidth(), resizedHeart3.getHeight(), matrix, true);
                matrix.postRotate(-angle);
                Bitmap.createBitmap(resizedHeart3, 0, 0, resizedHeart3.getWidth(), resizedHeart3.getHeight(), matrix, true);
                break;
            case 4:
                rotatedBitmap = Bitmap.createBitmap(resizedHeart4, 0, 0, resizedHeart4.getWidth(), resizedHeart4.getHeight(), matrix, true);
                matrix.postRotate(-angle);
                Bitmap.createBitmap(resizedHeart4, 0, 0, resizedHeart4.getWidth(), resizedHeart4.getHeight(), matrix, true);
                break;
            default:
                rotatedBitmap = Bitmap.createBitmap(resizedHeart1, 0, 0, resizedHeart1.getWidth(), resizedHeart1.getHeight(), matrix, true);
                matrix.postRotate(-angle);
                Bitmap.createBitmap(resizedHeart1, 0, 0, resizedHeart1.getWidth(), resizedHeart1.getHeight(), matrix, true);
                break;
        }
        return rotatedBitmap;
    }

    @Override
    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        init((Activity) this.getContext());
    }

    public void setNumOfHeart(int num) {
        previousNum = currentNum;
        currentNum = num;
    }

    public void startTime() {
        startTime = System.nanoTime() / 1000000;
    }

    public void resetAlpha() {
        alpha = 0;
    }

    public boolean isRunning() {
        return running;
    }

    public void update(int num) {
        isEnableAnimation = userLocal.getEnableAnimation();
        isEnableBackground = userLocal.getEnableBackground();
        //no update when the background is "onUpdate" alr
        if (alpha > 254) {
            setNumOfHeart(num);
            resetAlpha();
            startTime();
        }
    }

    public void update() {
        Log.d("by me", "updating");
        //no update when the background is "onUpdate" alr
        if (alpha > 254) {
            setNumOfHeart(currentNum);
            resetAlpha();
            startTime();
        }
    }

    public boolean onAnimation() {
        if (alpha < 255) {
            return true;
        }
        return false;
    }
}