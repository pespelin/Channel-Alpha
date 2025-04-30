package version0;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.TimerTask;

import javax.imageio.ImageIO;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.filechooser.FileFilter;

public class Version0_0 implements KeyListener, MouseListener, MouseMotionListener, MouseWheelListener {
	
	public static JFrame frame;
	public static JPanel canvas;
	public static TimerTask updater;
	public static java.util.Timer updatingTimer = new java.util.Timer();
	public static JFileChooser saveDialog, loadDialog;
	public static JButton chooseColor1, chooseColor3, saveButton, loadButton;
	
	public static class Constants {

		public static float zoomer = 17.f/16.f;
		
	}
	
	public static class Canvas {
		
		public static Color m1color = Color.black, m3color = Color.white;
		public static float zoomHorrizontal = 1.f, zoomVertical = 1.f;
		public static double verticalC = 0, horrizontalC = 0;
		
		public static BufferedImage image;
		public static WritableRaster rasterOFimage;
		
		public static int getImageXbyDisplayX(int horrizontalP) {
			return (int) ((horrizontalP + (rasterOFimage.getWidth()/2.f*(Canvas.zoomHorrizontal-1.f)))
					/ Canvas.zoomHorrizontal - horrizontalC);
		}
		public static int getImageYbyDisplayY(int verticalP) {
			return (int) ((verticalP + (rasterOFimage.getHeight()/2.f*(Canvas.zoomVertical-1.f)))
					/ Canvas.zoomVertical - verticalC);
		}
		public static int getDisplayXbyImageX(int horrizontalP) {
			return (int) ((horrizontalP * Canvas.zoomHorrizontal) -
					(rasterOFimage.getWidth()/2.f*(Canvas.zoomHorrizontal-1.f)));
//			return (int) ((horrizontalP + (rasterOFimage.getWidth()/2.f*(Canvas.zoomHorrizontal-1.f)))
//					/ Canvas.zoomHorrizontal);
		}
		public static int getDisplayYbyImageY(int verticalP) {
			return (int) ((verticalP * Canvas.zoomVertical) -
					(rasterOFimage.getWidth()/2.f*(Canvas.zoomVertical-1.f)));
		}
		
		public static void paint(Graphics2D brush) {
			int iw = image.getWidth(), ih = image.getHeight();
			int dhp = getDisplayXbyImageX((int)horrizontalC);
			int dvp = getDisplayYbyImageY((int)verticalC);
			int dw = (int)(iw * zoomHorrizontal), dh = (int)(ih * zoomVertical);
			brush.clearRect(0, 0, canvas.getWidth(), canvas.getHeight());
			brush.drawImage(image, dhp, dvp, dhp+dw, dvp+dh, 0, 0, image.getWidth(), image.getHeight(), canvas);
		}
		
		public static void updateCanvas() {
			float moveSpeed = 5.0f; // Hareket hızını artırmak için bu değeri ekledim
			if (Status.DAdown) { verticalC -= moveSpeed / zoomVertical; }
			if (Status.UAdown) { verticalC += moveSpeed / zoomVertical; }
			if (Status.RAdown) { horrizontalC -= moveSpeed / zoomHorrizontal; }
			if (Status.LAdown) { horrizontalC += moveSpeed / zoomHorrizontal; }
			if (!Status.mouseONcanvas) { return; }
			if (!(Status.mouse1down || Status.mouse3down)) { return; }
			int mhp = 0, mvp = 0;
			{
				Point mp = MouseInfo.getPointerInfo().getLocation();
				Point cp = canvas.getLocationOnScreen();
				mhp = getImageXbyDisplayX(mp.x - cp.x);
				mvp = getImageYbyDisplayY(mp.y - cp.y);
			}
			if (mhp < 0 || mvp < 0 || mhp >= image.getWidth() || mvp >= image.getHeight()) {
				return;
			}
			if (Status.mouse1down && Status.mouse3down) {

			} else if (Status.mouse1down) {
				drawSquare(mhp, mvp, 1);
			} else if (Status.mouse3down) {
				drawSquare(mhp, mvp, 3);
			}
		}
		public static void drawStroke(int beginH, int beginV, int endH, int endV, int buttonIndex) {
			int vectorH = endH - beginH, vectorV = endV - beginV;
			int timer = Math.abs(Math.max(vectorV, beginV));
			for(int ct = 0; ct != timer; ct++) {
				drawSquare(beginH + vectorH*ct/timer, beginV + vectorV*ct/timer, buttonIndex);
			}
		}
		public static void drawSquare(int hor, int ver, int buttonIndex) {
			if((buttonIndex == 1 ? Status.brush1size : Status.brush3size) == -0.1f) { return; }
			int bsize = (int) (buttonIndex == 1 ? Status.brush1size : Status.brush3size);
			int[] data;
			if(buttonIndex == 1) {
				data = new int[] { m1color.getRed(), m1color.getGreen(), m1color.getBlue() };
			} else {
				data = new int[] { m3color.getRed(), m3color.getGreen(), m3color.getBlue() };
			}
			if(bsize == 0.f) {
				if(hor < 0 || hor >= image.getWidth()) { return; }
				if(ver < 0 || ver >= image.getHeight()) { return; }
				rasterOFimage.setPixel(hor, ver, data); return;
			}
			for(int hi = -bsize; hi != bsize; hi++) {
				if(hor+hi < 0 || hor+hi >= image.getWidth()) { continue; }
				for(int vi = -bsize; vi != bsize; vi++) {
					if(ver+vi < 0 || ver+vi >= image.getHeight()) { continue; }
					rasterOFimage.setPixel(hor+hi, ver+vi, data);
				}
			}
		}
		
	}
	
	public static class Status {
		
		public static boolean mouseInside = false, mouseONcanvas = false;
		public static boolean mouse1down = false, mouse2down = false, mouse3down = false;
		public static long mouse1downSince = 0, mouse2downSince = 0, mouse3downSince = 0;
		public static Point lastM1down = null, lastM2down = null, lastM3down = null;
		public static float brush1size = 2, brush3size = 2;
		public static boolean UAdown = false, LAdown = false, RAdown = false, DAdown = false;
		
	}
	@Override
	public void mouseEntered(MouseEvent e) {
		if(e.getComponent() == frame) { Status.mouseInside = true; }
		if(e.getComponent() == canvas) { Status.mouseONcanvas = true; }
	}
	@Override
	public void mouseExited(MouseEvent e) {
		if(e.getComponent() == frame) { Status.mouseInside = false; }
		if(e.getComponent() == canvas) { Status.mouseONcanvas = false; }
	}

	@Override
	public void mousePressed(MouseEvent e) {
		switch(e.getButton()) {
		case MouseEvent.BUTTON1:
			if(Status.mouse1down) { break; }
			Status.lastM1down = e.getLocationOnScreen();
			Status.mouse1downSince = new Date().getTime(); Status.mouse1down = true;
			break;
		case MouseEvent.BUTTON2:
			if(Status.mouse2down) { break; }
			Status.mouse2downSince = new Date().getTime(); Status.mouse2down = true;
			break;
		case MouseEvent.BUTTON3:
			if(Status.mouse3down) { break; }
			Status.lastM3down = e.getLocationOnScreen();
			Status.mouse3downSince = new Date().getTime(); Status.mouse3down = true;
			break;
		}
	}
	@Override
	public void mouseReleased(MouseEvent e) {
		switch(e.getButton()) {
		case MouseEvent.BUTTON1: Status.mouse1down = false; break;
		case MouseEvent.BUTTON2: Status.mouse2down = false; break;
		case MouseEvent.BUTTON3: Status.mouse3down = false; break;
		}
	}
	@Override
	public void mouseDragged(MouseEvent e) {
		if(Status.mouse1down && Status.mouse3down) {
			
		} else if(Status.mouse1down) {
			Point cp = canvas.getLocationOnScreen();
			Point mcp = e.getLocationOnScreen();
			int mchp  = Canvas.getImageXbyDisplayX(mcp.x - cp.x);
			int mcvp  = Canvas.getImageYbyDisplayY(mcp.y - cp.y);
			int mlhp  = Canvas.getImageXbyDisplayX(Status.lastM1down.x - cp.x);
			int mlvp  = Canvas.getImageYbyDisplayY(Status.lastM1down.y - cp.y);
			Canvas.drawStroke(mlhp, mlvp, mchp, mcvp, 1);
			Status.lastM1down = mcp;
		} else if(Status.mouse3down) {
			Point cp = canvas.getLocationOnScreen();
			Point mcp = e.getLocationOnScreen();
			int mchp  = Canvas.getImageXbyDisplayX(mcp.x - cp.x);
			int mcvp  = Canvas.getImageYbyDisplayY(mcp.y - cp.y);
			int mlhp  = Canvas.getImageXbyDisplayX(Status.lastM3down.x - cp.x);
			int mlvp  = Canvas.getImageYbyDisplayY(Status.lastM3down.y - cp.y);
			Canvas.drawStroke(mlhp, mlvp, mchp, mcvp, 3);
			Status.lastM3down = mcp;
		}
		
	}
	@Override
	public void mouseWheelMoved(MouseWheelEvent e) {
		if(Status.mouse1down && Status.mouse3down) {
			
		} else if(Status.mouse1down) {
			Status.brush1size -= e.getWheelRotation()/2.f;
			Status.brush1size = Status.brush1size < 0.f ? -0.1f : Status.brush1size;
		} else if(Status.mouse3down) {
			Status.brush3size -= e.getWheelRotation()/2.f;
			Status.brush3size = Status.brush3size < 0.f ? -0.1f : Status.brush3size;
		} else {
			if(e.getWheelRotation() < 0) {
				Canvas.zoomHorrizontal *= Constants.zoomer;
				Canvas.zoomVertical *= Constants.zoomer;
			} else {
				Canvas.zoomHorrizontal /= Constants.zoomer;
				Canvas.zoomVertical /= Constants.zoomer;
			}
		}
	}

	@Override
	public void keyPressed(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_UP: Status.UAdown = true; break;
		case KeyEvent.VK_LEFT: Status.LAdown = true; break;
		case KeyEvent.VK_RIGHT: Status.RAdown = true; break;
		case KeyEvent.VK_DOWN: Status.DAdown = true; break;
		}
	}
	@Override
	public void keyReleased(KeyEvent e) {
		switch(e.getKeyCode()) {
		case KeyEvent.VK_UP: Status.UAdown = false; break;
		case KeyEvent.VK_LEFT: Status.LAdown = false; break;
		case KeyEvent.VK_RIGHT: Status.RAdown = false; break;
		case KeyEvent.VK_DOWN: Status.DAdown = false; break;
		}
	}

	public static void main(String[] args) {
		frame = new JFrame("Channel ALPHA v0.0 (test build)");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setSize(800, 700);
		frame.setLocationRelativeTo(null);
		frame.setLayout(null);
		frame.getContentPane().setBackground(Color.black);
		frame.setVisible(true);
		Version0_0 listener = new Version0_0();
		frame.addMouseListener(listener);
		frame.addMouseMotionListener(listener);
		frame.addMouseWheelListener(listener);
		frame.addKeyListener(listener);
		canvas = new JPanel() {
			private static final long serialVersionUID = 406019431948654527L;
			@Override
			public void paintComponent(Graphics g) { Canvas.paint((Graphics2D) g); }
		};
		canvas.setSize(600, 600);
		canvas.setLocation(180, 50);
		canvas.addMouseListener(listener);
		canvas.addMouseMotionListener(listener);
		Canvas.image = new BufferedImage(600, 600, BufferedImage.TYPE_3BYTE_BGR);
		Canvas.rasterOFimage = Canvas.image.getRaster();

		chooseColor1 = new JButton(); chooseColor3 = new JButton();
		chooseColor1.setFocusable(false); chooseColor3.setFocusable(false);
		chooseColor1.setBounds(30, 230, 70, 70); chooseColor3.setBounds(100, 230, 70, 70);
		chooseColor1.setBackground(Canvas.m1color); chooseColor3.setBackground(Canvas.m3color);
		chooseColor1.addActionListener(event -> {
			Color candidate = JColorChooser.showDialog(frame, "Choose Primary Color", Canvas.m1color);
			if(candidate == null) { return; }
			Canvas.m1color = candidate;
			chooseColor1.setBackground(Canvas.m1color);
		});
		chooseColor3.addActionListener(event -> {
			 Color candidate = JColorChooser.showDialog(frame, "Choose Secondary Color", Canvas.m3color);
			 if(candidate == null) { return; }
			 Canvas.m3color = candidate;
			chooseColor3.setBackground(Canvas.m3color);
		});
		frame.add(chooseColor1); frame.add(chooseColor3);

		saveButton = new JButton("save"); saveButton.setFocusable(false);
		saveButton.setBounds(60, 30, 100, 60);
		saveButton.setBackground(Color.CYAN);
		saveDialog = new JFileChooser();
		saveDialog.setDialogTitle("Only PNG Supported");
		saveDialog.setDialogType(JFileChooser.SAVE_DIALOG);
//		saveDialog.setFileFilter(new FileFilter() {
//			@Override
//			public boolean accept(File f) {
//				char[] pathname.
//				return false;
//			}
//			@Override
//			public String getDescription() {
//				return "custom";
//			}
//			
//		});;
		saveButton.addActionListener(event -> {
			if(saveDialog.showDialog(frame, "save") == JFileChooser.APPROVE_OPTION) {
				try {
					ImageIO.write(Canvas.image, "png", saveDialog.getSelectedFile());
				} catch (IOException e) {
					JOptionPane.showMessageDialog(frame, "Image failed to be saved!");
					e.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(frame, "Image will not be saved!");
			}
		});
		loadButton = new JButton("load"); loadButton.setFocusable(false);
		loadButton.setBounds(60, 90, 100, 60);
		loadButton.setBackground(Color.YELLOW);
		loadDialog = new JFileChooser();
		loadDialog.setDialogType(JFileChooser.FILES_ONLY);
		loadButton.addActionListener(event -> {
			if(loadDialog.showDialog(frame, "load") == JFileChooser.APPROVE_OPTION) {
				try {
					Canvas.image = ImageIO.read(loadDialog.getSelectedFile());
					Canvas.rasterOFimage = Canvas.image.getRaster();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(frame, "Image failed to be loaded!");
					e.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(frame, "Image will not be loaded!");
			}
		});
		frame.add(saveButton); frame.add(loadButton);
		
		JButton resizeButton = new JButton("Resize");
		resizeButton.setFocusable(false);
		resizeButton.setBounds(60, 150, 100, 60);
		resizeButton.setBackground(Color.GREEN);
		resizeButton.addActionListener(event -> {
			JPanel panel = new JPanel();
			panel.setLayout(new java.awt.GridLayout(2, 2));

			JLabel widthLabel = new JLabel("Width: ");
			JLabel heightLabel = new JLabel("Height: ");
			JTextField widthField = new JTextField(10);
			JTextField heightField = new JTextField(10);

			panel.add(widthLabel);
			panel.add(widthField);
			panel.add(heightLabel);
			panel.add(heightField);

			int result = JOptionPane.showConfirmDialog(frame, panel, "Enter new dimensions",
					JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE);

			if (result == JOptionPane.OK_OPTION) {
				try {
					int newWidth = Integer.parseInt(widthField.getText());
					int newHeight = Integer.parseInt(heightField.getText());

					if (newWidth <= 0 || newHeight <= 0) {
						JOptionPane.showMessageDialog(frame, "Width and height must be positive numbers!", "Error",
								JOptionPane.ERROR_MESSAGE);
						return;
					}

					BufferedImage newImage = new BufferedImage(newWidth, newHeight, BufferedImage.TYPE_3BYTE_BGR);
					Graphics2D g = newImage.createGraphics();
					g.setBackground(Color.white);
					g.clearRect(0, 0, newWidth, newHeight);

					// Eski resmi merkezden yeni resme çizme
					int x = (newWidth - Canvas.image.getWidth()) / 2;
					int y = (newHeight - Canvas.image.getHeight()) / 2;
					if (x < 0)
						x = 0;
					if (y < 0)
						y = 0;

					g.drawImage(Canvas.image, x, y, null);
					g.dispose();

					// Yeni resmi atama
					Canvas.image = newImage;
					Canvas.rasterOFimage = Canvas.image.getRaster();

					// Zoom ve center değerlerini sıfırlama
					Canvas.zoomHorrizontal = 1.0f;
					Canvas.zoomVertical = 1.0f;
					Canvas.horrizontalC = 0;
					Canvas.verticalC = 0;

					JOptionPane.showMessageDialog(frame, "Image resized successfully!", "Success",
							JOptionPane.INFORMATION_MESSAGE);

				} catch (NumberFormatException e) {
					JOptionPane.showMessageDialog(frame, "Please enter valid numbers!", "Error",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		frame.add(resizeButton);
		
		{
			Graphics2D cg = (Graphics2D) Canvas.image.getGraphics();
			cg.setBackground(Color.white);
			cg.clearRect(0, 0, Canvas.image.getWidth(), Canvas.image.getHeight());
		}
		frame.add(canvas);
		updater = new TimerTask() {
			@Override
			public void run() {
				Canvas.updateCanvas();
				frame.repaint();
			}
		};
		updatingTimer.scheduleAtFixedRate(updater, 0, 12);
		
	}



	@Override
	public void mouseMoved(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void mouseClicked(MouseEvent e) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void keyTyped(KeyEvent e) {
		// TODO Auto-generated method stub
		
	}


}
