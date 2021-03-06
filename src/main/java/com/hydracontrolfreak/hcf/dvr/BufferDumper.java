package com.hydracontrolfreak.hcf.dvr;

import com.hydracontrolfreak.hcf.freak.api.FreakApi;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class BufferDumper {
	private FreakApi freak;
	static final Logger logger = Logger.getLogger(BufferDumper.class);

	public BufferDumper(FreakApi freak) {
		this.freak = freak;
	}

	void dumpImage(String cam, ImageBuffer imageBuffer) {
		try {
			String dir = "tmp_images/" + cam + "/";
			String filename = imageBuffer.getTimestamp() + ".jpg";

			File dirFile = new File(freak.getHcfBase() + "/disk/hcf/" + dir);
			dirFile.mkdirs();
			File file = new File(freak.getHcfBase() + "/disk/hcf/" + dir
					+ filename);

			if (logger.isDebugEnabled())
				logger.debug("Filename \"" + filename + "\"");
			FileOutputStream outStream = new FileOutputStream(file);
			logger.debug("Writing " + imageBuffer.size());
			outStream.write(imageBuffer.getBuffer(), 0, imageBuffer.size());
			outStream.close();

		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
