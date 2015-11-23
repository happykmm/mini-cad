import java.awt.BorderLayout;
import java.awt.Canvas;
import java.awt.Color;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Shape;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Rectangle2D;
import java.awt.geom.RectangularShape;

import javafx.scene.layout.Border;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JToggleButton;

public class MiniCAD extends JFrame {

	private MiniCAD frame = this;
	private ButtonPanel buttonPanel = new ButtonPanel();
	private CanvasPanel canvasPanel = new CanvasPanel();
	private MyButton activeButton;


	public MiniCAD() {
		frame.setTitle("MiniCAD - ZhongYu");
		frame.setSize(new Dimension(800, 600));
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
					activeButton.mouseDown(e.getX(), e.getY());
				}

				public void mouseClicked(MouseEvent e) {
					canvasPanel.requestFocus();
					int actualX = e.getX();
					int actualY = e.getY();
					if (e.getComponent() instanceof ShapeComponent) {
						actualX += e.getComponent().getX();
						actualY += e.getComponent().getY();
					}
					activeButton.mouseClick(actualX, actualY);
				}

				public void mouseReleased(MouseEvent e) {
					activeButton.mouseUp(e.getX(), e.getY());
				}
			});
		}
		
		public void NewEllipse(int width, int height, int left, int top) {
			EllipseComponent ellipse = new EllipseComponent(width, height, left, top);
			canvasPanel.add(ellipse);
			canvasPanel.repaint();
		}
		
		public void NewRectangle(int width, int height, int left, int top) {
			RectangleComponent rectangle = new RectangleComponent(width, height, left, top);
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
			
			public ShapeComponent(int width, int height, int left, int top) {
				this.width = width;
				this.height = height;
				this.left = left;
				this.top = top;
				shapeComponent.setSize(width, height);
				shapeComponent.setLocation(left, top);
				shapeComponent.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						if (!(activeButton instanceof SelectButton)) {
							return;
						}
						if (shape2D().contains(e.getX(), e.getY())) {
							shapeComponent.requestFocus();
							handleRect2D = shape2D().getBounds2D();
							deltaX = e.getX();
							deltaY = e.getY();
						} else {
							handleRect2D = null;
						}
						shapeComponent.repaint();
					}
					
					public void mouseClicked(MouseEvent e) {
						if (!(activeButton instanceof SelectButton)) {
							canvasPanel.processMouseEvent(e);
							return;
						}
					}
				});
				
				shapeComponent.addMouseMotionListener(new MouseMotionAdapter() {
					public void mouseDragged(MouseEvent e) {
						if (!(activeButton instanceof SelectButton))  {
							return;
						}
						if (shape2D().contains(e.getX(), e.getY())) {
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

			public EllipseComponent(int width, int height, int left, int top) {
				super(width+1, height+1, left, top);
				ellipse2D = new Ellipse2D.Double(0, 0, width, height);
			}
			
			@Override
			protected Shape shape2D() {
				return ellipse2D;
			}
			
			@Override
			public void paint(Graphics g) {
				Graphics2D g2D = (Graphics2D) g;
				g2D.draw(ellipse2D);
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
		}
		
				
		class RectangleComponent extends ShapeComponent {
			Rectangle2D rectangle2D;
			
			public RectangleComponent(int width, int height, int left, int top) {
				super(width+1, height+1, left, top);
				rectangle2D = new Rectangle2D.Double(0, 0, width, height);
			}

			@Override
			protected Shape shape2D() {
				return rectangle2D;
			}
			
			@Override
			public void paint(Graphics g) {
				Graphics2D g2D = (Graphics2D) g;
				g2D.draw(rectangle2D);
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
		}
		
		class LineComponent extends ShapeComponent {
			Line2D line2D;
			
			public LineComponent(int x1, int y1, int x2, int y2) {
				int width = Math.abs(x2 - x1);
				int height = Math.abs(y2 - y1);
				int left = Math.min(x1, x2);
				int top = Math.min(y1, y2);
				super(width, height, left, top);
				line2D = new Line2D.Double(0, 0, width, height);
			}

			@Override
			protected Shape shape2D() {
				return line2D;
			}
			
			@Override
			public void paint(Graphics g) {
				Graphics2D g2D = (Graphics2D) g;
				g2D.draw(line2D);
				if (handleRect2D != null) {
					double x = handleRect2D.getX();
					double y = handleRect2D.getY();
					double w = handleRect2D.getWidth();
					double h = handleRect2D.getHeight();
					g2D.setColor(Color.black);
					// top-left
					g2D.fill(new Rectangle.Double(x-3.0, y-3.0, 6.0, 6.0));
					// bottom-right
					g2D.fill(new Rectangle.Double(x+w-3.0, y+h-3.0, 6.0, 6.0));
				}
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
		
		public ButtonPanel() {
			buttonPanel.setBackground(Color.lightGray);
			buttonPanel.setPreferredSize(new Dimension(500, 42));
			buttonPanel.add(btnSelect);	buttonPanel.add(btnLine); buttonPanel.add(btnRect);	buttonPanel.add(btnElli); buttonPanel.add(btnText);
			buttonGroup.add(btnSelect);	buttonGroup.add(btnLine); buttonGroup.add(btnRect); buttonGroup.add(btnElli); buttonGroup.add(btnText);
			activeButton = btnSelect;
		}

	}

	
	class MyButton extends JToggleButton {
		private MyButton myButton = this;

		public MyButton() {
			this("");
		}

		public MyButton(String title) {
			super(title);
			myButton.setPreferredSize(new Dimension(130, 32));
			myButton.addMouseListener(new MouseAdapter() {
				public void mouseClicked(MouseEvent e) {
					System.out.println("A button is clicked");
					activeButton.Cancel();
					activeButton = myButton;
				}
			});
		}
		
		public void mouseClick(int x, int y) {};
		public void mouseDown(int x, int y) {};
		public void mouseUp(int x, int y) {};
		public void Cancel() {};
	}
	
	
	class SelectButton extends MyButton {
		
		public SelectButton(String string) {
			super(string);
		}

		@Override
		public void mouseClick(int x, int y) {
			System.out.println("Select is clicked");
		}


	}
	
	
	class DrawTextButton extends MyButton {

		public DrawTextButton(String string) {
			super(string);
		}

		@Override
		public void mouseClick(int x, int y) {
			System.out.println("Draw Text is clicked");
			
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
		public void mouseClick(int x, int y) {
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
			int width = Math.abs(endX - startX);
			int height = Math.abs(endY - startY);
			int left = Math.min(startX, endX);
			int top = Math.min(startY, endY);
			canvasPanel.NewRectangle(width, height, left, top);
		}
	}
	
	
	class DrawElliButton extends DrawShapeButton {
		public DrawElliButton(String string) {
			super(string);
		}

		@Override
		protected void DrawShape(int endX, int endY) {
			int width = Math.abs(endX - startX);
			int height = Math.abs(endY - startY);
			int left = Math.min(startX, endX);
			int top = Math.min(startY, endY);
			canvasPanel.NewEllipse(width, height, left, top);
		}
	}
	
	


}
