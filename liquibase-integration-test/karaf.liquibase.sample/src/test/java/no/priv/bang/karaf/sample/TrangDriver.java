package no.priv.bang.karaf.sample;

import com.thaiopensource.relaxng.translate.Driver;

/**
 * This is a replacement for the Driver class of the trang
 * XML schema converter.  The difference is that this class
 * does not call System.exit() which abruptly brings down
 * maven and also eclipse opening a maven project.
 */
public class TrangDriver {

	public static void main(String[] args) {
		new Driver().run(args);
	}

}
