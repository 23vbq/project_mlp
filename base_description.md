# MLP Project — Base Description

## Overview

This is a **from-scratch Java implementation of a Multi-Layer Perceptron (MLP)** — a classic feedforward artificial neural network. The project includes a **2D decision-boundary visualizer** built with Java Swing that renders how a randomly initialized network partitions the input space.

There is **no training (backpropagation) implemented yet** — the network is initialized with random weights and immediately visualized, so each run produces a different, untrained decision boundary.

## Architecture

The codebase follows a clean bottom-up decomposition:

```
Neuron  →  Warstwa (Layer)  →  Siec (Network)  →  Test (Visualization)
```

### `Neuron.java` — Single Neuron

- Holds an array of **weights** (`wagi`), where `wagi[0]` is the **bias** and `wagi[1..n]` correspond to each input.
- Weights are randomly initialized in the range `[-10, 10]` (large range chosen for visual effect; a smaller `[-0.01, 0.01]` range is commented out for actual training use).
- **Activation function**: unipolar sigmoid `1 / (1 + exp(-x))`. Alternatives (step function, linear) are present as comments.
- `oblicz_wyjscie(inputs)` computes the weighted sum of inputs + bias, then applies the activation function.

### `Warstwa.java` — Layer

- Contains an array of `Neuron` objects.
- Constructor takes the number of inputs (fan-in) and the number of neurons in the layer; each neuron is created with the given fan-in.
- `oblicz_wyjscie(inputs)` performs a **forward pass** through all neurons in the layer and returns an output vector.

### `Siec.java` — Network

- Contains an array of `Warstwa` (Layer) objects.
- Constructor: `Siec(numInputs, numLayers, neuronsPerLayer[])` — builds the full topology. The first layer's fan-in equals `numInputs`; subsequent layers' fan-in equals the previous layer's neuron count.
- `oblicz_wyjscie(inputs)` chains the layers: output of layer *i* becomes input to layer *i+1*. Returns the final layer's output.

### `Test.java` — Swing Visualization

- Opens a window centered on screen (half screen width/height).
- For every pixel `(x, y)`, maps coordinates to the `[-1, 1]` range and feeds them as a 2-element input vector into the network.
- Colors the pixel **red** if the network output > 0.5, **green** otherwise (a binary decision boundary).
- Default configuration: **3 layers** with **25 → 5 → 1** neurons (2 inputs). Commented-out alternatives include a single-layer (1 neuron) and a two-layer (10 → 1) setup.

## Purpose

The project serves as an **educational tool / lab exercise** for understanding:

1. How an MLP is structured (neurons, layers, network).
2. How a forward pass propagates signals through the network.
3. How network **topology** (depth and width) affects the complexity of the decision boundary — even without training, deeper/wider networks carve the 2D plane into more intricate regions.
4. The role of the **activation function** (sigmoid vs. step vs. linear) in shaping the output.

## What's Missing (Potential Next Steps)

| Feature | Status |
|---|---|
| Forward pass | Done |
| Backpropagation / training | Not implemented |
| Loss function | Not implemented |
| Training dataset | Not present |
| Configurable activation functions | Partially (commented-out alternatives) |

The natural next step would be to implement **backpropagation** and train the network on a dataset (e.g., XOR, spiral, or another 2D classification problem), then visualize the learned decision boundary.
