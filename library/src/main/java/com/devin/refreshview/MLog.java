package com.devin.refreshview;

import android.util.Log;

public class MLog {

	public static final String TAG = MLog.class.getSimpleName();

	public static boolean DEBUG = BuildConfig.DEBUG;

	public static void d(String msg) {
		if (DEBUG) {
			Log.d(TAG, msg);
		}
	}

	public static void d(String tag, String msg) {
		if (DEBUG) {
			Log.d(tag, msg);
		}
	}
}
