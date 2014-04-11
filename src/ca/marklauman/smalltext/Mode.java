package ca.marklauman.smalltext;

import android.view.View;

public abstract class Mode {
	
	/** Called when the {@link View} of this
	 *  {@link Mode} is needed.           */
	public abstract View getView();
	
	/** Called every time this view is displayed.
	 *  (As opposed to the constructor, which is only
	 *  called once)                               */
	public abstract void onStart();
	
	/* Called when the mode is about to be destroyed.
	 * Used for final cleanup.                     */
	public abstract void onDestroy();
}
