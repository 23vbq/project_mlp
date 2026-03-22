import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

import javax.swing.*;
import javax.swing.event.*;

public class MainAppPanel extends JPanel {

	private static final DateTimeFormatter LOG_TIME = DateTimeFormatter.ofPattern("HH:mm:ss");

	private Siec mlpNetwork;

	private final PaintCanvasComponent paintCanvas;
	private final JLabel resultLabel;
	private final JTextArea logArea;
	private final JSlider epochsSlider;
	private final JLabel epochsValueLabel;
	private final JSlider learningRateSlider;
	private final JLabel learningRateValueLabel;
	private final JRadioButton radioE;
	private final JRadioButton radioF;
	private final JRadioButton radioZ;

	private final JPanel trainingChartPlaceholder;
	private final JPanel testChartPlaceholder;
	private final JPanel outputNeuronsPlaceholder;

	public MainAppPanel(Siec network) {
		super(new BorderLayout(8, 8));
		this.mlpNetwork = network;

		setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

		paintCanvas = new PaintCanvasComponent(8);
		paintCanvas.setPreferredSize(new Dimension(280, 280));

		resultLabel = new JLabel("Wynik: —");
		resultLabel.setHorizontalAlignment(SwingConstants.LEFT);

		logArea = new JTextArea(12, 40);
		logArea.setEditable(false);
		logArea.setLineWrap(true);
		logArea.setWrapStyleWord(true);
		JScrollPane logScroll = new JScrollPane(logArea);
		logScroll.setBorder(BorderFactory.createTitledBorder("Log"));

		epochsSlider = new JSlider(JSlider.HORIZONTAL, 100, 10000, 1000);
		epochsSlider.setMajorTickSpacing(2000);
		epochsSlider.setMinorTickSpacing(100);
		epochsSlider.setPaintTicks(true);
		epochsSlider.setPaintLabels(true);
		epochsSlider.setSnapToTicks(true);
		epochsSlider.setLabelTable(epochsSlider.createStandardLabels(2000));
		epochsSlider.setUI(new ModernRectSliderUI(epochsSlider, new Color(0, 114, 178)));
		styleSliderFont(epochsSlider);
		epochsValueLabel = boldValueLabel(String.valueOf(epochsSlider.getValue()));
		epochsSlider.addChangeListener(e -> epochsValueLabel.setText(String.valueOf(epochsSlider.getValue())));

		learningRateSlider = new JSlider(JSlider.HORIZONTAL, 1, 100, 10);
		learningRateSlider.setMajorTickSpacing(25);
		learningRateSlider.setMinorTickSpacing(5);
		learningRateSlider.setPaintTicks(true);
		learningRateSlider.setPaintLabels(false);
		learningRateSlider.setUI(new ModernRectSliderUI(learningRateSlider, new Color(34, 139, 34)));
		styleSliderFont(learningRateSlider);
		learningRateValueLabel = boldValueLabel(formatLearningRate(learningRateSlider.getValue()));
		learningRateSlider.addChangeListener(
				e -> learningRateValueLabel.setText(formatLearningRate(learningRateSlider.getValue())));

		radioE = new JRadioButton("E", true);
		radioF = new JRadioButton("F");
		radioZ = new JRadioButton("Z");
		ButtonGroup letterGroup = new ButtonGroup();
		letterGroup.add(radioE);
		letterGroup.add(radioF);
		letterGroup.add(radioZ);

		outputNeuronsPlaceholder = new JPanel();
		outputNeuronsPlaceholder.setBorder(BorderFactory.createTitledBorder("Wyjścia sieci"));
		outputNeuronsPlaceholder.setPreferredSize(new Dimension(200, 72));
		outputNeuronsPlaceholder.add(new JLabel("—"));

		trainingChartPlaceholder = new JPanel(new BorderLayout());
		trainingChartPlaceholder.setBorder(BorderFactory.createTitledBorder("Wykres MSE (uczenie)"));
		trainingChartPlaceholder.add(new JLabel(" ", SwingConstants.CENTER), BorderLayout.CENTER);
		trainingChartPlaceholder.setPreferredSize(new Dimension(200, 160));

		testChartPlaceholder = new JPanel(new BorderLayout());
		testChartPlaceholder.setBorder(BorderFactory.createTitledBorder("Wykres accuracy (test)"));
		testChartPlaceholder.add(new JLabel(" ", SwingConstants.CENTER), BorderLayout.CENTER);
		testChartPlaceholder.setPreferredSize(new Dimension(200, 160));

		JPanel chartsRow = new JPanel(new GridLayout(1, 2, 8, 0));
		chartsRow.add(trainingChartPlaceholder);
		chartsRow.add(testChartPlaceholder);

		JPanel rightColumn = new JPanel(new BorderLayout(0, 8));
		rightColumn.add(logScroll, BorderLayout.NORTH);
		rightColumn.add(outputNeuronsPlaceholder, BorderLayout.CENTER);
		rightColumn.add(chartsRow, BorderLayout.SOUTH);

		JPanel leftTop = buildLeftTopPanel();
		JPanel leftBottom = buildLeftBottomPanel();

		JSplitPane leftSplit = new JSplitPane(JSplitPane.VERTICAL_SPLIT, leftTop, leftBottom);
		leftSplit.setResizeWeight(0.55);
		leftSplit.setOneTouchExpandable(true);

		// Stałe 50/50 lewo/prawo — bez poziomego JSplitPane (nie da się zmieniać szerokości kolumn)
		JPanel mainColumns = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.weighty = 1.0;
		gbc.weightx = 0.5;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.insets = new Insets(0, 0, 0, 6);
		mainColumns.add(leftSplit, gbc);
		gbc.gridx = 1;
		gbc.insets = new Insets(0, 6, 0, 0);
		mainColumns.add(rightColumn, gbc);

		add(mainColumns, BorderLayout.CENTER);

		log("UI załadowane. Handlery w trybie stub (tylko log).");
	}

	private JPanel buildLeftTopPanel() {
		JPanel east = new JPanel();
		east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
		east.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

		JButton guessBtn = new JButton("Zgadnij");
		guessBtn.addActionListener(e -> onGuess());
		JButton clearBtn = new JButton("Wyczyść");
		clearBtn.addActionListener(e -> onClear());

		east.add(guessBtn);
		east.add(Box.createVerticalStrut(8));
		east.add(clearBtn);
		east.add(Box.createVerticalStrut(16));
		east.add(resultLabel);
		east.add(Box.createVerticalGlue());

		JPanel p = new JPanel(new BorderLayout());
		p.setBorder(BorderFactory.createTitledBorder("Rysowanie"));
		p.add(paintCanvas, BorderLayout.CENTER);
		p.add(east, BorderLayout.EAST);
		return p;
	}

	private JPanel buildLeftBottomPanel() {
		JPanel p = new JPanel();
		p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
		p.setBorder(BorderFactory.createTitledBorder("Uczenie i dane"));

		epochsSlider.setPreferredSize(new Dimension(320, 72));
		learningRateSlider.setPreferredSize(new Dimension(320, 56));

		JPanel epochRow = new JPanel(new BorderLayout(8, 4));
		epochRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
		JLabel epL = new JLabel("Liczba epok");
		epL.setFont(epL.getFont().deriveFont(Font.PLAIN));
		epochRow.add(epL, BorderLayout.NORTH);
		JPanel epochSouth = new JPanel(new BorderLayout(8, 0));
		epochSouth.add(epochsSlider, BorderLayout.CENTER);
		epochSouth.add(epochsValueLabel, BorderLayout.EAST);
		epochRow.add(epochSouth, BorderLayout.CENTER);

		JPanel lrRow = new JPanel(new BorderLayout(8, 4));
		lrRow.setBorder(BorderFactory.createEmptyBorder(4, 0, 8, 0));
		JLabel lrL = new JLabel("Learning rate");
		lrL.setFont(lrL.getFont().deriveFont(Font.PLAIN));
		lrRow.add(lrL, BorderLayout.NORTH);
		JPanel lrSouth = new JPanel(new BorderLayout(8, 0));
		lrSouth.add(learningRateSlider, BorderLayout.CENTER);
		lrSouth.add(learningRateValueLabel, BorderLayout.EAST);
		lrRow.add(lrSouth, BorderLayout.CENTER);

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton trainBtn = new JButton("Ucz");
		trainBtn.addActionListener(e -> onTrain());
		JButton testBtn = new JButton("Testuj");
		testBtn.addActionListener(e -> onTest());
		JButton resetBtn = new JButton("Reset sieć");
		resetBtn.addActionListener(e -> onResetNetwork());
		actions.add(trainBtn);
		actions.add(testBtn);
		actions.add(resetBtn);

		JPanel letters = new JPanel(new FlowLayout(FlowLayout.LEFT));
		letters.add(new JLabel("Etykieta:"));
		letters.add(radioE);
		letters.add(radioF);
		letters.add(radioZ);

		JPanel appendRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
		JButton appendBtn = new JButton("Dopisz do ciągu uczącego");
		appendBtn.addActionListener(e -> onAppendTraining());
		appendRow.add(appendBtn);

		p.add(epochRow);
		p.add(lrRow);
		p.add(actions);
		p.add(letters);
		p.add(appendRow);

		return p;
	}

	private static JLabel boldValueLabel(String text) {
		JLabel l = new JLabel(text);
		l.setFont(l.getFont().deriveFont(Font.BOLD, l.getFont().getSize() + 1f));
		l.setBorder(BorderFactory.createEmptyBorder(0, 4, 0, 0));
		return l;
	}

	private static void styleSliderFont(JSlider s) {
		Font f = s.getFont().deriveFont(Font.PLAIN, Math.max(11f, s.getFont().getSize2D() - 0.5f));
		s.setFont(f);
		if (s.getLabelTable() != null) {
			s.getLabelTable().values().forEach(l -> {
				if (l instanceof JLabel) {
					((JLabel) l).setFont(f);
				}
			});
		}
	}

	private static String formatLearningRate(int sliderValue) {
		return String.format("%.2f", sliderValue / 100.0);
	}

	public double getLearningRate() {
		return learningRateSlider.getValue() / 100.0;
	}

	public int getEpochs() {
		return epochsSlider.getValue();
	}

	private void log(String msg) {
		String line = "[" + LocalTime.now().format(LOG_TIME) + "] " + msg + "\n";
		logArea.append(line);
		logArea.setCaretPosition(logArea.getDocument().getLength());
	}

	private void onGuess() {
		log("[Zgadnij] stub — sieć: " + (mlpNetwork != null ? "ok" : "null"));
	}

	private void onClear() {
		paintCanvas.clear();
		log("[Wyczyść] siatka wyczyszczona.");
	}

	private void onTrain() {
		log("[Ucz] stub — epoki=" + getEpochs() + ", lr=" + getLearningRate());
	}

	private void onTest() {
		log("[Testuj] stub");
	}

	private void onResetNetwork() {
		int[] layers = { 8, 5, 3 };
		mlpNetwork = new Siec(64, 3, layers);
		logArea.setText("");
		log("[Reset sieć] nowa Siec(64→8→5→3), log wyczyszczony (stub wykresów bez zmian).");
	}

	private void onAppendTraining() {
		char label = radioE.isSelected() ? 'E' : (radioF.isSelected() ? 'F' : 'Z');
		log("[Dopisz] stub — wybrana etykieta: " + label);
	}
}
