#!/usr/bin/env python3
"""
mc-lookup: Look up Mojang-mapped Minecraft source code from the deobf jar.

Usage:
    python scripts/mc-lookup.py net.minecraft.world.entity.player.Inventory
    python scripts/mc-lookup.py net.minecraft.world.Container addItem
    python scripts/mc-lookup.py net.minecraft.world.entity.player.Inventory --methods  # list all methods
    python scripts/mc-lookup.py net.minecraft.world.entity.player.Inventory add       # grep for "add"
    python scripts/mc-lookup.py --client net.minecraft.client.gui.screens.MenuScreens
"""

import os, sys, subprocess, re

PROJECT_DIR = os.path.abspath(os.path.join(os.path.dirname(__file__), "..", "..", ".."))
while not os.path.isfile(os.path.join(PROJECT_DIR, "build.gradle")):
    PROJECT_DIR = os.path.dirname(PROJECT_DIR)
    if PROJECT_DIR == os.path.dirname(PROJECT_DIR):
        print("ERROR: could not find project root (build.gradle)", file=sys.stderr)
        sys.exit(1)

SEARCH_ROOT = os.path.join(os.path.expanduser("~"), ".gradle", "caches", "fabric-loom", "minecraftMaven")

def find_jar(filename):
    for root, dirs, files in os.walk(SEARCH_ROOT):
        for f in files:
            if f == filename:
                return os.path.join(root, f)
    return None

DEOBF_JARS = {
    "common": find_jar("minecraft-common-deobf-26.2.jar"),
    "client": find_jar("minecraft-clientonly-deobf-26.2.jar"),
}

if not DEOBF_JARS["common"]:
    print("ERROR: common deobf jar not found. Run `gradlew.bat genSources` first.", file=sys.stderr)
    sys.exit(1)

JAVA_HOME = os.environ.get("JAVA_HOME")
if JAVA_HOME:
    JAVAP = os.path.join(JAVA_HOME, "bin", "javap")
else:
    candidates = [
        "C:\\Program Files\\Java\\jdk-21.0.2\\bin\\javap.exe",
        "C:\\Program Files\\Java\\jdk-17\\bin\\javap.exe",
    ]
    JAVAP = None
    for c in candidates:
        if os.path.isfile(c):
            JAVAP = c
            break
    if not JAVAP:
        JAVAP = "javap"

def main():
    args = sys.argv[1:]
    if not args:
        print(__doc__, file=sys.stderr)
        sys.exit(1)

    use_client = "--client" in args
    args = [a for a in args if a != "--client"]

    if not args:
        print("ERROR: no class name provided", file=sys.stderr)
        sys.exit(1)

    class_name = args[0]
    filter_pattern = None
    show_all = False

    for arg in args[1:]:
        if arg == "--methods":
            show_all = True
        else:
            filter_pattern = arg

    jar = DEOBF_JARS["client"] if use_client else DEOBF_JARS["common"]
    if not jar:
        print("ERROR: client deobf jar not found.", file=sys.stderr)
        sys.exit(1)

    cmd = [JAVAP, "-cp", jar, class_name]
    try:
        result = subprocess.run(cmd, capture_output=True, text=True, timeout=30)
    except FileNotFoundError:
        print(f"ERROR: javap not found at {JAVAP}. Set JAVA_HOME.", file=sys.stderr)
        sys.exit(1)
    except subprocess.TimeoutExpired:
        print("ERROR: javap timed out", file=sys.stderr)
        sys.exit(1)

    if result.returncode != 0:
        print(f"ERROR: javap failed for {class_name}", file=sys.stderr)
        print(result.stderr.strip(), file=sys.stderr)
        sys.exit(1)

    lines = result.stdout.splitlines()

    if show_all:
        output_lines = lines
    elif filter_pattern:
        output_lines = [l for l in lines if filter_pattern in l]
    else:
        output_lines = []
        in_methods = False
        for l in lines:
            if l.strip().startswith("public ") or l.strip().startswith("protected ") or l.strip().startswith("private "):
                in_methods = True
            if l.strip().startswith("Compiled from"):
                output_lines.append(l)
                continue
            if not in_methods and not l.strip().startswith("Compiled from"):
                continue
            output_lines.append(l)

    for line in output_lines:
        print(line)

    if filter_pattern:
        prefix = "client" if use_client else "common"
        print(f"\n--- Found {len(output_lines)} matches for '{filter_pattern}' in {class_name} ({prefix})")

if __name__ == "__main__":
    main()