package com.dlz.helper.sample;

import com.dlz.helper.sample.app.POISampleApplication;

public class SampleCodeMainApp {

	private static POISampleApplication poiApp = new POISampleApplication();

	public static void main(String[] s) {
		poiApp.generateSql();
	}

}
