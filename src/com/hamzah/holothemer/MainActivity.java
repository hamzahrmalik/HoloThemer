package com.hamzah.holothemer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SearchView.OnQueryTextListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TableLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends ActionBarActivity {

	private class ListAdapter extends BaseAdapter {

		ArrayList<PInfo> data;
		LayoutInflater inflater;
		PInfo pinfo;

		public ListAdapter(ArrayList<PInfo> data) {
			this.data = data;
			inflater = getLayoutInflater();
		}

		@Override
		public int getCount() {
			return data.size();
		}

		@Override
		public Object getItem(int arg0) {
			return data.get(arg0);
		}

		@Override
		public long getItemId(int arg0) {
			return arg0;
		}

		class ViewHolder {
			TextView name, pname;
			ImageView icon;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {

			View vi = convertView;
			if (vi == null) {
				vi = inflater.inflate(R.layout.list_adapter, null);
				ViewHolder holder = new ViewHolder();
				holder.name = (TextView) vi.findViewById(R.id.adapter_name);
				holder.pname = (TextView) vi.findViewById(R.id.adapter_package);
				holder.icon = (ImageView) vi.findViewById(R.id.adapter_icon);
				vi.setTag(holder);
			}

			ViewHolder holder = (ViewHolder) vi.getTag();

			pinfo = null;
			pinfo = data.get(position);
			holder.name.setText(pinfo.appname);
			holder.pname.setText(pinfo.pname);
			holder.icon.setImageDrawable(pinfo.icon);
			return vi;
		}

	}

	File addon_XML;
	String URL = "https://docs.google.com/uc?authuser=0&id=0B-TJgzL2nB-WNXN3cWloT2tqd2M&export=download";

	private class DownloadTask extends AsyncTask<String, Integer, String> {

		@Override
		protected String doInBackground(String... params) {
			try {
				downloadFile(URL, addon_XML.getAbsolutePath());
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}

	}

	ListView app_list;

	ArrayList<PInfo> pinfos;

	Spinner spinner;
	CheckBox hide_icon;
	TableLayout layout;

	SharedPreferences pref, hide_icon_pref;

	ListAdapter adapter;

	// does the device support material, ie is higher than API 21
	boolean material = false;

	class PInfo {
		private String appname = "";
		private String pname = "";
		private Drawable icon = null;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		load();

		addon_XML = new File(
				"/data/data/com.hamzah.holothemer/files/addons.xml");
		File f = new File("/data/data/com.hamzah.holothemer/files");
		f.mkdirs();

		final DownloadTask downloadTask = new DownloadTask();
		downloadTask.execute(URL);

		AdView adView = (AdView) this.findViewById(R.id.adView);
		AdRequest adRequest = new AdRequest.Builder().build();
		adView.loadAd(adRequest);

		Toast.makeText(this, "Press menu for more options", Toast.LENGTH_LONG)
				.show();

		checkFirstTime();

		if (Build.VERSION.SDK_INT >= 21)
			material = true;
	}

	@SuppressWarnings("deprecation")
	public void load() {
		pref = getSharedPreferences(Keys.PREF_NAME, Context.MODE_WORLD_READABLE);
		hide_icon_pref = getSharedPreferences(Keys.HIDE_ICON,
				Context.MODE_WORLD_READABLE);

		app_list = (ListView) findViewById(R.id.app_list_view);

		pinfos = getInstalledApps();

		Collections.sort(pinfos, new Comparator<PInfo>() {

			@Override
			public int compare(PInfo lhs, PInfo rhs) {
				return lhs.appname.compareTo(rhs.appname);
			}
		});

		adapter = new ListAdapter(pinfos);
		app_list.setAdapter(adapter);
		app_list.setOnItemClickListener(new OnItemClickListener() {
			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				selected(position);
				Log.d("SELECTED APP ", "" + position);
			}
		});
		app_list.setOnItemLongClickListener(new OnItemLongClickListener() {

			@Override
			public boolean onItemLongClick(AdapterView<?> parent, View view,
					int position, long id) {
				launch(position);
				return false;
			}
		});
	}

	public void selected(final int pos) {
		final Context c = this;
		AlertDialog.Builder builder = new AlertDialog.Builder(c);
		builder.setTitle(pinfos.get(pos).appname);
		builder.setMessage("What theme do you want this app to use?");

		String[] list = { "Default", "Holo Dark", "Holo Light",
				"Light with Dark Actionbar", "Dark - No Actionbar",
				"Light - No Actionbar", "User Wallpaper",
				"User Wallpaper - No Titlebar", "Device Default",
				"Device Default - Light", "Pure Black",
				"Pure Black - No Titlebar", "User Wallpaper - Semi opaque",
				"User Wallpaper - No Titlebar - Semi opaque" };
		if (material)// change the list is supports material
			list = new String[] { "Default", "Holo Dark", "Holo Light",
					"Light with Dark Actionbar", "Dark - No Actionbar",
					"Light - No Actionbar", "User Wallpaper",
					"User Wallpaper - No Titlebar", "Device Default",
					"Device Default - Light", "Pure Black",
					"Pure Black - No Titlebar", "User Wallpaper - Semi opaque",
					"User Wallpaper - No Titlebar - Semi opaque",
					"Material Dark", "Material Light",
					"Material Light with Dark Actionbar" };

		ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
				android.R.layout.simple_spinner_item, list);

		adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

		spinner = new Spinner(c);
		hide_icon = new CheckBox(c);
		hide_icon.setText("Hide app icon in Actionbar");
		layout = new TableLayout(c);

		spinner.setAdapter(adapter);
		spinner.setSelection(pref.getInt(pinfos.get(pos).pname, 0));
		layout.addView(spinner);
		layout.addView(hide_icon);
		builder.setView(layout);

		builder.setPositiveButton("Save", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Editor editor = pref.edit();
				editor.putInt(pinfos.get(pos).pname,
						spinner.getSelectedItemPosition());

				editor.apply();
				Editor editor1 = hide_icon_pref.edit();
				editor1.putBoolean(pinfos.get(pos).pname, hide_icon.isChecked());
				editor1.apply();
				Toast.makeText(c,
						"Settings saved for " + pinfos.get(pos).appname,
						Toast.LENGTH_SHORT).show();
			}
		});

		builder.show();
	}

	public void launch(final int pos) {
		Intent i = getPackageManager().getLaunchIntentForPackage(
				pinfos.get(pos).pname);
		if (i != null)
			startActivity(i);
	}

	public ArrayList<PInfo> getInstalledApps() {
		ArrayList<PInfo> res = new ArrayList<PInfo>();
		List<PackageInfo> packs = getPackageManager().getInstalledPackages(0);
		for (int i = 0; i < packs.size(); i++) {
			PackageInfo p = packs.get(i);
			PInfo newInfo = new PInfo();
			newInfo.appname = p.applicationInfo.loadLabel(getPackageManager())
					.toString();
			newInfo.pname = p.packageName;
			newInfo.icon = p.applicationInfo.loadIcon(getPackageManager());
			if (!newInfo.pname.contains("com.hamzah.holothemer"))
				res.add(newInfo);
		}
		return res;
	}

	public void checkFirstTime() {
		boolean firstTime = pref.getBoolean(Keys.FIRST_TIME, true);
		if (firstTime) {
			Editor editor = pref.edit();
			editor.putBoolean(Keys.FIRST_TIME, false);
			editor.apply();
			AlertDialog.Builder builder = new AlertDialog.Builder(this);
			builder.setTitle("Welcome to Holo Themer!");
			builder.setMessage("Please remember the following points when using this app\n"
					+ "Some apps won't work\n"
					+ "Only use actionbar themes on apps that have an actionbar and vice versa\n\n"
					+ "Tap an app to change theme\n"
					+ "Long hold to launch the app\n\n"
					+ "Thanks for trying Holo Themer, enjoy!");
			builder.show();
		}
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menu_xda:
			XDA();
			break;
		case R.id.menu_addons:
			addons();
			break;
		case R.id.menu_backup:
			backups();
			break;
		case R.id.menu_reload:
			reload();
			break;
		case R.id.menu_create_custom:
			createCustom();
			break;
		}
		return true;
	}

	public void XDA() {
		Intent browserIntent = new Intent(
				Intent.ACTION_VIEW,
				Uri.parse("http://forum.xda-developers.com/xposed/modules/mod-holo-themer-force-holo-dark-light-t2768706"));
		startActivity(browserIntent);
	}

	public void addons() {
		Intent intent = new Intent(this, Addons.class);
		startActivity(intent);
	}

	public void backups() {
		Intent intent = new Intent(this, Backup.class);
		startActivity(intent);
	}

	public void reload() {
		finish();
		startActivity(getIntent());
	}

	public void createCustom() {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Buy Premium");
		builder.setMessage("Please consider buying premium version for just £0.69 ($0.99). You will get the following\n\n"
				+ "Quicker updates\nNo adverts\nCreate custom themes\n\nThank you");
		builder.setPositiveButton("Buy (Play Store)", new OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				try {
					startActivity(new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("market://details?id=com.hamzah.holothemer.premium")));
				} catch (android.content.ActivityNotFoundException anfe) {
					startActivity(new Intent(
							Intent.ACTION_VIEW,
							Uri.parse("http://play.google.com/store/apps/details?id=com.hamzah.holothemer.premium")));
				}
			}
		});
		builder.show();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);

		// final Context context = this;

		// Create the search view
		final SearchView searchView = new SearchView(getSupportActionBar()
				.getThemedContext());
		searchView.setQueryHint("Search");

		menu.add(Menu.NONE, Menu.NONE, 1, "Search")
				.setIcon(R.drawable.ic_search_white_48dp)
				.setActionView(searchView)
				.setShowAsAction(
						MenuItem.SHOW_AS_ACTION_ALWAYS
								| MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);

		searchView.setOnQueryTextListener(new OnQueryTextListener() {
			@Override
			public boolean onQueryTextChange(String newText) {
				/*
				 * if (newText.length() > 0) {
				 * adapter.getFilter().filter(newText); } else
				 * adapter.getFilter().filter("");
				 */
				return false;
			}

			@Override
			public boolean onQueryTextSubmit(String query) {

				return false;
			}
		});

		return true;
	}

	public void downloadFile(String url, String dest_file_path)
			throws IOException {
		File dest_file = addon_XML;
		URL u = new URL(url);
		URLConnection conn = u.openConnection();
		int contentLength = conn.getContentLength();
		DataInputStream stream = new DataInputStream(u.openStream());
		byte[] buffer = new byte[contentLength];
		stream.readFully(buffer);
		stream.close();
		DataOutputStream fos = new DataOutputStream(new FileOutputStream(
				dest_file));
		fos.write(buffer);
		fos.flush();
		fos.close();
	}
}
