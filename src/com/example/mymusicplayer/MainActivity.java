package com.example.mymusicplayer;

import java.util.ArrayList;
import java.util.List;
import com.example.Utils.FileUtils;
import com.example.Utils.MediaUtil;
import com.example.Utils.ShakeListener;
import com.example.Utils.ShakeListener.OnShakeListener;
import com.example.domain.ControlConstant;
import com.example.domain.Menu;
import com.example.domain.Mp3Info;
import com.example.domain.MyAction;
import com.example.musicadpter.MenuAdapter;
import com.example.musicadpter.MusicListAdapter;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.Typeface;
import android.text.method.DigitsKeyListener;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity {

	private List<Mp3Info> mp3Infos = null;
	private TextView musicTitle;
	private TextView musicDuration;
	private ListView musicListView;
	private HomeReceiver homeReceiver;
	private int currentTime;// ����ʱ��
	private Button playBtn; // ���ţ����š���ͣ��
	private Button nextBtn;// ��һ��
	private Button previousBtn;// ��һ��
	private Button repeatBtn; // �ظ�������ѭ����ȫ��ѭ����
	private Button shuffleBtn; // �������
	private Button musicPlaying;// ��ת����ʽ���İ�ť
	private int listPosition;
	private final int NEXT = 0;// ��һ��
	private final int PREVIOUS = 1;// ��һ��
	private ShakeListener shakeListener;
	private boolean isShuffle = false;
	private boolean isRepeat = false;
	private boolean isFirstTime = true;
	private boolean isPlaying = false;
	private Menu MyMenu;
	private Timers timer;
	private final String MUSIC_SERVICE = "com.example.mymusicplayer.MUSIC_SERVICE";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		listPosition = FileUtils.getPosition(this);
		findViewById();

		setListener();
		mp3Infos = MediaUtil.getMp3Infos(this);

		musicTitle.setText(mp3Infos.get(listPosition).getTitle());
		musicDuration.setText(MediaUtil.formatTime(mp3Infos.get(listPosition)
				.getDuration()));

		musicListView.setAdapter(new MusicListAdapter(MainActivity.this,
				mp3Infos));
		musicListView.setOnItemClickListener(new MusicListItemClickListener());
		homeReceiver = new HomeReceiver();
		IntentFilter filter = new IntentFilter();
		filter.addAction(MyAction.MUSIC_CURRENT);
		filter.addAction(MyAction.UPDATE_ACTION);
		registerReceiver(homeReceiver, filter);
		shakeListener = new ShakeListener(this);// ҡ�εļ�����
		shakeListener.setOnShakeListener(new OnShakeListener() {

			@Override
			public void onShake() {
				MediaPlayer.create(MainActivity.this, R.raw.shake_music)
						.start();
				isPlaying = true;
				isFirstTime = false;
				changeMusic(NEXT);
			}
		});
		LoadMenu();

	}

	@Override
	public boolean onMenuOpened(int featureId, android.view.Menu menu) {

		MyMenu.showAtLocation(findViewById(R.id.homeRLLayout), Gravity.BOTTOM
				| Gravity.CENTER_HORIZONTAL, 10, 0);
		// �������true�Ļ��ͻ���ʾϵͳ�Դ��Ĳ˵�����֮����false�Ļ�������ʾ�Լ�д��
		return false;
	}

	@Override
	public boolean onCreateOptionsMenu(android.view.Menu menu) {
		// TODO Auto-generated method stub
		return super.onCreateOptionsMenu(menu);
	}

	private class MusicListItemClickListener implements OnItemClickListener {

		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {

			listPosition = position;
			isFirstTime = false;
			isPlaying = true;
			playBtn.setBackgroundResource(R.drawable.pause_selector);
			Mp3Info mp3Info = mp3Infos.get(position);
			musicTitle.setText(mp3Info.getTitle());
			musicDuration.setText(MediaUtil.formatTime(mp3Info.getDuration()));
			Intent playerIntent = new Intent(MainActivity.this,
					PlayerActivity.class);
			playerIntent.putExtra("path", mp3Info.getUrl());
			playerIntent.putExtra("current", listPosition);
			playerIntent.putExtra("controlMSG", ControlConstant.PLAY_MSG);
			playerIntent.putExtra("duration", mp3Info.getDuration());
			playerIntent.putExtra("title", mp3Info.getTitle());
			playerIntent.putExtra("artist", mp3Info.getArtist());
			playerIntent.putExtra("isPlaying", isPlaying);
			startActivity(playerIntent);
		}

	}

	public class HomeReceiver extends BroadcastReceiver {

		@Override
		public void onReceive(Context context, Intent intent) {
			// TODO Auto-generated method stub
			isFirstTime = false;
			currentTime = intent.getIntExtra("currenttime", -1);// ����ʱ��
			musicDuration.setText(MediaUtil.formatTime(currentTime));// ��Ŀʱ�䳤��
			if (intent.getAction() == MyAction.UPDATE_ACTION) {
				musicTitle.setText(intent.getStringExtra("title"));
				listPosition = intent.getIntExtra("current", 0);
			}
		}
	}

	public void findViewById() {
		musicTitle = (TextView) findViewById(R.id.music_title);
		musicTitle.setMovementMethod(ScrollingMovementMethod.getInstance());
		musicDuration = (TextView) findViewById(R.id.music_duration);
		musicListView = (ListView) findViewById(R.id.music_list);
		playBtn = (Button) findViewById(R.id.play_music);
		nextBtn = (Button) findViewById(R.id.next_music);
		previousBtn = (Button) findViewById(R.id.previous_music);
		shuffleBtn = (Button) findViewById(R.id.shuffle_music);
		repeatBtn = (Button) findViewById(R.id.repeat_music);
		musicPlaying = (Button) findViewById(R.id.playing);
	}

	private void setListener() {
		ButtonOnClickListener buttonOnClickListener = new ButtonOnClickListener();
		playBtn.setOnClickListener(buttonOnClickListener);
		nextBtn.setOnClickListener(buttonOnClickListener);
		previousBtn.setOnClickListener(buttonOnClickListener);
		shuffleBtn.setOnClickListener(buttonOnClickListener);
		repeatBtn.setOnClickListener(buttonOnClickListener);
		musicPlaying.setOnClickListener(buttonOnClickListener);
	}

	public class ButtonOnClickListener implements OnClickListener {

		@Override
		public void onClick(View view) {
			// TODO Auto-generated method stub
			switch (view.getId()) {
			case R.id.play_music:
				if (isFirstTime) {
					playMusic();
					isFirstTime = false;
					isPlaying = true;
				} else {
					if (isPlaying) {
						playBtn.setBackgroundResource(R.drawable.play_selector);// ���ڲ���ʱ����ͣ�İ�ť�����Ե���֮���ɲ��Ű�ť
						Intent intent = new Intent();
						intent.setAction(MUSIC_SERVICE);
						intent.putExtra("controlMSG", ControlConstant.PAUSE_MSG);
						startService(intent);
						isPlaying = false;
					} else {
						playBtn.setBackgroundResource(R.drawable.pause_selector);
						Intent intent = new Intent();
						intent.setAction(MUSIC_SERVICE);
						intent.putExtra("controlMSG",
								ControlConstant.CONTINUE_MSG);
						startService(intent);
						isPlaying = true;
					}
				}
				break;
			case R.id.next_music:
				isPlaying = true;
				isFirstTime = false;
				changeMusic(NEXT);
				break;
			case R.id.previous_music:
				isPlaying = true;
				isFirstTime = false;
				changeMusic(PREVIOUS);
				break;
			case R.id.repeat_music:
				if (isRepeat) {
					isRepeat = false;
					shuffleBtn.setClickable(true);
					repeatBtn.setBackgroundResource(R.drawable.repeat_none);
					showToast(R.string.repeat_none);
				} else {
					isRepeat = true;
					shuffleBtn.setClickable(false);
					repeatBtn.setBackgroundResource(R.drawable.repeat_current);
					showToast(R.string.repeat);
				}
				sendBroadcastToChangeStatus();
				break;
			case R.id.shuffle_music:
				if (isShuffle) {
					isShuffle = false;
					shuffleBtn.setBackgroundResource(R.drawable.shuffle_none);
					repeatBtn.setClickable(true);
					showToast(R.string.shuffle_none);

				} else {
					isShuffle = true;
					shuffleBtn.setBackgroundResource(R.drawable.shuffle);
					repeatBtn.setClickable(false);
					showToast(R.string.shuffle);
				}
				sendBroadcastToChangeStatus();
				break;
			case R.id.playing:
				if (true) {
					Intent playerIntent = new Intent(MainActivity.this,
							PlayerActivity.class);
					playerIntent.putExtra("path", mp3Infos.get(listPosition)
							.getUrl());
					playerIntent.putExtra("current", listPosition);
					playerIntent.putExtra("MSG", ControlConstant.PLAYING_MSG);
					playerIntent.putExtra("title", mp3Infos.get(listPosition)
							.getTitle());
					playerIntent.putExtra("artist", mp3Infos.get(listPosition)
							.getArtist());
					playerIntent.putExtra("isPlaying", isPlaying);
					startActivity(playerIntent);
				}
				break;

			}

		}

		private void sendBroadcastToChangeStatus() {
			Intent intent = new Intent();
			intent.setAction(MyAction.SHUFFLE_ACTION);
			intent.putExtra("isShuffle", isShuffle);
			intent.putExtra("isRepeat", isRepeat);
			sendBroadcast(intent);
			Log.i("isRepeat isShuffle",
					String.valueOf(isRepeat) + String.valueOf(isShuffle));
		}

		private void showToast(int resId) {
			// TODO Auto-generated method stub
			Toast.makeText(MainActivity.this, resId, 1000).show();
		}

		private void playMusic() {
			// TODO Auto-generated method stub
			playBtn.setBackgroundResource(R.drawable.pause_selector);
			Mp3Info mp3Info = mp3Infos.get(listPosition);
			// musicTitle.setText(mp3Info.getTitle());
			Intent intent = new Intent();
			intent.setAction(MUSIC_SERVICE);
			intent.putExtra("current", listPosition);
			intent.putExtra("path", mp3Info.getUrl());
			startService(intent);
		}

	}

	public void changeMusic(int flag) {
		// TODO Auto-generated method stub
		playBtn.setBackgroundResource(R.drawable.pause_selector);
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
	}

	private void LoadMenu() {
		MyMenu = new Menu(this);

		List<int[]> data1 = new ArrayList<int[]>();
		data1.add(new int[] { R.drawable.btn_menu_exit, R.string.menu_exit_txt });
		MyMenu.addItem("����", data1, new MenuAdapter.ItemListener() {

			@Override
			public void onClickListener(int position, View view) {
				MyMenu.cancel();
				if (position == 0) {
					exit();
				}
			}
		});

		List<int[]> data2 = new ArrayList<int[]>();
		data2.add(new int[] { R.drawable.btn_menu_sleep, R.string.menu_time_txt });
		MyMenu.addItem("����", data2, new MenuAdapter.ItemListener() {

			@Override
			public void onClickListener(int position, View view) {
				MyMenu.cancel();
				if (position == 0) {
					Sleep();
				}
			}

		});

		List<int[]> data3 = new ArrayList<int[]>();
		data3.add(new int[] { R.drawable.btn_menu_about, R.string.about_title });
		MyMenu.addItem("����", data3, new MenuAdapter.ItemListener() {
			@Override
			public void onClickListener(int position, View view) {
				MyMenu.cancel();
				if (position == 0) {
					Intent intent = new Intent(MainActivity.this,
							AboutActivity.class);
					startActivity(intent);

				}
			}
		});
		MyMenu.create(); // �����˵�
	}

	private class Timers extends CountDownTimer {

		public Timers(long millisInFuture, long countDownInterval) {
			super(millisInFuture, countDownInterval);
		}

		@Override
		public void onTick(long millisUntilFinished) {
		}

		@Override
		public void onFinish() {
			exit();
		}
	}

	private void Sleep() {
		final EditText editText = new EditText(this);
		editText.setText("10");
		editText.setGravity(Gravity.CENTER_HORIZONTAL);
		editText.setTextColor(Color.BLUE);
		editText.selectAll();
		editText.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD));
		editText.setKeyListener(new DigitsKeyListener(false, true));
		new AlertDialog.Builder(this).setView(editText)
				.setIcon(R.drawable.timer_icon).setTitle("������ʱ��")
				.setPositiveButton("����", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {

						dialog.dismiss();
						dialog.cancel();
						String input = editText.getText().toString();
						/** �������С��2���ߵ���0���֪�û� **/
						if (editText.length() <= 2 && editText.length() != 0) {
							if (input.endsWith(".") | input.startsWith(".")) {
								Toast.makeText(MainActivity.this, "������С��3λ������",
										0).show();
							} else {
								final String time = editText.getText()
										.toString();
								long minute = Integer.parseInt(time);
								long endTime = minute * 60000;// ת��Ϊ��msΪ��λ
								timer = new Timers(endTime, 1000);
								timer.start();// ����ʱ��ʼ
								Toast.makeText(MainActivity.this,
										"����ģʽ����������" + minute + "���Ӻ�ر�", 0)
										.show();
							}

						} else {
							Toast.makeText(MainActivity.this, "������С��3λ������", 0)
									.show();
						}

					}

				}).setNegativeButton("ȡ��", null).show();

	}

	/**
	 * �˳�����
	 */
	private void exit() {
		Intent intent = new Intent();
		intent.setAction(MUSIC_SERVICE);
		stopService(intent);
		finish();
		onDestroy();
		android.os.Process.killProcess(android.os.Process.myPid());// ��ɱ
	}

	@Override
	public void onBackPressed() {
		new AlertDialog.Builder(this).setTitle("������")
				.setMessage("��ȷ��Ҫ�˳�Ӧ�ó�����").setIcon(R.drawable.ic_dialog)
				.setPositiveButton("ȷ��", new DialogInterface.OnClickListener() {

					@Override
					public void onClick(DialogInterface dialog, int which) {
						exit();
					}
				}).setNegativeButton("ȡ��", null).show();
	}

	@Override
	protected void onPause() {
		// TODO Auto-generated method stub
		super.onPause();
		System.out.println("main onpause");
		shakeListener.stop();
	}

	@Override
	protected void onResume() {
		// TODO Auto-generated method stub
		super.onResume();
		shakeListener.start();
		System.out.println("onresume");
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		FileUtils.savePosition(this, listPosition);
		System.out.println("ondestroy" + listPosition);
		unregisterReceiver(homeReceiver);
	}

}
