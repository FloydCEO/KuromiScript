import os
import sys
import time
import tkinter as tk
from tkinter import filedialog, messagebox, ttk
import threading
import pygame

# Initialize pygame mixer for sounds
pygame.mixer.init()

# ============================================================
#  HELPER: Safe import of Kuromi interpreter functions
# ============================================================
def get_kuromi_api():
    """Lazy import to avoid circular import errors with PyInstaller."""
    from kuromi_interpreter import run_kuromi_code, resource_path
    return run_kuromi_code, resource_path


# ============================================================
#  SPLASH SCREEN
# ============================================================
def show_splash_screen():
    """Show KuromiCore splash screen."""
    splash = tk.Tk()
    splash.title("KuromiCore")
    splash.overrideredirect(True)

    width, height = 400, 300
    screen_width = splash.winfo_screenwidth()
    screen_height = splash.winfo_screenheight()
    x = (screen_width - width) // 2
    y = (screen_height - height) // 2
    splash.geometry(f"{width}x{height}+{x}+{y}")
    splash.configure(bg="#4B0082")

    try:
        _, resource_path = get_kuromi_api()
        icon_path = resource_path("assets/icon.ico")
        splash.iconbitmap(icon_path)
    except Exception:
        pass

    tk.Label(
        splash, text="KuromiCore", font=("Arial", 36, "bold"),
        fg="white", bg="#4B0082"
    ).pack(expand=True, pady=(80, 10))
    tk.Label(
        splash, text="by Logan Whaley",
        font=("Arial", 14), fg="#E0E0E0", bg="#4B0082"
    ).pack(pady=(0, 80))
    tk.Label(
        splash, text="Loading...", font=("Arial", 10),
        fg="#B0B0B0", bg="#4B0082"
    ).pack(side=tk.BOTTOM, pady=20)

    splash.update()
    time.sleep(1.5)

    try:
        _, resource_path = get_kuromi_api()
        startup_sound = resource_path("assets/startup.wav")
        pygame.mixer.music.load(startup_sound)
        pygame.mixer.music.play()
    except Exception:
        pass

    splash.destroy()


# ============================================================
#  MAIN WINDOW SETUP
# ============================================================
show_splash_screen()

root = tk.Tk()
root.title("KuromiCore by Logan Whaley")
root.geometry("900x600")
root.configure(bg="#1e1e1e")

# Icon
try:
    _, resource_path = get_kuromi_api()
    icon_path = resource_path("assets/icon.ico")
    root.iconbitmap(icon_path)
except Exception as e:
    print(f"[Warning] Could not set engine icon: {e}")

# ============================================================
#  GUI SETUP
# ============================================================
top_frame = tk.Frame(root, bg="#1e1e1e")
top_frame.pack(fill=tk.X, pady=10)

resolution_var = tk.StringVar(value="800x600")
tk.Label(
    top_frame, text="Resolution:", bg="#1e1e1e",
    fg="white", font=("Consolas", 10)
).pack(side=tk.LEFT, padx=(30, 5))

ttk.Combobox(
    top_frame, textvariable=resolution_var,
    values=["640x480", "800x600", "1024x768", "1280x720"],
    width=15
).pack(side=tk.LEFT)

editor = tk.Text(
    root, height=18, bg="#2d2d2d", fg="#ffffff",
    insertbackground="white", relief="flat",
    font=("Consolas", 12)
)
editor.pack(fill=tk.BOTH, padx=10, pady=10, expand=True)

output_box = tk.Text(
    root, height=10, bg="#111", fg="#00ffcc",
    relief="flat", font=("Consolas", 11)
)
output_box.pack(fill=tk.BOTH, padx=10, pady=(0, 10), expand=True)


# ============================================================
#  CORE FUNCTIONS
# ============================================================
def run_code():
    """Run KuromiScript code in debug mode."""
    output_box.delete("1.0", tk.END)
    code = editor.get("1.0", tk.END).strip()

    if not code:
        output_box.insert(tk.END, "[!] No code to run!\n")
        return

    output_box.insert(tk.END, "[>] Starting KuromiCore Debug Window...\n")
    run_kuromi_code, resource_path = get_kuromi_api()

    game_window = tk.Toplevel()
    game_window.title("KuromiCore Game")
    game_window.geometry("800x600")

    try:
        icon_path = resource_path("assets/icon.ico")
        game_window.iconbitmap(icon_path)
    except Exception:
        pass

    canvas = tk.Canvas(game_window, width=800, height=600, bg="#4B0082")
    canvas.pack()

    splash_text = canvas.create_text(
        400, 300, text="Made with KuromiCore",
        fill="white", font=("Arial", 24, "bold")
    )
    game_window.update()

    def continue_game():
        canvas.delete(splash_text)
        canvas.configure(bg="black")
        game_window.update()

        def debug_print(msg):
            output_box.insert(tk.END, f"{msg}\n")
            output_box.see(tk.END)

        try:
            debug_print("[+] Running your game...")
            run_kuromi_code(code, print_func=debug_print, debug_mode=True, root=game_window, canvas=canvas)
            debug_print("\n[OK] Game execution complete!")
        except Exception as e:
            import traceback
            debug_print(f"\n[ERROR] {e}\n{traceback.format_exc()}")

    root.after(2000, continue_game)


def build_to_exe():
    """Save .KUROMI then call KSCompiler to build .exe."""
    code = editor.get("1.0", tk.END).strip()
    if not code:
        messagebox.showwarning("No Code", "Please write some code before building!")
        return

    save_path = filedialog.asksaveasfilename(
        defaultextension=".KUROMI",
        filetypes=[("Kuromi Files", "*.KUROMI")],
        title="Save Kuromi Game File"
    )
    if not save_path:
        return

    with open(save_path, "w", encoding="utf-8") as f:
        f.write(code)

    output_box.insert(tk.END, f"[KuromiCore]: Saved script to {save_path}\n")
    output_box.insert(tk.END, "[KuromiCore]: Launching KSCompiler...\n")

    kscompiler_path = os.path.join(os.getcwd(), "KSCompiler.bat")
    if not os.path.exists(kscompiler_path):
        messagebox.showerror("Missing KSCompiler", f"Could not find {kscompiler_path}")
        return

    # Launch KSCompiler
    os.system(f'start cmd /k "{kscompiler_path}" "{save_path}"')


def open_file():
    """Open a .KUROMI file into editor."""
    path = filedialog.askopenfilename(filetypes=[("Kuromi Files", "*.KUROMI"), ("All Files", "*.*")])
    if not path:
        return
    try:
        with open(path, "r", encoding="utf-8") as f:
            editor.delete("1.0", tk.END)
            editor.insert("1.0", f.read())
        root.title(f"KuromiCore - {os.path.basename(path)}")
        output_box.insert(tk.END, f"[OK] Opened: {os.path.basename(path)}\n")
    except Exception as e:
        messagebox.showerror("Error", f"Failed to open file:\n{e}")


def save_file():
    """Save the current code to a .KUROMI file."""
    path = filedialog.asksaveasfilename(
        defaultextension=".KUROMI",
        filetypes=[("Kuromi Files", "*.KUROMI"), ("All Files", "*.*")]
    )
    if not path:
        return
    try:
        with open(path, "w", encoding="utf-8") as f:
            f.write(editor.get("1.0", tk.END))
        output_box.insert(tk.END, f"[OK] Saved: {os.path.basename(path)}\n")
        messagebox.showinfo("Saved", "File saved successfully!")
    except Exception as e:
        messagebox.showerror("Error", f"Failed to save file:\n{e}")


# ============================================================
#  BUTTONS
# ============================================================
buttons = [
    ("[OPEN]", open_file, "#6a5acd"),
    ("[SAVE]", save_file, "#6a5acd"),
    ("[RUN]", run_code, "#7a3db8"),
    ("[BUILD EXE]", build_to_exe, "#b83d8f")
]

for text, cmd, color in buttons:
    tk.Button(
        top_frame, text=text, command=cmd,
        bg=color, fg="white", relief="flat",
        padx=10, pady=5, font=("Consolas", 10, "bold")
    ).pack(side=tk.LEFT, padx=5)


# ============================================================
#  SAMPLE GAME
# ============================================================
sample_code = """// KuromiCore Sample Game - Drawing Sprites!
Kuromi <Start>

DrawCircle 400 200 50 "pink"
DrawRectangle 375 250 50 100 "purple"
DrawCircle 385 190 8 "black"
DrawCircle 415 190 8 "black"
DrawLine 390 220 410 220 "red"

ShowText (centered) "Made with shapes!" 400 400
ShowText (centered) "Try DrawRectangle, DrawCircle, DrawLine, DrawTriangle!" 400 450
"""
editor.insert("1.0", sample_code)

# ============================================================
root.mainloop()
