package com.hamzah.holothemer; 

import static de.robv.android.xposed.XposedHelpers.findAndHookMethod;
import android.app.Activity;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.IXposedHookZygoteInit;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XSharedPreferences;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.callbacks.XC_LoadPackage.LoadPackageParam;
 
public class Main implements IXposedHookLoadPackage, IXposedHookZygoteInit{
	
	XSharedPreferences pref;
	XSharedPreferences hide_icon_pref;

	@Override
	public void handleLoadPackage(LoadPackageParam lpparam) throws Throwable {
		final String pname = lpparam.packageName;
		
		findAndHookMethod(Activity.class, "onCreate", Bundle.class, new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				pref.reload();
				hide_icon_pref.reload();
				Activity a = (Activity) param.thisObject;
				XposedBridge.log(a.getClass().getName());
				
				int option = pref.getInt(pname, 0);
				
				if(option==Keys.HOLO_DARK)
					a.setTheme(android.R.style.Theme_Holo);
				else if(option==Keys.HOLO_LIGHT)
					a.setTheme(android.R.style.Theme_Holo_Light);
				else if(option==Keys.HOLO_LIGHT_DARK_ACTIONBAR)
					a.setTheme(android.R.style.Theme_Holo_Light_DarkActionBar);
				
				else if(option==Keys.HOLO_DARK_NO_ACTIONBAR)
					a.setTheme(android.R.style.Theme_Holo_NoActionBar);
				else if(option==Keys.HOLO_LIGHT_NO_ACTIONBAR)
					a.setTheme(android.R.style.Theme_Holo_Light_NoActionBar);
				
				else if(option==Keys.WALLPAPER)
					a.setTheme(android.R.style.Theme_Holo_Wallpaper);
				else if(option==Keys.WALLPAPER_NO_ACTIONBAR)
					a.setTheme(android.R.style.Theme_Holo_Wallpaper_NoTitleBar);
				
				else if(option==Keys.DEVICE_DEFAULT)
					a.setTheme(android.R.style.Theme_DeviceDefault);
				else if(option==Keys.DEVICE_DEFAULT_LIGHT)
					a.setTheme(android.R.style.Theme_DeviceDefault_Light);
				
				else if(option==Keys.PURE_BLACK)
					a.setTheme(android.R.style.Theme_Black);
				else if(option==Keys.PURE_BLACK_TO_TITLEBAR)
					a.setTheme(android.R.style.Theme_Black_NoTitleBar);
				
				else if(option==Keys.WALLPAPER_SEMI_OPAQUE){
					a.setTheme(android.R.style.Theme_Holo_Wallpaper);
					a.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80000000")));
				}
				else if(option==Keys.WALLPAPER_NO_ACTIONBAR_SEMI_OPAQUE){
					a.setTheme(android.R.style.Theme_Holo_Wallpaper_NoTitleBar);
					a.getWindow().setBackgroundDrawable(new ColorDrawable(Color.parseColor("#80000000")));
				}
				
				
				if(hide_icon_pref.getBoolean(pname, false))
					a.getActionBar().setIcon(new ColorDrawable(a.getResources().getColor(android.R.color.transparent)));
			}
		});
		
		/*findAndHookMethod(Context.class, "setTheme", int.class, new XC_MethodHook(){
			@Override
			protected void beforeHookedMethod(MethodHookParam param)
					throws Throwable {
				param.args[0] = android.R.style.Theme_DeviceDefault_Light;
			}
		});*/
	}

	@Override
	public void initZygote(StartupParam startupParam) throws Throwable {
		pref = new XSharedPreferences("com.hamzah.holothemer", Keys.PREF_NAME);
		hide_icon_pref = new XSharedPreferences("com.hamzah.holothemer", Keys.HIDE_ICON);
	}
}