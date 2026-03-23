#!/usr/bin/env python3
"""
Generate synthetic 8x8 CSV datasets for letters E, F, Z.

Output format per row:
    64 binary values in row-major order + label

Primary classes are E/F/Z.
A configurable portion of samples is added for all remaining letters
(A-Z excluding E/F/Z), distributed evenly, to represent unknown letters.

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


def draw_line(grid: Grid, r0: int, c0: int, r1: int, c1: int) -> None:
    steps = max(abs(r1 - r0), abs(c1 - c0))
    if steps == 0:
        grid[r0][c0] = 1
        return
    for i in range(steps + 1):
        t = i / steps
        rr = int(round(r0 + (r1 - r0) * t))
        cc = int(round(c0 + (c1 - c0) * t))
        if 0 <= rr < 8 and 0 <= cc < 8:
            grid[rr][cc] = 1


def stroke_letter(label: str) -> Grid:
    g = blank_grid()

    def hline(r: int, c0: int, c1: int) -> None:
        draw_line(g, r, c0, r, c1)

    def vline(c: int, r0: int, r1: int) -> None:
        draw_line(g, r0, c, r1, c)

    if label == "A":
        vline(1, 1, 7)
        vline(6, 1, 7)
        hline(1, 1, 6)
        hline(4, 1, 6)
    elif label == "B":
        vline(1, 0, 7)
        hline(0, 1, 5)
        hline(3, 1, 5)
        hline(7, 1, 5)
        vline(6, 1, 2)
        vline(6, 4, 6)
    elif label == "C":
        hline(0, 2, 6)
        hline(7, 2, 6)
        vline(1, 1, 6)
    elif label == "D":
        vline(1, 0, 7)
        hline(0, 1, 5)
        hline(7, 1, 5)
        vline(6, 1, 6)
    elif label == "G":
        hline(0, 2, 6)
        hline(7, 2, 6)
        vline(1, 1, 6)
        hline(4, 4, 6)
        vline(6, 4, 6)
    elif label == "H":
        vline(1, 0, 7)
        vline(6, 0, 7)
        hline(4, 1, 6)
    elif label == "I":
        hline(0, 1, 6)
        hline(7, 1, 6)
        vline(3, 1, 6)
    elif label == "J":
        hline(0, 1, 6)
        vline(5, 1, 6)
        hline(7, 2, 5)
        vline(2, 5, 6)
    elif label == "K":
        vline(1, 0, 7)
        draw_line(g, 4, 2, 0, 6)
        draw_line(g, 4, 2, 7, 6)
    elif label == "L":
        vline(1, 0, 7)
        hline(7, 1, 6)
    elif label == "M":
        vline(1, 0, 7)
        vline(6, 0, 7)
        draw_line(g, 0, 1, 3, 3)
        draw_line(g, 3, 3, 0, 6)
    elif label == "N":
        vline(1, 0, 7)
        vline(6, 0, 7)
        draw_line(g, 0, 1, 7, 6)
    elif label == "O":
        hline(0, 2, 5)
        hline(7, 2, 5)
        vline(1, 1, 6)
        vline(6, 1, 6)
    elif label == "P":
        vline(1, 0, 7)
        hline(0, 1, 6)
        hline(3, 1, 6)
        vline(6, 1, 3)
    elif label == "Q":
        hline(0, 2, 5)
        hline(7, 2, 5)
        vline(1, 1, 6)
        vline(6, 1, 6)
        draw_line(g, 5, 5, 7, 7)
    elif label == "R":
        vline(1, 0, 7)
        hline(0, 1, 6)
        hline(3, 1, 6)
        vline(6, 1, 3)
        draw_line(g, 3, 3, 7, 6)
    elif label == "S":
        hline(0, 2, 6)
        hline(3, 2, 6)
        hline(7, 2, 6)
        vline(1, 1, 3)
        vline(6, 4, 6)
    elif label == "T":
        hline(0, 1, 6)
        vline(3, 1, 7)
    elif label == "U":
        vline(1, 0, 6)
        vline(6, 0, 6)
        hline(7, 2, 5)
    elif label == "V":
        draw_line(g, 0, 1, 7, 3)
        draw_line(g, 0, 6, 7, 3)
    elif label == "W":
        vline(1, 0, 7)
        vline(6, 0, 7)
        draw_line(g, 7, 1, 4, 3)
        draw_line(g, 4, 3, 7, 6)
    elif label == "X":
        draw_line(g, 0, 1, 7, 6)
        draw_line(g, 0, 6, 7, 1)
    elif label == "Y":
        draw_line(g, 0, 1, 3, 3)
        draw_line(g, 0, 6, 3, 3)
        vline(3, 3, 7)
    else:
        # Fallback for unsupported labels: random multi-stroke shape.
        stroke_count = 4
        for _ in range(stroke_count):
            r0, c0 = random.randint(0, 7), random.randint(0, 7)
            r1, c1 = random.randint(0, 7), random.randint(0, 7)
            draw_line(g, r0, c0, r1, c1)

    return g


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


def synthesize_unknown(rng: random.Random, *, hard: bool) -> List[int]:
    g = blank_grid()

    stroke_count = rng.randint(3, 5 if hard else 4)
    for _ in range(stroke_count):
        r0, c0 = rng.randint(0, 7), rng.randint(0, 7)
        r1, c1 = rng.randint(0, 7), rng.randint(0, 7)
        draw_line(g, r0, c0, r1, c1)

    if rng.random() < 0.5:
        g = thicken(g, rng, prob=0.20 if not hard else 0.28)
    if rng.random() < 0.25:
        g = dropout(g, rng, prob=0.10 if not hard else 0.16)
    g = flip_noise(g, rng, prob=0.02 if not hard else 0.04)

    return to_row_major(g)


def synthesize_empty_unknown(rng: random.Random, *, hard: bool) -> List[int]:
    g = blank_grid()
    # Keep most empty samples truly empty, but allow tiny accidental noise.
    if rng.random() < (0.15 if not hard else 0.25):
        g = flip_noise(g, rng, prob=0.01 if not hard else 0.02)
    return to_row_major(g)


def synthesize_line_unknown(rng: random.Random, *, hard: bool) -> List[int]:
    g = blank_grid()
    stroke_count = rng.randint(1, 2 if not hard else 3)
    for _ in range(stroke_count):
        r0, c0 = rng.randint(0, 7), rng.randint(0, 7)
        r1, c1 = rng.randint(0, 7), rng.randint(0, 7)
        draw_line(g, r0, c0, r1, c1)

    if rng.random() < (0.30 if not hard else 0.45):
        g = thicken(g, rng, prob=0.10 if not hard else 0.16)
    if rng.random() < (0.12 if not hard else 0.20):
        g = dropout(g, rng, prob=0.10 if not hard else 0.16)
    g = flip_noise(g, rng, prob=0.01 if not hard else 0.02)
    return to_row_major(g)


def synthesize_obvious_confuser_unknown(rng: random.Random, *, hard: bool) -> List[int]:
    g = blank_grid()

    variant = rng.randint(0, 5)
    if variant == 0:
        # Left vertical line -> commonly confused with F backbone.
        col = rng.choice([0, 1])
        draw_line(g, 0, col, 7, col)
    elif variant == 1:
        # Top + bottom bars -> commonly confused with Z frame.
        top = rng.choice([0, 1])
        bottom = rng.choice([6, 7])
        draw_line(g, top, 0, top, 7)
        draw_line(g, bottom, 0, bottom, 7)
    elif variant == 2:
        # Top bar only.
        row = rng.choice([0, 1])
        draw_line(g, row, 0, row, 7)
    elif variant == 3:
        # Bottom bar only.
        row = rng.choice([6, 7])
        draw_line(g, row, 0, row, 7)
    elif variant == 4:
        # Left vertical + top bar (partial F-like).
        col = rng.choice([0, 1])
        row = rng.choice([0, 1])
        draw_line(g, 0, col, 7, col)
        draw_line(g, row, 0, row, 7)
    else:
        # Top + bottom + short diagonal fragment (partial Z-like).
        top = rng.choice([0, 1])
        bottom = rng.choice([6, 7])
        draw_line(g, top, 0, top, 7)
        draw_line(g, bottom, 0, bottom, 7)
        draw_line(g, 2, 5, 5, 2)

    if rng.random() < (0.28 if not hard else 0.42):
        g = thicken(g, rng, prob=0.10 if not hard else 0.16)
    if rng.random() < (0.16 if not hard else 0.24):
        g = dropout(g, rng, prob=0.10 if not hard else 0.16)
    g = flip_noise(g, rng, prob=0.01 if not hard else 0.02)
    return to_row_major(g)


def synthesize_noise_unknown(rng: random.Random, *, hard: bool) -> List[int]:
    g = blank_grid()
    on_prob = 0.07 if not hard else 0.11
    for r in range(8):
        for c in range(8):
            if rng.random() < on_prob:
                g[r][c] = 1

    if rng.random() < (0.25 if not hard else 0.40):
        g = thicken(g, rng, prob=0.12 if not hard else 0.18)
    if rng.random() < (0.20 if not hard else 0.30):
        g = dropout(g, rng, prob=0.14 if not hard else 0.22)
    g = flip_noise(g, rng, prob=0.01 if not hard else 0.02)
    return to_row_major(g)


def synthesize_unknown_letter(label: str, rng: random.Random, *, hard: bool) -> List[int]:
    g = stroke_letter(label)

    max_shift = 1 if not hard else 2
    dr = rng.randint(-max_shift, max_shift)
    dc = rng.randint(-max_shift, max_shift)
    g = shift_grid(g, dr, dc)

    if rng.random() < (0.35 if not hard else 0.50):
        g = thicken(g, rng, prob=0.16 if not hard else 0.24)
    if rng.random() < (0.20 if not hard else 0.32):
        g = dropout(g, rng, prob=0.08 if not hard else 0.14)
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


def add_unknown_rows(
    rows: List[Tuple[List[int], str]],
    known_count: int,
    all_letters_ratio: float,
    rng: random.Random,
    *,
    hard: bool,
) -> None:
    if all_letters_ratio <= 0.0:
        return

    unknown_count = int(round((known_count * all_letters_ratio) / max(1e-9, 1.0 - all_letters_ratio)))
    letters = [ch for ch in "ABCDEFGHIJKLMNOPQRSTUVWXYZ" if ch not in {"E", "F", "Z"}]

    if unknown_count <= 0:
        return

    # Ensure every non-E/F/Z letter appears in similar proportion.
    letter_pool = letters[:]
    rng.shuffle(letter_pool)
    for i in range(unknown_count):
        label = letter_pool[i % len(letter_pool)]
        # Mix several unknown styles so the net learns [0,0,0] for blanks/noise/garbage too.
        p = rng.random()
        if p < 0.45:
            pixels = synthesize_unknown_letter(label, rng, hard=hard)
        elif p < 0.65:
            pixels = synthesize_obvious_confuser_unknown(rng, hard=hard)
        elif p < 0.78:
            pixels = synthesize_unknown(rng, hard=hard)
        elif p < 0.89:
            pixels = synthesize_line_unknown(rng, hard=hard)
        elif p < 0.97:
            pixels = synthesize_noise_unknown(rng, hard=hard)
        else:
            pixels = synthesize_empty_unknown(rng, hard=hard)
        rows.append((pixels, label))


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
    parser.add_argument(
        "--all-letters-ratio",
        type=float,
        default=0.25,
        help="Fraction of additional A-Z(excluding E/F/Z) samples among all rows (default: 0.25).",
    )
    parser.add_argument(
        "--unknown-ratio",
        type=float,
        default=None,
        help="Deprecated alias for --all-letters-ratio.",
    )
    parser.add_argument("--seed", type=int, default=42, help="Random seed.")
    parser.add_argument(
        "--hard-test",
        action="store_true",
        help="Generate noisier/more shifted test samples.",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    all_letters_ratio = args.all_letters_ratio if args.unknown_ratio is None else args.unknown_ratio
    if not (0.0 <= all_letters_ratio < 1.0):
        raise SystemExit("--all-letters-ratio (or --unknown-ratio) must be in range [0.0, 1.0).")

    rng = random.Random(args.seed)

    labels = ("E", "F", "Z")
    train_rows = generate_rows(labels, args.train_per_class, rng, hard=False)
    test_rows = generate_rows(labels, args.test_per_class, rng, hard=args.hard_test)

    add_unknown_rows(
        train_rows,
        known_count=len(train_rows),
        all_letters_ratio=all_letters_ratio,
        rng=rng,
        hard=False,
    )
    add_unknown_rows(
        test_rows,
        known_count=len(test_rows),
        all_letters_ratio=all_letters_ratio,
        rng=rng,
        hard=args.hard_test,
    )
    rng.shuffle(train_rows)
    rng.shuffle(test_rows)

    train_path = Path(args.train_out)
    test_path = Path(args.test_out)

    write_csv(train_path, train_rows)
    write_csv(test_path, test_rows)

    print(f"Generated: {train_path} ({len(train_rows)} rows, all-letters ratio target={all_letters_ratio:.2f})")
    print(f"Generated: {test_path} ({len(test_rows)} rows, all-letters ratio target={all_letters_ratio:.2f})")


if __name__ == "__main__":
    main()
