package eu.imind.imindlib.ui;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import eu.imind.imindlib.R;
import eu.imind.imindlib.videoplayer.media.VideoPlayerView;
import eu.imind.imindlib.videoplayer.ui.VideoPlayerController;

public class MainActivity extends AppCompatActivity {

    private static final String SAMPLE_MP4_URL = "http://www.html5videoplayer.net/videos/toystory.mp4";

    private VideoPlayerController mVPC;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mVPC = new TutorialVideoPlayerController(this);
        mVPC.prepareVideoPlayer((VideoPlayerView) findViewById(R.id.video_player1), SAMPLE_MP4_URL);
        mVPC.prepareVideoPlayer((VideoPlayerView) findViewById(R.id.video_player2), SAMPLE_MP4_URL);

        findViewById(R.id.open_player_dialog).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TutorialVideoPlayerDialog.inst(SAMPLE_MP4_URL).show(getSupportFragmentManager(), "vpd");
            }
        });
    }

    @Override
    protected void onStop() {
        super.onStop();
        mVPC.pause();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mVPC.release();
    }

}
