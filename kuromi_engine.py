#kuromi_engine.py

import os
import sys
import time
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
    splash.overrideredirect(True)
    
    width = 400
    height = 300
    screen_width = splash.winfo_screenwidth()
    screen_height = splash.winfo_screenheight()
    x = (screen_width - width) // 2
    y = (screen_height - height) // 2
    splash.geometry(f"{width}x{height}+{x}+{y}")
    
    try:
        icon_path = resource_path("assets/icon.ico")
        splash.iconbitmap(icon_path)
    except:
        pass
    
    splash.configure(bg="#4B0082")
    
    title_label = tk.Label(splash, text="KuromiCore", 
                          font=("Arial", 36, "bold"), 
                          fg="white", bg="#4B0082")
    title_label.pack(expand=True, pady=(80, 10))
    
    author_label = tk.Label(splash, text="by Logan Whaley", 
                           font=("Arial", 14), 
                           fg="#E0E0E0", bg="#4B0082")
    author_label.pack(pady=(0, 80))
    
    loading_label = tk.Label(splash, text="Loading...", 
                            font=("Arial", 10), 
                            fg="#B0B0B0", bg="#4B0082")
    loading_label.pack(side=tk.BOTTOM, pady=20)
    
    splash.update()
    time.sleep(2)
    
    try:
        startup_sound = resource_path("assets/startup.wav")
        pygame.mixer.music.load(startup_sound)
        pygame.mixer.music.play()
    except:
        pass
    
    splash.destroy()

show_splash_screen()

# Create main engine window
root = tk.Tk()
root.title("KuromiCore by Logan Whaley")
root.geometry("900x600")
root.configure(bg="#1e1e1e")

icon_path = resource_path("assets/icon.ico")
try:
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
    """Run the KuromiScript code in debug mode."""
    output_box.delete("1.0", tk.END)
    code = editor.get("1.0", tk.END).strip()
    
    if not code:
        output_box.insert(tk.END, "[!] No code to run!\n")
        return
    
    output_box.insert(tk.END, "[>] Starting KuromiCore Debug Window...\n")
    
    def debug_print(message):
        output_box.insert(tk.END, f"{message}\n")
        output_box.see(tk.END)
    
    game_window = tk.Toplevel()
    game_window.title("KuromiCore Game")
    game_window.geometry("800x600")
    
    try:
        icon_path = resource_path("assets/icon.ico")
        game_window.iconbitmap(default=icon_path)
        game_window.iconbitmap(icon_path)
        game_window.update_idletasks()
    except (tk.TclError, Exception) as e:
        debug_print(f"[Warning] Could not set game icon: {e}")
    
    canvas = tk.Canvas(game_window, width=800, height=600, bg="#4B0082")
    canvas.pack()
    
    debug_print("[*] Showing KuromiCore splash screen...")
    splash_text = canvas.create_text(400, 300, text="Made with KuromiCore", 
                                     fill="white", font=("Arial", 24, "bold"))
    game_window.update()
    
    root.after(2000, lambda: continue_game(game_window, canvas, splash_text, code, debug_print))

def continue_game(game_window, canvas, splash_text, code, debug_print):
    """Continue game execution after splash screen."""
    try:
        canvas.delete(splash_text)
        canvas.configure(bg="black")
        game_window.update()
        
        debug_print("[+] Running your game...\n")
        run_kuromi_code(code, print_func=debug_print, debug_mode=True, 
                       root=game_window, canvas=canvas)
        
        debug_print("\n[OK] Game execution complete!")
    except tk.TclError:
        debug_print("\n[X] Game window closed")
    except Exception as e:
        debug_print(f"\n[ERROR] Error during execution: {e}")
        import traceback
        debug_print(traceback.format_exc())

def build_to_exe():
    """Build the current Kuromi game into a standalone EXE using the exporter."""
    code = editor.get("1.0", tk.END).strip()
    
    if not code:
        messagebox.showwarning("No Code", "Please write some code before building!")
        return
    
    # Ask for save location
    build_path = filedialog.asksaveasfilename(
        defaultextension=".exe",
        filetypes=[("Executable Files", "*.exe")],
        title="Save Game Executable As",
        initialdir=os.path.join(os.getcwd(), "dist", "builds")
    )
    
    if not build_path:
        return
    
    build_name = os.path.splitext(os.path.basename(build_path))[0]
    
    output_box.delete("1.0", tk.END)
    output_box.insert(tk.END, f"[BUILD] Building: {build_name}.exe\n")
    output_box.insert(tk.END, "[BUILD] This may take 1-2 minutes...\n\n")
    output_box.see(tk.END)
    root.update()
    
    def build_thread():
        try:
            # Import the exporter
            from exporter import export_game_to_exe
            
            # Run the export
            success, exe_path, error = export_game_to_exe(code, build_name)
            
            if success:
                file_size = os.path.getsize(exe_path) / 1024 / 1024
                output_box.insert(tk.END, f"\n[✓] BUILD SUCCESSFUL!\n")
                output_box.insert(tk.END, f"[FILE] {exe_path}\n")
                output_box.insert(tk.END, f"[SIZE] {file_size:.2f} MB\n")
                output_box.insert(tk.END, f"\n[OK] Your game is ready to distribute!\n")
                output_box.see(tk.END)
                
                messagebox.showinfo("Build Success", 
                    f"Game built successfully!\n\nLocation: {exe_path}\nSize: {file_size:.2f} MB")
            else:
                output_box.insert(tk.END, f"\n[✗] BUILD FAILED\n")
                output_box.insert(tk.END, f"[ERROR] {error}\n")
                output_box.see(tk.END)  # FIXED: Removed extra parenthesis
                
                messagebox.showerror("Build Failed", f"Build failed:\n\n{error}")
                
        except ImportError:
            output_box.insert(tk.END, "\n[✗] ERROR: exporter.py not found!\n")
            output_box.insert(tk.END, "[!] Make sure exporter.py is in the same folder as kuromi_engine.py\n")
            output_box.see(tk.END)
            messagebox.showerror("Exporter Missing", "exporter.py not found!\n\nMake sure it's in the same folder as the engine.")
        except Exception as e:
            output_box.insert(tk.END, f"\n[✗] UNEXPECTED ERROR\n")
            output_box.insert(tk.END, f"{str(e)}\n")
            import traceback
            output_box.insert(tk.END, traceback.format_exc())
            output_box.see(tk.END)
            messagebox.showerror("Build Error", f"An error occurred:\n{e}")
    
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