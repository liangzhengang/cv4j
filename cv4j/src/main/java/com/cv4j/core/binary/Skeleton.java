package com.cv4j.core.binary;

import com.cv4j.core.datamodel.ByteProcessor;
import java.util.Arrays;

public class Skeleton {

	public void process(ByteProcessor binary) {
		int width = binary.getWidth();
		int height = binary.getHeight();
		byte[] pixels = binary.getGray();

		byte[] output = new byte[width*height];
		int[] distmap = new int[width*height];
		System.arraycopy(pixels, 0, output, 0, output.length);
		Arrays.fill(distmap, 0);
		
		// initialize distance value
		int offset =0;
		int pv = 0;
		for(int row=0; row<height; row++) {
			offset = row*width;
			for(int col=0; col<width; col++) {
				pv = pixels[offset+col];
				if(pv == 255) {
					distmap[offset+col] = 1;
				}
			}
		}

		// distance transform stage
		boolean stop = false;
		int level = 0;
		while(!stop) {
			stop = dt(pixels, output, distmap, level, width, height);
			System.arraycopy(output, 0, pixels, 0, output.length);
			level++;
		}

		// extract skeleton from DT image
		int dis = 0;
		int p1=0, p2=0, p3=0, p4=0;
		Arrays.fill(output, (byte)0);
		for(int row=1; row<height-1; row++) {
			offset = row*width;
			for(int col=1; col<width-1; col++) {
				dis = distmap[offset+col];
				p1 = distmap[offset+col-1];
				p2 = distmap[offset+col+1];
				p3 = distmap[offset-width+col];
				p4 = distmap[offset+width+col];
				
				if(dis == 0) {
					output[offset+col] = (byte)0;
				}
				else {
					if(dis < p1 || dis < p2 || dis < p3 || dis < p4) {
						output[offset+col] = (byte)0;
					}
					else {
						output[offset+col] = (byte)255;
					}
				}
			}
		}

		// update pixels and release memory
		binary.putGray(output);
		output = null;
		distmap = null;
	}

	private boolean dt(byte[] input, byte[] output, int[] distmap, int level, int width, int height) {
		boolean stop = true;
		int p1=0, p2=0, p3=0;
		int p4=0, p5=0, p6=0;
		int p7=0, p8=0, p9=0;
		int offset = 0;
		for(int row=1; row<height-1; row++) {
			offset = row*width;
			for(int col=1; col<width-1; col++) {
				p1 = input[offset-width+col-1]&0xff;
				p2 = input[offset-width+col]&0xff;
				p3 = input[offset-width+col+1]&0xff;
				p4 = input[offset+col-1]&0xff;
				p5 = input[offset+col]&0xff;
				p6 = input[offset+col-1]&0xff;
				p7 = input[offset+width+col-1]&0xff;
				p8 = input[offset+width+col]&0xff;
				p9 = input[offset+width+col+1]&0xff;
				int sum = p1+p2+p3+p4+p6+p7+p8+p9;
				int total = 255*8;
				if(p5 == 255 &&  sum != total) {
					output[offset+col] = (byte)0;
					distmap[offset+col] = distmap[offset+col] + level;
					stop = false;
				}
			}
		}
		return stop;
	}

}
