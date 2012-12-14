package com.thoughtcomplex.starlight.image;

import org.lwjgl.opengl.GL11;
import org.lwjgl.util.Color;
import org.lwjgl.util.ReadableColor;

public class Font extends Texture {
	private int charwidth = 5;
	private int charheight = 9;
	private int spacing = 1;
	
	public Font(String resource, int charwidth, int charheight) {
		super(resource);
	}
	
	public void paintChar(int x, int y, char ch, GLColor c) {
		if (ch<33) return; //space or nonprintable
		if (ch>126) return; //nonprintable or extended/unicode
		int charNum = ((int)ch)-33;
		//c.bind();
		paint(x, y, charwidth, charheight, (charwidth+spacing)*charNum, 0, charwidth,charheight,c);
	}
	
	public void paintString(int x, int y, String s) {
		for(int i=0; i<s.length(); i++) {
			paintChar(x+((charwidth+1)*i),y, s.charAt(i),GLColor.WHITE);
		}
	}
	
	public void paintString(int x, int y, String s, GLColor c) {
		for(int i=0; i<s.length(); i++) {
			paintChar(x+((charwidth+1)*i),y, s.charAt(i),c);
		}
	}
	
	public void paintStringCentered(int x, int y, String s, GLColor c) {
		int paintX = x - ((s.length()*(charwidth+1)) / 2);
		paintString(paintX,y,s,c);
	}
	
	private int hexByte(char ch) {
		if (ch>='A' & ch<='F') {
			return (ch-'A') + 10;
		}
		if (ch>='a' & ch<='f') {
			return (ch-'a') + 10;
		}
		if (ch>='0' & ch<='9') {
			return ch-'0';
		}
		return 0;
	}
	
	private boolean isHexCode(char ch) {
		return (ch>='A' & ch<='F') |
				(ch>='a' & ch<='f') |
				(ch>='0' & ch<='9');
	}
	
	public void paintStringWithFormatCodes(int x, int y, String s) {
		GLColor curColor = GLColor.WHITE;
		int r = 0; int g = 0; int b = 0;
		int escapeByte = 0;
		for(int i=0; i<s.length(); i++) {
			char ch = s.charAt(i);
			if (ch=='¤') {
				//scoop up the control code
				i++; if (i>=s.length()) return;
				char controlOne =s.charAt(i);
				if (isHexCode(controlOne)) {
					i++; if (i>=s.length()) return;
					char controlTwo =s.charAt(i);
					i++; if (i>=s.length()) return;
					char controlThree =s.charAt(i);
					
					curColor = GLColor.fromBytes(hexByte(controlOne),hexByte(controlTwo),hexByte(controlThree));
				} else {
					//other format codes!
				}
			} else {
				paintChar(x+((charwidth+1)*i),y, s.charAt(i),curColor);
			}

		}
	}
}
