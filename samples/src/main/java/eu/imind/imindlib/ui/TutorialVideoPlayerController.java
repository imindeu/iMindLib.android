package eu.imind.imindlib.ui;

import android.content.Context;
import android.support.annotation.NonNull;
import android.widget.Toast;

import eu.imind.imindlib.IMindLibApplication;
import eu.imind.imindlib.R;
import eu.imind.imindlib.videoplayer.media.VideoPlayer;
import eu.imind.imindlib.videoplayer.ui.VideoPlayerController;
import okhttp3.OkHttpClient;

public class TutorialVideoPlayerController extends VideoPlayerController {

    public TutorialVideoPlayerController(@NonNull Context context) {
        super(context);
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
