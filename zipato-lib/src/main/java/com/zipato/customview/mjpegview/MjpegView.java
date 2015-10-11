/*
 *  Copyright (C) 2011-2015 Tri plus grupa d.o.o <info@3plus.hr>
 *  All rights reserved.
 */

/**
 *
 */
package com.zipato.customview.mjpegview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.util.AttributeSet;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import com.squareup.okhttp.Authenticator;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.apache.http.Header;
import org.apache.http.HttpRequest;
import org.apache.http.auth.Credentials;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.message.BasicHeader;

import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;

public class MjpegView extends SurfaceView implements SurfaceHolder.Callback {
    public final static int POSITION_UPPER_LEFT = 9;
    public final static int POSITION_UPPER_RIGHT = 3;
    public final static int POSITION_LOWER_LEFT = 12;
    public final static int POSITION_LOWER_RIGHT = 6;

    public final static int SIZE_STANDARD = 1;
    public final static int SIZE_BEST_FIT = 4;
    public final static int SIZE_FULLSCREEN = 8;
    private MjpegViewThread thread;
    private MjpegInputStream mjpegInputStream;
    private boolean showFps = false;
    private boolean mRun = false;
    private boolean surfaceDone;
    private Paint overlayPaint;
    private int overlayTextColor;
    private int overlayBackgroundColor;
    private int ovlPos;
    private int displayMode;
    private MjpegViewListener listener;
    private boolean suspending;
    private String url;
    private OkHttpClient okHttpClient;
    private Request request;

    public MjpegView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public MjpegView(Context context) {
        super(context);
        init();
    }

    public void setListener(MjpegViewListener listener) {
        this.listener = listener;
    }

    private void init() {
        getHolder().addCallback(this);
        setFocusable(true);
        overlayPaint = new Paint();
        overlayPaint.setTextAlign(Paint.Align.LEFT);
        overlayPaint.setTextSize(12);
        overlayPaint.setTypeface(Typeface.DEFAULT);
        overlayTextColor = Color.WHITE;
        overlayBackgroundColor = Color.BLACK;
        ovlPos = MjpegView.POSITION_LOWER_RIGHT;
        displayMode = MjpegView.SIZE_STANDARD;
    }

    public void startPlayback() {
        if (mRun)
            stopPlayback();
        mRun = true;
        thread = new MjpegViewThread(getHolder());
        thread.start();
    }

    public void resumePlayback() {
        if (suspending) {
            mRun = true;
            suspending = false;
            startPlayback();
        }
    }

    public boolean isSuspending() {
        return suspending;
    }

    public boolean ismRun() {
        return mRun;
    }

    public void stopPlayback() {
        if (mRun) {
            suspending = true;
            if (thread != null) {
                try {
                    thread.setCancelThread(true);
                    thread.join();
                } catch (Exception e) {
                    //Empty
                }
                thread = null;
            }
        }
        mRun = false;
    }

    public void surfaceDestroyed(SurfaceHolder holder) {
        surfaceDone = false;
        stopPlayback();
        holder.removeCallback(this);
    }

    public void surfaceCreated(SurfaceHolder holder) {
        surfaceDone = true;
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

    }

    public void showFps(boolean b) {
        showFps = b;
    }

    public void setSource(String url) {
        this.url = url;
        startPlayback();
    }

    public void setOverlayPaint(Paint p) {
        overlayPaint = p;
    }

    public void setOverlayTextColor(int c) {
        overlayTextColor = c;
    }

    public void setOverlayBackgroundColor(int c) {
        overlayBackgroundColor = c;
    }

    public void setOverlayPosition(int p) {
        ovlPos = p;
    }

    public void setDisplayMode(int s) {
        displayMode = s;
    }

    public class MjpegViewThread extends Thread {
        private SurfaceHolder mSurfaceHolder;
        private int frameCounter = 0;
        private long start;
        private Bitmap ovl;
        private boolean cancelThread;

        public MjpegViewThread(SurfaceHolder surfaceHolder) {
            mSurfaceHolder = surfaceHolder;
        }

        public void setCancelThread(boolean cancelThread) {
            this.cancelThread = cancelThread;
        }

        private Rect destRect(int bmw, int bmh) {
            int tempx;
            int tempy;
            if (displayMode == MjpegView.SIZE_STANDARD) {
                tempx = (getWidth() / 2) - (bmw / 2);
                tempy = (getHeight() / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_BEST_FIT) {
                float bmasp = (float) bmw / (float) bmh;
                bmw = getWidth();
                bmh = (int) (getWidth() / bmasp);
                if (bmh > getHeight()) {
                    bmh = getHeight();
                    bmw = (int) (getHeight() * bmasp);
                }
                tempx = (getWidth() / 2) - (bmw / 2);
                tempy = (getHeight() / 2) - (bmh / 2);
                return new Rect(tempx, tempy, bmw + tempx, bmh + tempy);
            }
            if (displayMode == MjpegView.SIZE_FULLSCREEN)
                return new Rect(0, 0, getWidth(), getHeight());
            return null;
        }

        private Bitmap makeFpsOverlay(Paint p, String text) {
            Rect b = new Rect();
            p.getTextBounds(text, 0, text.length(), b);
            int bwidth = b.width() + 2;
            int bheight = b.height() + 2;
            Bitmap bm = Bitmap.createBitmap(bwidth, bheight, Bitmap.Config.ARGB_8888);
            Canvas c = new Canvas(bm);
            p.setColor(overlayBackgroundColor);
            c.drawRect(0, 0, bwidth, bheight, p);
            p.setColor(overlayTextColor);
            c.drawText(text, -b.left + 1, (bheight / 2) - ((p.ascent() + p.descent()) / 2) + 1, p);
            return bm;
        }

        public void run() {
            if (url == null) {
                if (listener != null)
                    listener.error();
                return;
            }
            if (listener != null)
                listener.onThreadStart();
            start = System.currentTimeMillis();
            PorterDuffXfermode mode = new PorterDuffXfermode(PorterDuff.Mode.DST_OVER);
            Bitmap bm = null;
            int width;
            int height;
            Rect destRect;
            Canvas c = null;
            Paint p = new Paint();
            String fps = "";
            InputStream is = null;

            try {
                if (okHttpClient == null)
                    okHttpClient = new OkHttpClient();

                okHttpClient.setAuthenticator(new Authenticator() {
                    @Override
                    public Request authenticate(Proxy proxy, Response response) throws IOException {
                        Log.d("MjpegView", "Need authentication");
                        final Header headerChallenge = new BasicHeader("WWW-Authenticate", response.header("WWW-Authenticate"));
                        final DigestScheme digestScheme = new DigestScheme();
                        Header headerResponse = null;
                        try {
                            digestScheme.processChallenge(headerChallenge);
                            Log.d("MjpegView", String.format("UserInfo: %s", response.request().url().getUserInfo()));
                            final String[] userInfo = response.request().url().getUserInfo().split(":");
                            final Credentials credentials = new UsernamePasswordCredentials(userInfo[0], userInfo[1]);
                            final HttpRequest httpGet = new HttpGet(response.request().urlString());
                            headerResponse = digestScheme.authenticate(credentials, httpGet);
                            Log.d("MjpegView", String.format("digestHear: name = %s Value = %s", headerResponse.getName(), headerResponse.getValue()));
                        } catch (Exception e) {
                            Log.d("MjpegView", "", e);
                        }
                        return response.request().newBuilder().header((headerResponse == null) ? "" : headerResponse.getName(), (headerResponse == null) ? "" : headerResponse.getValue()).build();
                    }

                    @Override
                    public Request authenticateProxy(Proxy proxy, Response response) throws IOException {
                        return null;
                    }
                });

                request = new Request.Builder().url(url).build();
                final Response response = okHttpClient.newCall(request).execute();
                is = response.body().byteStream();
                mjpegInputStream = new MjpegInputStream(is);
                while (mRun && !cancelThread) {
                    if (surfaceDone) {
                        try {

                            c = mSurfaceHolder.lockCanvas();
                            synchronized (mSurfaceHolder) {
                                try {
                                    bm = mjpegInputStream.readMjpegFrame();
                                    destRect = destRect(bm.getWidth(), bm.getHeight());
                                    c.drawColor(Color.BLACK);
                                    c.drawBitmap(bm, null, destRect, p);
                                    if (showFps) {
                                        p.setXfermode(mode);
                                        if (ovl != null) {
                                            height = ((ovlPos & 1) == 1) ? destRect.top : destRect.bottom - ovl.getHeight();
                                            width = ((ovlPos & 8) == 8) ? destRect.left : destRect.right - ovl.getWidth();
                                            c.drawBitmap(ovl, width, height, null);
                                        }
                                        p.setXfermode(null);
                                        frameCounter++;
                                        if ((System.currentTimeMillis() - start) >= 1000) {
                                            fps = frameCounter + " fps";
                                            frameCounter = 0;
                                            start = System.currentTimeMillis();
                                            ovl = makeFpsOverlay(overlayPaint, fps);
                                        }
                                    }
                                } catch (Exception e) {
                                    Log.d("MjpegView", "", e);
                                    mRun = false;
                                    if (listener != null)
                                        listener.error();
                                }
                            }
                        } finally {
                            if (c != null) {
                                mSurfaceHolder.unlockCanvasAndPost(c);
                                if ((listener != null))
                                    listener.success();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.d("MjpegView", "", e);
                if (listener != null)
                    listener.error();

            } finally {
                Log.d("MjpegView", "Trying to cancel current request");
                if (okHttpClient != null)
                    okHttpClient.cancel(request);
                if (is != null)
                    try {
                        Log.d("MjpegView", "Closing inputStream");
                        is.close();
                    } catch (IOException e) {
                        Log.d("MjpegView", "", e);
                    }
                if (mjpegInputStream != null) {
                    try {
                        Log.d("MjpegView", "Closing mjpegInputStream");
                        mjpegInputStream.close();
                    } catch (IOException e) {
                        Log.d("MjpegView", "", e);
                    }
                    mjpegInputStream = null;
                }
            }

        }
    }


}