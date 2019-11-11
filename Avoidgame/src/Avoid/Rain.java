package Avoid;

public class Rain {	//ºø¹æ¿ï ÇÏ³ªÀÇ °´Ã¼
	protected int x;
	protected int y;
	protected int speed;

	public Rain(int x, int y , int speed) {
		this.x = x;
		this.y = y;
		this.speed =speed;
	}
	
	public void move() {
		y+=speed;
	}
	
}
