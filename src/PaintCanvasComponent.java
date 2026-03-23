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

		int cellSize = getCellSize();
		if (cellSize <= 0) {
			return;
		}
		int gridSize = cellSize * ceilsAmount;
		int offsetX = (getWidth() - gridSize) / 2;
		int offsetY = (getHeight() - gridSize) / 2;

		for (int row = 0; row < ceilsAmount; row++) {
			for (int col = 0; col < ceilsAmount; col++) {
				g.setColor(Color.GRAY);
				g.fillRect(offsetX + col * cellSize, offsetY + row * cellSize, cellSize, cellSize);
				g.setColor(canvas[row][col] ? Color.BLACK : Color.WHITE);
				if (cellSize > 2) {
					g.fillRect(offsetX + col * cellSize + 1, offsetY + row * cellSize + 1, cellSize - 2, cellSize - 2);
				}
			}
		}
	}
	
	private void paintEventHandler(MouseEvent e, boolean force) {
		int cellSize = getCellSize();
		if (cellSize <= 0) {
			return;
		}
		int gridSize = cellSize * ceilsAmount;
		int offsetX = (getWidth() - gridSize) / 2;
		int offsetY = (getHeight() - gridSize) / 2;

		int localX = e.getX() - offsetX;
		int localY = e.getY() - offsetY;
		if (localX < 0 || localY < 0 || localX >= gridSize || localY >= gridSize) {
			return;
		}

		int col = localX / cellSize;
		int row = localY / cellSize;

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
	
	private int getCellSize() {
		return Math.min(getWidth(), getHeight()) / ceilsAmount;
	}
}
