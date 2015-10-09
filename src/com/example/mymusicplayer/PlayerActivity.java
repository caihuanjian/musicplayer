package com.example.mymusicplayer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.example.Utils.FileUtils;
import com.example.Utils.LrcView;
import com.example.Utils.MediaUtil;
import com.example.domain.ControlConstant;
import com.example.domain.Mp3Info;
import com.example.domain.MyAction;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Toast;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;

public class PlayerActivity extends Activity {

	public static LrcView lrcView; // 自定义歌词视图null;
	private int current;
	private final String MUSIC_SERVICE = "com.example.mymusicplayer.MUSIC_SERVICE";
	private int currentTime;// 当前播放时间
	private int duration;// 总时长
	private SeekBar music_progressBar;
	private PlayerActivityReceiver playerReceiver;
	private TextView musicTitle;
	private TextView musicArtist;
	private TextView current_progress;
	private TextView final_progress;
	private boolean isPlaying;
	private Button play_music;
	private Button next_music;
	private Button previous_music;
	private Button btn_voice;
	private ImageButton like;
	private ImageButton back_return;
	private ImageButton favourite_music_btn;
	private int listPosition;
	private List<Mp3Info> mp3Infos = null;
	private final int NEXT = 0;// 下一首
	private final int PREVIOUS = 1;// 上一首
	private AudioManager audioManager;
	private int currentVolume;
	private int tempVolume;

	// private ShakeListener shakeListener;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
		System.out.println("PlayerActivity-->>" + "onCreate");
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.lrclayout);
		Bundle bundle = getIntent().getExtras();
		listPosition = bundle.getInt("current");
		mp3Infos = MediaUtil.getMp3Infos(this);
		init();
		audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
		currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
		System.out.println("currentVolume" + currentVolume);
		if (currentVolume == 0) {
			btn_voice.setBackgroundResource(R.drawable.btn_voice_mute);
		}
	}

	public void init() {
		findView();
		isPlaying = getIntent().getBooleanExtra("isPlaying", true);
		if (isPlaying) {
			play_music.setBackgroundResource(R.drawable.pause_selector);
		}
		setListener();
		musicTitle.setText(getIntent().getStringExtra("title"));
		musicArtist.setText(getIntent().getStringExtra("artist"));
		initPlayerActivityReceiver();// 注册广播
		toStartService();// 启动service播放歌曲
		if (getIntent().getIntExtra("MSG", -1) == ControlConstant.PLAYING_MSG) {// 如果为了显示歌词界面
			Intent intent = new Intent();
			intent.putExtra("listPosition", current);
			intent.setAction(MyAction.SHOW_LRC);
			sendBroadcast(intent);
			System.out.println("PLAYING_MSG的广播发送了");
		}
	}

	@Override
	protected void onStart() {
		// TODO Auto-generated method stub
		super.onStart();
		System.out.println("PlayerActivity-->>" + "onStart");
	}

	private void setListener() {
		ButtonOnClickListener clickListener = new ButtonOnClickListener();
		music_progressBar.setOnSeekBarChangeListener(new SeekBarListener());
		play_music.setOnClickListener(clickListener);
		next_music.setOnClickListener(clickListener);
		previous_music.setOnClickListener(clickListener);
		back_return.setOnClickListener(clickListener);
		btn_voice.setOnClickListener(clickListener);
		favourite_music_btn.setOnClickListener(clickListener);
		like.setOnClickListener(clickListener);
	}

	private void findView() {
		lrcView = (LrcView) findViewById(R.id.lrcShowView);
		music_progressBar = (SeekBar) this.findViewById(R.id.audioTrack);
		musicTitle = (TextView) this.findViewById(R.id.musicTitle);
		musicArtist = (TextView) this.findViewById(R.id.musicArtist);
		current_progress = (TextView) this.findViewById(R.id.current_progress);
		final_progress = (TextView) this.findViewById(R.id.final_progress);
		play_music = (Button) this.findViewById(R.id.play_music);
		previous_music = (Button) this.findViewById(R.id.previous_music);
		next_music = (Button) this.findViewById(R.id.next_music);
		back_return = (ImageButton) this.findViewById(R.id.back_return);
		btn_voice = (Button) this.findViewById(R.id.btn_voice);
		favourite_music_btn = (ImageButton) this
				.findViewById(R.id.favourite_music_btn);
		like = (ImageButton) this.findViewById(R.id.like);
	}

	public class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View v) {
			// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.play_music:
				if (isPlaying) {
					play_music.setBackgroundResource(R.drawable.play_selector);// 正在播放时是暂停的按钮，所以点了之后变成播放按钮
					Intent intent = new Intent();
					intent.setAction(MUSIC_SERVICE);
					intent.putExtra("controlMSG", ControlConstant.PAUSE_MSG);
					startService(intent);
					isPlaying = false;
				} else {
					play_music.setBackgroundResource(R.drawable.pause_selector);
					Intent intent = new Intent();
					intent.setAction(MUSIC_SERVICE);
					intent.putExtra("controlMSG", ControlConstant.CONTINUE_MSG);
					startService(intent);
					isPlaying = true;
				}
				break;
			case R.id.next_music:
				isPlaying = true;
				changeMusic(NEXT);
				break;
			case R.id.previous_music:
				isPlaying = true;
				changeMusic(PREVIOUS);
				break;
			case R.id.back_return:
				finish();
				break;
			case R.id.btn_voice:
				currentVolume = audioManager
						.getStreamVolume(AudioManager.STREAM_MUSIC);
				if (currentVolume > 0) {
					btn_voice.setBackgroundResource(R.drawable.btn_voice_mute);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 0,
							0);
					tempVolume = currentVolume;
				} else {
					btn_voice
							.setBackgroundResource(R.drawable.btn_voice_normal);
					audioManager.setStreamVolume(AudioManager.STREAM_MUSIC,
							tempVolume, 0);
				}
				break;
			case R.id.favourite_music_btn:
				showFavorite();
				break;
			case R.id.like:
				boolean flag = FileUtils.addToFavoriteList(
						getApplicationContext(),
						(int) mp3Infos.get(listPosition).getId());
				if (flag) {
					Toast.makeText(PlayerActivity.this, "已加入喜欢的列表", 0).show();
				} else {
					Toast.makeText(PlayerActivity.this, "该歌曲已存在", 0).show();
				}
				break;
			}
		}
	}

	/**
	 * 显示播放列表
	 */
	public void showFavorite() {
		LayoutInflater layoutInflater = (LayoutInflater) this
				.getSystemService(LAYOUT_INFLATER_SERVICE);
		View playQueueLayout = layoutInflater.inflate(R.layout.favorite_list,
				null);// 一个listview
		ListView queuelist = (ListView) playQueueLayout
				.findViewById(R.id.favorite);
		List<HashMap<String, String>> mp3list = getMusicMaps(mp3Infos);
		SimpleAdapter adapter = new SimpleAdapter(this, mp3list,
				R.layout.favorite_item_layout, new String[] { "title",
						"Artist", "duration" }, new int[] { R.id.music_title,
						R.id.favorite_music_artist, R.id.music_duration });
		queuelist.setAdapter(adapter);
		AlertDialog.Builder builder;
		final AlertDialog dialog;
		builder = new AlertDialog.Builder(this).setTitle("我的收藏").setIcon(
				R.drawable.like);
		dialog = builder.create();
		dialog.setView(playQueueLayout);
		dialog.show();
	}

	public List<HashMap<String, String>> getMusicMaps(List<Mp3Info> mp3Infos) {
		List<HashMap<String, String>> mp3list = new ArrayList<HashMap<String, String>>();
		List<Integer> favoriteIdList = FileUtils
				.getFavoriteIdList(PlayerActivity.this);// 获取喜欢的列表歌曲id
		for (Iterator iterator1 = mp3Infos.iterator(); iterator1.hasNext();) {// 遍历歌曲列表
			Mp3Info mp3Info = (Mp3Info) iterator1.next();
			int id = (int) mp3Info.getId();// 获得歌曲id
			for (Iterator iterator2 = favoriteIdList.iterator(); iterator2 // 遍历喜欢的歌曲
					.hasNext();) {
				if (iterator2.next().toString().equals("" + id)) { // 为喜欢里的歌曲就加入到favorite列表中
					HashMap<String, String> map = new HashMap<String, String>();
					map.put("title", mp3Info.getTitle());
					map.put("Artist", mp3Info.getArtist());
					map.put("duration",
							MediaUtil.formatTime(mp3Info.getDuration()));
					mp3list.add(map);
				}
			}
		}
		return mp3list;
	}

	private void changeMusic(int flag) {
		// TODO Auto-generated method stub
		play_music.setBackgroundResource(R.drawable.pause_selector);
		if (flag == NEXT) {
			listPosition++;
			if (listPosition > mp3Infos.size() - 1)
				listPosition = 0;
		}
		if (flag == PREVIOUS) {
			listPosition--;
			if (listPosition < 0)
				listPosition = mp3Infos.size() - 1;
		}
		musicTitle.setText(mp3Infos.get(listPosition).getTitle());
		Intent intent = new Intent();
		intent.setAction(MUSIC_SERVICE);
		intent.putExtra("current", listPosition);
		intent.putExtra("path", mp3Infos.get(listPosition).getUrl());
		intent.putExtra("controlMSG", ControlConstant.PLAY_MSG);
		startService(intent);
		Intent updateIntent = new Intent(); // 向主界面传递参数
		updateIntent.setAction(MyAction.UPDATE_ACTION);
		updateIntent.putExtra("current", listPosition);
		updateIntent.putExtra("title", mp3Infos.get(listPosition).getTitle());
		sendBroadcast(updateIntent);
	}

	public void initPlayerActivityReceiver() {
		playerReceiver = new PlayerActivityReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MyAction.MUSIC_CURRENT);
		filter.addAction(MyAction.UPDATE_ACTION);
		registerReceiver(playerReceiver, filter);
	}

	@Override
	protected void onStop() {

		super.onStop();
		System.out.println("PlayerActivity-->>" + "onStop");
	}

	public class PlayerActivityReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			if (MyAction.MUSIC_CURRENT.equals(intent.getAction())) {
				duration = intent.getIntExtra("duration", 1);
				currentTime = intent.getIntExtra("currenttime", 0);
				music_progressBar.setMax(duration);
				music_progressBar.setProgress(currentTime);
				current_progress.setText(MediaUtil.formatTime(currentTime));
				final_progress.setText(MediaUtil.formatTime(duration));
			}
			if (MyAction.UPDATE_ACTION.equals(intent.getAction())) {
				musicTitle.setText(intent.getStringExtra("title"));
				musicArtist.setText(intent.getStringExtra("artist"));
			}

		}
	}

	public void toStartService() {
		System.out.println("toStartService");
		Intent ServiceIntent = new Intent();// 通知要播放的曲目
		ServiceIntent.setAction(MUSIC_SERVICE);
		ServiceIntent.putExtra("path", getIntent().getStringExtra("path"));
		ServiceIntent
				.putExtra("current", getIntent().getIntExtra("current", 0));
		ServiceIntent.putExtra("controlMSG",
				getIntent().getIntExtra("controlMSG", 0));
		startService(ServiceIntent);
	}

	public class SeekBarListener implements OnSeekBarChangeListener {

		@Override
		public void onProgressChanged(SeekBar seekBar, int progress,
				boolean fromUser) {
			// TODO Auto-generated method stub
			switch (seekBar.getId()) {
			case R.id.audioTrack:
				if (fromUser) {
					audioTrackChange(progress); // 用户控制进度的改变
				}
				break;
			}
		}

		@Override
		public void onStartTrackingTouch(SeekBar seekBar) {
		}

		@Override
		public void onStopTrackingTouch(SeekBar seekBar) {
		}

	}

	private void audioTrackChange(int progress) {
		// 该方法要通知service更改音乐进度
		Intent intent = new Intent();
		intent.setAction(MyAction.PROGRESS_ACTION);
		intent.putExtra("currenttime", progress);
		sendBroadcast(intent);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		System.out.println("PlayerActivity-->>" + "onDestroy");
	}

}
