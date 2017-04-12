package eu.imind.imindlib.videoplayer.ui;

import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import eu.imind.imindlib.videoplayer.R;
import eu.imind.imindlib.videoplayer.media.VideoPlayer;
import eu.imind.imindlib.videoplayer.media.VideoPlayerView;
import okhttp3.OkHttpClient;

import static eu.imind.imindlib.videoplayer.Utils.debug;
import static eu.imind.imindlib.videoplayer.Utils.warn;

/**
 * This class helps you to display {@link VideoPlayerView} in a dialog window.
 *
 * Initialize by passing the url of the video in the fragment's arguments: {@link #ARG_URI}
 * and {@link AppCompatDialogFragment#show(FragmentManager, String)} to reveal and start video playback.
 */
public abstract class VideoPlayerDialog extends AppCompatDialogFragment implements OkHttpClientProvider,
        VideoPlayer.Listener, VideoPlayerView.OnClickRetryListener, View.OnTouchListener, View.OnClickListener {

    protected static final String ARG_URI = "uri";

    protected Context mContext;
    protected OkHttpClient mHttpClient;

    protected VideoPlayerView mVideoView;
    protected VideoPlayer mVideoPlayer;

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
        mHttpClient = getOkHttpClient();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setStyle(STYLE_NO_TITLE, R.style.Dialog_VideoPlayer);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.exo_video_player_dialog, container, false);

        mVideoView = (VideoPlayerView) root.findViewById(R.id.exo_video_player_view);

        View vpc = root.findViewById(R.id.exo_video_player_dialog_layout);
        if (vpc != null) {
            vpc.setOnTouchListener(this);
        }

        View close = root.findViewById(R.id.exo_overlay_close_button);
        if (close != null) {
            close.setOnClickListener(this);
        }

        return root;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        Uri uri = getArguments() != null ? (Uri) getArguments().getParcelable(ARG_URI) : null;
        if (mVideoView == null || uri == null) {
            warn("Missing video view or uri");
            dismiss();
            return;
        }

        mVideoPlayer = new VideoPlayer(mContext, mHttpClient, mVideoView);
        mVideoPlayer.setListener(this);
        mVideoPlayer.prepare(uri);
        mVideoPlayer.play();

        mVideoView.hideController();
        mVideoView.setOnClickRetryListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (mVideoPlayer != null) {
            mVideoPlayer.pause();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (mVideoPlayer != null) {
            mVideoPlayer.release();
        }
    }

    @Override
    public boolean onTouch(View view, MotionEvent event) {
        if (view.getId() == R.id.exo_video_player_dialog_layout) {
            dismiss();
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.exo_overlay_close_button) {
            dismiss();
        }
    }

    @Override
    public void onClickRetry(VideoPlayer player) {
        player.retry();
    }

    @Override
    public void onPlayerStateChanged(VideoPlayer player, int playbackState, boolean playWhenReady) {
        // Do nothing
    }

    @Override
    public void onPlayerError(VideoPlayer player, Exception exception, boolean whileLoadingSource) {
        debug(exception);
    }

}
