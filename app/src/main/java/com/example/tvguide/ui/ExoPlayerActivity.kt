package com.example.tvguide.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.tvguide.changeWindowStatusBar
import com.example.tvguide.databinding.ActivityExoplayerBinding
import com.example.tvguide.model.Analyst
import com.example.tvguide.model.PlayItem
import com.google.android.exoplayer2.ExoPlayer
import com.google.android.exoplayer2.MediaItem


/**
 * Created by luyiling on 2021/11/23
 * Modified by
 *
 * TODO:
 * Description:
 *
 *
 * exoplayer 播放器 UI: playerView, StyledPlayerView
 * exoplayer 播放器控制工具行 UI: playerControlView, StyledPlayerControlView
 *
 * StyledPlayerView provide more ui decoration possibilities to dev
 * PlayerView/StyledPlayerView contains bottom toolbar (播放器控制工具行)
 *
 * You can use PlayerControlView/StyledPlayerControlView
 *
 * NOTE  Exoplayer guideline(https://exoplayer.dev/media-items.html)
 *
 * @code exo_styled_player_view.xml (官方) 的佈局
 * 以相同的xml名字和id 做佈局的改寫
   ++++++++++++++++++++++++++++++
   +                            +
   + +++++++++++++++++++++++++  + AspectRatioFrameLayout (id=contentFrame) 私有，可調控滿版/16:4/...
   + +                       +  +
   + +  ++++++++++++++++++   +  +
   + +  ++++++++++++++++++   +  + Dynamically add surfaceView here (position = 0) into AspectRatioFrameLayout
   + +  ++++++++++++++++++   +  +
   + +                       +  +
   + +  ++++++++++++++++++   +  + shutter for snapshot or record the video
   + +  ++++++++++++++++++   +  +
   + +  ++++++++++++++++++   +  +
   + +                       +  +
   + +++++++++++++++++++++++++  +
   +                            +
   + +++++++++++++++++++++++++  +
   + +++++++++++++++++++++++++  +  Ad overlay
   + +++++++++++++++++++++++++  +
   +                            +
   + +++++++++++++++++++++++++  +
   + +++++++++++++++++++++++++  +  Bottom Player Toolbar
   + +++++++++++++++++++++++++  +
   +                            +
   ++++++++++++++++++++++++++++++
 * @params
 * @params
 */
const val ARG_PLAY = "ARG_Play"
const val ARG_BOT = "ARG_Bot"
class ExoPlayerActivity : AppCompatActivity() {

    lateinit var binding : ActivityExoplayerBinding
    lateinit var exoPlayer : ExoPlayer
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityExoplayerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 全螢幕 //
        changeWindowStatusBar()

        val playItem = intent.getParcelableExtra<PlayItem>(ARG_PLAY)
        val bot = intent.getParcelableExtra<Analyst>(ARG_BOT)

        checkNotNull(playItem)
        val videoUri = playItem.currentUrl
        val mediaItem = MediaItem.fromUri(videoUri)


        /*PATCH: use playerView to fill exoplayer inside*/

        // Instantiate the player.
        exoPlayer = ExoPlayer.Builder(baseContext).build()
        // Attach player to the view.
        binding.player.player = exoPlayer
        // Set the media item to be played.
        exoPlayer.setMediaItem(mediaItem)
        // Prepare the player.
        exoPlayer.prepare()
        //Play when ready
        exoPlayer.playWhenReady = true




        /*
        PATCH: use playerControlView to fill exoplayer inside

        val exoPlayer = ExoPlayer.Builder(baseContext).build()
        binding.playerControlVIew.player = exoPlayer
        exoPlayer.setMediaItem(mediaItem)
        exoPlayer.prepare()
        */


    }


    override fun onResume() {
        super.onResume()
        //FIXME player control
    }

    override fun onDestroy() {
        super.onDestroy()

        //player release
        exoPlayer.stop()
        exoPlayer.release()
    }
}