package com.thoughtcomplex.starlight.image;

public class WavesFilteredImage implements FilteredImage {

	double xfrequency = 0.02;
	double xamplitude = 3.0;
	double yfrequency = 0.0;
	double yamplitude = 0.0;
	Texture basis = null;
	
	private double xphase = 0.0;
	private double yphase = 0.0;
	private double xrate = 0.03;
	private double yrate = 0.0;
	
	public WavesFilteredImage(Texture t, double xfrequency, double xamplitude, double yfrequency, double yamplitude) {
		basis=t;
		this.xfrequency=xfrequency;
		this.xamplitude=xamplitude;
		this.yfrequency=yfrequency;
		this.yamplitude=yamplitude;
		
		xphase = Math.random()*Math.PI*2;
		yphase = Math.random()*Math.PI*2;
	}
	
	@Override
	public void paint(int x, int y) {
		GLColor.WHITE.bind();
		if (basis==null) return;
		if (yamplitude<=0.0) {
			//simple-case
			for(int yi=0; yi<basis.height; yi++) {
				//draw a slice
				double offset = Math.sin(xphase+(yi*0.01))*xamplitude;
				
				basis.paint((int)(x+offset), y+yi,basis.width,1,0,yi,basis.width,1);
			}
		} else {
			//complex case
			//TODO: VERTICAL SIMPLE CASE AND COMPLEX CASE
		}
	}

	@Override
	public void tick() {
		xphase = (xphase + xrate) % (Math.PI*2);
		yphase = (yphase + yrate) % (Math.PI*2);
	}

}
