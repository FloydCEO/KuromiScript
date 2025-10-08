cmd = [
    python_exe, "-m", "PyInstaller",
    "--clean", "--noconfirm", "--onedir", "--windowed",
    "--distpath", BUILD_ROOT,
    "--additional-hooks-dir", BASE_DIR,  # 👈 this line loads your hook-tkinter.py
    spec_path
]
