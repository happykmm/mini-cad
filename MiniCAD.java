import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.util.ArrayList;
import java.io.*;

import javax.swing.*;

public class MiniCAD extends JFrame {

	private MiniCAD frame = this;
	private ButtonPanel buttonPanel = new ButtonPanel();
	private CanvasPanel canvasPanel = new CanvasPanel();
	private MyButton activeButton;


	public MiniCAD() {
		frame.setTitle("MiniCAD - ZhongYu");
		frame.setSize(new Dimension(1000, 600));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.add(canvasPanel, BorderLayout.CENTER);
		frame.add(buttonPanel, BorderLayout.NORTH);
		frame.setVisible(true);
	}

	public static void main(String arg[]) {
		new MiniCAD();
	}
	
	
	
	
	class CanvasPanel extends JPanel {
		private CanvasPanel canvasPanel = this;

		public CanvasPanel() {
			canvasPanel.setLayout(null);
			canvasPanel.setBackground(Color.WHITE);
			canvasPanel.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					int actualX = e.getX();
					int actualY = e.getY();
					if (e.getComponent() instanceof ShapeComponent) {
						actualX += e.getComponent().getX();
						actualY += e.getComponent().getY();
					}
					activeButton.mouseDown(actualX, actualY);
				}

				public void mouseClicked(MouseEvent e) {
					canvasPanel.requestFocus();
				}
			});
		}
		
		public void SaveData() {
			String data = "";
			for (Component component : canvasPanel.getComponents()) {
				if (component instanceof LineComponent) {
					LineComponent line = ((LineComponent)component);
					Line2D line2D = line.line2D;
					double x1 = line2D.getX1() + line.getX();
					double y1 = line2D.getY1() + line.getY();
					double x2 = line2D.getX2() + line.getX();
					double y2 = line2D.getY2() + line.getY();
					data += "line " + x1 + " " + y1 + " " + x2 + " " + y2 + ";";
				} else
				if (	component instanceof RectangleComponent || 
						component instanceof EllipseComponent ||
						component instanceof JTextField) {
					double x = component.getX();
					double y = component.getY();
					double w = component.getWidth();
					double h = component.getHeight();
					String type, extra = "";
					if (component instanceof RectangleComponent) 
						type = "rect";
					else if (component instanceof EllipseComponent)
						type = "elli";
					else {
						type = "text";
						extra = " " + ((JTextField)component).getText();
					}
					data += type + " " + x + " " + y + " " + w + " " + h + extra + ";";
				}
			}
			System.out.println(data);
			try {
				FileOutputStream file = new FileOutputStream("data.txt");
				OutputStreamWriter writer = new OutputStreamWriter(file, "UTF-8");
				writer.write(data);
				writer.close();
				file.close();
			}
			catch(Exception e) {
				System.out.println(e);
			}
			
		}
		
		public void NewText(int x, int y) {
			JTextField text = new JTextField();
			text.setSize(new Dimension(100, 30));
			text.setLocation(x, y);
			text.setBackground(null);
			text.requestFocus();
			canvasPanel.add(text);
			canvasPanel.repaint();
		}
		
		public void NewEllipse(int x1, int y1, int x2, int y2) {
			EllipseComponent ellipse = new EllipseComponent(x1, y1, x2, y2);
			canvasPanel.add(ellipse);
			canvasPanel.repaint();
		}
		
		public void NewRectangle(int x1, int y1, int x2, int y2) {
			RectangleComponent rectangle = new RectangleComponent(x1, y1, x2, y2);
			canvasPanel.add(rectangle);
			canvasPanel.repaint();
		}
		
		public void NewLine(int x1, int y1, int x2, int y2) {
			LineComponent line = new LineComponent(x1, y1, x2, y2);
			canvasPanel.add(line);
			canvasPanel.repaint();
		}
		
		
		abstract class ShapeComponent extends JComponent {
			private ShapeComponent shapeComponent = this;
			int width, height, left, top;
			int deltaX, deltaY, currentX, currentY;
			
			Rectangle2D handleRect2D;
			
			abstract protected Shape shape2D();
			abstract protected boolean contains(double x, double y);
			
			public ShapeComponent(int x1, int y1, int x2, int y2) {
				this.width = Math.abs(x2 - x1);
				this.height = Math.abs(y2 - y1);
				this.left = Math.min(x1, x2);
				this.top = Math.min(y1, y2);
				shapeComponent.setSize(width, height);
				shapeComponent.setLocation(left, top);
				shapeComponent.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						if (!(activeButton instanceof SelectButton)) {
							canvasPanel.processMouseEvent(e);
							return;
						}
						if (contains(e.getX(), e.getY())) {
							shapeComponent.requestFocus();
							handleRect2D = shape2D().getBounds2D();
							deltaX = e.getX();
							deltaY = e.getY();
						} else {
							handleRect2D = null;
						}
						shapeComponent.repaint();
					}
				});
				
				shapeComponent.addMouseMotionListener(new MouseMotionAdapter() {
					public void mouseDragged(MouseEvent e) {
						if (!(activeButton instanceof SelectButton))  {
							return;
						}
						if (contains(e.getX(), e.getY())) {
							currentX = e.getX() + shapeComponent.getX();
							currentY = e.getY() + shapeComponent.getY();
							shapeComponent.setLocation(currentX - deltaX, currentY - deltaY);
						}
					}
				});
				
				shapeComponent.addFocusListener(new FocusListener() {
					public void focusGained(FocusEvent e) {
						shapeComponent.handleRect2D = shape2D().getBounds2D();
						shapeComponent.repaint();
					}
					public void focusLost(FocusEvent e) {
						shapeComponent.handleRect2D = null;
						shapeComponent.repaint();
					}
				});
				
				shapeComponent.addKeyListener(new KeyAdapter() {
					public void keyPressed(KeyEvent e) {
						if (e.getKeyCode() == 127) {  //DEL
							canvasPanel.remove(shapeComponent);
							canvasPanel.repaint();
						}
					}
				});
			}
		}
		
		
		class EllipseComponent extends ShapeComponent {
			Ellipse2D ellipse2D;

			public EllipseComponent(int x1, int y1, int x2, int y2) {
				super(x1, y1, x2, y2);
				ellipse2D = new Ellipse2D.Double(0, 0, width-1, height-1);
			}
			
			@Override
			protected Shape shape2D() {
				return ellipse2D;
			}
			
			@Override
			public void paint(Graphics g) {
				Graphics2D g2D = (Graphics2D) g;
				g2D.draw(shape2D());
				if (handleRect2D != null) {
					double x = handleRect2D.getX();
					double y = handleRect2D.getY();
					double w = handleRect2D.getWidth();
					double h = handleRect2D.getHeight();
					g2D.setColor(Color.black);
					// top
					g2D.fill(new Rectangle.Double(x + w * 0.5 - 3.0, y - 3.0, 6.0, 6.0));
					// left
					g2D.fill(new Rectangle.Double(x - 3.0, y + h * 0.5 - 3.0, 6.0, 6.0));
					// right
					g2D.fill(new Rectangle.Double(x + w - 3.0, y + h * 0.5 - 3.0, 6.0, 6.0));
					// bottom
					g2D.fill(new Rectangle.Double(x + w * 0.5 - 3.0, y + h - 3.0, 6.0, 6.0));
				}
			}

			@Override
			protected boolean contains(double x, double y) {
				return shape2D().contains(x, y);
			}
		}
		
				
		class RectangleComponent extends ShapeComponent {
			Rectangle2D rectangle2D;
			
			public RectangleComponent(int x1, int y1, int x2, int y2) {
				super(x1, y1, x2, y2);
				rectangle2D = new Rectangle2D.Double(0, 0, width-1, height-1);
			}

			@Override
			protected Shape shape2D() {
				return rectangle2D;
			}
			
			@Override
			public void paint(Graphics g) {
				Graphics2D g2D = (Graphics2D) g;
				g2D.draw(shape2D());
				if (handleRect2D != null) {
					double x = handleRect2D.getX();
					double y = handleRect2D.getY();
					double w = handleRect2D.getWidth();
					double h = handleRect2D.getHeight();
					g2D.setColor(Color.black);
					// top-left
					g2D.fill(new Rectangle.Double(x-3.0, y-3.0, 6.0, 6.0));
					// top-right
					g2D.fill(new Rectangle.Double(x+w-3.0, y-3.0, 6.0, 6.0));
					// bottom-left
					g2D.fill(new Rectangle.Double(x-3.0, y+h-3.0, 6.0, 6.0));
					// bottom-right
					g2D.fill(new Rectangle.Double(x+w-3.0, y+h-3.0, 6.0, 6.0));
				}
			}

			@Override
			protected boolean contains(double x, double y) {
				return shape2D().contains(x, y);
			}
		}
		
		class LineComponent extends ShapeComponent {
			public Line2D line2D;
			
			public LineComponent(int x1, int y1, int x2, int y2) {
				super(x1, y1, x2, y2);
				line2D = new Line2D.Double(x1-left, y1-top, x2-left, y2-top);
			}

			@Override
			protected Shape shape2D() {
				return line2D;
			}
			
			@Override
			public void paint(Graphics g) {
				Graphics2D g2D = (Graphics2D) g;
				g2D.draw(shape2D());
				if (handleRect2D != null) {
					double x1 = line2D.getX1();
					double y1 = line2D.getY1();
					double x2 = line2D.getX2();
					double y2 = line2D.getY2();
					g2D.setColor(Color.black);
					// start point
					g2D.fill(new Rectangle.Double(x1-3.0, y1-3.0, 6.0, 6.0));
					// end point
					g2D.fill(new Rectangle.Double(x2-3.0, y2-3.0, 6.0, 6.0));
				}
			}

			@Override
			protected boolean contains(double x, double y) {
				return line2D.intersectsLine(x-1, y-1, 2, 2);
			}
			
		}
	}

	
	
	
	
	class ButtonPanel extends JPanel {
		private ButtonPanel buttonPanel = this;
		private ButtonGroup buttonGroup = new ButtonGroup();
		private SelectButton btnSelect = new SelectButton("Select");
		private DrawLineButton btnLine = new DrawLineButton("Draw Line");
		private DrawRectButton btnRect = new DrawRectButton("Draw Rectangle");
		private DrawElliButton btnElli = new DrawElliButton("Draw Ellipse");
		private DrawTextButton btnText = new DrawTextButton("Draw Text");
		private SaveButton btnSave = new SaveButton("Save");
		private OpenButton btnOpen = new OpenButton("Open");
		
		public ButtonPanel() {
			buttonPanel.setBackground(Color.lightGray);
			buttonPanel.setPreferredSize(new Dimension(500, 42));
			buttonPanel.add(btnSelect);	buttonPanel.add(btnLine); buttonPanel.add(btnRect);	buttonPanel.add(btnElli); buttonPanel.add(btnText);
			buttonGroup.add(btnSelect);	buttonGroup.add(btnLine); buttonGroup.add(btnRect); buttonGroup.add(btnElli); buttonGroup.add(btnText);
			buttonPanel.add(btnSave);  buttonPanel.add(btnOpen);
			activeButton = btnSelect;
		}

	}

	
	class SaveButton extends JButton {
		private SaveButton saveButton = this;

		public SaveButton(String title) {
			super(title);
			saveButton.setPreferredSize(new Dimension(130, 32));
			saveButton.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					canvasPanel.SaveData();
				}
			});
		}
	}
	
	
	class OpenButton extends JButton {
		private OpenButton openButton = this;
		
		public OpenButton(String title) {
			super(title);
			openButton.setPreferredSize(new Dimension(130, 32));
			openButton.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					System.out.println("Open Success");
				}
			});
		}
	}
	
	class MyButton extends JToggleButton {
		private MyButton myButton = this;

		public MyButton(String title) {
			super(title);
			myButton.setPreferredSize(new Dimension(130, 32));
			myButton.addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					activeButton.Cancel();
					activeButton = myButton;
				}
			});
		}
		
		public void mouseClick(int x, int y) {};
		public void mouseDown(int x, int y) {};
		public void Cancel() {};
	}
	
	
	class SelectButton extends MyButton {
		
		public SelectButton(String string) {
			super(string);
		}

		@Override
		public void mouseDown(int x, int y) {
			System.out.println("Select is clicked");
		}
	}
	
	
	class DrawTextButton extends MyButton {

		public DrawTextButton(String string) {
			super(string);
		}

		@Override
		public void mouseDown(int x, int y) {
			canvasPanel.NewText(x, y);
		}
		
	}
	
	
	abstract class DrawShapeButton extends MyButton {
		protected int startX;
		protected int startY;
		private int state;
		
		public DrawShapeButton(String string) {
			super(string);
		}

		@Override
		public void mouseDown(int x, int y) {
			state++;
			if (state == 1) {
				startX = x;
				startY = y;
			} else {  // state == 2
				DrawShape(x, y);
			}
			state %= 2;
		}
		
		public void Cancel() {
			state = 0;
		}
		
		abstract protected void DrawShape(int endX, int endY);
	}
	
	
	class DrawLineButton extends DrawShapeButton {
		public DrawLineButton(String string) {
			super(string);
		}

		@Override
		protected void DrawShape(int endX, int endY) {
			canvasPanel.NewLine(startX, startY, endX, endY);
		}
	}
	
	
	class DrawRectButton extends DrawShapeButton {
		public DrawRectButton(String string) {
			super(string);
		}

		@Override
		protected void DrawShape(int endX, int endY) {
			canvasPanel.NewRectangle(startX, startY, endX, endY);
		}
	}
	
	
	class DrawElliButton extends DrawShapeButton {
		public DrawElliButton(String string) {
			super(string);
		}

		@Override
		protected void DrawShape(int endX, int endY) {
			canvasPanel.NewEllipse(startX, startY, endX, endY);
		}
	}
	
	


}
