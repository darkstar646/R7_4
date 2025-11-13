
public class Car {
	int x,y,w,h,vx,vy;
	public Car(int x, int y, int w, int h, int vx, int vy) {
		this.x=x;
		this.y=y;
		this.w=w;
		this.h=h;
		this.vx=vx;
		this.vy=vy;
		
	}
	
	public void draw(MyFrame2 frame) {

        int ux = w/8;
        int uy = h/6;

        frame.fillRect(x-ux*2, y-uy*3, ux*4, uy*2);
        frame.fillRect(x-ux*4, y-uy, ux*8, uy*2);
        frame.fillOval(x-ux*2-uy, y+uy, uy*2, uy*2);
        frame.fillOval(x+ux*2-uy, y+uy, uy*2, uy*2);
	}

	
	public void move() {
		x+=vx;
		y+=vy;
	}
	
	public void stop() {
		this.vx=0;
		this.vy=0;
	}
	
	public boolean isMoving() {
		if(vx!=0 || vy!=0) {
			return true;
		}else {
			return false;
		}
	}

}
