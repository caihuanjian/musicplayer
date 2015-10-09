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

	public static LrcView lrcView; // �Զ�������ͼ
	private List<Mp3Info> mp3Infos;
	private MediaPlayer mediaPlayer;
	private String path;
	private int msg;
	private int currentTime;
	private int current;// ��ǰ���ŵ������б��е�position
	private boolean isRepeat;
	private boolean isShuffle;
	private PlayerReceiver playerReceiver;

	private int index = 0; // ��ʼ���ֵ
	private List<LrcContent> lrcList;// ��Ÿ���б����
	private LrcProcess mLrcProcess;
	private int duration; // ��������

	private Handler handler = new Handler() {
		public void handleMessage(Message msg) {
			if (msg.what == 1) {
				currentTime = mediaPlayer.getCurrentPosition();// ��õ�ǰ�Ĳ���λ��long��
				duration = mediaPlayer.getDuration();
				Intent intent = new Intent();
				intent.setAction(MyAction.MUSIC_CURRENT);
				intent.putExtra("currenttime", currentTime);
				intent.putExtra("title", mp3Infos.get(current).getTitle());
				intent.putExtra("duration", duration);
				sendBroadcast(intent);
			}
			handler.sendEmptyMessageDelayed(1, 1000);// ÿ�����һ��currentTime
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
					current++;// ��ͨģʽ�£�˳�򲥷�
					if (current > mp3Infos.size() - 1)
						current = 0;// current+1,���������б�����0

				} else {
					if (isShuffle) {
						current = (int) Math.floor(Math.random()
								* (mp3Infos.size() - 1) + 1);// �����������ѭ������
					}
				}
				Log.i("-----�����----��", (mp3Infos.size() - 1) + " current "
						+ current);
				Intent intent = new Intent();// ֪ͨ�޸ı���
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
		System.out.println("ִ�е�play��");
		mediaPlayer.reset();
		try {
			path = mp3Infos.get(current).getUrl();
			mediaPlayer.setDataSource(path);
			mediaPlayer.prepare();
			mediaPlayer.start();
			initLrc();
			Toast.makeText(this, "���ڲ���  " + mp3Infos.get(current).getTitle(),
					1000).show();

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	/**
	 * ��ʼ���������
	 */
	public void initLrc() {// ִ����κ�ѭ��ִ��run����
		mLrcProcess = new LrcProcess();
		// ��ȡ����ļ�
		System.out.println("readLRC(path)" + mp3Infos.get(current).getUrl());
		mLrcProcess.readLRC(mp3Infos.get(current).getUrl());
		// ���ش����ĸ���ļ�
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
	 * ����ʱ���ȡ�����ʾ������ֵ
	 * 
	 * @return
	 */
	public int lrcIndex() {
		if (mediaPlayer.isPlaying()) {
			currentTime = mediaPlayer.getCurrentPosition();
			duration = mediaPlayer.getDuration();
		}
		if (currentTime < duration) { // ��ǰ����δ����
			for (int i = 0; i < lrcList.size(); i++) { // ����ÿһ�и��
				if (i < lrcList.size() - 1) { // û�����һ��
					if (currentTime < lrcList.get(i).getLrcTime() && i == 0) { // ��һ��
						index = i;
					}
					if (currentTime > lrcList.get(i).getLrcTime() // ����ʱ�䳬����ǰ���и�ʵ���ʼʱ��
							&& currentTime < lrcList.get(i + 1).getLrcTime()) {// ����δ������һ��
						index = i; // ��������Ϊ��ǰ���
					}
				}
				if (i == lrcList.size() - 1 // �������һ��,ͣ�������һ��
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
					initLrc();// ����߻�ȡʱ��ʱ�����ַ�������Ϊ����ʱ�д��󣬲�֪��ԭ��
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
		
		System.out.println("service��������");
	}
}
