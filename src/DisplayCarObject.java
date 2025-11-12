
public class DisplayCarObject extends MyFrame2{
	public void run() {
		Car car1=new Car(50,50,10,5);
		Car car2=new Car(200,100,-10,-5);
		
		for(int i=0; i<300; i++) {
			clear();
			car1.draw(this);
			car2.draw(this);
			car1.move();
			car2.move();
			
			sleep(0.1);
			
		}
	}
	
	public static void main(String[] args) {
		MyFrame2 f=new  DisplayCarObject();
	}


}
