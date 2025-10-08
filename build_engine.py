import os
import subprocess

print("🎀 Kuromi Engine Builder 🎀")
print("---------------------------")

# Ask for build number
build_number = input("Enter build version (e.g. 1.0.0): ").strip()
if not build_number:
    print("❌ No build number entered. Cancelled.")
    exit()

# Optional: ask for notes or a label
label = input("Optional label (e.g. beta, dev, stable): ").strip()
if label:
    exe_name = f"KuromiEngine_v{build_number}_{label}.exe"
else:
    exe_name = f"KuromiEngine_v{build_number}.exe"

# Define build paths
engine_file = "kuromi_engine.py"
output_dir = "engine_builds"
os.makedirs(output_dir, exist_ok=True)

# Build with PyInstaller
print(f"🚧 Building {exe_name}...")
try:
    subprocess.run([
        "pyinstaller",
        "--onefile",
        "--noconsole",
        "--distpath", output_dir,
        "--name", exe_name,
        engine_file
    ], check=True)
    print(f"\n✅ Build complete! {output_dir}/{exe_name}")
except subprocess.CalledProcessError as e:
    print(f"❌ Build failed: {e}")
