package com.example.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import android.content.Context;
import android.database.Cursor;
import android.provider.MediaStore;
import com.example.domain.Mp3Info;

public class MediaUtil {

	public MediaUtil(Context context) {
		// TODO Auto-generated constructor stub
	}

	public static List<Mp3Info> getMp3Infos(Context context) {
		Cursor cursor = context.getContentResolver().query(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, null, null, null,
				MediaStore.Audio.Media.DEFAULT_SORT_ORDER);
		List<Mp3Info> mp3Infos = new ArrayList<Mp3Info>();

		if (cursor != null) {
			while (cursor.moveToNext()) {
				Mp3Info mp3Info = new Mp3Info();
				long id = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media._ID));
				String displayName = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.DISPLAY_NAME));
				String title = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.TITLE));
				long duration = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media.DURATION));
				String artist = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.ARTIST));
				String url = cursor.getString(cursor
						.getColumnIndex(MediaStore.Audio.Media.DATA));
				long albumId = cursor.getInt(cursor
						.getColumnIndex(MediaStore.Audio.Media.ALBUM_ID));
				long size = cursor.getLong(cursor
						.getColumnIndex(MediaStore.Audio.Media.SIZE));
				int isMusic = cursor.getInt(cursor
						.getColumnIndex(MediaStore.Audio.Media.IS_MUSIC));
				if (isMusic != 0) {

					mp3Info.setArtist(artist);
					mp3Info.setDisplayName(displayName);
					mp3Info.setDuration(duration);
					mp3Info.setId(id);
					mp3Info.setAlbumId(albumId);
					mp3Info.setTitle(title);
					mp3Info.setSize(size);
					mp3Info.setUrl(url);
				}
				mp3Infos.add(mp3Info);
			}
		}
		cursor.close();
		return mp3Infos;

	}

	public static String formatTime(long time) {
		Date date = new Date(time);
		SimpleDateFormat formater = new SimpleDateFormat("mm:ss");
		String result = formater.format(date);
		return result;
	}
}
