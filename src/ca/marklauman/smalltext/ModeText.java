package ca.marklauman.smalltext;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

import ca.marklauman.smalltext.DropdownAdapter.OnCloseListener;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

public class ModeText extends Mode {
	
	private static final int MAX_SEGMENT = 9000;
	private static final String KEY_SELECT = "cur_file";
	private static final String KEY_FILENAMES = "open_files";
	private static final String KEY_SCROLL_IND = "scroll_item:";
	private static final String KEY_SCROLL_OFF = "scroll_offset:";
	
	private static String open;
	private static String loading;
	private static String err_nofile;
	private static String err_access;
	private static String err_parse;

	private ModeSwapper switch_mode;
	private Context context;
	private View view;
	private Spinner dropdown;
	private DropdownAdapter drop_adapt;
	private ListView txt_list;
	
	private int cur_file;
	
	ModeText(Context c, ModeSwapper swapper) {
		// String resources
		Resources r = c.getResources();
		open = r.getString(R.string.open);
		loading = r.getString(R.string.loading);
		err_nofile = r.getString(R.string.err_nofile);
		err_access = r.getString(R.string.err_access);
		err_parse = r.getString(R.string.err_parse);
		
		context = c;
		switch_mode = swapper;
		
		// View resources
		view = View.inflate(c, R.layout.mode_text,
									null);
		dropdown = (Spinner) view.findViewById(R.id.dropdown);
		DropdownListener drop_list = new DropdownListener();
		dropdown.setOnItemSelectedListener(drop_list);
		drop_adapt = new DropdownAdapter(c, drop_list);
		dropdown.setAdapter(drop_adapt);
		txt_list = (ListView) view.findViewById(R.id.text_panel);
		ArrayAdapter<String> txt_adapter = new ArrayAdapter<String>(c, R.layout.list_text_view);
		txt_list.setAdapter(txt_adapter);
		
		// Load data
		SharedPreferences data = PreferenceManager.getDefaultSharedPreferences(context);
		drop_adapt.add(open);
		drop_adapt.addAll(data.getStringSet(KEY_FILENAMES, new HashSet<String>()));
		
		String sel_str = data.getString(KEY_SELECT, null);
		if(sel_str == null) return;
		int sel = drop_adapt.find(sel_str);
		if(sel == -1) return;
		dropdown.setSelection(sel);
	}
	
	@Override
	public void onStart() {
		dropdown.setSelection(cur_file);
	}
	
	@Override
	public View getView() {
		return view;
	}
	
	@Override
	public void onDestroy() {
		if(0 < cur_file) saveScroll();
		
		SharedPreferences.Editor edit =
				PreferenceManager.getDefaultSharedPreferences(context)
								 .edit();
		edit.putStringSet(KEY_FILENAMES,
						  drop_adapt.getFilenames());
		if(1 < drop_adapt.size())
			edit.putString(KEY_SELECT,
					       drop_adapt.getItem(cur_file));
		else
			edit.putString(KEY_SELECT, null);
		edit.commit();
	}
	
	
	/** Checks to see if a file is open.
	 *  @return {@code true} if a file is open.                    */
	public boolean isFileOpen() {
		return 1 < drop_adapt.size();
	}
	
	/** Set the specified file to be the one on display.
	 *  If the file was not opened previously, it is
	 *  opened now.
	 *  @param filepath The path of the file to open. */
	public void setFile(String filepath) {
		int sel = drop_adapt.find(filepath);
		if(sel < 0) {
			drop_adapt.add(filepath);
			sel = drop_adapt.find(filepath);
		}
		dropdown.setSelection(sel);
		cur_file = sel;
	}
	
	
	/** Display text in the text field.
	 *  @param txt The text to show. */
	private void showText(String txt) {
		ArrayAdapter<String> txt_adapter = new ArrayAdapter<String>(
				context,
				R.layout.list_text_view,
				new String[] {txt});
		txt_list.setAdapter(txt_adapter);
	}
	
	
	/** Display an error message in the text field.
	 *  @param msg The error message to display. */
	private void showErr(String msg) {
		ArrayAdapter<String> txt_adapter = new ArrayAdapter<String>(
				context,
				R.layout.list_text_view,
				new String[] {msg});
		txt_list.setAdapter(txt_adapter);
	}
	
	
	/** Load the provided file and place it in the list
	 *  adapter in MAX_SEGMENT character chunks.
	 *  @param file The file to load.                */
	private void loadFile(File file) {
		showText(loading);
		
		// basic error checking
		if(!file.exists() || !file.isFile()) {
			showErr(err_nofile);
			return;
		} if(!file.canRead()) {
			showErr(err_access);
			return;
		}
		
		// try to load the file
		StringBuilder item = new StringBuilder(MAX_SEGMENT);
		ArrayList<String> res = new ArrayList<String>();
		BufferedReader in = null;
		try {
			FileReader read = new FileReader(file);
			in = new BufferedReader(read);
			String line = in.readLine();
			while(line != null) {
				if(MAX_SEGMENT < line.length() + item.length()) {
					res.add(item.toString().substring(1));
					item = new StringBuilder(MAX_SEGMENT);
				}
				item.append('\n')
					.append(line);
				line = in.readLine();
			}
			ArrayAdapter<String> txt_adapter = new ArrayAdapter<String>(
					context,
					R.layout.list_text_view,
					res);
			txt_list.setAdapter(txt_adapter);
		} catch (FileNotFoundException e) {
			showErr(err_nofile);
		} catch (IOException e) {
			showErr(err_parse);
		} finally {
			try{ if(in != null) in.close(); }
			catch(Exception e) {}
		}
		
		loadScroll();
	}
	
	/** Save the current scroll position to
	 *  internal storage. Typically called just before
	 *  a file is swapped out.
	 *  {@link #loadScroll()} may be called to
	 *  retrieve this position, and {@link #forgetScroll()}
	 *  to forget it.                                    */
	private void saveScroll() {
		String file = drop_adapt.getItem(cur_file);
		int index = txt_list.getFirstVisiblePosition();
		View first = txt_list.getChildAt(0);
		int offset = 0;
		if(first != null) offset = first.getTop();
		PreferenceManager.getDefaultSharedPreferences(context)
		 				 .edit()
		 				 .putInt(KEY_SCROLL_IND + file, index)
		 				 .putInt(KEY_SCROLL_OFF + file, offset)
		 				 .commit();
	}
	
	/** Load the scroll position of the current file into
	 *  memory, and apply it to the {@link #txt_list}.
	 *  Typically called just after a file is loaded. */
	private void loadScroll() {
		String file = drop_adapt.getItem(cur_file);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
		int index = prefs.getInt(KEY_SCROLL_IND + file, 0);
		int offset = prefs.getInt(KEY_SCROLL_OFF + file, 0);
		txt_list.setSelectionFromTop(index, offset);
	}
	
	/** Forget the scroll position of the provided file.
	 *  Often called just before a file is closed.
	 *  @param filepath The path of the file to forget. */
	private void forgetScroll(String filepath) {
		PreferenceManager.getDefaultSharedPreferences(context)
		 				 .edit()
		 				 .remove(KEY_SCROLL_IND + filepath)
		 				 .remove(KEY_SCROLL_OFF + filepath)
		 				 .commit();
	}
	
	
	private class DropdownListener implements OnItemSelectedListener,
    										  OnCloseListener {
		@Override
		public void onItemSelected(AdapterView<?> parent,
				View view, int position, long id) {
			if(0 < cur_file && cur_file < drop_adapt.size())
				saveScroll();
			
			if(position == 0) {
				switch_mode.browseMode();
				return;
			}
			cur_file = position;
			loadFile(drop_adapt.getFile(position));
		}
		
		@Override
		public void onNothingSelected(AdapterView<?> parent) {}
		
		@Override
		public void onCloseClick(View view, int position) {
			int old_select = dropdown.getSelectedItemPosition();
			forgetScroll(drop_adapt.getItem(position));
			drop_adapt.remove(drop_adapt.getItem(position));
			
			/* Dropdown selection changes asynchronously.
			 * This updates the current file to match. */
			if(position < old_select)
				cur_file--;
			else if(position == old_select
					&& position == 1
					&& 1 < drop_adapt.size()) {
				cur_file = 1;
				dropdown.setSelection(1);
				onItemSelected(null, null, 1, 0);
			}
		}
	}
}