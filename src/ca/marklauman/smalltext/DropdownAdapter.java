package ca.marklauman.smalltext;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import android.content.Context;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class DropdownAdapter extends ArrayAdapter<String> {
	
	private ArrayList<String> entries;
	private ClickListener click_list;
	private OnCloseListener closer;
	
	
	public interface OnCloseListener {
		public abstract void onCloseClick(View view, int position);
	}
	
	
	public DropdownAdapter(Context context) {
		super(context, R.layout.spinner_item);
		this.setDropDownViewResource(R.layout.spinner_dropdown);
		click_list = new ClickListener();
		entries = new ArrayList<String>();
		closer = null;
	}
	
	
	public DropdownAdapter(Context context, OnCloseListener listener) {
		super(context, R.layout.spinner_item);
		this.setDropDownViewResource(R.layout.spinner_dropdown);
		click_list = new ClickListener();
		entries = new ArrayList<String>();
		closer = listener;
	}
	
	
	@Override
	public void add(String entry) {
		super.add(entry);
		entries.add(entry);
	}
	
	
	@Override
	public void addAll(Collection<? extends String> collection) {
		super.addAll(collection);
		entries.addAll(collection);
	}
	
	
	@Override
	public void remove(String entry) {
		super.remove(entry);
		entries.remove(entry);
	}
	
	
	@Override
	public View getView(int position, View convertView,
						ViewGroup parent) {
		View res = super.getView(
				position, convertView, parent);
		String entry = entries.get(position);
		
		if(!entry.startsWith("/"))
			return res;
		
		TextView txt = (TextView) res.findViewById(android.R.id.text1);
		String fname = getFile(position).getName();
		if("".equals(fname)) fname = "/";
		txt.setText(fname);
		return res;
	}
	
	
	@Override
	public View getDropDownView(int position,
			View convertView, ViewGroup parent) {
		View res = View.inflate(getContext(),
				R.layout.spinner_dropdown, null);
		String entry = entries.get(position);
		
		TextView txt = (TextView) res.findViewById(android.R.id.text1);
		ImageView close_btn = (ImageView) res.findViewById(R.id.close_btn);
		close_btn.setTag(position);
		close_btn.setOnClickListener(click_list);
		
		if(!entry.startsWith("/")) {
			txt.setText(entry);
			close_btn.setVisibility(View.GONE);
			return res;
		}
		
		File file = getFile(position);
		String fname = file.getName();
		if("".equals(fname)) fname = "/";
		if(file.isDirectory())
			close_btn.setVisibility(View.GONE);
		txt.setText(fname);
		return res;
	}
	
	
	public void setOnCloseListener(OnCloseListener l) {
		closer = l;
	}
	
	
	public File getFile(int position) {
		return new File(entries.get(position));
	}
	
	
	public HashSet<String> getFilenames() {
		HashSet<String> names = new HashSet<String>(entries.size());
		for(String entry : entries) {
			if(entry.startsWith("/"))
				names.add(entry);
		}
		return names;
	}
	
	/** Searches this adapter for the specified String
	 *  and returns the index of the first occurrence.
	 *  @param s the String to search for.
	 *  @return the index of the first occurrence
	 *  of the object, or -1 if it was not found.    */
	public int find(String s) {
		return entries.indexOf(s);
	}
	
	public int size() {
		return entries.size();
	}
	
	private class ClickListener implements OnClickListener {
		@Override
		public void onClick(View v) {
			if(closer != null)
				closer.onCloseClick(v, (Integer)v.getTag());
		}
	}
}