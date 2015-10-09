package com.example.service;

import java.util.List;

import com.example.Utils.FileUtils;
import com.example.Utils.LrcProcess;
import com.example.Utils.LrcView;
import com.example.Utils.MediaUtil;
import com.example.domain.ControlConstant;
import com.example.domain.LrcContent;
import com.example.domain.Mp3Info;
import com.example.domain.MyAction;
import com.example.mymusicplayer.PlayerActivity;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.Toast;

public class PlayerSevice extends Service {

	public static LrcView lrcView; // 自定义歌词视图
	private List<Mp3Info> mp3Infos;
	private MediaPlayer mediaPlayer;
	private String path;
	private int msg;
	private int currentTime;
	private int current;// 当前播放的音乐列表中的position
	private boolean isRepeat;
	private boolean isShuffle;
	private PlayerReceiver playerReceiver;

	private int index = 0; // 歌词检索值
	private List<LrcContent> lrcList;// 存放歌词列表对象
	private LrcProcess mLrcProcess;
	private int duration; // 歌曲长度

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				currentTime = mediaPlayer.getCurrentPosition();// 获得当前的播放位置long型
				duration = mediaPlayer.getDuration();
				Intent intent = new Intent();
				intent.setAction(MyAction.MUSIC_CURRENT);
				intent.putExtra("currenttime", currentTime);
				intent.putExtra("title", mp3Infos.get(current).getTitle());
				intent.putExtra("duration", duration);
				sendBroadcast(intent);
			}
			handler.sendEmptyMessageDelayed(1, 1000);// 每秒更新一次currentTime
		};
	};

	@Override
	public void onCreate() {
		// TODO Auto-generated method stub
		super.onCreate();
		System.out.println("service  onCreate");
		playerReceiver = new PlayerReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MyAction.SHUFFLE_ACTION);
		filter.addAction(MyAction.SHOW_LRC);
		filter.addAction(MyAction.PROGRESS_ACTION);
		registerReceiver(playerReceiver, filter);
		mediaPlayer = new MediaPlayer();
		mp3Infos = MediaUtil.getMp3Infos(PlayerSevice.this);
		mediaPlayer.setOnCompletionListener(new OnCompletionListener() {

			@Override
			public void onCompletion(MediaPlayer mP) {
				if (!isRepeat && !isShuffle) {
					current++;// 普通模式下，顺序播放
					if (current > mp3Infos.size() - 1)
						current = 0;// current+1,超出播放列表重置0

				} else {
					if (isShuffle) {
						current = (int) Math.floor(Math.random()
								* (mp3Infos.size() - 1) + 1);// 产生随机数，循环播放
					}
				}
				Log.i("-----随机数----》", (mp3Infos.size() - 1) + " current "
						+ current);
				Intent intent = new Intent();// 通知修改标题
				intent.setAction(MyAction.UPDATE_ACTION);
				intent.putExtra("title", mp3Infos.get(current).getTitle());
				intent.putExtra("artist", mp3Infos.get(current).getArtist());
				intent.putExtra("current", current);
				intent.putExtra("duration", mp3Infos.get(current).getDuration());
				sendBroadcast(intent);
				play(0);
			}
		});

	}

	@Override
	public IBinder onBind(Intent arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		// TODO Auto-generated method stub
		System.out.println("onStartCommand");
		path = intent.getStringExtra("path");
		current = intent.getIntExtra("current", 0);
		msg = intent.getIntExtra("controlMSG", 0);
		if (msg == ControlConstant.PLAY_MSG) // PLAY_MSG = 1
			play(0);
		else if (msg == ControlConstant.PAUSE_MSG) {
			pause();
		} else if (msg == ControlConstant.CONTINUE_MSG) {
			resume();
		} else if (msg == ControlConstant.NEXT_MSG) {
			play(0);
		}
		handler.sendEmptyMessage(1);
		return super.onStartCommand(intent, flags, startId);
	}

	private void resume() {
		// TODO Auto-generated method stub
		mediaPlayer.start();
	}

	private void pause() {
		// TODO Auto-generated method stub
		if (mediaPlayer != null && mediaPlayer.isPlaying()) {
			mediaPlayer.pause();
		}
	}

	private void play(int currentTime) {
		System.out.println("执行到play了");
		mediaPlayer.reset();
		try {
			path = mp3Infos.get(current).getUrl();
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
			mediaPlayer.start();
			initLrc();
			Toast.makeText(this, "正在播放  " + mp3Infos.get(current).getTitle(),
					1000).show();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * 初始化歌词配置
	 */
	public void initLrc() {// 执行这段后，循环执行run（）
		mLrcProcess = new LrcProcess();
		// 读取歌词文件
		System.out.println("readLRC(path)" + mp3Infos.get(current).getUrl());
		mLrcProcess.readLRC(mp3Infos.get(current).getUrl());
		// 传回处理后的歌词文件
		lrcList = mLrcProcess.getLrcList();
		PlayerActivity.lrcView.setmLrcList(lrcList);
		handler.post(mRunnable);

	}

	Runnable mRunnable = new Runnable() {

		@Override
		public void run() {
			PlayerActivity.lrcView.setIndex(lrcIndex());
			PlayerActivity.lrcView.invalidate();
			handler.postDelayed(mRunnable, 100);
		}
	};

	/**
	 * 根据时间获取歌词显示的索引值
	 * 
	 * @return
	 */
	public int lrcIndex() {
		if (mediaPlayer.isPlaying()) {
			currentTime = mediaPlayer.getCurrentPosition();
			duration = mediaPlayer.getDuration();
		}
		if (currentTime < duration) { // 当前歌曲未结束
			for (int i = 0; i < lrcList.size(); i++) { // 遍历每一行歌词
				if (i < lrcList.size() - 1) { // 没到最后一行
					if (currentTime < lrcList.get(i).getLrcTime() && i == 0) { // 第一行
						index = i;
					}
					if (currentTime > lrcList.get(i).getLrcTime() // 播放时间超过当前这行歌词的起始时间
							&& currentTime < lrcList.get(i + 1).getLrcTime()) {// 并且未超过下一行
						index = i; // 于是索引为当前这句
					}
				}
				if (i == lrcList.size() - 1 // 到了最后一行,停留在最后一行
						&& currentTime > lrcList.get(i).getLrcTime()) {
					index = i;
				}
			}
		}
		return index;
	}

	public class PlayerReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {

			if (MyAction.SHOW_LRC.equals(intent.getAction())) {
				try {
					initLrc();// 这里边获取时间时，将字符串解析为数字时有错误，不知道原因！
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
			if (MyAction.SHUFFLE_ACTION.equals(intent.getAction())) {
				isRepeat = intent.getBooleanExtra("isRepeat", false);
				isShuffle = intent.getBooleanExtra("isShuffle", false);
			}
			if (MyAction.PROGRESS_ACTION.equals(intent.getAction())) {
				currentTime = intent.getIntExtra("currenttime", currentTime);
				mediaPlayer.seekTo(currentTime);
			}
		}

	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		System.out.println("service被销毁了");
	}
}
