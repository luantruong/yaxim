package org.yaxim.androidclient.data;

import org.yaxim.androidclient.exceptions.YaximXMPPAdressMalformedException;
import org.yaxim.androidclient.util.DataBaseHelper;
import org.yaxim.androidclient.util.PreferenceConstants;
import org.yaxim.androidclient.util.XMPPHelper;

import android.content.ContentValues;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class YaximConfiguration implements OnSharedPreferenceChangeListener {

	private static final String TAG = "YaximConfiguration";

	public String password;
	public String ressource;
	public int port;
	public int priority;
	public boolean bootstart;
	public boolean connStartup;
	public boolean reconnect;
	public String userName;
	public String server;

	public boolean isLEDNotify;
	public boolean isVibraNotify;

	private final SharedPreferences prefs;

	public YaximConfiguration(SharedPreferences _prefs) {
		prefs = _prefs;
		prefs.registerOnSharedPreferenceChangeListener(this);
		loadPrefs(prefs);
	}
	
	public void save(SQLiteDatabase db) {
		ContentValues values = new ContentValues();
		
		values.put("user_name", userName);
		values.put("password", password);
		values.put("server", server);
		values.put("port", port);
		values.put("ressource", ressource);
		values.put("default_priority", priority);
		values.put("auto_reconnect", reconnect);
		values.put("auto_connect", connStartup);
		
		db.insert(DataBaseHelper.ACCOUNTS, "user_name", values);
	}

	@Override
	public void finalize() {
		prefs.unregisterOnSharedPreferenceChangeListener(this);
	}
	
	public void onSharedPreferenceChanged(SharedPreferences prefs, String key) {
		Log.i(TAG, "onSharedPreferenceChanged(): " + key);
		loadPrefs(prefs);
	}


	private void splitAndSetJabberID(String jid) {
		String[] res = jid.split("@");
		this.userName = res[0];
		this.server = res[1];
	}

	private int validatePriority(int jabPriority) {
		if (jabPriority > 127)
			return 127;
		else if (jabPriority < -127)
			return -127;
		return jabPriority;
	}

	private void loadPrefs(SharedPreferences prefs) {
		this.isLEDNotify = prefs.getBoolean(PreferenceConstants.LEDNOTIFY,
				false);
		this.isVibraNotify = prefs.getBoolean(
				PreferenceConstants.VIBRATIONNOTIFY, false);
		this.password = prefs.getString(PreferenceConstants.PASSWORD, "");
		this.ressource = prefs.getString(PreferenceConstants.RESSOURCE,
				"Yaxim");
		this.port = XMPPHelper.tryToParseInt(prefs.getString(
				PreferenceConstants.PORT, PreferenceConstants.DEFAULT_PORT),
				PreferenceConstants.DEFAULT_PORT_INT);

		this.priority = validatePriority(XMPPHelper.tryToParseInt(prefs
				.getString("account_prio", "0"), 0));
		
		this.bootstart = prefs.getBoolean(
				PreferenceConstants.BOOTSTART, false);

		this.connStartup = prefs.getBoolean(PreferenceConstants.CONN_STARTUP,
				false);
		this.reconnect = prefs.getBoolean(
				PreferenceConstants.AUTO_RECONNECT, false);

		String jid = prefs.getString(PreferenceConstants.JID, "");

		try {
			XMPPHelper.verifyJabberID(jid);
			splitAndSetJabberID(jid);
		} catch (YaximXMPPAdressMalformedException e) {
			Log.e(TAG, "Exception in getPreferences(): " + e);
		}
	}
}
