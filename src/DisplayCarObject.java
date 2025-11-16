
public class DisplayCarObject extends MyFrame2{
	int n=2;
	public void run() {
		
		Car car0=new Car(50+10*n,350+10*n,32, 24,n+5, -n-5);
		Car car1=new Car(450-10*n,250-10*n,32, 24,-n-5, n+5);
		
		
		for(int i=0; i<300; i++) {
			clear();
			drawLine(300, 0, 300, 500);
			car0.draw(this);
			car1.draw(this);
			car0.move();
			car1.move();
			
			if(car0.x>300) {
				car0.stop();
			}
			
			if(car1.x<300) {
				car1.stop();
			}
			
			sleep(0.1);
			
		}
	}
	
	public static void main(String[] args) {
		MyFrame2 f=new  DisplayCarObject();
		f.setSize(600,500);
	}
}
