package ca.marklauman.smalltext;

public interface ModeSwapper {
	
	/** Trigger file browsing. */
	public void browseMode();
	
	/** Trigger file viewing mode. */
	public void fileMode();
	
	/** Trigger file viewing mode with the provided path.
	 *  @param filepath The path of the file to open. */
	public void fileMode(String filepath);
	
	/** Checks to see if a file is open.
	 *  @return {@code true} if any number of
	 *  files are open.                    */
	public boolean fileOpen();
}
