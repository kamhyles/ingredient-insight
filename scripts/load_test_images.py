#!/usr/bin/env python3
"""Copy the repository's manual OCR fixtures into an Android device's gallery."""

import argparse
from pathlib import Path
import shutil
import subprocess
import sys


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--adb", default="adb", help="Path to adb (default: adb on PATH)")
    parser.add_argument("--serial", help="Target device serial from adb devices")
    args = parser.parse_args()

    adb = shutil.which(args.adb)
    if not adb:
        parser.error("adb was not found. Add SDK platform-tools to PATH or use --adb.")

    command = [adb]
    if args.serial:
        command += ["-s", args.serial]

    source = Path(__file__).resolve().parent.parent / "test_images"
    images = sorted(p for p in source.iterdir() if p.suffix.lower() in {".png", ".jpg", ".jpeg", ".webp"})
    destination = "/sdcard/Pictures/IngredientInsightTests"

    if not images:
        parser.error(f"No label images found in {source}")

    try:
        subprocess.run(command + ["get-state"], check=True)
        subprocess.run(command + ["shell", "mkdir", "-p", destination], check=True)
        for index, image in enumerate(images, 1):
            remote = f"{destination}/test_{index}{image.suffix.lower()}"
            subprocess.run(command + ["push", str(image), remote], check=True)
            subprocess.run(command + [
                "shell", "am", "broadcast", "-a", "android.intent.action.MEDIA_SCANNER_SCAN_FILE",
                "-d", "file://" + remote,
            ], check=True)
            print(f"{image.name} -> {remote}", flush=True)
    except subprocess.CalledProcessError:
        print("Transfer failed. Check adb devices, unlock the device, and use --serial if needed.", file=sys.stderr)
        return 1

    print(f"Loaded {len(images)} images. Reopen the gallery picker and select IngredientInsightTests.")
    return 0


if __name__ == "__main__":
    sys.exit(main())
