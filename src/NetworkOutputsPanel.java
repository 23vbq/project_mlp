import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;

/**
 * Trzy neurony wyjściowe (E, F, Z): kropka + litera + wartość.
 * Logika predykcji podłączymy później; na razie {@link #setIdle()}.
 */
public class NetworkOutputsPanel extends JPanel {

	public enum DotMode {
		/** Szara obwódka (brak aktywności / start). */
		IDLE,
		/** Zielone wypełnienie — ten neuron „wygrał”. */
		WINNER,
		/** Szara pusta — pozostałe przy wygranej jednej klasie. */
		OTHER,
		/** Czerwone — żaden niepewny (plan: próg 0.5). */
		ALL_UNCERTAIN
	}

	private static final String[] LETTERS = { "E", "F", "Z" };

	private final DotIndicator[] dots = new DotIndicator[3];
	private final JLabel[] valueLabels = new JLabel[3];

	public NetworkOutputsPanel() {
		super(new BorderLayout(4, 4));
		setBorder(BorderFactory.createTitledBorder("Wyjścia sieci"));

		JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 16, 8));
		row.setOpaque(false);

		for (int i = 0; i < 3; i++) {
			JPanel cell = new JPanel(new FlowLayout(FlowLayout.CENTER, 6, 0));
			cell.setOpaque(false);

			dots[i] = new DotIndicator();
			valueLabels[i] = new JLabel("—", SwingConstants.CENTER);
			valueLabels[i].setFont(valueLabels[i].getFont().deriveFont(Font.MONOSPACED, 12f));

			cell.add(dots[i]);
			cell.add(new JLabel(LETTERS[i]));
			cell.add(valueLabels[i]);
			row.add(cell);
		}

		add(row, BorderLayout.CENTER);
		setIdle();
	}

	/** Stan początkowy / po wyczyszczeniu podglądu. */
	public void setIdle() {
		for (int i = 0; i < 3; i++) {
			dots[i].setMode(DotMode.IDLE);
			valueLabels[i].setText("—");
		}
		repaint();
	}

	/**
	 * Ustawia surowe wartości i tryb kropek (bez logiki progów — tylko UI).
	 */
	public void setReadout(double v0, double v1, double v2, DotMode m0, DotMode m1, DotMode m2) {
		valueLabels[0].setText(String.format("%.2f", v0));
		valueLabels[1].setText(String.format("%.2f", v1));
		valueLabels[2].setText(String.format("%.2f", v2));
		dots[0].setMode(m0);
		dots[1].setMode(m1);
		dots[2].setMode(m2);
		repaint();
	}

	private static final class DotIndicator extends JPanel {

		private static final int SZ = 18;

		private DotMode mode = DotMode.IDLE;

		DotIndicator() {
			setOpaque(false);
			setPreferredSize(new Dimension(SZ, SZ));
			setMinimumSize(new Dimension(SZ, SZ));
		}

		void setMode(DotMode m) {
			this.mode = m;
			repaint();
		}

		@Override
		protected void paintComponent(Graphics g) {
			super.paintComponent(g);
			Graphics2D g2 = (Graphics2D) g.create();
			g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
			int pad = 2;
			int d = SZ - 2 * pad;
			int x = (getWidth() - d) / 2;
			int y = (getHeight() - d) / 2;

			Color fill;
			Color stroke = new Color(90, 90, 90);
			switch (mode) {
			case WINNER:
				fill = new Color(30, 160, 70);
				break;
			case ALL_UNCERTAIN:
				fill = new Color(200, 60, 50);
				break;
			case OTHER:
			case IDLE:
			default:
				fill = null;
				break;
			}

			if (fill != null) {
				g2.setColor(fill);
				g2.fillOval(x, y, d, d);
			}
			g2.setColor(stroke);
			g2.setStroke(new BasicStroke(1.5f));
			g2.drawOval(x, y, d, d);
			g2.dispose();
		}
	}
}
