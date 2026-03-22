import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Rectangle;
import java.awt.RenderingHints;

import javax.swing.JSlider;
import javax.swing.plaf.basic.BasicSliderUI;

/**
 * Poziomy suwak: szary tor, wypełnienie kolorem po lewej stronie wartości, prostokątny uchwyt.
 */
public class ModernRectSliderUI extends BasicSliderUI {

	private final Color fillColor;
	private final Color trackBackground;

	public ModernRectSliderUI(JSlider slider, Color fillColor) {
		super(slider);
		this.fillColor = fillColor;
		this.trackBackground = new Color(228, 228, 232);
	}

	@Override
	protected Dimension getThumbSize() {
		return new Dimension(12, 20);
	}

	@Override
	public void paintTrack(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
		Rectangle tr = trackRect;
		if (tr.width <= 0 || tr.height <= 0) {
			g2.dispose();
			return;
		}
		g2.setColor(trackBackground);
		g2.fillRoundRect(tr.x, tr.y, tr.width, tr.height, 6, 6);

		int centerX = thumbRect.x + thumbRect.width / 2;
		int fillWidth = Math.min(tr.width, Math.max(0, centerX - tr.x));
		g2.setColor(fillColor);
		g2.fillRoundRect(tr.x, tr.y, fillWidth, tr.height, 6, 6);
		g2.dispose();
	}

	@Override
	public void paintThumb(Graphics g) {
		Graphics2D g2 = (Graphics2D) g.create();
		Rectangle r = thumbRect;
		g2.setColor(Color.WHITE);
		g2.fillRect(r.x, r.y, r.width, r.height);
		g2.setColor(fillColor.darker());
		g2.drawRect(r.x, r.y, r.width - 1, r.height - 1);
		g2.dispose();
	}
}
