[ ![Download](https://api.bintray.com/packages/imindeu/imindlib/videoplayer/images/download.svg) ](https://bintray.com/imindeu/imindlib/videoplayer/_latestVersion)

## Video player

An [ExoPlayer](https://github.com/google/ExoPlayer) mod to simplify it's integration for video playback. See `VideoPlayerController.java` and `VideoPlayerDialog.java` classes for further description.

```xml
<eu.imind.imindlib.videoplayer.media.VideoPlayerView
         android:id="@id/exo_video_player"
         android:layout_width="match_parent"
         android:layout_height="wrap_content"
         app:resize_mode="fixed_width" />
```


### Publishing

Prerequisites: `BINTRAY_USER` and `BINTRAY_KEY` defined.

```
./gradlew clean videoplayer:publishBintray
```
