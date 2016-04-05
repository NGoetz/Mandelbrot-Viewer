package fractal;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.awt.image.*;
import java.io.File;
import java.io.IOException;
import java.util.Stack;

import javax.imageio.*;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JButton;
import javax.swing.JDialog;

public class Apple extends JFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	static int resolution;
	static int windowMarginLRB =5; //window margin left, right, bottom
	static int windowMarginT=31; //window margin top
	static int itermax =2000; //maximum iterations
	static int trigger = 35; 
	static int [][] colors ={//different colors, depending on speed of divergence
			{ 1,255, 255,255},
			{300, 10, 10 ,40},
			{500,205,60,40},
			{850,120,140,255},
			{1000,50,30,255},
			{1300, 255,255,0},
			{1500,0,255,0},
			{1997,20,70,20},
			{itermax, 0,0,0}};
	double imageWidth=0.000003628; //width of the picture in the complex plane
	double [] imagePos; //position of Image 
	double imageHeight=imageWidth*3.f/4.f; //height of Image
	AImage AppleImage; //Panel which shows the image
	Stack<Double> zoomsaverx; //saves all zoom-in operations
	Stack<Double> zoomsavery;
	JLabel status;//information panel content
	JLabel help1;
	JLabel help2;
	JTextField name;
	JButton save;

	BufferedImage image;
	JDialog MyJDialog;

	public Apple(String title) {
		imagePos=new double [2];
		imagePos[0]=-0.743643135-(2*imageWidth/2); //initalizes the start position
		imagePos[1]=0.131825963-(2*imageWidth*3.f/8.f);
		zoomsaverx=new Stack<Double>();
		zoomsavery=new Stack<Double>();
		Dimension screensize=Toolkit.getDefaultToolkit().getScreenSize();
		resolution=(int) (screensize.getHeight()-150-windowMarginT);  //resolution is optimized for user
		MyJDialog=new JDialog();
		MyJDialog.addWindowListener(new WindowAdapter(){
			public void windowClosing(WindowEvent e){
				System.exit(0);;
			}
		});
		MyJDialog.setTitle(title);
		Dimension d=new Dimension(resolution+2*windowMarginLRB, resolution+windowMarginLRB+windowMarginT+80);
		MyJDialog.setSize(d);
		MyJDialog.setLayout(new BorderLayout());
		JPanel Controller=new JPanel();
		Controller.setLayout(new FlowLayout());
		help1=new JLabel("Left click on a area to zoom in; right click to zoom out.");
		help2=new JLabel("Enter file name to save image. By Niklas Götz.");
		status=new JLabel("Ready                   ");
		name=new JTextField(30);
		save=new JButton("save");//saves picture in file
		save.addActionListener(new Buttonpress());
		save.setActionCommand("save");
		Controller.add(help1);
		Controller.add(help2);
		Controller.add(status);
		Controller.add(name);
		Controller.add(save);
		Controller.setPreferredSize(new Dimension(resolution+2*windowMarginLRB, 80));
		AImage AppleImage =new AImage();
		AppleImage.setPreferredSize(new Dimension(resolution+2*windowMarginLRB,resolution));
		image=new BufferedImage(resolution, resolution, BufferedImage.TYPE_INT_RGB);
		Graphics gimg =image.createGraphics();
		calcImage(gimg);//calculates the picture
		MyJDialog.add(Controller, BorderLayout.NORTH);
		MyJDialog.add(AppleImage, BorderLayout.SOUTH);

		MyJDialog.addMouseListener(new MouseAdapter(){
			public void mouseClicked(MouseEvent e){//operation dependet on mouse button
				if(e.getButton()==1){
					zoomin(e);

				}else if(e.getButton()==3){
					zoomout(e);
				}
			}
		});
		MyJDialog.pack();
		MyJDialog.setVisible(true);
	}
	class Buttonpress implements ActionListener{
		public void actionPerformed(ActionEvent e){
			String cmd=e.getActionCommand();
			if(cmd.equals("save")){
				try{
					image.createGraphics();
					ImageIO.write(image, "JPEG", new File(name.getText()+".jpg"));
				}catch(IOException ex){
					name.setText("Error occured");
				}
			}
		}
	}
	public void zoomout(MouseEvent e){//reverts last zoomin if given; saves position and doubles width and height if not

		if(e.getY()<110+windowMarginT){
			return;
		}
		if(zoomsaverx.empty()){

		}else{
			imagePos[0]=zoomsaverx.pop();
			imagePos[1]=zoomsavery.pop();
		}
		imageWidth=imageWidth*2;
		status.setText("Calculating...");
		MyJDialog.update(MyJDialog.getGraphics());
		Graphics gimg =image.createGraphics();
		calcImage(gimg);
		status.setText("Ready             ");
		MyJDialog.update(MyJDialog.getGraphics());
	}
	public void zoomin(MouseEvent e){//reads in which quadrant the click was; shows it 

		if(e.getY()<110+windowMarginT){
			return;
		}
		zoomsaverx.push(imagePos[0]);
		zoomsavery.push(imagePos[1]);

		imagePos[0]=imagePos[0]+(((double)e.getX()-2*(double)windowMarginLRB)/(double)resolution)*imageWidth-imageWidth/(double)4;
		imagePos[1]=imagePos[1]+(((double)e.getY()-(double)(110+windowMarginT))/(double)resolution)*(imageWidth*4.f/3.f)-(imageWidth*3.f/8.f);

		imageWidth=imageWidth/(double)2;
		status.setText("Calculating...");
		MyJDialog.update(MyJDialog.getGraphics());
		Graphics gimg =image.createGraphics();
		calcImage(gimg);
		status.setText("Ready                 ");
		MyJDialog.update(MyJDialog.getGraphics());
	}
	class AImage extends JPanel{
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		protected void paintComponent(Graphics g){
			super.paintComponent(g);
			g.drawImage(image, windowMarginLRB, windowMarginT, this);


		};

	}


	public Color calcColor(int iter){//gets the color by the given amount of iterations, uses weighted average for a smooth look
		int co[]=new int[3];
		for(int i=1; i<colors.length-1;i++){
			if(iter<colors[i][0]){
				int iterationInterval=colors[i-1][0]-colors[i][0];
				double weightedAverage=(iter-colors[i][0])/(double)iterationInterval;
				for(int f=0; f<3;f++){
					int colorInterval=colors[i-1][f+1]-colors[i][f+1];
					co[f]=(int)(weightedAverage*colorInterval)+colors[i][f+1];

				}	
				return new Color(co[0],co[1],co[2]);
			}

		}
		return Color.BLACK;	
	}
	public int calcPoint(ComplexNumber c){//calculates the needed iterations until divergence
		ComplexNumber z=new ComplexNumber(0,0);
		int iter=0;
		for(iter=0; (iter<itermax)&&(z.norm()<trigger);iter++)
			z=(z.multiply(z)).add(c);
		return iter;
	}
	public void calcImage(Graphics g){//calculates the color for every point
		for(int x=0;x<resolution;x++ ){
			for(int y=0;y<resolution;y++){
				ComplexNumber c=new ComplexNumber(imageWidth*(double)(x)/resolution+imagePos[0], imageWidth*(double)(y)/resolution+imagePos[1]);
				g.setColor(calcColor(calcPoint(c)));
				g.fillRect(x, y, 1, 1);
			}
		}
	}

	public static void main(String[]args){
		Apple a=new Apple ("Mandelbrot-Viewer" );

	}
}

