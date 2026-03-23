#!/usr/bin/env python3
"""
Generate synthetic 8x8 CSV datasets for letters E, F, Z.

Output format per row:
  64 binary values in row-major order + label (E/F/Z)

Default outputs:
  - dane_uczace.csv
  - dane_testowe.csv
"""

from __future__ import annotations

import argparse
import csv
import random
from pathlib import Path
from typing import Iterable, List, Sequence, Tuple

Grid = List[List[int]]


def blank_grid() -> Grid:
    return [[0 for _ in range(8)] for _ in range(8)]


def base_pattern(label: str) -> Grid:
    g = blank_grid()

    if label == "E":
        for r in range(8):
            g[r][0] = 1
        for c in range(7):
            g[0][c] = 1
            g[3][c] = 1
            g[7][c] = 1

    elif label == "F":
        for r in range(8):
            g[r][0] = 1
        for c in range(7):
            g[0][c] = 1
            g[3][c] = 1

    elif label == "Z":
        for c in range(8):
            g[0][c] = 1
            g[7][c] = 1
        for r in range(1, 7):
            c = 7 - r
            g[r][c] = 1

    else:
        raise ValueError(f"Unsupported label: {label}")

    return g


def shift_grid(grid: Grid, dr: int, dc: int) -> Grid:
    out = blank_grid()
    for r in range(8):
        for c in range(8):
            if grid[r][c] == 0:
                continue
            rr = r + dr
            cc = c + dc
            if 0 <= rr < 8 and 0 <= cc < 8:
                out[rr][cc] = 1
    return out


def thicken(grid: Grid, rng: random.Random, prob: float) -> Grid:
    out = [row[:] for row in grid]
    for r in range(8):
        for c in range(8):
            if grid[r][c] == 1:
                for rr, cc in ((r - 1, c), (r + 1, c), (r, c - 1), (r, c + 1)):
                    if 0 <= rr < 8 and 0 <= cc < 8 and rng.random() < prob:
                        out[rr][cc] = 1
    return out


def dropout(grid: Grid, rng: random.Random, prob: float) -> Grid:
    out = [row[:] for row in grid]
    for r in range(8):
        for c in range(8):
            if out[r][c] == 1 and rng.random() < prob:
                out[r][c] = 0
    return out


def flip_noise(grid: Grid, rng: random.Random, prob: float) -> Grid:
    out = [row[:] for row in grid]
    for r in range(8):
        for c in range(8):
            if rng.random() < prob:
                out[r][c] = 1 - out[r][c]
    return out


def to_row_major(grid: Grid) -> List[int]:
    return [grid[r][c] for r in range(8) for c in range(8)]


def synthesize(label: str, rng: random.Random, *, hard: bool) -> List[int]:
    g = base_pattern(label)

    max_shift = 1 if not hard else 2
    dr = rng.randint(-max_shift, max_shift)
    dc = rng.randint(-max_shift, max_shift)
    g = shift_grid(g, dr, dc)

    # Slight morphology for diversity.
    if rng.random() < (0.30 if not hard else 0.45):
        g = thicken(g, rng, prob=0.18 if not hard else 0.24)
    if rng.random() < (0.18 if not hard else 0.30):
        g = dropout(g, rng, prob=0.08 if not hard else 0.13)

    # Pixel flips simulate imperfect drawing.
    g = flip_noise(g, rng, prob=0.015 if not hard else 0.03)

    return to_row_major(g)


def generate_rows(
    labels: Sequence[str],
    per_class: int,
    rng: random.Random,
    *,
    hard: bool,
) -> List[Tuple[List[int], str]]:
    rows: List[Tuple[List[int], str]] = []
    for label in labels:
        for _ in range(per_class):
            rows.append((synthesize(label, rng, hard=hard), label))
    rng.shuffle(rows)
    return rows


def write_csv(path: Path, rows: Iterable[Tuple[List[int], str]]) -> None:
    path.parent.mkdir(parents=True, exist_ok=True)
    with path.open("w", newline="", encoding="utf-8") as f:
        w = csv.writer(f)
        for pixels, label in rows:
            w.writerow(pixels + [label])


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Generate EFZ CSV datasets for 8x8 MLP training.")
    parser.add_argument("--train-out", default="dane_uczace.csv", help="Output path for training CSV.")
    parser.add_argument("--test-out", default="dane_testowe.csv", help="Output path for testing CSV.")
    parser.add_argument("--train-per-class", type=int, default=300, help="Samples per class for training set.")
    parser.add_argument("--test-per-class", type=int, default=90, help="Samples per class for test set.")
    parser.add_argument("--seed", type=int, default=42, help="Random seed.")
    parser.add_argument(
        "--hard-test",
        action="store_true",
        help="Generate noisier/more shifted test samples.",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    rng = random.Random(args.seed)

    labels = ("E", "F", "Z")
    train_rows = generate_rows(labels, args.train_per_class, rng, hard=False)
    test_rows = generate_rows(labels, args.test_per_class, rng, hard=args.hard_test)

    train_path = Path(args.train_out)
    test_path = Path(args.test_out)

    write_csv(train_path, train_rows)
    write_csv(test_path, test_rows)

    print(f"Generated: {train_path} ({len(train_rows)} rows)")
    print(f"Generated: {test_path} ({len(test_rows)} rows)")


if __name__ == "__main__":
    main()
