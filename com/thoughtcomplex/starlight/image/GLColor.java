package com.thoughtcomplex.starlight.image;

import org.lwjgl.opengl.GL11;

public class GLColor {
	public float r = 1.0f;
	public float g = 1.0f;
	public float b = 1.0f;
	public float a = 1.0f;
	
	public GLColor(float r, float g, float b) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = 1.0f;
	}
	
	public GLColor(float r, float g, float b, float a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
	
	public static GLColor fromBytes(int r, int g, int b) {
		return new GLColor(
				((float)r)/255.0f,
				((float)g)/255.0f,
				((float)b)/255.0f
				);
	}
	
	public void bind() {
		GL11.glColor4f(r, g, b, a);
	}
	
	public static GLColor WHITE = new GLColor(1,1,1);
	public static GLColor RED = new GLColor(1,0,0);
	public static GLColor GREEN = new GLColor(0,1,0);
	public static GLColor BLUE = new GLColor(0,0,1);
	public static GLColor GRAY = new GLColor(0.5f,0.5f,0.5f);
	public static GLColor YELLOW = new GLColor(1,1,0);
}
