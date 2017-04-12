package eu.imind.imindlib.videoplayer.media;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;

import com.google.android.exoplayer2.DefaultLoadControl;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.LoadControl;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.ext.okhttp.OkHttpDataSourceFactory;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.extractor.ExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.FixedTrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultAllocator;

import eu.imind.imindlib.videoplayer.BuildConfig;
import okhttp3.OkHttpClient;

import static com.google.android.exoplayer2.C.TIME_UNSET;
import static com.google.android.exoplayer2.ExoPlaybackException.TYPE_SOURCE;
import static eu.imind.imindlib.videoplayer.Utils.warn;

public final class VideoPlayer {

    private static final int BUFFER_SEGMENT_SIZE = 64 * 1024;
    private static final int MIN_BUFFER_MS = 15000;
    private static final int MAX_BUFFER_MS = 30000;
    private static final int BUFFER_FOR_PLAYBACK_MS = 2500;
    private static final int BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 5000;
    private static final int MIN_RETRY_COUNT_FOR_MEDIA = 2;

    public interface Listener {
        void onPlayerStateChanged(VideoPlayer player, int playbackState, boolean playWhenReady);
        void onPlayerError(VideoPlayer player, Exception exception, boolean whileLoadingSource);
    }

    private enum State {
        IDLE, PREPARING, READY, RELEASED
    }

    private final OkHttpClient mHttpClient;
    private final String mUserAgent;
    private State mCurrentState;
    private Listener mListener;

    private final SimpleExoPlayer mPlayer;
    private final VideoPlayerView mView;
    private MediaSource mMediaSource;

    public VideoPlayer(@NonNull Context context,
                       @NonNull OkHttpClient httpClient,
                       @NonNull VideoPlayerView view) {
        this(context, httpClient, view, BuildConfig.APPLICATION_ID + "/" + BuildConfig.VERSION_NAME);
    }

    public VideoPlayer(@NonNull Context context,
                       @NonNull OkHttpClient httpClient,
                       @NonNull VideoPlayerView view,
                       @NonNull String userAgent) {
        mHttpClient = httpClient;
        mUserAgent = userAgent;
        mCurrentState = State.IDLE;

        DefaultAllocator allocator = new DefaultAllocator(true, BUFFER_SEGMENT_SIZE);
        LoadControl loadControl = new DefaultLoadControl(allocator,
                MIN_BUFFER_MS, MAX_BUFFER_MS,
                BUFFER_FOR_PLAYBACK_MS, BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS);
        TrackSelector trackSelector = new DefaultTrackSelector(new FixedTrackSelection.Factory());

        mPlayer = ExoPlayerFactory.newSimpleInstance(context, trackSelector, loadControl);
        mPlayer.addListener(mELA);

        mView = view;
        if (mView.getVideoPlayer() != null) {
            mView.getVideoPlayer().release();
        }
        mView.setVideoPlayer(this);
        mView.setPlayer(mPlayer);
    }

    public void prepare(@NonNull Uri uri) {
        if (mCurrentState == State.RELEASED) {
            warn("Wrong state: player is already released");
            return;
        } else if (mCurrentState != State.IDLE) {
            mPlayer.stop();
        }

        mCurrentState = State.PREPARING;

        DataSource.Factory dataSourceFactory = new OkHttpDataSourceFactory(mHttpClient, mUserAgent, null);
        ExtractorsFactory extractorsFactory = new DefaultExtractorsFactory();
        mMediaSource = new ExtractorMediaSource(uri, dataSourceFactory, extractorsFactory,
                MIN_RETRY_COUNT_FOR_MEDIA, null, null, null);

        mPlayer.setPlayWhenReady(false);
        mPlayer.prepare(mMediaSource);

        mCurrentState = State.READY;
    }

    public void retry() {
        if (mCurrentState == State.RELEASED) {
            warn("Wrong state: player is already released");
            return;
        } else if (mCurrentState != State.IDLE) {
            warn("Wrong state: player is ready");
            return;
        } else if (mMediaSource == null) {
            warn("Missing media source");
            return;
        }

        mCurrentState = State.PREPARING;

        mPlayer.setPlayWhenReady(true);
        mPlayer.prepare(mMediaSource, true, false);

        mCurrentState = State.READY;
    }

    public void release() {
        if (mCurrentState == State.RELEASED) {
            warn("Wrong state: player is already released");
            return;
        } else if (mCurrentState != State.IDLE) {
            mPlayer.stop();
        }

        mCurrentState = State.RELEASED;

        mPlayer.removeListener(mELA);
        mPlayer.release();

        mView.setPlayer(null);
    }

    public void seekTo(long position, boolean playWhenReady) {
        if (mCurrentState == State.RELEASED) {
            warn("Wrong state: player is already released");
            return;
        } else if (mCurrentState == State.IDLE) {
            warn("Wrong state: player is not prepared");
            return;
        } else if (mMediaSource == null) {
            warn("Missing media source");
            return;
        }

        mPlayer.seekTo(position);
        mPlayer.setPlayWhenReady(playWhenReady);
    }

    public void play() {
        mPlayer.setPlayWhenReady(true);
    }

    public void pause() {
        mPlayer.setPlayWhenReady(false);
    }

    public boolean isPlaying() {
        return mPlayer.getPlayWhenReady();
    }

    public void adjustVolume(float volume) {
        mPlayer.setVolume(Math.max(0, Math.min(1, volume)));
    }

    public long getPlaybackPosition() {
        return mPlayer.isCurrentWindowSeekable() ? Math.max(0, mPlayer.getCurrentPosition()) : TIME_UNSET;
    }

    public void setListener(Listener listener) {
        mListener = listener;
    }

    private final ExoPlayerEventListenerAdapter mELA = new ExoPlayerEventListenerAdapter() {
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            if (mListener != null) {
                mListener.onPlayerStateChanged(VideoPlayer.this, playbackState, playWhenReady);
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            mCurrentState = State.IDLE;
            if (mListener != null) {
                mListener.onPlayerError(VideoPlayer.this, (Exception) error.getCause(),
                        error.type == TYPE_SOURCE);
            }
        }
    };

}
