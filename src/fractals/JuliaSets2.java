package fractals;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.Timer;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Color;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.geom.Point2D;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.util.Calendar;

@SuppressWarnings("serial")
public class JuliaSets2 extends JPanel implements ActionListener {
	//to do: dimension-based rounding
	//progress thread
	private int vOffset;
	private int hOffset;
	private Image pic;
	private JButton startBtn;
	private JButton resetBtn;
	private JButton imageBtn;
	private JButton zoomIn;
	private JButton zoomOut;
	private double realPart;
	private double imagPart;
	private double xMin;
	private double xMax;
	private double yMin;
	private double yMax;
	private double xWin; //xMax - xMin / 2, used for zoom
	private double yWin; //aka how offset our max/min is from the center
	private double xDim;
	private double yDim;
	private Point2D center; //our zoom center (uses coordinate plane vals)
	private JTextField cTxt;
	private String input;
	private int divide; //the divide index for our txt input
	private JLabel picLabel;
	private JLabel mouseLabel; //shows our center location
	private JLabel xMinLabel;
	private JLabel xMaxLabel;
	private JLabel yMinLabel;
	private JLabel yMaxLabel;
	private JLabel progressLbl;
	private int[][] cells;
	private Timer timer;

	private double CReal;
	private double CImag;
	private double CTemp;
	private int k;

	private final double SIZE = 1.5; //how much of the coordinate plane from the origin to begin with

	public JuliaSets2(int xSize, int ySize) {
		super(new GridBagLayout());                       				// set up graphics window
		setBackground(Color.LIGHT_GRAY);
		addMouseListener(new MAdapter());
		addMouseMotionListener(new MAdapter());
		setFocusable(true);
		setDoubleBuffered(true);
		xMin = -1 * SIZE;
		yMin = -1 * SIZE;

		xMax = SIZE;
		yMax = SIZE;
		xDim = xSize;
		yDim = ySize;
		xWin = SIZE;
		yWin = SIZE;
		initBtns();
		initTxt();
		initLabels();
		pic = new BufferedImage(xSize, ySize, BufferedImage.TYPE_INT_RGB);
		center = new Point.Double();
		center.setLocation(0 , 0);

		cells = new int[xSize][ySize];

		picLabel = new JLabel(new ImageIcon(pic));
		timer = new Timer(1 , this);

		timer.start();
		drawCells(pic.getGraphics());
		addThingsToPanel();
	}

	//more of that annoying placement code
	public void addThingsToPanel() {
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.HORIZONTAL;
		c.insets = new Insets(1, 1, 0, 1);
		c.gridx = 0;
		c.gridy = 1;
		c.gridwidth = 6;
		c.gridheight = 14;
		add(picLabel, c);
		c.gridwidth = 1;
		c.gridheight = 1;
		c.insets = new Insets(0, 2, 0, 2);
		c.gridx = 0;
		c.gridy = 0;
		add(startBtn, c);
		c.gridx = 1;
		c.gridy = 0;
		add(resetBtn, c);
		c.gridx = 3;
//		add(speedBtn, c);
		c.insets = new Insets(0, 10, 0, 10);
		c.gridx = 4;
		c.gridy = 0;
		c.fill = GridBagConstraints.VERTICAL;
//		add(generations, c);
		c.gridx = 5;
		add(mouseLabel, c);
		c.gridx = 6;
		c.gridy = 1;
		c.fill = GridBagConstraints.HORIZONTAL;
		add(imageBtn, c);
		c.gridy = 2;
		c.fill = GridBagConstraints.BOTH;
		add(new JLabel("f(z) = z^2 + c"), c);
		c.gridy = 3;
		add(new JLabel("C"), c);
		c.gridx = 7;
		c.gridy = 2;
		add(new JLabel("where c = a+bi"), c);
		c.gridy = 3;
		add(cTxt, c);
		c.gridy = 7;
		add(xMinLabel, c);
		c.gridy = 8;
		add(xMaxLabel, c);
		c.gridy = 9;
		add(yMinLabel, c);
		c.gridy = 10;
		add(yMaxLabel, c);
		c.gridy = 11;
		add(zoomIn , c);
		c.gridy = 12;
		add(zoomOut, c);
		c.gridy = 13;
		add(progressLbl , c);
	}

	public void initTxt() {

		cTxt = new JTextField("0", 8);
		cTxt.getDocument().addDocumentListener(new DocumentListener(){
			//no need to press enter with document listeners
			public void changedUpdate(DocumentEvent arg0) {
				try {
					input = cTxt.getText();

					for(int place = 1 ; place < cTxt.getText().length() ; place++)
						if(input.charAt(place) == '-' || input.charAt(place) == '+')
							divide = place; //finding the + or - in 'a+bi'

					realPart = Double.parseDouble(input.substring(0, divide));
					imagPart = Double.parseDouble(input.substring(divide , input.length()-1));
				} catch (NumberFormatException e) {
				}
				catch (StringIndexOutOfBoundsException d){

				}
			}

			public void insertUpdate(DocumentEvent arg0) {
				try {
					input = cTxt.getText();

					for(int place = 1 ; place < cTxt.getText().length() ; place++)
						if(input.charAt(place) == '-' || input.charAt(place) == '+')
							divide = place;

					realPart = Double.parseDouble(input.substring(0, divide));
					imagPart = Double.parseDouble(input.substring(divide , input.length()-1));
				} catch (NumberFormatException e) {
				}
				catch (StringIndexOutOfBoundsException d){

				}
			}

			public void removeUpdate(DocumentEvent arg0) {
				try {
					input = cTxt.getText();

					for(int place = 1 ; place < cTxt.getText().length() ; place++)
						if(input.charAt(place) == '-' || input.charAt(place) == '+')
							divide = place;

					realPart = Double.parseDouble(input.substring(0, divide));
					imagPart = Double.parseDouble(input.substring(divide , input.length()-1));
				} catch (NumberFormatException e) {
				}
				catch (StringIndexOutOfBoundsException d){

				}
			}

		});

	}

	public void initBtns() {
		startBtn = new JButton("Generate");
		startBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				updateCells();
				drawCells(pic.getGraphics());
				updateLabels(); //updateLabels() also repaints.
			}
		});

		resetBtn = new JButton("Reset");
		resetBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				xMin = -1 * SIZE;
				yMin = -1 * SIZE;
				xMax = SIZE;
				yMax = SIZE;
				xWin = SIZE;
				yWin = SIZE;
				center.setLocation(0,0);
				updateCells();
				drawCells(pic.getGraphics());
				updateLabels();
			}
		});
		imageBtn = new JButton("Save Picture");
		imageBtn.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Calendar c = Calendar.getInstance();
					String fileName = ".\\" + realPart + "+" + imagPart + "i" + "@" + c.get(Calendar.HOUR) + "." + c.get(Calendar.MINUTE) + "." + c.get(Calendar.SECOND)+ ".png";
					System.out.println(fileName);
					File outputFile = new File(fileName);
					outputFile.createNewFile();
					ImageIO.write((RenderedImage) pic, "png", outputFile);
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		zoomIn = new JButton("Zoom in");
		zoomIn.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				//shrinks the area by around 50%
				xWin*=0.75;
				yWin*=0.75;
				//build our window around the center
				xMax = center.getX() + xWin;
				xMin = center.getX() - xWin;
				yMax = center.getY() + yWin;
				yMin = center.getY() - yWin;
				updateCells();
				drawCells(pic.getGraphics());
				updateLabels();
			}
		});
		zoomOut = new JButton("Zoom Out");
		zoomOut.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent e){
				xWin/=0.75;
				yWin/=0.75;
				xMax = center.getX() + xWin;
				xMin = center.getX() - xWin;
				yMax = center.getY() + yWin;
				yMin = center.getY() - yWin;
				updateCells();
				drawCells(pic.getGraphics());
				updateLabels();
			}
		});
	}

	private void initLabels() {
		mouseLabel = new JLabel("Zoom Center At (0,0)");
		xMinLabel = new JLabel("xMin: " + xMin);
		xMaxLabel = new JLabel("xMax: " + xMax);
		yMinLabel = new JLabel("yMin: " + yMin);
		yMaxLabel = new JLabel("yMax: " + yMax);
		progressLbl = new JLabel("Ready");
	}

	//380-750, using http://www.efg2.com/Lab/ScienceAndEngineering/Spectra.htm chart
	//I used to think every color was in the spectrum...
	public static Color makeColor2(int input) {
		if (input < 0) {
			return Color.BLACK;
		}
		if (input < 380) {
			input += 380;
		}
		int red = 0, green = 0, blue = 0;
		//red
		if ((input >= 520) && (input <= 580)) red = 255 * (input - 520) / 60;
		else if ((input > 580) && (input <= 700)) red = 255;
		else if ((input > 700) && (input <= 750)) red = 255 - 155 * (input - 700) / 50; // multiplier was 95, this wraps better
		else if ((input >= 380) && (input <= 400)) red = 100 + 30 * (input - 380) / 20;
		else if ((input > 400) && (input <= 420)) red = 130 - 30 * (input - 400) / 20;
		else if ((input > 420) && (input <= 440)) red = 100 - 100 * (input - 420) / 20;
		//green
		if ((input >= 440) && (input <= 480)) green = 255 * (input - 440) / 40;
		else if ((input > 480) && (input <= 560)) green = 255;
		else if ((input > 560) && (input <= 645)) green = 255 - 255 * (input - 560) / 85;
		//blue
		if ((input >= 380) && (input <= 420)) blue = 100 + 155 * (input - 380) / 40;
		else if ((input > 420) && (input <= 490)) blue = 255;
		else if ((input > 490) && (input <= 510)) blue = 255 - 255 * (input - 490) / 20;
		else if ((input > 730) && (input <= 750)) blue = 100 * (input - 730) / 20;
		return new Color(red, green, blue);
	}

	public void paintComponent(Graphics g) { 	                 // draw graphics in the panel
		super.paintComponent(g);                              	 // call superclass to make panel display correctly
	}

	//@Override
	public void actionPerformed(ActionEvent e) { 		//things to change every timer tick
		hOffset = picLabel.getLocationOnScreen().x - getLocationOnScreen().x;
		vOffset = picLabel.getLocationOnScreen().y - getLocationOnScreen().y;
	} //all we have to do is keep track of offsets, all the label/drawing things take care of each other.

	//use setColor and fillRect (or drawRect) to adjust the corresponding graphics to cells in the pic variable
	private void drawCells(Graphics g) {
		for (int i = 0; i < cells.length; i++) {
			for (int j = 0; j < cells[i].length; j++) {
				g.setColor(makeColor2(cells[i][j])); //feel free to create your own color function
				g.drawRect(i, j, 1, 1);
			}
		}
	}

	//Calculate the k value of each pixel
	private void updateCells() {

		for(int x = 0 ; x < cells.length ; x++){
			for(int y = 0 ; y < cells[x].length ; y++){

				CReal = xMin + x*((xMax - xMin)/xDim);
				CImag = yMin + (yDim-y)*((yMax - yMin)/yDim);


					realPart = CReal;  //for the Mandelbrot set (which has no seed)
					imagPart = CImag;


				k = 0;

				while(CReal*CReal + CImag*CImag <= 4){

					CTemp = CReal * CReal - CImag * CImag + realPart;
					CImag = 2 * CReal * CImag + imagPart;
					CReal = CTemp;

					if(k == 371){
						k = -1;
						break;
					}

					k++;
				}

				cells[x][y] = k;

			}
		}


	}
	//update the labels
	private void updateLabels() {

		mouseLabel.setText("Zoom Center At (" + center.getX() + " , " + center.getY() + ")");

		xMinLabel.setText("xMin: " + xMin);
		xMaxLabel.setText("xMax: " + xMax);
		yMinLabel.setText("yMin: " + yMin);
		yMaxLabel.setText("yMax: " + yMax);

		repaint();
	}

	//For selecting zoom center coordinates
	private class MAdapter extends MouseAdapter {

		@Override
		public void mouseClicked(MouseEvent e) {
			if(e.getX()-hOffset <= cells.length && e.getX()-hOffset >= 0 && e.getY()-vOffset <= cells.length && e.getY()-vOffset >= 0){
				center.setLocation((xMin + (xMax - xMin) * (double)(e.getX() - hOffset) / (xDim - 1)),
						(yMax - (yMax - yMin) * (double)(e.getY() - vOffset) / (yDim - 1)));
				updateLabels();
			}
		}

	}

}