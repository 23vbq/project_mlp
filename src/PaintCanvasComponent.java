import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.*;

public class PaintCanvasComponent extends JComponent {
	private int ceilsAmount;
	private boolean[][] canvas;
	
	public PaintCanvasComponent(int ceilsAmount) {
		super();
		
		this.ceilsAmount = ceilsAmount;
		this.canvas = new boolean[ceilsAmount][ceilsAmount];
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				paintEventHandler(e, false);
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				paintEventHandler(e, true);
			}
		});
	}
	
	public void clear() {
		for (int i = 0; i < ceilsAmount; i++) {
			for (int j = 0; j < ceilsAmount; j++) {
				canvas[i][j] = false;
			}
		}
		repaint();
	}
	
	public double[] getContent() {
		double[] fields = new double[ceilsAmount * ceilsAmount];
		
		for (int i = 0; i < ceilsAmount; i++) {
			for (int j = 0; j < ceilsAmount; j++) {
				fields[i * ceilsAmount + j] = canvas[i][j] ? 1.0 : 0.0;
			}
		}
		
		return fields;
	}
	
	public int getCeilsAmount() {
		return ceilsAmount;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		int cellW = getCeilWidth();
		int cellH = getCeilHeight();
		
		for (int i = 0; i < ceilsAmount; i++) {
			for (int j = 0; j < ceilsAmount; j++) {
				g.setColor(Color.GRAY);
				g.fillRect(i * cellW, j * cellH, cellW, cellH);
				
				g.setColor(canvas[i][j] ? Color.BLACK : Color.WHITE);
				g.fillRect(i * cellW + 1, j * cellH + 1, cellW - 2, cellH - 2);
			}
		}
		
		super.paintComponent(g);
	}
	
	private void paintEventHandler(MouseEvent e, boolean force) {
		int cellW = getCeilWidth();
		int cellH = getCeilHeight();
		
		int i = e.getX() / cellW;
		int j = e.getY() / cellH;
		
		if (
			i < 0
			|| i > ceilsAmount - 1
			|| j < 0
			|| j > ceilsAmount - 1
		) {
			return;
		}
		
		if (force) {
			canvas[i][j] = true;
		} else {
			canvas[i][j] = !canvas[i][j];
		}
		
		repaint();
	}
	
	private int getCeilWidth() {
		return getWidth() / ceilsAmount;
	}
	
	private int getCeilHeight() {
		return getHeight() / ceilsAmount;
	}
}
