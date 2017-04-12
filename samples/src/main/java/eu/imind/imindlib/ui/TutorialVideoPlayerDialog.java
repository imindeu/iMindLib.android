package eu.imind.imindlib.ui;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.widget.Toast;

import eu.imind.imindlib.IMindLibApplication;
import eu.imind.imindlib.R;
import eu.imind.imindlib.videoplayer.media.VideoPlayer;
import eu.imind.imindlib.videoplayer.ui.VideoPlayerDialog;
import okhttp3.OkHttpClient;

public class TutorialVideoPlayerDialog extends VideoPlayerDialog {

    public static TutorialVideoPlayerDialog inst(@NonNull String url) {
        return inst(Uri.parse(url));
    }

    public static TutorialVideoPlayerDialog inst(@NonNull Uri uri) {
        Bundle args = new Bundle();
        args.putParcelable(ARG_URI, uri);
        TutorialVideoPlayerDialog frag = new TutorialVideoPlayerDialog();
        frag.setArguments(args);
        return frag;
    }

    @Override
    public OkHttpClient getOkHttpClient() {
        return ((IMindLibApplication) mContext.getApplicationContext()).getHttpClient();
    }

    @Override
    public void onPlayerError(VideoPlayer player, Exception exception, boolean whileLoadingSource) {
        super.onPlayerError(player, exception, whileLoadingSource);
        if (whileLoadingSource) {
            Toast.makeText(mContext, R.string.error_loading_media, Toast.LENGTH_SHORT).show();
        }
    }

}
