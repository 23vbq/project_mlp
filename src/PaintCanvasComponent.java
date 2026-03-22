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
		for (int row = 0; row < ceilsAmount; row++) {
			for (int col = 0; col < ceilsAmount; col++) {
				canvas[row][col] = false;
			}
		}
		
		repaint();
	}
	
	public double[] getContent() {
		double[] fields = new double[ceilsAmount * ceilsAmount];

		for (int row = 0; row < ceilsAmount; row++) {
			for (int col = 0; col < ceilsAmount; col++) {
				fields[row * ceilsAmount + col] = canvas[row][col] ? 1.0 : 0.0;
			}
		}

		return fields;
	}
	
	public int getCeilsAmount() {
		return ceilsAmount;
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);

		int cellW = getCeilWidth();
		int cellH = getCeilHeight();

		for (int row = 0; row < ceilsAmount; row++) {
			for (int col = 0; col < ceilsAmount; col++) {
				g.setColor(Color.GRAY);
				g.fillRect(col * cellW, row * cellH, cellW, cellH);
				g.setColor(canvas[row][col] ? Color.BLACK : Color.WHITE);
				g.fillRect(col * cellW + 1, row * cellH + 1, cellW - 2, cellH - 2);
			}
		}
	}
	
	private void paintEventHandler(MouseEvent e, boolean force) {
		int cellW = getCeilWidth();
		int cellH = getCeilHeight();
		int col = e.getX() / cellW;
		int row = e.getY() / cellH;

		if (col < 0 || col > ceilsAmount - 1 || row < 0 || row > ceilsAmount - 1) {
			return;
		}

		if (force) {
			canvas[row][col] = true;
		} else {
			canvas[row][col] = !canvas[row][col];
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
