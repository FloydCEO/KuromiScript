import tkinter as tk
from tkinter import filedialog, messagebox
from PIL import Image, ImageTk
import subprocess, os, tempfile, threading

# --- Kuromi Transpiler (placeholder) ---
def transpile_kuromi_to_python(kuromi_code):
    lines = kuromi_code.splitlines()
    py_code = ["print('✨ Kuromi Game Started ✨')"]
    for line in lines:
        if line.startswith("Print"):
            py_code.append("print(" + line[6:] + ")")
        elif "Let" in line:
            py_code.append(line.replace("Let", "").replace("=", "="))
    return "\n".join(py_code)

# --- Run & Build Functions ---
def run_code():
    output_box.delete("1.0", tk.END)
    code = editor.get("1.0", tk.END)
    py_code = transpile_kuromi_to_python(code)
    try:
        exec(py_code, {})
    except Exception as e:
        output_box.insert(tk.END, f"Error: {e}\n")

def build_to_exe():
    code = editor.get("1.0", tk.END)
    py_code = transpile_kuromi_to_python(code)
    temp_dir = tempfile.mkdtemp()
    py_path = os.path.join(temp_dir, "game.py")
    exe_output = os.path.join(os.getcwd(), "builds")
    os.makedirs(exe_output, exist_ok=True)

    with open(py_path, "w", encoding="utf-8") as f:
        f.write(py_code)

    def build_thread():
        output_box.insert(tk.END, "🚧 Building game...\n")
        try:
            subprocess.run(["python", "-m", "PyInstaller", "--onefile", "--noconsole", "--distpath", exe_output, py_path], check=True)
            output_box.insert(tk.END, f"✅ Build complete! Saved in {exe_output}\n")
        except Exception as e:
            output_box.insert(tk.END, f"❌ Build failed: {e}\n")

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

# Load Background Image
try:
    bg_img = Image.open("assets/kuromi_bg.png").resize((900, 600))
    bg_img = ImageTk.PhotoImage(bg_img)
    bg_label = tk.Label(root, image=bg_img)
    bg_label.place(x=0, y=0, relwidth=1, relheight=1)
except Exception:
    pass

# Top Bar
top_frame = tk.Frame(root, bg="#1e1e1e")
top_frame.pack(fill=tk.X, pady=10)

for text, cmd, color in [
    ("📂 Open", open_file, "#6a5acd"),
    ("💾 Save", save_file, "#6a5acd"),
    ("▶ Run", run_code, "#7a3db8"),
    ("💾 Build to EXE", build_to_exe, "#b83d8f")
]:
    tk.Button(top_frame, text=text, command=cmd, bg=color, fg="white", relief="flat", padx=10, pady=5).pack(side=tk.LEFT, padx=5)

# Editor
editor = tk.Text(root, height=18, bg="#2d2d2d", fg="#ffffff", insertbackground="white", relief="flat", font=("Consolas", 12))
editor.pack(fill=tk.BOTH, padx=10, pady=10, expand=True)

# Output Box
output_box = tk.Text(root, height=10, bg="#111", fg="#00ffcc", relief="flat", font=("Consolas", 11))
output_box.pack(fill=tk.BOTH, padx=10, pady=(0,10), expand=True)

root.mainloop()
