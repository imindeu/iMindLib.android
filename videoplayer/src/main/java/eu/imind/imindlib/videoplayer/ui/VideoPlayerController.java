package eu.imind.imindlib.videoplayer.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import eu.imind.imindlib.videoplayer.media.VideoPlayer;
import eu.imind.imindlib.videoplayer.media.VideoPlayerView;
import okhttp3.OkHttpClient;

import static com.google.android.exoplayer2.ExoPlayer.STATE_READY;
import static eu.imind.imindlib.videoplayer.Utils.debug;

/**
 * This class helps you to easily initialize and handle states of {@link VideoPlayer} instances.
 *
 * Usage example:
 * - use one instance per activity
 * - initialize {@link VideoPlayer} instances in {@link AppCompatActivity#onCreate(Bundle)}
 *   by passing the proper the {@link VideoPlayerView} reference and url
 *   to the {@link #prepareVideoPlayer(VideoPlayerView, String)} method
 * - and {@link #release()} {@link VideoPlayer} instances in {@link AppCompatActivity#onDestroy()}
 */
public abstract class VideoPlayerController implements OkHttpClientProvider,
        VideoPlayerView.OnClickRetryListener, VideoPlayer.Listener {

    protected final Context mContext;
    protected final OkHttpClient mHttpClient;
    protected final List<VideoPlayer> mVideoPlayers;
    protected VideoPlayer mLastActiveVideoPlayer;

    public VideoPlayerController(@NonNull Context context) {
        mContext = context;
        mHttpClient = getOkHttpClient();
        mVideoPlayers = new ArrayList<>();
    }

    public void prepareVideoPlayer(@NonNull VideoPlayerView videoView, @NonNull String url) {
        prepareVideoPlayer(videoView, Uri.parse(url));
    }

    public void prepareVideoPlayer(@NonNull VideoPlayerView videoView, @NonNull Uri uri) {
        VideoPlayer videoPlayer = new VideoPlayer(mContext, mHttpClient, videoView);
        mVideoPlayers.add(videoPlayer);

        videoPlayer.setListener(this);
        videoPlayer.prepare(uri);

        videoView.setOnClickRetryListener(this);
    }

    public void pause() {
        pause(null);
    }

    protected void pause(VideoPlayer exclude) {
        for (VideoPlayer videoPlayer : mVideoPlayers) {
            if (!videoPlayer.equals(exclude)) {
                videoPlayer.pause();
            }
        }
    }

    public void mute() {
        for (VideoPlayer videoPlayer : mVideoPlayers) {
            if (videoPlayer.isPlaying()) {
                videoPlayer.adjustVolume(0);
                mLastActiveVideoPlayer = videoPlayer;
                return;
            }
        }
        mLastActiveVideoPlayer = null;
    }

    public void unmute() {
        if (mLastActiveVideoPlayer != null) {
            mLastActiveVideoPlayer.adjustVolume(1);
        }
    }

    public void release() {
        mLastActiveVideoPlayer = null;
        Iterator<VideoPlayer> it = mVideoPlayers.iterator();
        while (it.hasNext()) {
            it.next().release();
            it.remove();
        }
    }

    @Override
    public void onClickRetry(VideoPlayer player) {
        player.retry();
    }

    @Override
    public void onPlayerStateChanged(VideoPlayer player, int playbackState, boolean playWhenReady) {
        if (playbackState == STATE_READY && playWhenReady) {
            pause(player);
        }
    }

    @Override
    public void onPlayerError(VideoPlayer player, Exception exception, boolean whileLoadingSource) {
        debug(exception);
    }

}
