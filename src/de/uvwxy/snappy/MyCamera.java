package de.uvwxy.snappy;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import android.hardware.Camera;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.Size;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceHolder.Callback;
import android.widget.CheckBox;
import android.widget.TextView;

public class MyCamera {
	private File path; // Directory to save pictures in
	private SurfaceHolder surfaceHolder; // The surface used to preview an image

	private int surface_width; // when preview surface is created remember width
	private int surface_height; // and height

	private Camera camera; // the actual interface to the camera
	private Parameters parameters; // parameters for the camera
	boolean ready = false;

	private TextView tvInfo;
	private CheckBox cbPreview;
	private int img_count = 0;
	
	private Focus focus;
	
	boolean isOpen = false;
	public MyCamera(String path, SurfaceHolder surfaceHolder, TextView tvInfo, CheckBox cbPreview) {
		this.path = new File("/sdcard/" + path);
		this.path.mkdir();
		Log.i("SNAPPY", "Created " + path.toString());
		this.tvInfo = tvInfo;
		this.cbPreview = cbPreview;
		this.surfaceHolder = surfaceHolder;
		this.surfaceHolder.addCallback(surfaceCallback);
		this.surfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
	}

	public void setFocus(Focus focus) {
		this.focus = focus;
	}

	private Callback surfaceCallback = new Callback() {
		public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
			// TODO Auto-generated method stub
			ready = true;
			Log.i("SNAPPY", "Surface changed");
			surface_width = arg2;
			surface_height = arg3;
		}

		public void surfaceCreated(SurfaceHolder arg0) {
		}

		public void surfaceDestroyed(SurfaceHolder arg0) {
		}
	};
	public PictureCallback jpeg = new PictureCallback() {
		public void onPictureTaken(byte[] data, Camera arg1) {
			// write jpeg data
			Log.i("SNAPPY", "Callback: jpeg");
			try {
				// Get date and time:
				Calendar cal = Calendar.getInstance();
				SimpleDateFormat sdf = new SimpleDateFormat("yyyy_MM_dd__HH_mm_ss");
				String date = sdf.format(cal.getTime());
				// Create file
				File f = new File(path + "/" + date + ".jpg");
				tvInfo.setText("Last file: " + path + "/" + date + ".jpg  (" + ++img_count  + ")");
				Log.i("SNAPPY", "Saving to " + f.toString());
				FileOutputStream os = new FileOutputStream(f);
				BufferedOutputStream bos = new BufferedOutputStream(os);
				bos.write(data);
				bos.close();
			} catch (Exception e) {// Catch exception if any
				Log.i("SNAPPY", "Error: " + e.getMessage());
			}
			if(cbPreview.isChecked()){
				camera.startPreview();
				isOpen = true;
			} else {
				camera.release();
				isOpen = false;
			}
		}
	};

	public void open() throws IOException {
		if (!ready)
			return;
		Log.i("SNAPPY", "Opening device");
		camera = Camera.open();
		parameters = camera.getParameters();
		parameters.setFlashMode(Parameters.FLASH_MODE_OFF);
		parameters.setJpegQuality(90);
		parameters.setJpegThumbnailQuality(90);
		ArrayList<Size> sizes = (ArrayList<Size>) parameters.getSupportedPictureSizes();
		int max_width = Integer.MIN_VALUE;
		int max_height = Integer.MIN_VALUE;
		int i = 0;
		for(Size size: sizes){
			Log.i("SNAPPY", "Resolution " + i + " Width: " + size.width + " Height: " + size.height);
			if(size.width >= max_width){
				max_width = size.width;
				max_height = size.height;
			}
		}
		Log.i("SNAPPY", "Width: " + max_width + " Height: " + max_height);
		parameters.setPictureSize(max_width, max_height);
		
		switch(focus){
			case MACRO:
				parameters.setFocusMode(Parameters.FOCUS_MODE_MACRO);
				break;
			case INFINITY:
				parameters.setFocusMode(Parameters.FOCUS_MODE_INFINITY);
				break;
			default:
				parameters.setFocusMode(Parameters.FOCUS_MODE_AUTO);
		}
		
		List<Size> previewSizes = parameters.getSupportedPreviewSizes();
		Size bestSize = null;
		int bestDiff = 0;
		int diff = 0;
		for (Size size : previewSizes) {
			diff = Math.abs(surface_height - size.height) + Math.abs(surface_width - size.width);
			if (bestSize == null || diff < bestDiff) {
				bestSize = size;
				bestDiff = diff;
			}
			parameters.setPreviewSize(bestSize.width, bestSize.height);
		}
		// parameters.setPictureFormat(PixelFormat.JPEG);
		camera.setParameters(parameters);
		camera.setPreviewDisplay(surfaceHolder);
		camera.startPreview();
		isOpen = true;
		Log.i("SNAPPY", "Device open");

	}

	public void snap() {
		if (!ready)
			return;
		Log.i("SNAPPY", "Taking picutre");
		camera.takePicture(null, null, null, jpeg);
	}
	public boolean isOpen(){
		return isOpen;
	}
	public void close() {
		if (!ready)
			return;
		camera.release();
		isOpen = false;
	}

}
