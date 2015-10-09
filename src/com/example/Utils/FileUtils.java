package com.example.Utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.R.integer;
import android.content.Context;
import android.text.TextUtils;

public class FileUtils {

	public FileUtils() {
		// TODO Auto-generated constructor stub
	}

	public static boolean savePosition(Context context, int position) {

		File file = new File(context.getFilesDir(), "musicposition.txt");
		try {
			FileOutputStream fos = new FileOutputStream(file);
			String save = position + "\n";
			fos.write(save.getBytes());
			fos.flush();
			fos.close();
			return true;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return false;
	}

	public static int getPosition(Context context) {

		FileInputStream fis = null;
		int position = 0;
		try {
			File f = new File(context.getFilesDir(), "musicposition.txt");
			fis = new FileInputStream(f);
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					fis));
			String line = reader.readLine();
			fis.close();
			if (TextUtils.isEmpty(line)) {
				return position;
			} else {
				position = Integer.valueOf(line);
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return position;
	}

	public static boolean addToFavoriteList(Context context, int id) {
		if (!isExist(context, id)) {
			File file = new File(context.getCacheDir(), "favorite.txt");
			try {
				FileOutputStream fos = new FileOutputStream(file, true);
				String idString = id + "\n";
				fos.write(idString.getBytes());
				fos.close();
				return true;
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static List<Integer> getFavoriteIdList(Context context) {
		List<Integer> list = null;
		File file = new File(context.getCacheDir(), "favorite.txt");
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(file)));
			String line = null;
			list = new ArrayList<Integer>();
			while ((line = reader.readLine()) != null) {
				list.add(Integer.valueOf(line));
			}
			reader.close();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return list;
	}

	public static boolean isExist(Context context, int id) {
		File f = new File(context.getCacheDir(), "favorite.txt");
		boolean flag = false;
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(
					new FileInputStream(f)));
			String line = null;
			while ((line = reader.readLine()) != null) {
				if (id == Integer.valueOf(line.trim())) {
					flag = true;
				}
			}
			reader.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return flag;
	}
}
