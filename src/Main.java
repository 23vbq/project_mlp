import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.*;

public class Main extends JFrame {
	
	Siec mlpNetwork;

	public Main(String title) {
		super(title);
		
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		Toolkit kit = Toolkit.getDefaultToolkit();
		Dimension d = kit.getScreenSize();
		setBounds(d.width / 4, d.height / 4, d.width / 2, d.height / 2);
		
		buildUI();
		initializeMLPNetwork();
		
		setVisible(true);
	}
	
	private void buildUI() {
		JPanel panel = new JPanel(new BorderLayout());
		
		add(panel);
	}

	private void initializeMLPNetwork() {
		int[] layersContent = {8, 5, 3};
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
