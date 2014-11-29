package com.hamzah.holothemer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TableLayout;
import android.widget.TextView;

public class Addons extends ActionBarActivity {
	
	private class Addon{
		private String name;
		private String packageName;
		private String description;
		private String activity;
		private int version;
		private boolean installed;
		
		public Addon(String name, String packageName, String description, String activity, int version, boolean installed){
			this.name = name;
			this.packageName = packageName;
			this.description = description;
			this.activity = activity;
			this.version = version;
			this.installed = installed;
		}
	}
	
	File addon_XML;
	
	ArrayList<Addon> avilableAddons;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_addons);

		addon_XML = new File("/data/data/com.hamzah.holothemer/files/addons.xml");
	//	addon_XML.mkdir();
		
		try {
			if(addon_XML.exists())
				load();
			else
				noInstalledAddons();
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
		} catch (SAXException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void load() throws ParserConfigurationException, SAXException, IOException{
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse (addon_XML);
        doc.getDocumentElement ().normalize ();
        
        NodeList available_addons = doc.getElementsByTagName(Tags.ADDON);
        int number_of_addons = available_addons.getLength();
        
        avilableAddons = new ArrayList<Addon>();
        
        if(number_of_addons!=0){
	        for(int i=0; i<number_of_addons; i++){
	        	Element el = (Element)available_addons.item(i);
	        	Addon addon = getAddon(el);
	        	avilableAddons.add(addon);
	        }
	        
	        addButtons();
        }
        else
        	noInstalledAddons();
		
	}
	
	public void addButtons(){
		TableLayout table = (TableLayout) findViewById(R.id.addons_activity_content);
		
		int i = 0;
		while(i<avilableAddons.size()){
			final Addon a = avilableAddons.get(i);
			
			if(a.installed){
				Button b = new Button(this);
				boolean outdated = checkOutdated(a);
				
				String versionText = (outdated? "<font color=red>(UPDATE AVAILABLE)</font>" : "(Latest version)");
				
				b.setText(Html.fromHtml("<b>" + a.name + "</b> " + versionText + "<br>" + a.description));
				b.setOnClickListener(new OnClickListener(){
					@Override
					public void onClick(View v) {
						launch(a.packageName, a.activity);
					}
				});
				table.addView(b);
			}
			i++;
		}
	}
	
	public Addon getAddon(Element el){
		String name = getTextValue(el, Tags.NAME);
		String packageName = getTextValue(el, Tags.PACKAGE);
		String description = getTextValue(el, Tags.DESCRIPTION);
		String activity = getTextValue(el, Tags.ACTIVITY);
		int version = getIntValue(el, Tags.VERSION);
		boolean installed = isInstalled(packageName);
		Addon a = new Addon(name, packageName, description, activity ,version, installed);
		return a;
	}
	
	public void noInstalledAddons() {
		TextView tv = (TextView) findViewById(R.id.title_installed_addons);
		String text = (String) tv.getText();
		text = text + ": None";
		tv.setText(text);
	}
	
	public boolean isInstalled(String pname){
		PackageManager pm=getPackageManager();
		try {
			@SuppressWarnings("unused")
			PackageInfo info=pm.getPackageInfo(pname,PackageManager.GET_META_DATA);
		} catch (NameNotFoundException e) {
			return false;
		}  
		    return true;
	}
	
	private String getTextValue(Element ele, String tagName) {
		String textVal = null;
		NodeList nl = ele.getElementsByTagName(tagName);
		if(nl != null && nl.getLength() > 0) {
			Element el = (Element)nl.item(0);
			textVal = el.getFirstChild().getNodeValue();
		}

		return textVal;
	}
	
	public void launch(String pname, String activity){
		if (!activity.equals("no_launch")) {
			Intent intent = new Intent("android.intent.action.MAIN");
			intent.setComponent(ComponentName.unflattenFromString(pname + "/"
					+ activity));
			intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			startActivity(intent);
		}
	}
	
	public boolean checkOutdated(Addon a){
		boolean outdated = false;
		
		String pname = a.packageName;
		int latestVersion = a.version;
		
		PackageManager pm = this.getPackageManager();
		try {
			int currentVersion = pm.getPackageInfo(pname, 0).versionCode;
			
			if(currentVersion!=latestVersion)
				outdated=true;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}
		
		return outdated;
	}
	
	private int getIntValue(Element ele, String tagName) {
		return Integer.parseInt(getTextValue(ele,tagName));
	}	 
	
	public void getAddons(View v){
		Intent i = new Intent(this, Addons_Store.class);
		startActivity(i);
	}
}
