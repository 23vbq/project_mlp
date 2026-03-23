import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JPanel;

public class MetricsChartPanel extends JPanel {

	public enum ChartType {
		LINE,
		BAR
	}

	private static final int PAD_LEFT = 38;
	private static final int PAD_RIGHT = 12;
	private static final int PAD_TOP = 14;
	private static final int PAD_BOTTOM = 28;

	private final ChartType chartType;
	private final List<Double> lineValues = new ArrayList<>();
	private String[] barLabels = new String[0];
	private double[] barValues = new double[0];

	public MetricsChartPanel(ChartType chartType, String title) {
		super(new BorderLayout());
		this.chartType = chartType;
		setBorder(BorderFactory.createTitledBorder(title));
		setPreferredSize(new Dimension(240, 180));
		setOpaque(true);
	}

	public void addLinePoint(double value) {
		if (chartType != ChartType.LINE) {
			return;
		}
		lineValues.add(value);
		repaint();
	}

	public void clearLine() {
		if (chartType != ChartType.LINE) {
			return;
		}
		lineValues.clear();
		repaint();
	}

	public void setBarData(String[] labels, double[] values) {
		if (chartType != ChartType.BAR) {
			return;
		}
		if (labels == null || values == null || labels.length != values.length) {
			barLabels = new String[0];
			barValues = new double[0];
		} else {
			barLabels = labels.clone();
			barValues = values.clone();
		}
		repaint();
	}

	public void clearBars() {
		if (chartType != ChartType.BAR) {
			return;
		}
		barLabels = new String[0];
		barValues = new double[0];
		repaint();
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

		int w = getWidth();
		int h = getHeight();
		int x0 = PAD_LEFT;
		int y0 = h - PAD_BOTTOM;
		int x1 = w - PAD_RIGHT;
		int y1 = PAD_TOP;

		if (x1 <= x0 || y0 <= y1) {
			g2.dispose();
			return;
		}

		g2.setColor(new Color(120, 120, 120));
		g2.drawLine(x0, y0, x1, y0);
		g2.drawLine(x0, y0, x0, y1);

		if (chartType == ChartType.LINE) {
			drawLineChart(g2, x0, y0, x1, y1);
		} else {
			drawBarChart(g2, x0, y0, x1, y1);
		}

		g2.dispose();
	}

	private void drawLineChart(Graphics2D g2, int x0, int y0, int x1, int y1) {
		if (lineValues.isEmpty()) {
			drawNoData(g2, x0, y0, x1, y1);
			return;
		}

		double max = 0.0;
		for (double v : lineValues) {
			if (v > max) {
				max = v;
			}
		}
		if (max <= 0.0) {
			max = 1.0;
		}

		int n = lineValues.size();
		double xStep = n > 1 ? (double) (x1 - x0) / (n - 1) : 0.0;
		g2.setColor(new Color(40, 110, 190));
		g2.setStroke(new BasicStroke(1.8f));

		int prevX = x0;
		int prevY = y0 - (int) Math.round((lineValues.get(0) / max) * (y0 - y1));
		for (int i = 1; i < n; i++) {
			int x = x0 + (int) Math.round(i * xStep);
			int y = y0 - (int) Math.round((lineValues.get(i) / max) * (y0 - y1));
			g2.drawLine(prevX, prevY, x, y);
			prevX = x;
			prevY = y;
		}

		String maxLabel = String.format("%.4f", max);
		g2.setColor(new Color(90, 90, 90));
		g2.drawString(maxLabel, 4, y1 + 10);
		g2.drawString("0", 18, y0);
	}

	private void drawBarChart(Graphics2D g2, int x0, int y0, int x1, int y1) {
		if (barValues.length == 0) {
			drawNoData(g2, x0, y0, x1, y1);
			return;
		}

		int n = barValues.length;
		int chartW = x1 - x0;
		int slotW = Math.max(1, chartW / n);
		int barW = Math.max(8, slotW - 10);
		FontMetrics fm = g2.getFontMetrics();

		for (int i = 0; i < n; i++) {
			double value = Math.max(0.0, Math.min(100.0, barValues[i]));
			int barH = (int) Math.round((value / 100.0) * (y0 - y1));
			int x = x0 + i * slotW + Math.max(0, (slotW - barW) / 2);
			int y = y0 - barH;

			g2.setColor(new Color(62, 148, 82));
			g2.fillRect(x, y, barW, barH);
			g2.setColor(new Color(80, 80, 80));
			g2.drawRect(x, y, barW, barH);

			String valueLabel = String.format("%.0f", value);
			int valueW = fm.stringWidth(valueLabel);
			g2.drawString(valueLabel, x + (barW - valueW) / 2, y - 4);

			if (i < barLabels.length) {
				String lab = barLabels[i];
				int labW = fm.stringWidth(lab);
				g2.drawString(lab, x + (barW - labW) / 2, y0 + fm.getAscent() + 2);
			}
		}

		g2.drawString("100", 6, y1 + 10);
		g2.drawString("0", 18, y0);
	}

	private void drawNoData(Graphics2D g2, int x0, int y0, int x1, int y1) {
		String text = "Brak danych";
		FontMetrics fm = g2.getFontMetrics();
		int tx = x0 + ((x1 - x0) - fm.stringWidth(text)) / 2;
		int ty = y1 + ((y0 - y1) + fm.getAscent()) / 2;
		g2.setColor(new Color(130, 130, 130));
		g2.drawString(text, tx, ty);
	}
}
