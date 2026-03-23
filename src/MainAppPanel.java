import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.io.IOException;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Dictionary;
import java.util.Enumeration;
import java.util.List;

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
	private JButton guessBtn;
	private JButton clearBtn;
	private JButton trainBtn;
	private JButton testBtn;
	private JButton resetBtn;
	private JButton appendBtn;

	private final MetricsChartPanel trainingChartPanel;
	private final MetricsChartPanel testChartPanel;
	private final NetworkOutputsPanel networkOutputsPanel;
	private boolean trainingInProgress;

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

		trainingChartPanel = new MetricsChartPanel(MetricsChartPanel.ChartType.LINE, "Wykres MSE (uczenie)");
		testChartPanel = new MetricsChartPanel(MetricsChartPanel.ChartType.LINE, "Wykres accuracy (test)");

		JPanel chartsColumn = new JPanel(new GridLayout(2, 1, 0, 8));
		chartsColumn.add(trainingChartPanel);
		chartsColumn.add(testChartPanel);

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

		log("UI zaladowane. Aplikacja gotowa do pracy.");
	}

	private JPanel buildLeftTopPanel() {
		JPanel east = new JPanel();
		east.setLayout(new BoxLayout(east, BoxLayout.Y_AXIS));
		east.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 0));

		guessBtn = new JButton("Zgadnij");
		guessBtn.addActionListener(e -> onGuess());
		clearBtn = new JButton("Wyczyść");
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
		trainBtn = new JButton("Ucz");
		trainBtn.addActionListener(e -> onTrain());
		testBtn = new JButton("Testuj");
		testBtn.addActionListener(e -> onTest());
		resetBtn = new JButton("Reset sieć");
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
		appendBtn = new JButton("Dopisz do ciągu uczącego");
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
		double[] input = paintCanvas.getContent();
		double[] output = mlpNetwork.oblicz_wyjscie(input);
		int predictedIndex = predictIndex(output);

		if (predictedIndex < 0) {
			resultLabel.setText("Wynik: Nie rozpoznano");
			networkOutputsPanel.setReadout(output[0], output[1], output[2],
					NetworkOutputsPanel.DotMode.ALL_UNCERTAIN,
					NetworkOutputsPanel.DotMode.ALL_UNCERTAIN,
					NetworkOutputsPanel.DotMode.ALL_UNCERTAIN);
			log("[Zgadnij] [" + String.format("%.2f", output[0]) + ", " + String.format("%.2f", output[1])
					+ ", " + String.format("%.2f", output[2]) + "] -> Nie rozpoznano");
			return;
		}

		char predictedLetter = indexToLabel(predictedIndex);
		resultLabel.setText("Wynik: " + predictedLetter);
		networkOutputsPanel.setReadout(output[0], output[1], output[2],
				predictedIndex == 0 ? NetworkOutputsPanel.DotMode.WINNER : NetworkOutputsPanel.DotMode.OTHER,
				predictedIndex == 1 ? NetworkOutputsPanel.DotMode.WINNER : NetworkOutputsPanel.DotMode.OTHER,
				predictedIndex == 2 ? NetworkOutputsPanel.DotMode.WINNER : NetworkOutputsPanel.DotMode.OTHER);
		log("[Zgadnij] [" + String.format("%.2f", output[0]) + ", " + String.format("%.2f", output[1])
				+ ", " + String.format("%.2f", output[2]) + "] -> " + predictedLetter);
	}

	private void onClear() {
		paintCanvas.clear();
		resultLabel.setText("Wynik: —");
		networkOutputsPanel.setIdle();
		log("[Wyczyść] siatka wyczyszczona.");
	}

	private void onTrain() {
		if (trainingInProgress) {
			return;
		}

		final int epochs = getEpochs();
		final double learningRate = getLearningRate();
		setTrainingControlsEnabled(false);
		log("[Ucz] Start uczenia w tle: epoki=" + epochs + ", lr=" + learningRate);

		SwingWorker<TrainingSummary, String> worker = new SwingWorker<TrainingSummary, String>() {
			@Override
			protected TrainingSummary doInBackground() throws Exception {
				Path path = Paths.get("dane_uczace.csv");
				CsvDatasetIO.Dataset dataset = CsvDatasetIO.readTrainingDataset(path, this::publish);
				if (dataset.size() == 0) {
					throw new IllegalStateException("Nie znaleziono poprawnych rekordow w pliku: dane_uczace.csv");
				}

				publish("[Ucz] Wczytano " + dataset.size() + " rekordow (odrzucone: " + dataset.skippedRows + ").");
				long start = System.nanoTime();
				double lastMse = 0.0;
				int logEvery = Math.max(1, epochs / 10);

				for (int epoch = 1; epoch <= epochs; epoch++) {
					lastMse = mlpNetwork.trainEpoch(dataset.inputs, dataset.expected, learningRate);
					publish("__MSE__" + lastMse);
					if (epoch == 1 || epoch == epochs || epoch % logEvery == 0) {
						publish("[Ucz] Epoka " + epoch + "/" + epochs + ", MSE=" + String.format("%.6f", lastMse));
					}
				}

				long elapsedMs = (System.nanoTime() - start) / 1_000_000L;
				return new TrainingSummary(dataset.size(), dataset.skippedRows, epochs, learningRate, lastMse, elapsedMs);
			}

			@Override
			protected void process(List<String> chunks) {
				for (String line : chunks) {
					if (line.startsWith("__MSE__")) {
						try {
							double mse = Double.parseDouble(line.substring("__MSE__".length()));
							trainingChartPanel.addLinePoint(mse);
						} catch (NumberFormatException ignored) {
						}
					} else {
						log(line);
					}
				}
			}

			@Override
			protected void done() {
				setTrainingControlsEnabled(true);
				try {
					TrainingSummary summary = get();
					log("[Ucz] Koniec uczenia, MSE koncowe=" + String.format("%.6f", summary.lastMse) + ", czas="
							+ summary.elapsedMs + " ms");
					JOptionPane.showMessageDialog(MainAppPanel.this,
							"Uczenie zakonczone.\n"
									+ "Probki: " + summary.sampleCount + "\n"
									+ "Odrzucone wiersze: " + summary.skippedRows + "\n"
									+ "Epoki: " + summary.epochs + "\n"
									+ "Learning rate: " + String.format("%.2f", summary.learningRate) + "\n"
									+ "MSE koncowe: " + String.format("%.6f", summary.lastMse) + "\n"
									+ "Czas: " + summary.elapsedMs + " ms",
							"Uczenie zakonczone",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
					if (cause instanceof NoSuchFileException) {
						log("[Ucz] Nie znaleziono pliku: dane_uczace.csv");
						JOptionPane.showMessageDialog(MainAppPanel.this,
								"Nie znaleziono pliku dane_uczace.csv\nKatalog roboczy: " + Paths.get("").toAbsolutePath(),
								"Blad pliku",
								JOptionPane.ERROR_MESSAGE);
					} else if (cause instanceof IllegalStateException) {
						log("[Ucz] " + cause.getMessage());
						JOptionPane.showMessageDialog(MainAppPanel.this,
								cause.getMessage(),
								"Brak danych",
								JOptionPane.WARNING_MESSAGE);
					} else if (cause instanceof IOException) {
						log("[Ucz] Blad IO: " + cause.getMessage());
						JOptionPane.showMessageDialog(MainAppPanel.this,
								"Nie udalo sie odczytac pliku dane_uczace.csv\n" + cause.getMessage(),
								"Blad IO",
								JOptionPane.ERROR_MESSAGE);
					} else {
						log("[Ucz] Blad: " + cause.getMessage());
						JOptionPane.showMessageDialog(MainAppPanel.this,
								"Uczenie przerwane: " + cause.getMessage(),
								"Blad",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		worker.execute();
	}

	private void onTest() {
		if (trainingInProgress) {
			return;
		}

		setTrainingControlsEnabled(false);
		testChartPanel.clearLine();
		log("[Testuj] Start testowania w tle.");

		SwingWorker<TestSummary, String> worker = new SwingWorker<TestSummary, String>() {
			@Override
			protected TestSummary doInBackground() throws Exception {
				Path path = Paths.get("dane_testowe.csv");
				CsvDatasetIO.Dataset dataset = CsvDatasetIO.readTestDataset(path, this::publish);
				if (dataset.size() == 0) {
					throw new IllegalStateException("Nie znaleziono poprawnych rekordow w pliku: dane_testowe.csv");
				}

				publish("[Testuj] Wczytano " + dataset.size() + " rekordow (odrzucone: " + dataset.skippedRows + ").");

				int[] totalByClass = new int[3];
				int[] correctByClass = new int[3];
				int totalCorrect = 0;
				int unknownTotal = 0;
				int unknownRejected = 0;
				int unknownGuessed = 0;
				int knownSeen = 0;
				int chartEvery = Math.max(1, dataset.size() / 160);

				for (int i = 0; i < dataset.size(); i++) {
					int expectedIndex = labelToIndex(dataset.labels[i]);
					double[] output = mlpNetwork.oblicz_wyjscie(dataset.inputs[i]);
					int predictedIndex = predictIndex(output);

					if (expectedIndex < 0) {
						unknownTotal++;
						if (predictedIndex < 0) {
							unknownRejected++;
						} else {
							unknownGuessed++;
						}
						continue;
					}
					totalByClass[expectedIndex]++;
					knownSeen++;
					if (predictedIndex == expectedIndex) {
						correctByClass[expectedIndex]++;
						totalCorrect++;
					}

					if ((knownSeen % chartEvery == 0) || i == dataset.size() - 1) {
						double runningAcc = percentage(totalCorrect, knownSeen);
						publish("__TEST_ACC__" + runningAcc);
					}
				}

				return new TestSummary(dataset.size(), dataset.skippedRows, totalByClass, correctByClass, totalCorrect,
						unknownTotal, unknownRejected, unknownGuessed);
			}

			@Override
			protected void process(List<String> chunks) {
				for (String line : chunks) {
					if (line.startsWith("__TEST_ACC__")) {
						try {
							double acc = Double.parseDouble(line.substring("__TEST_ACC__".length()));
							testChartPanel.addLinePoint(acc);
						} catch (NumberFormatException ignored) {
						}
					} else {
						log(line);
					}
				}
			}

			@Override
			protected void done() {
				setTrainingControlsEnabled(true);
				try {
					TestSummary summary = get();
					int knownTotal = summary.totalByClass[0] + summary.totalByClass[1] + summary.totalByClass[2];
					double eAcc = percentage(summary.correctByClass[0], summary.totalByClass[0]);
					double fAcc = percentage(summary.correctByClass[1], summary.totalByClass[1]);
					double zAcc = percentage(summary.correctByClass[2], summary.totalByClass[2]);
					double totalAcc = percentage(summary.totalCorrect, knownTotal);
					double unknownRejectAcc = percentage(summary.unknownRejected, summary.unknownTotal);

					log("[Testuj] E=" + String.format("%.2f", eAcc) + "%, F=" + String.format("%.2f", fAcc)
							+ "%, Z=" + String.format("%.2f", zAcc) + "%, TOTAL="
							+ String.format("%.2f", totalAcc) + "%, UNKNOWN_REJECT="
							+ String.format("%.2f", unknownRejectAcc) + "% (" + summary.unknownRejected + "/"
							+ summary.unknownTotal + "), UNKNOWN_GUESSED=" + summary.unknownGuessed);

					JOptionPane.showMessageDialog(MainAppPanel.this,
							"Test zakonczony.\n"
									+ "Probki: " + summary.sampleCount + "\n"
									+ "Odrzucone wiersze: " + summary.skippedRows + "\n"
									+ "Znane probki E/F/Z: " + knownTotal + "\n"
									+ "E: " + String.format("%.2f", eAcc) + "%\n"
									+ "F: " + String.format("%.2f", fAcc) + "%\n"
									+ "Z: " + String.format("%.2f", zAcc) + "%\n"
									+ "TOTAL(E/F/Z): " + String.format("%.2f", totalAcc) + "%\n"
									+ "Inne literki: " + summary.unknownTotal + "\n"
									+ "Inne odrzucone poprawnie: " + summary.unknownRejected + " ("
									+ String.format("%.2f", unknownRejectAcc) + "%)\n"
									+ "Inne blednie zgadniete: " + summary.unknownGuessed,
							"Wynik testowania",
							JOptionPane.INFORMATION_MESSAGE);
				} catch (Exception ex) {
					Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
					if (cause instanceof NoSuchFileException) {
						log("[Testuj] Nie znaleziono pliku: dane_testowe.csv");
						JOptionPane.showMessageDialog(MainAppPanel.this,
								"Nie znaleziono pliku dane_testowe.csv\nKatalog roboczy: " + Paths.get("").toAbsolutePath(),
								"Blad pliku",
								JOptionPane.ERROR_MESSAGE);
					} else if (cause instanceof IllegalStateException) {
						log("[Testuj] " + cause.getMessage());
						JOptionPane.showMessageDialog(MainAppPanel.this,
								cause.getMessage(),
								"Brak danych",
								JOptionPane.WARNING_MESSAGE);
					} else if (cause instanceof IOException) {
						log("[Testuj] Blad IO: " + cause.getMessage());
						JOptionPane.showMessageDialog(MainAppPanel.this,
								"Nie udalo sie odczytac pliku dane_testowe.csv\n" + cause.getMessage(),
								"Blad IO",
								JOptionPane.ERROR_MESSAGE);
					} else {
						log("[Testuj] Blad: " + cause.getMessage());
						JOptionPane.showMessageDialog(MainAppPanel.this,
								"Testowanie przerwane: " + cause.getMessage(),
								"Blad",
								JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
		worker.execute();
	}

	private void onResetNetwork() {
		int[] layers = { 8, 5, 3 };
		mlpNetwork = new Siec(64, 3, layers);
		logArea.setText("");
		networkOutputsPanel.setIdle();
		trainingChartPanel.clearLine();
		testChartPanel.clearLine();
		log("[Reset sieć] nowa Siec(64→8→5→3), wyczyszczono log i wykresy.");
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
		} catch (NoSuchFileException ex) {
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

	private static int labelToIndex(char label) {
		switch (Character.toUpperCase(label)) {
		case 'E':
			return 0;
		case 'F':
			return 1;
		case 'Z':
			return 2;
		default:
			return -1;
		}
	}

	private static char indexToLabel(int index) {
		switch (index) {
		case 0:
			return 'E';
		case 1:
			return 'F';
		case 2:
			return 'Z';
		default:
			return '?';
		}
	}

	private static int predictIndex(double[] networkOutput) {
		if (networkOutput == null || networkOutput.length < 3) {
			return -1;
		}

		int aboveThresholdCount = 0;
		int thresholdIndex = -1;
		for (int i = 0; i < 3; i++) {
			if (networkOutput[i] > 0.5) {
				aboveThresholdCount++;
				thresholdIndex = i;
			}
		}

		if (aboveThresholdCount == 0) {
			return -1;
		}
		if (aboveThresholdCount == 1) {
			return thresholdIndex;
		}

		int argmax = 0;
		double maxVal = networkOutput[0];
		for (int i = 1; i < 3; i++) {
			if (networkOutput[i] > maxVal) {
				maxVal = networkOutput[i];
				argmax = i;
			}
		}
		return argmax;
	}

	private static double percentage(int correct, int total) {
		if (total <= 0) {
			return 0.0;
		}
		return (100.0 * correct) / total;
	}

	private void setTrainingControlsEnabled(boolean enabled) {
		trainingInProgress = !enabled;
		trainBtn.setEnabled(enabled);
		testBtn.setEnabled(enabled);
		guessBtn.setEnabled(enabled);
		appendBtn.setEnabled(enabled);
		resetBtn.setEnabled(enabled);
	}

	private static class TrainingSummary {
		final int sampleCount;
		final int skippedRows;
		final int epochs;
		final double learningRate;
		final double lastMse;
		final long elapsedMs;

		TrainingSummary(int sampleCount, int skippedRows, int epochs, double learningRate, double lastMse,
				long elapsedMs) {
			this.sampleCount = sampleCount;
			this.skippedRows = skippedRows;
			this.epochs = epochs;
			this.learningRate = learningRate;
			this.lastMse = lastMse;
			this.elapsedMs = elapsedMs;
		}
	}

	private static class TestSummary {
		final int sampleCount;
		final int skippedRows;
		final int[] totalByClass;
		final int[] correctByClass;
		final int totalCorrect;
		final int unknownTotal;
		final int unknownRejected;
		final int unknownGuessed;

		TestSummary(int sampleCount, int skippedRows, int[] totalByClass, int[] correctByClass, int totalCorrect,
				int unknownTotal, int unknownRejected, int unknownGuessed) {
			this.sampleCount = sampleCount;
			this.skippedRows = skippedRows;
			this.totalByClass = totalByClass;
			this.correctByClass = correctByClass;
			this.totalCorrect = totalCorrect;
			this.unknownTotal = unknownTotal;
			this.unknownRejected = unknownRejected;
			this.unknownGuessed = unknownGuessed;
		}
	}
}
