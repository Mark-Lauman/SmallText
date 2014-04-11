package ca.marklauman.smalltext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.AdapterView.OnItemSelectedListener;

public class ModeBrowse extends Mode {
	
	private static final String KEY_DIR = "cur_dir";
	
	private Context context;
	private ModeSwapper switch_mode;
	
	private View view;
	private Spinner dropdown;
	private DropdownAdapter drop_adapt;
	private ListView listing;
	private ListingAdapter listing_adapt;
	private ImageButton back;
	
	
	ModeBrowse(Context c, ModeSwapper swapper) {
		context = c;
		switch_mode = swapper;
		
		DropdownListener drop_listen = new DropdownListener ();
		FilelistListener listing_listen = new FilelistListener();
		
		view = View.inflate(c, R.layout.mode_browse, null);
		dropdown = (Spinner) view.findViewById(R.id.dropdown);
		dropdown.setOnItemSelectedListener(drop_listen);
		listing = (ListView) view.findViewById(R.id.file_list);
		listing.setOnItemClickListener(listing_listen);
		back = (ImageButton) view.findViewById(R.id.back_button);
		back.setOnClickListener(new BackListener());
		
		SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
		String dir = Environment.getExternalStorageDirectory()
								.getPath();
		dir = data.getString(KEY_DIR, dir);
		setDir(dir);
	}
	
	
	@Override
	public void onStart() {
		if(switch_mode.fileOpen())
			back.setVisibility(View.VISIBLE);
		else
			back.setVisibility(View.GONE);
	}
	
	
	@Override
	public View getView() {
		return view;
	}
	
	
	@Override
	public void onDestroy() {
		SharedPreferences.Editor edit =
				PreferenceManager.getDefaultSharedPreferences(context)
								 .edit();
		int pos = dropdown.getSelectedItemPosition();
		edit.putString(KEY_DIR, drop_adapt.getItem(pos));
		edit.commit();
	}
	
	
	/** Set the current directory being browsed.
	 *  If the directory, does not exist, this function
	 *  will go up one directory until it finds one
	 *  that does exist.
	 *  @param dir_path The fully qualified path of the
	 *  directory.                                  */
	public void setDir(String dir_path) {
		drop_adapt = new DropdownAdapter(context);
		
		ArrayList<String> paths = new ArrayList<String>();
		File dir = new File(dir_path);
		
		// Go up 1+ dirs until the dir exists
		while(dir != null && !dir.exists()) {
			dir = dir.getParentFile();
		}
		
		while(dir != null) {
			paths.add(dir.getPath());
			dir = dir.getParentFile();
		}
		Collections.reverse(paths);
		drop_adapt.addAll(paths);
		dropdown.setAdapter(drop_adapt);
		dropdown.setSelection(paths.size() - 1);
	}
	
	
	private void getListing(File dir) {
		File[] list = dir.listFiles();
		if(list == null)
			listing_adapt = new ListingAdapter(context);
		else
			listing_adapt = new ListingAdapter(context, list);
		listing.setAdapter(listing_adapt);
	}
	
	
	private class BackListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			switch_mode.fileMode();
		}
	}
	
	private class DropdownListener implements OnItemSelectedListener {
		@Override
		public void onItemSelected(AdapterView<?> parent,
				View view, int position, long id) {
			getListing(drop_adapt.getFile(position));
		}
		
		@Override
		public void onNothingSelected(AdapterView<?> parent) {}
	}
	
	
	private class FilelistListener implements OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent,
				View view, int position, long id) {
			File file = listing_adapt.getItem(position);
			if(file.isDirectory())
				setDir(file.getPath());
			else
				switch_mode.fileMode(file.getPath());
		}
	}
}