package eu.imind.imindlib.videoplayer.media;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.KeyEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;

import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ui.AspectRatioFrameLayout.ResizeMode;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.PlaybackControlView.SeekDispatcher;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;
import com.google.android.exoplayer2.ui.SubtitleView;

import eu.imind.imindlib.videoplayer.R;

import static com.google.android.exoplayer2.C.TIME_UNSET;
import static com.google.android.exoplayer2.ExoPlaybackException.TYPE_SOURCE;

/**
 * A {@link com.google.android.exoplayer2.ui.SimpleExoPlayerView} (final class) wrapper.
 *
 * It can be customized as {@link com.google.android.exoplayer2.ui.SimpleExoPlayerView}:
 * by setting attributes (or calling corresponding methods),
 * overriding the view's layout file or by specifying a custom view layout file.
 *
 * To customize the layout of VideoPlayerView throughout your app, or just for certain
 * configurations, you can define {@code exo_simple_player_view.xml} and/or
 * {@code exo_playback_control_view.xml} layout files in your application {@code res/layout*}
 * directories.
 *
 * An example, how to embed:
 * <pre>
 *      <eu.imind.imindlib.videoplayer.media.VideoPlayerView
 *          android:id="@id/exo_video_player"
 *          android:layout_width="match_parent"
 *          android:layout_height="wrap_content" />
 * </pre>
 *
 * @see <a href="https://goo.gl/zqyvsF">SimpleExoPlayerView Reference</a>
 */
public final class VideoPlayerView extends FrameLayout {

    private static final String SAVED_SUPER_STATE = "super_state";
    private static final String SAVED_IS_PLAYING = "is_playing";
    private static final String SAVED_POSITION = "position";

    public interface OnClickRetryListener {
        void onClickRetry(VideoPlayer player);
    }

    private VideoPlayer mVideoPlayer;
    private OnClickRetryListener mRetryButtonListener;

    private final SimpleExoPlayerView mSEW;
    private final RelativeLayout mOverlayLayout;

    public VideoPlayerView(Context context) {
        this(context, null);
    }

    public VideoPlayerView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public VideoPlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        mSEW = new SimpleExoPlayerView(context, attrs, defStyleAttr);
        mOverlayLayout = (RelativeLayout) mSEW.findViewById(R.id.exo_overlay_layout);

        addView(mSEW);
        setupOverlay();
    }

    protected VideoPlayer getVideoPlayer() {
        return mVideoPlayer;
    }

    protected void setVideoPlayer(VideoPlayer videoPlayer) {
        mVideoPlayer = videoPlayer;
    }

    @Override
    protected Parcelable onSaveInstanceState() {
        Bundle bundle = new Bundle();
        bundle.putParcelable(SAVED_SUPER_STATE, super.onSaveInstanceState());
        if (mVideoPlayer != null) {
            bundle.putBoolean(SAVED_IS_PLAYING, mVideoPlayer.isPlaying());
            bundle.putLong(SAVED_POSITION, mVideoPlayer.getPlaybackPosition());
        }
        return bundle;
    }

    @Override
    protected void onRestoreInstanceState(Parcelable state) {
        if (state instanceof Bundle) {
            Bundle bundle = (Bundle) state;
            if (mVideoPlayer != null) {
                mVideoPlayer.seekTo(bundle.getLong(SAVED_POSITION, TIME_UNSET),
                        bundle.getBoolean(SAVED_IS_PLAYING, false));
            }
            state = bundle.getParcelable(SAVED_SUPER_STATE);
        }
        super.onRestoreInstanceState(state);
    }

    @Override
    protected void dispatchSaveInstanceState(SparseArray<Parcelable> container) {
        dispatchFreezeSelfOnly(container);
    }

    @Override
    protected void dispatchRestoreInstanceState(SparseArray<Parcelable> container) {
        dispatchThawSelfOnly(container);
    }

    public void setOnClickRetryListener(OnClickRetryListener listener) {
        mRetryButtonListener = listener;
    }

    private final ExoPlayerEventListenerAdapter mELA = new ExoPlayerEventListenerAdapter() {
        @Override
        public void onPlayerError(ExoPlaybackException error) {
            if (error.type == TYPE_SOURCE && mOverlayLayout != null) {
                mOverlayLayout.setVisibility(VISIBLE);
            }
        }
    };

    private void setupOverlay() {
        View retryButton = mOverlayLayout != null
                ? mOverlayLayout.findViewById(R.id.exo_overlay_retry_button) : null;
        if (retryButton != null) {
            retryButton.setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    mOverlayLayout.setVisibility(GONE);
                    if (mVideoPlayer != null && mRetryButtonListener != null) {
                        mRetryButtonListener.onClickRetry(mVideoPlayer);
                    }
                }
            });
        }
    }

    /**
     * Bridging {@link com.google.android.exoplayer2.ui.SimpleExoPlayerView} methods.
     */

    protected SimpleExoPlayer getPlayer() {
        return mSEW.getPlayer();
    }

    protected void setPlayer(SimpleExoPlayer player) {
        if (getPlayer() != null) {
            getPlayer().removeListener(mELA);
        }
        if (player != null) {
            player.addListener(mELA);
        }
        mSEW.setPlayer(player);
    }

    public void setResizeMode(@ResizeMode int resizeMode) {
        mSEW.setResizeMode(resizeMode);
    }

    public boolean getUseArtwork() {
        return mSEW.getUseArtwork();
    }

    public void setUseArtwork(boolean useArtwork) {
        mSEW.setUseArtwork(useArtwork);
    }

    public Bitmap getDefaultArtwork() {
        return mSEW.getDefaultArtwork();
    }

    public void setDefaultArtwork(Bitmap defaultArtwork) {
        mSEW.setDefaultArtwork(defaultArtwork);
    }

    public boolean getUseController() {
        return mSEW.getUseController();
    }

    public void setUseController(boolean useController) {
        mSEW.setUseController(useController);
    }

    public boolean dispatchMediaKeyEvent(KeyEvent event) {
        return mSEW.dispatchMediaKeyEvent(event);
    }

    public void showController() {
        mSEW.showController();
    }

    public void hideController() {
        mSEW.hideController();
    }

    public int getControllerShowTimeoutMs() {
        return mSEW.getControllerShowTimeoutMs();
    }

    public void setControllerShowTimeoutMs(int controllerShowTimeoutMs) {
        mSEW.setControllerShowTimeoutMs(controllerShowTimeoutMs);
    }

    public void setControllerVisibilityListener(PlaybackControlView.VisibilityListener listener) {
        mSEW.setControllerVisibilityListener(listener);
    }

    public void setSeekDispatcher(SeekDispatcher seekDispatcher) {
        mSEW.setSeekDispatcher(seekDispatcher);
    }

    public void setRewindIncrementMs(int rewindMs) {
        mSEW.setRewindIncrementMs(rewindMs);
    }

    public void setFastForwardIncrementMs(int fastForwardMs) {
        mSEW.setFastForwardIncrementMs(fastForwardMs);
    }

    public View getVideoSurfaceView() {
        return mSEW.getVideoSurfaceView();
    }

    public RelativeLayout getOverlayFrameLayout() {
        return mOverlayLayout;
    }

    public SubtitleView getSubtitleView() {
        return mSEW.getSubtitleView();
    }

}
