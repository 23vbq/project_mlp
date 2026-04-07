# MLP Letter Recognition — E, F, Z

A desktop application for recognizing handwritten letters **E**, **F**, and **Z** using a from-scratch Multi-Layer Perceptron neural network. Built in Java (Swing) with a Python data generation pipeline.

![screenshot](https://github.com/user-attachments/assets/b0d422bf-f21c-4f24-951b-b57452125940)

---

## Overview

The network takes an **8×8 pixel canvas** as input (64 float values in range `[0.0, 1.0]`) and classifies it into one of three classes, or rejects it as unrecognized. Training and testing run on CSV datasets; the user can also draw directly in the app and get a live prediction.

**Architecture:** `64 → 8 → 5 → 3`
- Two hidden layers with sigmoid activation
- Output layer uses one-hot encoding: `[1,0,0]` = E, `[0,1,0]` = F, `[0,0,1]` = Z
- Weights initialized in range `[-0.01, +0.01]`

---

## Features

- 🖊️ **Draw & predict** — paint a letter on the interactive canvas, click *Zgadnij* to classify
- 🧠 **Train** — load `dane_uczace.csv`, configure epochs (100–10 000) and learning rate (0.01–1.00), train in background
- 📊 **Live charts** — MSE loss curve updates in real time during training; accuracy chart after testing
- 🧪 **Test** — evaluate on `dane_testowe.csv` with per-class and overall accuracy breakdown
- ➕ **Extend dataset** — draw a sample, label it E/F/Z, append directly to the training CSV
- 🔄 **Reset** — reinitialize network weights without restarting the app

---

## Project Structure

```
├── Neuron.java                # Single neuron (sigmoid activation, weights)
├── Warstwa.java               # Layer of neurons
├── Siec.java                  # MLP network — forward pass + backpropagation
├── Main.java                  # Entry point, window setup
├── MainAppPanel.java          # Main UI panel, training/testing logic (SwingWorker)
├── PaintCanvasComponent.java  # Interactive 8×8 drawing canvas (512×512 internal res)
├── CsvDatasetIO.java          # CSV dataset reader/writer
├── MetricsChartPanel.java     # MSE and accuracy line charts
├── NetworkOutputsPanel.java   # Network output indicators (E / F / Z)
├── ModernRectSliderUI.java    # Custom Swing slider UI
└── generate_data.py           # Synthetic dataset generator
```

---

## Getting Started

### Requirements

- Java 11+
- Python 3.8+ (for dataset generation only)

### 1. Generate datasets

```bash
python generate_data.py
```

This produces `dane_uczace.csv` (training) and `dane_testowe.csv` (test) in the working directory.

**Default sizes:**
| Set | Known samples (E/F/Z) | Unknown samples | Total |
|-----|-----------------------|-----------------|-------|
| Training | 900 (300 × 3) | ~300 | ~1 200 |
| Test | 270 (90 × 3) | ~90 | ~360 |

Optional flags:
```
--train-per-class   INT     Samples per class for training (default: 300)
--test-per-class    INT     Samples per class for testing (default: 90)
--all-letters-ratio FLOAT   Fraction of unknown-class samples (default: 0.25)
--seed              INT     Random seed for reproducibility (default: 42)
--hard-test                 Enable noisier, more shifted test samples
```

### 2. Compile & run

```bash
javac *.java
java Main
```

Place `dane_uczace.csv` and `dane_testowe.csv` in the **same directory** as the compiled classes.

---

## Training & Backpropagation

The network is trained using **stochastic gradient descent** with backpropagation. Sample order is shuffled each epoch.

**Output delta:**
```
δᵢ = (yᵢ − oᵢ) · oᵢ · (1 − oᵢ)
```

**Hidden layer delta:**
```
δⱼ = oⱼ · (1 − oⱼ) · Σₖ (δₖ · wₖⱼ)
```

**Weight update:**
```
wᵢⱼ ← wᵢⱼ + η · δᵢ · xⱼ
```

Loss function is **MSE** averaged per epoch:
```
MSE = (1/n) · Σᵢ (yᵢ − oᵢ)²
```

---

## Results

Typical results after **1 000 epochs**, learning rate **0.10**:

| Metric | Value |
|--------|-------|
| Final MSE (training) | 0.003200 |
| Accuracy — E | 73.33% |
| Accuracy — F | 76.67% |
| Accuracy — Z | 93.33% |
| Accuracy — Total (E/F/Z) | 81.11% |
| Unknown correctly rejected | 75.56% |
| Training time | ~1 300 ms |

Z achieves the highest accuracy due to its geometrically distinct shape. E and F are occasionally confused — they differ only by the bottom horizontal bar, which is hard to distinguish at 8×8 resolution with noise.

---

## CSV Format

Each row: 64 pixel values + label

```
0.0000,0.9231,0.8812,...,0.0000,E
0.0000,0.0000,0.7654,...,0.0000,Z
```

- Pixel values must be in `[0.0, 1.0]`
- Label must be `E`, `F`, or `Z` (unknown-class rows use other letters and are treated as `[0,0,0]` targets)
- Rows not matching this format are skipped with a log warning


