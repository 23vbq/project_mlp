import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public final class CsvDatasetIO {

	private CsvDatasetIO() {
	}

	public static final class Dataset {
		public final double[][] inputs;
		public final double[][] expected;
		public final char[] labels;
		public final int skippedRows;

		Dataset(double[][] inputs, double[][] expected, char[] labels, int skippedRows) {
			this.inputs = inputs;
			this.expected = expected;
			this.labels = labels;
			this.skippedRows = skippedRows;
		}

		public int size() {
			return inputs.length;
		}
	}

	public static Dataset readDataset(Path csvPath, Consumer<String> log) throws IOException {
		return readDatasetInternal(csvPath, log, true);
	}

	public static Dataset readTrainingDataset(Path csvPath, Consumer<String> log) throws IOException {
		return readDatasetInternal(csvPath, log, true);
	}

	public static Dataset readTestDataset(Path csvPath, Consumer<String> log) throws IOException {
		return readDatasetInternal(csvPath, log, true);
	}

	private static Dataset readDatasetInternal(Path csvPath, Consumer<String> log, boolean allowUnknownLabels)
			throws IOException {
		List<double[]> inputs = new ArrayList<>();
		List<double[]> expected = new ArrayList<>();
		List<Character> labels = new ArrayList<>();
		int skipped = 0;

		try (BufferedReader reader = Files.newBufferedReader(csvPath)) {
			String line;
			int lineNo = 0;
			while ((line = reader.readLine()) != null) {
				lineNo++;
				if (line.isBlank()) {
					continue;
				}

				String[] parts = line.split(",", -1);
				if (parts.length != 65) {
					skipped++;
					log.accept("[CSV] Odrzucono wiersz " + lineNo + ": oczekiwano 65 kolumn, jest " + parts.length);
					continue;
				}

				double[] in = new double[64];
				boolean valid = true;
				for (int i = 0; i < 64; i++) {
					String field = parts[i].trim();
					double value;
					try {
						value = Double.parseDouble(field);
					} catch (NumberFormatException ex) {
						valid = false;
						break;
					}

					if (value < 0.0 || value > 1.0) {
						valid = false;
						break;
					}
					in[i] = value;
				}

				if (!valid) {
					skipped++;
					log.accept("[CSV] Odrzucono wiersz " + lineNo + ": piksele musza byc w zakresie 0.0-1.0");
					continue;
				}

				String rawLabel = parts[64].trim();
				if (rawLabel.length() != 1) {
					skipped++;
					log.accept("[CSV] Odrzucono wiersz " + lineNo + ": etykieta musi byc E/F/Z");
					continue;
				}
				char label = Character.toUpperCase(rawLabel.charAt(0));
				double[] out = toOneHot(label);
				if (out == null) {
					if (allowUnknownLabels) {
						out = new double[] { 0.0, 0.0, 0.0 };
					} else {
						skipped++;
						log.accept("[CSV] Odrzucono wiersz " + lineNo + ": etykieta musi byc E/F/Z");
						continue;
					}
				}

				inputs.add(in);
				expected.add(out);
				labels.add(label);
			}
		}

		double[][] inputsArr = inputs.toArray(new double[0][]);
		double[][] expectedArr = expected.toArray(new double[0][]);
		char[] labelsArr = new char[labels.size()];
		for (int i = 0; i < labels.size(); i++) {
			labelsArr[i] = labels.get(i);
		}
		return new Dataset(inputsArr, expectedArr, labelsArr, skipped);
	}

	public static void appendSample(Path csvPath, double[] pixels, char label) throws IOException {
		if (pixels == null || pixels.length != 64) {
			throw new IllegalArgumentException("Oczekiwano 64 pikseli.");
		}
		if (toOneHot(Character.toUpperCase(label)) == null) {
			throw new IllegalArgumentException("Etykieta musi byc E/F/Z.");
		}

		StringBuilder sb = new StringBuilder(256);
		for (int i = 0; i < pixels.length; i++) {
			double value = clamp01(pixels[i]);
			if (i > 0) {
				sb.append(',');
			}
			sb.append(formatPixelValue(value));
		}
		sb.append(',').append(Character.toUpperCase(label));

		try (BufferedWriter writer = Files.newBufferedWriter(csvPath,
				StandardOpenOption.CREATE,
				StandardOpenOption.APPEND)) {
			writer.write(sb.toString());
			writer.newLine();
		}
	}

	private static double clamp01(double value) {
		if (value < 0.0) {
			return 0.0;
		}
		if (value > 1.0) {
			return 1.0;
		}
		return value;
	}

	private static String formatPixelValue(double value) {
		String formatted = String.format(Locale.US, "%.6f", value);
		int trimAt = formatted.length();
		while (trimAt > 0 && formatted.charAt(trimAt - 1) == '0') {
			trimAt--;
		}
		if (trimAt > 0 && formatted.charAt(trimAt - 1) == '.') {
			trimAt--;
		}
		if (trimAt == 0) {
			return "0";
		}
		return formatted.substring(0, trimAt);
	}

	private static double[] toOneHot(char label) {
		switch (label) {
		case 'E':
			return new double[] { 1.0, 0.0, 0.0 };
		case 'F':
			return new double[] { 0.0, 1.0, 0.0 };
		case 'Z':
			return new double[] { 0.0, 0.0, 1.0 };
		default:
			return null;
		}
	}
}
