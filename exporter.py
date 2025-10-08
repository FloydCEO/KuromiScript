# exporter.py
import os
import sys
import tempfile
import subprocess
import shutil
import logging

# CONFIG — edit if you installed Python somewhere else
PREFERRED_PYTHON = r"C:\Users\LykoD\AppData\Local\Programs\Python\Python311-32\python.exe"
BASE_DIR = os.path.dirname(os.path.abspath(__file__))
ASSETS_DIR = os.path.join(BASE_DIR, "assets")
ICON_PATH = os.path.join(ASSETS_DIR, "icon.ico")
BUILD_ROOT = os.path.join(BASE_DIR, "dist", "dist", "builds")

# safe logging (OneDrive-proof)
def setup_logging():
    import tempfile
    try:
        log_path = os.path.join(BASE_DIR, "kuromi_export_log.txt")
        with open(log_path, "w", encoding="utf-8") as f:
            f.write("Kuromi Export Log\n")
        logging.basicConfig(filename=log_path, level=logging.DEBUG, format="%(asctime)s %(levelname)s: %(message)s")
        return logging.getLogger("KuromiExporter"), log_path
    except Exception:
        temp_path = os.path.join(tempfile.gettempdir(), "kuromi_export_log.txt")
        logging.basicConfig(filename=temp_path, level=logging.DEBUG, format="%(asctime)s %(levelname)s: %(message)s")
        return logging.getLogger("KuromiExporter"), temp_path

log, LOG_PATH = setup_logging()

def find_python():
    import shutil
    if os.path.exists(PREFERRED_PYTHON):
        return PREFERRED_PYTHON
    py = shutil.which("python")
    if py:
        return py
    return sys.executable

def ensure_deps(python_exe):
    # ensure pyinstaller and pygame are available in that interpreter
    try:
        r = subprocess.run([python_exe, "-m", "PyInstaller", "--version"], capture_output=True, text=True)
        if r.returncode != 0:
            print("[KuromiCore]: Installing PyInstaller...")
            subprocess.check_call([python_exe, "-m", "pip", "install", "pyinstaller"])
    except Exception:
        print("[KuromiCore]: Installing PyInstaller...")
        subprocess.check_call([python_exe, "-m", "pip", "install", "pyinstaller"])

    try:
        r = subprocess.run([python_exe, "-c", "import pygame; print(pygame.__version__)"], capture_output=True, text=True)
        if r.returncode != 0:
            print("[KuromiCore]: Installing pygame...")
            subprocess.check_call([python_exe, "-m", "pip", "install", "pygame"])
    except Exception:
        print("[KuromiCore]: Installing pygame...")
        subprocess.check_call([python_exe, "-m", "pip", "install", "pygame"])

def build_game(src_kuromi):
    python_exe = find_python()
    if not python_exe or not os.path.exists(python_exe):
        print("[KuromiCore]: Python executable not found. Edit exporter.py PREFERRED_PYTHON.")
        return False

    ensure_deps(python_exe)

    game_name = os.path.splitext(os.path.basename(src_kuromi))[0]
    os.makedirs(BUILD_ROOT, exist_ok=True)
    out_dir_for_game = os.path.join(BUILD_ROOT, game_name)

    tmp_dir = tempfile.mkdtemp(prefix="kuromi_build_")

    interp_path = os.path.join(BASE_DIR, "kuromi_interpreter.py")
    if not os.path.exists(interp_path):
        print("[KuromiCore]: kuromi_interpreter.py missing.")
        return False

    with open(interp_path, "r", encoding="utf-8") as f:
        interp_src = f.read()
    with open(src_kuromi, "r", encoding="utf-8") as f:
        ku_code = f.read()

    launcher_py = os.path.join(tmp_dir, f"{game_name}.py")
    with open(launcher_py, "w", encoding="utf-8") as out:
        out.write("# Auto-generated launcher by Kuromi exporter\n")
        out.write(interp_src + "\n\n")
        out.write("if __name__ == '__main__':\n")
        out.write("    import sys, time, traceback, os\n")
        out.write("    try:\n")
        out.write("        game_code = '''" + ku_code + "'''\n")
        out.write("        run_kuromi_code(game_code, debug_mode=False)\n")
        out.write("    except Exception:\n")
        out.write("        traceback.print_exc()\n")
        out.write("        with open('kuromi_runtime_error.txt','w', encoding='utf-8') as e:\n")
        out.write("            import traceback as _tb\n")
        out.write("            e.write(_tb.format_exc())\n")
        out.write("        time.sleep(5)\n")

    # Build command
    add_data_arg = ASSETS_DIR + os.pathsep + "assets"
    cmd = [
        python_exe, "-m", "PyInstaller",
        "--clean", "--noconfirm", "--onedir", "--windowed",
        "--distpath", out_dir_for_game,
        "--workpath", os.path.join(tmp_dir, "build"),
        "--specpath", tmp_dir,
        "--add-data", add_data_arg,
        "--collect-all", "pygame",
        "--hidden-import", "pygame",
        "--hidden-import", "tkinter"
    ]
    if os.path.exists(ICON_PATH):
        cmd += ["--icon", ICON_PATH]
    cmd.append(launcher_py)

    log.debug("PyInstaller cmd: %s" % " ".join(cmd))
    print("[KuromiCore]: Running PyInstaller... this may take a minute")
    proc = subprocess.run(cmd, capture_output=True, text=True)
    log.debug(proc.stdout)
    log.debug(proc.stderr)

    exe_path = os.path.join(out_dir_for_game, game_name, f"{game_name}.exe")
    if not os.path.exists(exe_path):
        # fallback search
        for root, _, files in os.walk(out_dir_for_game):
            for f in files:
                if f.lower().endswith(".exe") and game_name.lower() in f.lower():
                    exe_path = os.path.join(root, f)
                    break

    # Copy assets next to EXE to guarantee runtime availability
    try:
        target_assets = os.path.join(out_dir_for_game, game_name, "assets")
        if os.path.exists(ASSETS_DIR):
            shutil.copytree(ASSETS_DIR, target_assets, dirs_exist_ok=True)
    except Exception:
        log.exception("copy assets failed")

    shutil.rmtree(tmp_dir, ignore_errors=True)

    if os.path.exists(exe_path):
        print(f"[KuromiCore]: Build complete: {exe_path}")
        return True
    else:
        print("[KuromiCore]: Build failed. Check log:", LOG_PATH)
        print(proc.stderr)
        return False

if __name__ == "__main__":
    if len(sys.argv) < 2:
        print("Usage: python exporter.py <file.kuromi>")
        sys.exit(1)
    src = sys.argv[1]
    ok = build_game(src)
    sys.exit(0 if ok else 1)
