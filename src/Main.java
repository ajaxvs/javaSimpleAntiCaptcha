import java.awt.Point;
import java.nio.file.Files;
import java.nio.file.LinkOption;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import ru.ajaxvs.CajFuns;
import ru.ajaxvs.images.CajImage;

/**
 * java simple anticaptcha - images recognizing test.
 * @author ajaxvs.
 */
public class Main {
	//========================================
	/**
	 * Application entry point.
	 * @param args getting captcha URL from command line.
	 */
	static public void main(String[] args) {
		//test();		
		
		if (args.length < 1) {
			CajFuns.trace("error: launch with URL argument");
			return;
		}
		
		String sUrl = args[0];
		CajFuns.trace(getCaptcha(sUrl));
	}
	//========================================
	/**
	 * @private
	 */
	static private void test() {
		try {
			String testDir = "file:///" + CajFuns.getAppPath(Main.class) + "testCaptcha/";
			
			if (   getCaptcha(testDir + "1.gif").equals("794185")
				&& getCaptcha(testDir + "2.gif").equals("967218")
				&& getCaptcha(testDir + "3.gif").equals("231923")
				&& getCaptcha(testDir + "4.gif").equals("119273")
				) {
				CajFuns.trace("test: passed.");
			} else {
				CajFuns.trace("test: failed.");
			}
		} catch (Exception e) {
			CajFuns.trace("test error: " + e);
		}
	}
	//========================================
	/**
	 * Anticaptcha recognizing function.
	 * @param sUrl captcha URL. i.e. "http://ex.com/1.png"
	 * @return recognized string.
	 */
	static private String getCaptcha(String sUrl) {
		try {
			//loading captcha image:
			CajImage imgCap = new CajImage("cap");
			imgCap.load(sUrl, false, true);
			
			//loading symbol images for recognizing:
			String appDir = CajFuns.getAppPath(Main.class);			
			List<CajImage> aNumbers = new ArrayList<CajImage>();			
			for (int i = 0; i <= 9; i++) {
				String id = Integer.toString(i);
				CajImage imgBuf = new CajImage(id);				
				String address = appDir + "img/" + id + ".png";
				imgBuf.load(address, true, true);
				imgBuf.setIgnoredColor(true, 0xFF000000); //black by default.
				aNumbers.add(imgBuf);
			}
			
			//recognizing:
			Map<Integer, String> aResult = new TreeMap<Integer, String>();			
			for (CajImage imgBuf : aNumbers) {
				Point p;
				int startX = 0;
				for (;;) {
					p = imgCap.findInside(imgBuf, startX, 0);
					if (p == null) {
						break;
					} else {
						startX = p.x + 1;
						aResult.put(p.x, imgBuf.getId());
					}
				}
			}
			
			//getting result:
			if (aResult.size() == 0) {
				CajFuns.trace("getCaptcha error: captcha wasn't found.");
			} else {
				String res = "";
				Iterator<String> it = aResult.values().iterator();
				while (it.hasNext()) {
					res += it.next();
				}				
				return res;
			}
			
		} catch (Exception e) {
			CajFuns.trace("getCaptcha error: " + sUrl + " " + e);
		}
		
		return "";
	}
	//========================================	

}
