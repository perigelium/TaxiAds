package com.edisoninteractive.inrideads.Presenters;

import android.app.Activity;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.media.MediaPlayer;
import android.support.v4.content.res.ResourcesCompat;
import android.util.Log;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.VideoView;

import com.edisoninteractive.inrideads.Entities.GlobalConstants;
import com.edisoninteractive.inrideads.Entities.Tab;
import com.edisoninteractive.inrideads.EventHandlers.OnTouchDebouncedListener;
import com.edisoninteractive.inrideads.R;
import com.edisoninteractive.inrideads.Utils.FileUtils;

import static com.edisoninteractive.inrideads.Entities.GlobalConstants.APP_LOG_TAG;

/**
 * Created by Alex Angan on 29.01.2018
 */

public class TabLocalFileRenderer extends TabContentRenderer
{
    private RelativeLayout relativeLayout;
    private VideoView videoView;
    private ImageView ivPlay, ivImgPlaceHolder;
    private TextView tvErrorContentMessage;
    private String strPathToFile = "";

    public TabLocalFileRenderer(Activity activity, FrameLayout frameLayout, Tab tab)
    {
        super(frameLayout, tab);

        if (null == tab || null == tab.fileURL || tab.fileURL.isEmpty())
        {
            Log.d(APP_LOG_TAG, "Presenters/TabLocalFileRenderer: TabLocalFileRenderer() tab.fileURL = null/empty");
            return;
        }

        this.strPathToFile = GlobalConstants.DATA_PATH + tab.fileURL;

        initVideoView(activity);
        initPlayIcon(activity);
        initImagePlaceHolder(activity);
        initTextViewErrorMessage(activity);

        frameLayout.addView(relativeLayout);
    }

    private void initVideoView(Activity activity)
    {

        videoView = new VideoView(activity);

        relativeLayout = new RelativeLayout(activity);

        RelativeLayout.LayoutParams lprReal =
                new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        relativeLayout.setLayoutParams(lprReal);

        lprReal.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_TOP);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
        lprReal.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

        videoView.setLayoutParams(lprReal);

        videoView.setOnTouchListener(new OnTouchDebouncedListener()
        {
            @Override
            public boolean onTouched(View v, MotionEvent event)
            {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                {
                    if (videoView.isPlaying())
                    {
                        videoView.pause();
                        ivPlay.setVisibility(View.VISIBLE);
                    } else
                    {
                        ivPlay.setVisibility(View.INVISIBLE);
                        try
                        {
                            if (!videoView.isPlaying())
                            {
                                videoView.start();
                            }
                        } catch (Exception ex)
                        {
                            Log.d(APP_LOG_TAG, "Presenters/TabLocalFileRenderer: videoView cannot play this video, " + ex.getMessage());
                            ex.printStackTrace();
                        }
                    }
                }
                return false;
            }
        });

        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener()
        {
            @Override
            public boolean onError(MediaPlayer mediaPlayer, int what, int extra)
            {
                Log.w(APP_LOG_TAG, "Presenters/TabLocalFileRenderer: videoView onError() MediaPlayer error, this video cannot be played");

                videoView.stopPlayback();

                tvErrorContentMessage.setVisibility(View.VISIBLE);

                return true;
            }
        });

        relativeLayout.addView(videoView);
    }

    private void initPlayIcon(Activity activity)
    {
        ivPlay = new ImageView(activity);

        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        ivPlay.setLayoutParams(layoutParams);

        Drawable iconPlay = activity.getResources().getDrawable(R.drawable.icon_play);
        ivPlay.setImageDrawable(iconPlay);

        ivPlay.setVisibility(View.INVISIBLE);

        relativeLayout.addView(ivPlay);

        ivPlay.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ivPlay.setVisibility(View.INVISIBLE);
                try
                {
                    if (!videoView.isPlaying())
                    {
                        videoView.start();
                    }
                } catch (Exception ex)
                {
                    Log.d(APP_LOG_TAG, "Presenters/TabLocalFileRenderer: videoView cannot play this video, " + ex.getMessage());
                    ex.printStackTrace();
                }
            }
        });
    }

    private void initImagePlaceHolder(Activity activity)
    {
        ivImgPlaceHolder = new ImageView(activity);

        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        ivImgPlaceHolder.setLayoutParams(layoutParams);

        ivImgPlaceHolder.setScaleType(ImageView.ScaleType.FIT_CENTER);

        ivImgPlaceHolder.setVisibility(View.INVISIBLE);

        relativeLayout.addView(ivImgPlaceHolder);
    }

    private void initTextViewErrorMessage(Activity activity)
    {
        tvErrorContentMessage = new TextView(activity);

        RelativeLayout.LayoutParams layoutParams =
                new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);

        layoutParams.addRule(RelativeLayout.CENTER_IN_PARENT);

        tvErrorContentMessage.setLayoutParams(layoutParams);
        tvErrorContentMessage.setTextColor(ResourcesCompat.getColor(activity.getResources(), R.color.white, null));
        tvErrorContentMessage.setBackgroundColor(ResourcesCompat.getColor(activity.getResources(), R.color.black39, null));
        tvErrorContentMessage.setTextSize(35);
        tvErrorContentMessage.setTypeface(Typeface.DEFAULT_BOLD);
        String errorContentMessage = "Sorry, this content cannot be played now";
        tvErrorContentMessage.setText(errorContentMessage);
        tvErrorContentMessage.setVisibility(View.INVISIBLE);
        tvErrorContentMessage.setGravity(Gravity.CENTER);

        relativeLayout.addView(tvErrorContentMessage);
    }

    @Override
    void startRendering()
    {
        if (!strPathToFile.isEmpty())
        {
            Log.d(APP_LOG_TAG, "Presenters/TabLocalFileRenderer: startRendering() path = " + strPathToFile);

            boolean isImageFile = FileUtils.hasImageExtension(strPathToFile);

            if (isImageFile)
            {
                showImageInVideoView(strPathToFile);
            } else
            {
                ivImgPlaceHolder.setVisibility(View.INVISIBLE);
                videoView.setVisibility(View.VISIBLE);

                videoView.setVideoPath(strPathToFile); // start playing
                videoView.start();
                videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener()
                {
                    @Override
                    public void onCompletion(MediaPlayer mp)
                    {
                        videoView.seekTo(0);
                        ivPlay.setVisibility(View.VISIBLE);
                    }
                });
            }
        } else
        {
            Log.d(APP_LOG_TAG, "Presenters/TabLocalFileRenderer: startRendering() path is empty");
        }
    }

    @Override
    void stopRendering()
    {
        if (videoView != null)
        {
            videoView.setBackground(null);
            if (videoView.isPlaying())
            {
                videoView.stopPlayback();
                videoView.setVisibility(View.INVISIBLE);
            }
        }
        if (null != tvErrorContentMessage)
        {
            tvErrorContentMessage.setVisibility(View.INVISIBLE);
        }
    }

    private void showImageInVideoView(String strPathToFile)
    {
        Drawable d = Drawable.createFromPath(strPathToFile);

        if (d != null)
        {
            Log.d(APP_LOG_TAG, "Presenters/TabLocalFileRenderer: showImageInVideoView() path = " + strPathToFile);
            ivImgPlaceHolder.setImageDrawable(d);
            ivImgPlaceHolder.setVisibility(View.VISIBLE);
            videoView.setVisibility(View.INVISIBLE);
        } else
        {
            Log.d(APP_LOG_TAG, "Presenters/TabLocalFileRenderer: showImageInVideoView() drawable is null");
        }
    }
}
