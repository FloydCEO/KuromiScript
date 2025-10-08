import tkinter as tk
from tkinter import filedialog, messagebox, ttk
from PIL import Image, ImageTk
import subprocess, os, tempfile, threading, time

# --- Kuromi Transpiler ---
def transpile_kuromi_to_python(kuromi_code, resolution="640x480"):
    """Converts Kuromi script into standalone Python game window code."""
    lines = kuromi_code.splitlines()
    body = ["game_print('✨ Kuromi Game Started ✨')"]
    body.append("game_print('Welcome to the Kuromi Language!')")

    for line in lines:
        line = line.strip()
        if not line or line.startswith("#"):
            continue

        if line.startswith("Print "):
            content = line[6:].strip()
            body.append(f"game_print({content})")
        elif line.startswith("Let "):
            body.append(line.replace("Let ", ""))
        else:
            body.append(f"# Unknown command: {line}")

    py_code = [
        "import tkinter as tk",
        "import time",
        "",
        "def game_print(text):",
        "    label.config(text=label.cget('text') + str(text) + '\\n')",
        "    root.update()",
        "    time.sleep(0.3)",
        "",
        "root = tk.Tk()",
        "root.title('Kuromi Game 💜')",
        f"root.geometry('{resolution}')",
        "root.configure(bg='#1a001a')",
        "",
        "label = tk.Label(root, text='', fg='#ffccff', bg='#1a001a', font=('Comic Sans MS', 14))",
        "label.pack(expand=True, padx=20, pady=20)",
        "",
        *body,
        "",
        "label.config(text=label.cget('text') + '\\n🎀 The End 🎀')",
        "root.mainloop()"
    ]

    return "\n".join(py_code)


# --- Run Function (Debug) ---
def run_code():
    output_box.delete("1.0", tk.END)
    code = editor.get("1.0", tk.END)
    res = resolution_var.get()
    py_code = transpile_kuromi_to_python(code, res)

    # Write temp debug file
    temp_dir = tempfile.mkdtemp()
    temp_path = os.path.join(temp_dir, "kuromi_debug_game.py")
    with open(temp_path, "w", encoding="utf-8") as f:
        f.write(py_code)

    output_box.insert(tk.END, "▶ Running Kuromi debug window...\n")

    # Run the debug game in a separate window
    def debug_thread():
        subprocess.run(["python", temp_path], text=True)

    threading.Thread(target=debug_thread, daemon=True).start()


# --- Build to EXE Function ---
def build_to_exe():
    code = editor.get("1.0", tk.END)
    res = resolution_var.get()
    py_code = transpile_kuromi_to_python(code, res)
    temp_dir = tempfile.mkdtemp()
    py_path = os.path.join(temp_dir, "game.py")
    exe_output = os.path.join(os.getcwd(), "builds")
    os.makedirs(exe_output, exist_ok=True)

    with open(py_path, "w", encoding="utf-8") as f:
        f.write(py_code)

    def build_thread():
        output_box.insert(tk.END, "🚧 Building game...\n")
        output_box.see(tk.END)
        try:
            subprocess.run(
                ["python", "-m", "PyInstaller", "--onefile", "--distpath", exe_output, py_path],
                check=True,
                stdout=subprocess.PIPE,
                stderr=subprocess.STDOUT,
                text=True
            )
            output_box.insert(tk.END, f"✅ Build complete! Saved in {exe_output}\n")
        except subprocess.CalledProcessError as e:
            output_box.insert(tk.END, f"❌ Build failed:\n{e.output}\n")

    threading.Thread(target=build_thread, daemon=True).start()


# --- File Handling ---
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


# --- GUI Setup ---
root = tk.Tk()
root.title("Kuromi Engine 🎀")
root.geometry("900x600")
root.configure(bg="#1e1e1e")

# Background Image
try:
    bg_img = Image.open("assets/kuromi_bg.png").resize((900, 600))
    bg_img = ImageTk.PhotoImage(bg_img)
    bg_label = tk.Label(root, image=bg_img)
    bg_label.place(x=0, y=0, relwidth=1, relheight=1)
except Exception as e:
    print(f"[Background not loaded: {e}]")

# Top Frame
top_frame = tk.Frame(root, bg="#1e1e1e")
top_frame.pack(fill=tk.X, pady=10)

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

# Resolution Dropdown
resolution_var = tk.StringVar(value="640x480")
res_label = tk.Label(top_frame, text="Resolution:", bg="#1e1e1e", fg="white", font=("Consolas", 10))
res_label.pack(side=tk.LEFT, padx=(30, 5))
res_menu = ttk.Combobox(top_frame, textvariable=resolution_var, values=["640x480", "800x600", "1024x768", "1280x720", "Fullscreen"], width=15)
res_menu.pack(side=tk.LEFT)

# Editor
editor = tk.Text(root, height=18, bg="#2d2d2d", fg="#ffffff", insertbackground="white", relief="flat", font=("Consolas", 12))
editor.pack(fill=tk.BOTH, padx=10, pady=10, expand=True)

# Output Box
output_box = tk.Text(root, height=10, bg="#111", fg="#00ffcc", relief="flat", font=("Consolas", 11))
output_box.pack(fill=tk.BOTH, padx=10, pady=(0,10), expand=True)

root.mainloop()
