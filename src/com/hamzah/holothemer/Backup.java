package com.hamzah.holothemer;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.channels.FileChannel;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class Backup extends ActionBarActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_backup);
	}
	
	EditText input;
	ListView backups;
	String [] backup_list;
	
	public static final String pname = "com.hamzah.holothemer";
	
	public void backup(View v){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Backup current settings");
		builder.setMessage("Choose backup title. Exisiting backups with same name will be overwritten");
		input = new EditText(this);
		builder.setView(input);
		builder.setPositiveButton("Backup", new OnClickListener(){
			@Override
			public void onClick(DialogInterface dialog, int which) {
				String fileName = input.getText().toString();
				if(!fileName.isEmpty()){
					try {
						File dest = new File(Environment.getExternalStorageDirectory() + "/HoloThemer/backups/" + fileName);
						File src = new File("/data/data/" + pname + "/shared_prefs");
						copyFolder(src, dest);
						Toast.makeText(Backup.this, "Successfully backed up to " + dest.getAbsolutePath(), Toast.LENGTH_LONG).show();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		});
		builder.show();
	}
	
	public void restore(View v){
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setTitle("Backup current settings");
		builder.setMessage("Choose backup to restore");
		
		backups = new ListView(this);
		File backups_dir = new File(Environment.getExternalStorageDirectory() + "/HoloThemer/backups");
		backup_list = backups_dir.list();
		ArrayAdapter<String> adapter = new ArrayAdapter<String>(
                this, 
                android.R.layout.simple_list_item_1,
                backup_list);
		backups.setAdapter(adapter);
		
		builder.setView(backups);
		
		backups.setOnItemClickListener(new OnItemClickListener(){

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				// Log.d("SELECTED", backup_list[position]);
				try {
					String selected = backup_list[position];

					File src = new File(Environment
							.getExternalStorageDirectory()
							+ "/HoloThemer/backups/" + selected);
					File dest = new File(
							"/data/data/" + pname + "/shared_prefs");
					copyFolder(src, dest);
					
				} catch (IOException e) {
					e.printStackTrace();
				}
				Toast.makeText(Backup.this, "Restore Complete",
						Toast.LENGTH_SHORT).show();
			}

		});

		builder.show();
	}
	
	public void copy(File src, File dst) throws IOException {
	    FileInputStream inStream = new FileInputStream(src);
	    FileOutputStream outStream = new FileOutputStream(dst);
	    FileChannel inChannel = inStream.getChannel();
	    FileChannel outChannel = outStream.getChannel();
	    inChannel.transferTo(0, inChannel.size(), outChannel);
	    inStream.close();
	    outStream.close();
	}
	
	public static void copyFolder(File src, File dest)
	    	throws IOException{
	 
	    	if(src.isDirectory()){
	 
	    		//if directory not exists, create it
	    		if(!dest.exists()){
	    		   dest.mkdirs();
	    		   System.out.println("Directory copied from " 
	                              + src + "  to " + dest);
	    		}
	 
	    		//list all the directory contents
	    		String files[] = src.list();
	 
	    		for (String file : files) {
	    		   //construct the src and dest file structure
	    		   File srcFile = new File(src, file);
	    		   File destFile = new File(dest, file);
	    		   //recursive copy
	    		   copyFolder(srcFile,destFile);
	    		}
	 
	    	}else{
	    		//if file, then copy it
	    		//Use bytes stream to support all file types
	    		InputStream in = new FileInputStream(src);
	    	        OutputStream out = new FileOutputStream(dest); 
	 
	    	        byte[] buffer = new byte[1024];
	 
	    	        int length;
	    	        //copy the file content in bytes 
	    	        while ((length = in.read(buffer)) > 0){
	    	    	   out.write(buffer, 0, length);
	    	        }
	 
	    	        in.close();
	    	        out.close();
	    	        System.out.println("File copied from " + src + " to " + dest);
	    	        dest.setReadable(true, false);
	    	}
	    }
}
