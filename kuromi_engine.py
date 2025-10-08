import os
import sys
import tempfile
import subprocess
import threading
import tkinter as tk
from tkinter import filedialog, messagebox, ttk
from PIL import Image, ImageTk
from kuromi_interpreter import run_kuromi_code

root = tk.Tk()
root.title("Kuromi Engine 🎀")
root.geometry("900x600")
root.configure(bg="#1e1e1e")

# Set engine window icon
try:
    root.iconbitmap("icon.ico")
except tk.TclError as e:
    print(f"[Error] Failed to set engine icon: {e}")

# Background Image
try:
    bg_path = "kuromi_bg.png"
    print(f"Attempting to load engine background: {bg_path}")
    bg_img = Image.open(bg_path).resize((900, 600))
    bg_img = ImageTk.PhotoImage(bg_img)
    bg_label = tk.Label(root, image=bg_img)
    bg_label.place(x=0, y=0, relwidth=1, relheight=1)
except Exception as e:
    print(f"[Background not loaded: {e}]")

# Top Frame
top_frame = tk.Frame(root, bg="#1e1e1e")
top_frame.pack(fill=tk.X, pady=10)

# Resolution Dropdown
resolution_var = tk.StringVar(value="800x600")
res_label = tk.Label(top_frame, text="Resolution:", bg="#1e1e1e", fg="white", font=("Consolas", 10))
res_label.pack(side=tk.LEFT, padx=(30, 5))
res_menu = ttk.Combobox(top_frame, textvariable=resolution_var, values=["640x480", "800x600", "1024x768", "1280x720"], width=15)
res_menu.pack(side=tk.LEFT)

# Editor
editor = tk.Text(root, height=18, bg="#2d2d2d", fg="#ffffff", insertbackground="white", relief="flat", font=("Consolas", 12))
editor.pack(fill=tk.BOTH, padx=10, pady=10, expand=True)

# Output Box
output_box = tk.Text(root, height=10, bg="#111", fg="#00ffcc", relief="flat", font=("Consolas", 11))
output_box.pack(fill=tk.BOTH, padx=10, pady=(0,10), expand=True)

def resource_path(relative_path):
    """Get absolute path to resource, works for dev and for PyInstaller."""
    try:
        base_path = sys._MEIPASS
    except AttributeError:
        base_path = os.path.abspath(".")
    full_path = os.path.join(base_path, relative_path)
    print(f"Resolved resource path for {relative_path}: {full_path}")
    return full_path

def run_code():
    """Run the KuromiScript code in debug mode with branding splash screen."""
    output_box.delete("1.0", tk.END)
    code = editor.get("1.0", tk.END).strip()
    
    # KuromiCore branding splash screen
    branding_code = """
Kuromi <Game Start>
LoadImage "logo" "kuromi_logo.png"
DisplayImage "logo" 200 150
ShowText "Made in KuromiCore" 300 400
Wait 3000
"""
    
    # User splash screen
    splash_code = """
LoadImage "splash" "splash_bg.png"
DisplayImage "splash" 0 0
PlaySound "startup.wav"
Wait 3000
"""
    
    # Combine branding, splash, and user KuromiScript code
    kuromi_code = branding_code + splash_code + code
    
    output_box.insert(tk.END, "▶ Running Kuromi debug window...\n")
    
    # Create a debug output function
    def debug_print(message):
        output_box.insert(tk.END, f"{message}\n")
        output_box.see(tk.END)
    
    # Run in a separate thread to keep editor responsive
    def debug_thread():
        try:
            run_kuromi_code(kuromi_code, print_func=debug_print, debug_mode=True)
        except Exception as e:
            debug_print(f"[Debug Error] {e}")
    
    threading.Thread(target=debug_thread, daemon=True).start()

def build_to_exe():
    """Build the current Kuromi game into a standalone EXE with branding splash screen."""
    code = editor.get("1.0", tk.END).strip()
    build_num = filedialog.asksaveasfilename(
        defaultextension=".exe",
        filetypes=[("Executable Files", "*.exe")],
        title="Save Game Executable As"
    )
    if not build_num:
        return
    build_name = os.path.splitext(os.path.basename(build_num))[0]

    # Generate a temporary .py game with splash screen and interpreter
    temp_dir = tempfile.mkdtemp()
    src_path = os.path.join(temp_dir, f"kuromi_game_{build_name}.py")

    # KuromiCore branding splash screen
    branding_code = """
Kuromi <Game Start>
LoadImage "logo" "kuromi_logo.png"
DisplayImage "logo" 200 150
ShowText "Made in KuromiCore" 300 400
Wait 3000
"""
    
    # User splash screen
    splash_code = """
LoadImage "splash" "splash_bg.png"
DisplayImage "splash" 0 0
PlaySound "startup.wav"
Wait 3000
"""
    
    # Combine branding, splash, and user KuromiScript code
    kuromi_code = branding_code + splash_code + code
    
    # Read interpreter source code
    with open("kuromi_interpreter.py", "r", encoding="utf-8") as f:
        interpreter_code = f.read()
    
    # Generate game code with interpreter
    py_code = f"""
{interpreter_code}

# Run the KuromiScript code
run_kuromi_code('''{kuromi_code}''')
"""

    with open(src_path, "w", encoding="utf-8") as f:
        py_code = py_code.replace("\r\n", "\n")  # Normalize line endings
        f.write(py_code)

    # Output folder for built EXE
    builds_dir = os.path.join(os.getcwd(), "builds")
    os.makedirs(builds_dir, exist_ok=True)

    output_box.insert(tk.END, f"⚙ Building KuromiCore Game ({build_name})...\n")
    output_box.see(tk.END)

    # List of assets to bundle
    assets = [
        "icon.ico",
        "kuromi_bg.png",
        "kuromi_logo.png",
        "splash_bg.png",
        "startup.wav"
    ]

    # Verify assets exist
    missing_assets = [asset for asset in assets if not os.path.exists(asset)]
    if missing_assets:
        output_box.insert(tk.END, f"\n❌ Error: Missing assets: {', '.join(missing_assets)}\n")
        output_box.see(tk.END)
        return

    # Run PyInstaller on the temp file, bundling assets and setting icon
    def build_thread():
        cmd = [
            "python",
            "-m", "PyInstaller",
            "--onefile",
            "--noconsole",
            "--name", f"KuromiCore_{build_name}",
            "--distpath", builds_dir,
            "--icon", resource_path("icon.ico")
        ]
        
        separator = ";" if sys.platform.startswith("win") else ":"
        for asset in assets:
            cmd.extend(["--add-data", f"{asset}{separator}."])
        
        cmd.append(src_path)
        
        try:
            proc = subprocess.run(cmd, check=True, stdout=subprocess.PIPE, stderr=subprocess.STDOUT, text=True)
            output_box.insert(tk.END, proc.stdout)
            output_box.insert(tk.END, f"\n✅ Build complete: {os.path.join(builds_dir, f'KuromiCore_{build_name}.exe')}\n")
        except subprocess.CalledProcessError as e:
            output_box.insert(tk.END, f"\n❌ Build failed:\n{e.output}\n")
        output_box.see(tk.END)

    threading.Thread(target=build_thread, daemon=True).start()

def open_file():
    path = filedialog.askopenfilename(filetypes=[("Kuromi Files", "*.KUROMI")])
    if path:
        with open(path, "r", encoding="utf-8") as f:
            editor.delete("1.0", tk.END)
            editor.insert("1.0", f.read())
        root.title(f"Kuromi Engine 🎀 - {os.path.basename(path)}")

def save_file():
    path = filedialog.asksaveasfilename(defaultextension=".KUROMI", filetypes=[("Kuromi Files", "*.KUROMI")])
    if path:
        with open(path, "w", encoding="utf-8") as f:
            f.write(editor.get("1.0", tk.END))
        root.title(f"Kuromi Engine 🎀 - {os.path.basename(path)}")
        messagebox.showinfo("Saved", f"Saved to {path}")

# Buttons
buttons = [
    ("📂 Open", open_file, "#6a5acd"),
    ("💾 Save", save_file, "#6a5acd"),
    ("▶ Run", run_code, "#7a3db8"),
    ("⚙ Build to EXE", build_to_exe, "#b83d8f")
]

for text, cmd, color in buttons:
    tk.Button(
        top_frame,
        text=text,
        command=cmd,
        bg=color,
        fg="white",
        relief="flat",
        padx=10,
        pady=5,
        font=("Consolas", 10, "bold")
    ).pack(side=tk.LEFT, padx=5)

root.mainloop()