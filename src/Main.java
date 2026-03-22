import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;

import javax.swing.JFrame;

public class Main extends JFrame {

	Siec mlpNetwork;

	public Main(String title) {
		super(title);

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension d = kit.getScreenSize();
		setBounds(d.width / 4, d.height / 4, d.width / 2, d.height / 2);

		initializeMLPNetwork();
		buildUI();

		setVisible(true);
	}

	private void buildUI() {
		setContentPane(new MainAppPanel(mlpNetwork));
	}

	private void initializeMLPNetwork() {
		int[] layersContent = { 8, 5, 3 };
		mlpNetwork = new Siec(64, 3, layersContent);
	}

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				new Main("MLP Project - Recognition of letters E, F, Z");
			}
		});
	}
}
