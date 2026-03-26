import java.awt.Color;
import java.awt.Graphics;
import java.awt.image.BufferedImage;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.*;

public class PaintCanvasComponent extends JComponent {
	private static final int DRAW_RESOLUTION = 512;
	private static final int BRUSH_RADIUS = 8;
	private static final float BRUSH_STRENGTH = 0.24f;

	private int ceilsAmount;
	private float[][] canvas;
	private BufferedImage canvasImage;
	private int lastCanvasX;
	private int lastCanvasY;
	private boolean currentStrokeErase;
	
	public PaintCanvasComponent(int ceilsAmount) {
		super();
		
		this.ceilsAmount = ceilsAmount;
		this.canvas = new float[DRAW_RESOLUTION][DRAW_RESOLUTION];
		this.canvasImage = new BufferedImage(DRAW_RESOLUTION, DRAW_RESOLUTION, BufferedImage.TYPE_INT_RGB);
		this.lastCanvasX = -1;
		this.lastCanvasY = -1;
		this.currentStrokeErase = false;
		clear();
		
		addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent e) {
				int[] point = toCanvasCoordinates(e.getX(), e.getY());
				if (point == null) {
					lastCanvasX = -1;
					lastCanvasY = -1;
					return;
				}

				lastCanvasX = point[0];
				lastCanvasY = point[1];
				currentStrokeErase = SwingUtilities.isRightMouseButton(e);
			}

			@Override
			public void mouseReleased(MouseEvent e) {
				lastCanvasX = -1;
				lastCanvasY = -1;
			}
		});
		
		addMouseMotionListener(new MouseMotionAdapter() {
			@Override
			public void mouseDragged(MouseEvent e) {
				paintEventHandler(e);
			}
		});
	}
	
	public void clear() {
		for (int row = 0; row < DRAW_RESOLUTION; row++) {
			for (int col = 0; col < DRAW_RESOLUTION; col++) {
				setPixel(col, row, 0.0f);
			}
		}
		
		repaint();
	}
	
	public double[] getContent() {
		double[] fields = new double[ceilsAmount * ceilsAmount];
		double tileSize = (double) DRAW_RESOLUTION / ceilsAmount;

		for (int row = 0; row < ceilsAmount; row++) {
			for (int col = 0; col < ceilsAmount; col++) {
				int startY = (int) Math.floor(row * tileSize);
				int endY = (int) Math.floor((row + 1) * tileSize);
				int startX = (int) Math.floor(col * tileSize);
				int endX = (int) Math.floor((col + 1) * tileSize);
				double sum = 0.0;
				int count = 0;

				for (int y = startY; y < endY; y++) {
					for (int x = startX; x < endX; x++) {
						sum += canvas[y][x];
						count++;
					}
				}

				fields[row * ceilsAmount + col] = count == 0 ? 0.0 : sum / count;
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

		int drawSize = getDrawSize();
		if (drawSize <= 0) {
			return;
		}

		int offsetX = (getWidth() - drawSize) / 2;
		int offsetY = (getHeight() - drawSize) / 2;

		g.setColor(Color.WHITE);
		g.fillRect(offsetX, offsetY, drawSize, drawSize);
		g.drawImage(canvasImage, offsetX, offsetY, drawSize, drawSize, null);
	}
	
	private void paintEventHandler(MouseEvent e) {
		int[] point = toCanvasCoordinates(e.getX(), e.getY());
		if (point == null) {
			lastCanvasX = -1;
			lastCanvasY = -1;
			return;
		}

		int currentX = point[0];
		int currentY = point[1];
		if (lastCanvasX < 0 || lastCanvasY < 0) {
			lastCanvasX = currentX;
			lastCanvasY = currentY;
		}

		boolean eraseMode = currentStrokeErase || SwingUtilities.isRightMouseButton(e) || e.isMetaDown();
		drawStroke(lastCanvasX, lastCanvasY, currentX, currentY, eraseMode);

		lastCanvasX = currentX;
		lastCanvasY = currentY;
		repaint();
	}

	private void drawStroke(int x0, int y0, int x1, int y1, boolean eraseMode) {
		int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
		if (steps == 0) {
			applyBrush(x0, y0, eraseMode);
			return;
		}

		for (int i = 0; i <= steps; i++) {
			double t = (double) i / steps;
			int x = (int) Math.round(x0 + (x1 - x0) * t);
			int y = (int) Math.round(y0 + (y1 - y0) * t);
			applyBrush(x, y, eraseMode);
		}
	}

	private void applyBrush(int centerX, int centerY, boolean eraseMode) {
		int radiusSq = BRUSH_RADIUS * BRUSH_RADIUS;

		for (int dy = -BRUSH_RADIUS; dy <= BRUSH_RADIUS; dy++) {
			for (int dx = -BRUSH_RADIUS; dx <= BRUSH_RADIUS; dx++) {
				int distSq = dx * dx + dy * dy;
				if (distSq > radiusSq) {
					continue;
				}

				int x = centerX + dx;
				int y = centerY + dy;
				if (x < 0 || x >= DRAW_RESOLUTION || y < 0 || y >= DRAW_RESOLUTION) {
					continue;
				}

				float falloff = 1.0f - ((float) distSq / radiusSq);
				float delta = BRUSH_STRENGTH * falloff;
				float next = eraseMode ? canvas[y][x] - delta : canvas[y][x] + delta;
				setPixel(x, y, clamp(next));
			}
		}
	}

	private void setPixel(int x, int y, float value) {
		canvas[y][x] = value;
		int shade = 255 - Math.round(value * 255.0f);
		int rgb = (shade << 16) | (shade << 8) | shade;
		canvasImage.setRGB(x, y, rgb);
	}

	private float clamp(float value) {
		if (value < 0.0f) {
			return 0.0f;
		}
		if (value > 1.0f) {
			return 1.0f;
		}
		return value;
	}

	private int[] toCanvasCoordinates(int x, int y) {
		int drawSize = getDrawSize();
		if (drawSize <= 0) {
			return null;
		}

		int offsetX = (getWidth() - drawSize) / 2;
		int offsetY = (getHeight() - drawSize) / 2;

		int localX = x - offsetX;
		int localY = y - offsetY;
		if (localX < 0 || localY < 0 || localX >= drawSize || localY >= drawSize) {
			return null;
		}

		int canvasX = (int) ((long) localX * DRAW_RESOLUTION / drawSize);
		int canvasY = (int) ((long) localY * DRAW_RESOLUTION / drawSize);
		return new int[] { canvasX, canvasY };
	}

	private int getDrawSize() {
		return Math.min(getWidth(), getHeight());
	}
}
