package ca.marklauman.smalltext;

import java.io.File;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ListingAdapter extends ArrayAdapter<File> {
	
	private static String dir_desc;
	private static String file_desc;
	
	private File[] files;
	
	public ListingAdapter(Context context) {
		super(context, R.layout.list_dir_listing);
		files = new File[0];
		Resources r = context.getResources();
		dir_desc = r.getString(R.string.dir);
		file_desc = r.getString(R.string.file);
	}
	
	public ListingAdapter(Context context, File[] listing) {
		super(context, R.layout.list_dir_listing, listing);
		files = listing;
		Resources r = context.getResources();
		dir_desc = r.getString(R.string.dir);
		file_desc = r.getString(R.string.file);
	}
	
	@Override
	public View getView(int position,
			View convertView, ViewGroup parent) {
		View res = View.inflate(getContext(),
				R.layout.list_dir_listing, null);
		TextView txt = (TextView) res.findViewById(android.R.id.text1);
		ImageView img = (ImageView) res.findViewById(android.R.id.icon);
		
		File file = files[position];
		txt.setText(file.getName());
		if(file.isDirectory()) {
			img.setImageResource(R.drawable.ic_dir);
			img.setContentDescription(dir_desc);
		} else {
			img.setImageResource(R.drawable.ic_file);
			img.setContentDescription(file_desc);
		}
		if(!file.canRead())
			txt.setTextColor(Color.RED);
		
		return res;
	}
}