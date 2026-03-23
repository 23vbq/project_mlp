import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Dictionary;
import java.util.Enumeration;

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
	private final NetworkOutputsPanel networkOutputsPanel;

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
		logArea.setFont(new Font(Font.SANS_SERIF, Font.PLAIN, 13));
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

		networkOutputsPanel = new NetworkOutputsPanel();
		networkOutputsPanel.setPreferredSize(new Dimension(10, 64));

		trainingChartPlaceholder = new JPanel(new BorderLayout());
		trainingChartPlaceholder.setBorder(BorderFactory.createTitledBorder("Wykres MSE (uczenie)"));
		trainingChartPlaceholder.add(new JLabel(" ", SwingConstants.CENTER), BorderLayout.CENTER);
		trainingChartPlaceholder.setPreferredSize(new Dimension(240, 180));

		testChartPlaceholder = new JPanel(new BorderLayout());
		testChartPlaceholder.setBorder(BorderFactory.createTitledBorder("Wykres accuracy (test)"));
		testChartPlaceholder.add(new JLabel(" ", SwingConstants.CENTER), BorderLayout.CENTER);
		testChartPlaceholder.setPreferredSize(new Dimension(240, 180));

		JPanel chartsColumn = new JPanel(new GridLayout(2, 1, 0, 8));
		chartsColumn.add(trainingChartPlaceholder);
		chartsColumn.add(testChartPlaceholder);

		JPanel rightColumn = new JPanel(new BorderLayout(0, 8));
		rightColumn.add(logScroll, BorderLayout.NORTH);
		JPanel rightCenter = new JPanel(new BorderLayout(0, 8));
		rightCenter.add(networkOutputsPanel, BorderLayout.NORTH);
		rightCenter.add(chartsColumn, BorderLayout.CENTER);
		rightColumn.add(rightCenter, BorderLayout.CENTER);

		JPanel leftTop = buildLeftTopPanel();
		JPanel leftBottom = buildLeftBottomPanel();

		JPanel leftColumn = new JPanel(new BorderLayout(0, 8));
		leftColumn.add(leftTop, BorderLayout.CENTER);
		leftColumn.add(leftBottom, BorderLayout.SOUTH);

		JPanel leftWrapper = new JPanel(new BorderLayout());
		leftWrapper.setBorder(BorderFactory.createTitledBorder("Siatka i uczenie"));
		leftWrapper.add(leftColumn, BorderLayout.CENTER);

		JPanel rightWrapper = new JPanel(new BorderLayout());
		rightWrapper.setBorder(BorderFactory.createTitledBorder("Log, wyjścia i wykresy"));
		rightWrapper.add(rightColumn, BorderLayout.CENTER);

		// GridLayout: dwie kolumny zawsze dokładnie po 50% — brak uchwytu do skalowania lewo/prawo
		JPanel mainColumns = new JPanel(new GridLayout(1, 2, 12, 0));
		mainColumns.add(leftWrapper);
		mainColumns.add(rightWrapper);

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

		JPanel actions = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		JButton trainBtn = new JButton("Ucz");
		trainBtn.addActionListener(e -> onTrain());
		JButton testBtn = new JButton("Testuj");
		testBtn.addActionListener(e -> onTest());
		JButton resetBtn = new JButton("Reset sieć");
		resetBtn.addActionListener(e -> onResetNetwork());
		actions.add(trainBtn);
		actions.add(testBtn);
		actions.add(resetBtn);
		JPanel actionsGroup = new JPanel(new BorderLayout());
		actionsGroup.setBorder(BorderFactory.createTitledBorder("Akcje sieci"));
		actionsGroup.add(actions, BorderLayout.CENTER);

		JPanel letters = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		letters.add(new JLabel("Etykieta:"));
		letters.add(radioE);
		letters.add(radioF);
		letters.add(radioZ);

		JPanel appendRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 4));
		JButton appendBtn = new JButton("Dopisz do ciągu uczącego");
		appendBtn.addActionListener(e -> onAppendTraining());
		appendRow.add(appendBtn);
		JPanel dataGroup = new JPanel(new BorderLayout(0, 4));
		dataGroup.setBorder(BorderFactory.createTitledBorder("Dane uczące"));
		dataGroup.add(letters, BorderLayout.NORTH);
		dataGroup.add(appendRow, BorderLayout.CENTER);

		p.add(epochRow);
		p.add(lrRow);
		p.add(actionsGroup);
		p.add(dataGroup);

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
		Dictionary<?, ?> labelTable = s.getLabelTable();
		if (labelTable != null) {
			Enumeration<?> labels = labelTable.elements();
			while (labels.hasMoreElements()) {
				Object label = labels.nextElement();
				if (label instanceof JLabel) {
					((JLabel) label).setFont(f);
				}
			}
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
		// Tymczasowy podgląd UI wyjść (bez prawdziwej inferencji)
		networkOutputsPanel.setReadout(0.62, 0.21, 0.19,
				NetworkOutputsPanel.DotMode.WINNER,
				NetworkOutputsPanel.DotMode.OTHER,
				NetworkOutputsPanel.DotMode.OTHER);
	}

	private void onClear() {
		paintCanvas.clear();
		networkOutputsPanel.setIdle();
		log("[Wyczyść] siatka wyczyszczona.");
	}

	private void onTrain() {
		readAndShowDataset("dane_uczace.csv", "Ucz");
	}

	private void onTest() {
		readAndShowDataset("dane_testowe.csv", "Czytaj");
	}

	private void onResetNetwork() {
		int[] layers = { 8, 5, 3 };
		mlpNetwork = new Siec(64, 3, layers);
		logArea.setText("");
		networkOutputsPanel.setIdle();
		log("[Reset sieć] nowa Siec(64→8→5→3), log wyczyszczony (stub wykresów bez zmian).");
	}

	private void onAppendTraining() {
		char label = radioE.isSelected() ? 'E' : (radioF.isSelected() ? 'F' : 'Z');
		double[] pixels = paintCanvas.getContent();
		Path path = Paths.get("dane_uczace.csv");
		try {
			CsvDatasetIO.appendSample(path, pixels, label);
			log("[Dopisz] Dodano rekord do dane_uczace.csv z etykieta " + label + ".");
			JOptionPane.showMessageDialog(this,
					"Dodano rekord do dane_uczace.csv\nEtykieta: " + label,
					"Dopisano",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (IllegalArgumentException ex) {
			log("[Dopisz] Blad danych: " + ex.getMessage());
			JOptionPane.showMessageDialog(this,
					ex.getMessage(),
					"Blad danych",
					JOptionPane.WARNING_MESSAGE);
		} catch (IOException ex) {
			log("[Dopisz] Blad zapisu: " + ex.getMessage());
			JOptionPane.showMessageDialog(this,
					"Nie udalo sie dopisac do dane_uczace.csv\n" + ex.getMessage(),
					"Blad zapisu",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private void readAndShowDataset(String fileName, String actionName) {
		Path path = Paths.get(fileName);
		try {
			CsvDatasetIO.Dataset dataset = CsvDatasetIO.readDataset(path, this::log);
			if (dataset.size() == 0) {
				log("[" + actionName + "] Brak poprawnych rekordow w pliku " + fileName + ".");
				JOptionPane.showMessageDialog(this,
						"Nie znaleziono poprawnych rekordow w pliku: " + fileName,
						"Brak danych",
						JOptionPane.WARNING_MESSAGE);
				return;
			}

			log("[" + actionName + "] Wczytano " + dataset.size() + " rekordow (odrzucone: " + dataset.skippedRows
					+ ") z pliku " + fileName + ".");
			JOptionPane.showMessageDialog(this,
					buildDatasetPreview(fileName, dataset, 4),
					actionName + " - podglad danych",
					JOptionPane.INFORMATION_MESSAGE);
		} catch (FileNotFoundException ex) {
			log("[" + actionName + "] Nie znaleziono pliku: " + fileName);
			JOptionPane.showMessageDialog(this,
					"Nie znaleziono pliku " + fileName + "\nKatalog roboczy: " + Paths.get("").toAbsolutePath(),
					"Blad pliku",
					JOptionPane.ERROR_MESSAGE);
		} catch (IOException ex) {
			log("[" + actionName + "] Blad IO: " + ex.getMessage());
			JOptionPane.showMessageDialog(this,
					"Nie udalo sie odczytac pliku " + fileName + "\n" + ex.getMessage(),
					"Blad IO",
					JOptionPane.ERROR_MESSAGE);
		}
	}

	private static String buildDatasetPreview(String fileName, CsvDatasetIO.Dataset dataset, int maxRows) {
		StringBuilder sb = new StringBuilder();
		sb.append("Plik: ").append(fileName).append('\n');
		sb.append("Rekordy poprawne: ").append(dataset.size()).append('\n');
		sb.append("Rekordy odrzucone: ").append(dataset.skippedRows).append("\n\n");

		int shown = Math.min(maxRows, dataset.size());
		for (int i = 0; i < shown; i++) {
			double[] in = dataset.inputs[i];
			double[] out = dataset.expected[i];
			sb.append("#").append(i + 1).append(" label=").append(dataset.labels[i]);
			sb.append(" in[0..7]=[");
			for (int j = 0; j < 8; j++) {
				if (j > 0) {
					sb.append(',');
				}
				sb.append((int) in[j]);
			}
			sb.append("] oneHot=[");
			sb.append((int) out[0]).append(',').append((int) out[1]).append(',').append((int) out[2]).append(']');
			sb.append('\n');
		}

		if (dataset.size() > shown) {
			sb.append("... (+").append(dataset.size() - shown).append(" kolejnych)");
		}
		return sb.toString();
	}
}
