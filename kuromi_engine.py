import os
import sys
import time
import tempfile
import subprocess
import tkinter as tk
from tkinter import filedialog, messagebox, ttk
import pygame
from kuromi_interpreter import run_kuromi_code, resource_path

# Initialize pygame mixer for startup sound
pygame.mixer.init()

def show_splash_screen():
    """Show KuromiCore splash screen on engine startup."""
    splash = tk.Tk()
    splash.title("KuromiCore")
    splash.overrideredirect(True)  # Remove window decorations
    
    # Center the splash screen
    width = 400
    height = 300
    screen_width = splash.winfo_screenwidth()
    screen_height = splash.winfo_screenheight()
    x = (screen_width - width) // 2
    y = (screen_height - height) // 2
    splash.geometry(f"{width}x{height}+{x}+{y}")
    
    # Set icon for splash
    try:
        icon_path = resource_path("assets/icon.ico")
        splash.iconbitmap(icon_path)
    except:
        pass
    
    # Splash content
    splash.configure(bg="#4B0082")
    
    # KuromiCore title
    title_label = tk.Label(splash, text="KuromiCore", 
                          font=("Arial", 36, "bold"), 
                          fg="white", bg="#4B0082")
    title_label.pack(expand=True, pady=(80, 10))
    
    # By Logan Whaley
    author_label = tk.Label(splash, text="by Logan Whaley", 
                           font=("Arial", 14), 
                           fg="#E0E0E0", bg="#4B0082")
    author_label.pack(pady=(0, 80))
    
    # Loading text
    loading_label = tk.Label(splash, text="Loading...", 
                            font=("Arial", 10), 
                            fg="#B0B0B0", bg="#4B0082")
    loading_label.pack(side=tk.BOTTOM, pady=20)
    
    splash.update()
    
    # Wait 2 seconds
    time.sleep(2)
    
    # Play startup sound
    try:
        startup_sound = resource_path("assets/startup.wav")
        pygame.mixer.music.load(startup_sound)
        pygame.mixer.music.play()
    except:
        pass  # Sound optional
    
    splash.destroy()

# Show splash screen before main engine
show_splash_screen()

# Create main engine window
root = tk.Tk()
root.title("KuromiCore by Logan Whaley")
root.geometry("900x600")
root.configure(bg="#1e1e1e")

# CRITICAL: Set icon AFTER window is created and force update
icon_path = resource_path("assets/icon.ico")
try:
    # Set icon multiple times to ensure it sticks
    root.iconbitmap(default=icon_path)
    root.iconbitmap(icon_path)
    root.update_idletasks()
except (tk.TclError, Exception) as e:
    print(f"[Warning] Could not set engine icon: {e}")

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

def run_code():
    """Run the KuromiScript code in debug mode with branding splash screen."""
    output_box.delete("1.0", tk.END)
    code = editor.get("1.0", tk.END).strip()
    
    if not code:
        output_box.insert(tk.END, "[!] No code to run!\n")
        return
    
    output_box.insert(tk.END, "[>] Starting KuromiCore Debug Window...\n")
    
    # Create a debug output function
    def debug_print(message):
        output_box.insert(tk.END, f"{message}\n")
        output_box.see(tk.END)
    
    # Create game window
    game_window = tk.Toplevel()
    game_window.title("KuromiCore Game")
    game_window.geometry("800x600")
    
    # Set game window icon - force it to stick
    try:
        icon_path = resource_path("assets/icon.ico")
        game_window.iconbitmap(default=icon_path)
        game_window.iconbitmap(icon_path)
        game_window.update_idletasks()
    except (tk.TclError, Exception) as e:
        debug_print(f"[Warning] Could not set game icon: {e}")
    
    canvas = tk.Canvas(game_window, width=800, height=600, bg="#4B0082")
    canvas.pack()
    
    # Show branding splash screen
    debug_print("[*] Showing KuromiCore splash screen...")
    splash_text = canvas.create_text(400, 300, text="Made with KuromiCore", 
                                     fill="white", font=("Arial", 24, "bold"))
    game_window.update()
    
    # Wait for splash
    root.after(2000, lambda: continue_game(game_window, canvas, splash_text, code, debug_print))

def continue_game(game_window, canvas, splash_text, code, debug_print):
    """Continue game execution after splash screen."""
    try:
        # Clear splash
        canvas.delete(splash_text)
        canvas.configure(bg="black")
        game_window.update()
        
        debug_print("[+] Running your game...\n")
        
        # Run user code
        run_kuromi_code(code, print_func=debug_print, debug_mode=True, 
                       root=game_window, canvas=canvas)
        
        debug_print("\n[OK] Game execution complete!")
    except tk.TclError:
        # Window was closed
        debug_print("\n[X] Game window closed")
    except Exception as e:
        debug_print(f"\n[ERROR] Error during execution: {e}")
        import traceback
        debug_print(traceback.format_exc())

def build_to_exe():
    """Build the current Kuromi game into a standalone EXE with branding splash screen."""
    code = editor.get("1.0", tk.END).strip()
    
    if not code:
        messagebox.showwarning("No Code", "Please write some code before building!")
        return
    
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
    
    # Read interpreter source code
    try:
        interpreter_path = os.path.join(os.path.dirname(__file__), "kuromi_interpreter.py")
        with open(interpreter_path, "r", encoding="utf-8") as f:
            interpreter_code = f.read()
    except FileNotFoundError:
        output_box.insert(tk.END, f"\n[ERROR] kuromi_interpreter.py not found at {interpreter_path}\n")
        output_box.see(tk.END)
        return
    
    # Escape the user code for embedding
    escaped_code = code.replace("\\", "\\\\").replace("'''", "\\'\\'\\'")
    
    # Generate standalone game with splash
    py_code = f'''{interpreter_code}

# Standalone KuromiCore Game
import tkinter as tk
import time

if __name__ == "__main__":
    # Create game window
    root = tk.Tk()
    root.title("KuromiCore Game")
    root.geometry("800x600")
    
    # Set icon
    try:
        root.iconbitmap(resource_path("assets/icon.ico"))
    except:
        pass
    
    canvas = tk.Canvas(root, width=800, height=600, bg="#4B0082")
    canvas.pack()
    
    # Show branding splash
    canvas.create_text(400, 300, text="Made with KuromiCore", 
                      fill="white", font=("Arial", 24, "bold"))
    root.update()
    time.sleep(2)
    
    # Clear splash and run game
    canvas.delete("all")
    canvas.configure(bg="black")
    
    # User's game code
    game_code = """{escaped_code}"""
    
    run_kuromi_code(game_code, debug_mode=True, root=root, canvas=canvas)
'''

    with open(src_path, "w", encoding="utf-8") as f:
        f.write(py_code)

    # Output folder for built EXE
    builds_dir = os.path.join(os.getcwd(), "builds")
    os.makedirs(builds_dir, exist_ok=True)

    output_box.insert(tk.END, f"[BUILD] Building KuromiCore Game: {build_name}...\n")
    output_box.see(tk.END)

    # List of assets to bundle
    assets = [
        os.path.join("assets", "icon.ico"),
        os.path.join("assets", "splash_bg.png"),
        os.path.join("assets", "startup.wav")
    ]

    # Build command
    def build_thread():
        cmd = [
            sys.executable,
            "-m", "PyInstaller",
            "--onefile",
            "--noconsole",
            "--clean",
            "--name", f"KuromiCore_{build_name}",
            "--distpath", builds_dir,
            "--icon", os.path.join("assets", "icon.ico")
        ]
        
        separator = ";" if sys.platform.startswith("win") else ":"
        for asset in assets:
            if os.path.exists(asset):
                cmd.extend(["--add-data", f"{asset}{separator}assets"])
        
        cmd.append(src_path)
        
        try:
            output_box.insert(tk.END, "Building... This may take a minute...\n")
            output_box.see(tk.END)
            root.update()
            
            proc = subprocess.run(cmd, check=True, capture_output=True, text=True)
            
            exe_path = os.path.join(builds_dir, f'KuromiCore_{build_name}.exe')
            
            # Check if file actually exists
            if os.path.exists(exe_path):
                output_box.insert(tk.END, f"\n[OK] Build complete!\n")
                output_box.insert(tk.END, f"[FILE] Location: {exe_path}\n")
                output_box.insert(tk.END, f"[SIZE] File size: {os.path.getsize(exe_path) / 1024 / 1024:.2f} MB\n")
                messagebox.showinfo("Build Success", f"Game built successfully!\n\n{exe_path}")
            else:
                output_box.insert(tk.END, f"\n[ERROR] Build failed: EXE not found at expected location\n")
                output_box.insert(tk.END, f"Expected: {exe_path}\n")
                output_box.insert(tk.END, f"Build output:\n{proc.stdout}\n")
                messagebox.showerror("Build Failed", "EXE was not created. Check output for details.")
                
        except subprocess.CalledProcessError as e:
            output_box.insert(tk.END, f"\n[ERROR] Build failed:\n{e.stderr}\n")
            messagebox.showerror("Build Failed", "Build failed! Check output for details.")
        except Exception as e:
            output_box.insert(tk.END, f"\n[ERROR] Unexpected error: {e}\n")
        
        output_box.see(tk.END)
    
    import threading
    threading.Thread(target=build_thread, daemon=True).start()

def open_file():
    path = filedialog.askopenfilename(filetypes=[("Kuromi Files", "*.KUROMI"), ("All Files", "*.*")])
    if path:
        try:
            with open(path, "r", encoding="utf-8") as f:
                editor.delete("1.0", tk.END)
                editor.insert("1.0", f.read())
            root.title(f"KuromiCore by Logan Whaley - {os.path.basename(path)}")
            output_box.insert(tk.END, f"[OK] Opened: {os.path.basename(path)}\n")
        except Exception as e:
            messagebox.showerror("Error", f"Failed to open file:\n{e}")

def save_file():
    path = filedialog.asksaveasfilename(defaultextension=".KUROMI", 
                                       filetypes=[("Kuromi Files", "*.KUROMI"), ("All Files", "*.*")])
    if path:
        try:
            with open(path, "w", encoding="utf-8") as f:
                f.write(editor.get("1.0", tk.END))
            root.title(f"KuromiCore by Logan Whaley - {os.path.basename(path)}")
            output_box.insert(tk.END, f"[OK] Saved: {os.path.basename(path)}\n")
            messagebox.showinfo("Saved", f"File saved successfully!")
        except Exception as e:
            messagebox.showerror("Error", f"Failed to save file:\n{e}")

# Buttons
buttons = [
    ("[OPEN]", open_file, "#6a5acd"),
    ("[SAVE]", save_file, "#6a5acd"),
    ("[RUN]", run_code, "#7a3db8"),
    ("[BUILD EXE]", build_to_exe, "#b83d8f")
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

# Insert sample code on startup
sample_code = """// KuromiCore Sample Game - Drawing Sprites!
Kuromi <Start>

// Draw a simple character
DrawCircle 400 200 50 "pink"
DrawRectangle 375 250 50 100 "purple"
DrawCircle 385 190 8 "black"
DrawCircle 415 190 8 "black"
DrawLine 390 220 410 220 "red"

ShowText (centered) "Made with shapes!" 400 400
ShowText (centered) "Try DrawRectangle, DrawCircle, DrawLine, DrawTriangle!" 400 450
"""
editor.insert("1.0", sample_code)

root.mainloop()