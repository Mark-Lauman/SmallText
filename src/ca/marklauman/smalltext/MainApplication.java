package ca.marklauman.smalltext;

import android.widget.ViewAnimator;

import com.sony.smallapp.SmallAppWindow;
import com.sony.smallapp.SmallApplication;

public class MainApplication extends SmallApplication
							 implements ModeSwapper {
	private static final int TEXT_MODE_ID = 0;
	private static final int BROWSE_MODE_ID = 1;
	
	ViewAnimator switcher;
	ModeText text_mode;
	ModeBrowse browse_mode;
	
	@Override
    public void onCreate() {
		super.onCreate();
		setContentView(R.layout.main);
        setTitle(R.string.app_name);
        
        // Sony SmallApp Setup
        SmallAppWindow.Attributes attr = getWindow().getAttributes();
        attr.minWidth = 100; /* The minimum width of the application, if it's resizable.*/
        attr.minHeight = 100; /*The minimum height of the application, if it's resizable.*/
        attr.width = 400;  /*The requested width of the application.*/
        attr.height = 300;  /*The requested height of the application.*/
        /* Use this flag to enable the application window to be resizable*/
        attr.flags |= SmallAppWindow.Attributes.FLAG_RESIZABLE;
        /* Use this flag to remove the titlebar from the window*/
        attr.flags |= SmallAppWindow.Attributes.FLAG_NO_TITLEBAR;
        getWindow().setAttributes(attr);
        
        switcher = (ViewAnimator) findViewById(R.id.switcher);
        text_mode = new ModeText(this, this);
        browse_mode = new ModeBrowse(this, this);
        switcher.addView(text_mode.getView());
        switcher.addView(browse_mode.getView());
        switcher.setDisplayedChild(TEXT_MODE_ID);
	}
	
    @Override
    public void onStart() {
        super.onStart();
    }
    
    @Override
    public void onStop() {
        super.onStop();
    }
    
    @Override
    public void onDestroy() {
        text_mode.onDestroy();
        browse_mode.onDestroy();
        super.onDestroy();
    }

	@Override
	public void fileMode() {
		text_mode.onStart();
		switcher.setDisplayedChild(TEXT_MODE_ID);
	}
    
	@Override
	public void fileMode(String filepath) {
		// TODO: NOT DONE
		text_mode.setFile(filepath);
		text_mode.onStart();
		switcher.setDisplayedChild(TEXT_MODE_ID);
	}
	
	@Override
	public void browseMode() {
		browse_mode.onStart();
		switcher.setDisplayedChild(BROWSE_MODE_ID);
	}

	@Override
	public boolean fileOpen() {
		return text_mode.isFileOpen();
	}
}