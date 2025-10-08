import re
import time
import tkinter as tk
import pygame
import sys
import os

def resource_path(relative_path):
    """Get absolute path to resource, works for dev and for PyInstaller."""
    try:
        base_path = sys._MEIPASS
    except AttributeError:
        base_path = os.path.abspath(".")
    full_path = os.path.join(base_path, "assets", relative_path)
    print(f"Resolved resource path for {relative_path}: {full_path}")
    return full_path

def run_kuromi_code(code, print_func=print, debug_mode=False):
    variables = {}
    root = None  # Tkinter root for GUI elements
    canvas = None  # Canvas for displaying images
    lines = code.splitlines()
    i = 0
    total_lines = len(lines)

    while i < total_lines:
        line = lines[i].strip()
        if not line or line.startswith("#"):
            i += 1
            continue

        # 🎮 Game start
        if line.startswith("Kuromi <Game Start>"):
            print_func("✨ Kuromi Game Started ✨")
            if root is None:
                root = tk.Tk()
                root.title("KuromiCore Game")
                root.geometry("800x600")
                icon_path = resource_path("icon.ico")
                print_func(f"Attempting to set game window icon: {icon_path}")
                try:
                    root.iconbitmap(icon_path)
                except tk.TclError as e:
                    print_func(f"[Error] Failed to set game window icon: {e}")
                canvas = tk.Canvas(root, width=800, height=600, bg="black")
                canvas.pack()

        # 🖼️ Load Image
        elif line.startswith("LoadImage "):
            match = re.match(r'LoadImage "(\w+)" "([^"]+)"', line)
            if match:
                var_name, img_path = match.groups()
                full_img_path = resource_path(img_path)
                print_func(f"Attempting to load image: {full_img_path}")
                try:
                    img = tk.PhotoImage(file=full_img_path)
                    variables[var_name] = img
                    print_func(f"Loaded image {full_img_path} into variable {var_name}")
                except tk.TclError as e:
                    print_func(f"[Error] Failed to load image {full_img_path}: {e}")
            else:
                print_func(f"[Syntax Error] Invalid LoadImage syntax: {line}")

        # 🖼️ Display Image
        elif line.startswith("DisplayImage "):
            match = re.match(r'DisplayImage "(\w+)" (\d+) (\d+)', line)
            if match:
                var_name, x, y = match.groups()
                x, y = int(x), int(y)
                if var_name in variables and canvas:
                    try:
                        canvas.create_image(x, y, image=variables[var_name], anchor="nw")
                        print_func(f"Displayed image {var_name} at ({x}, {y})")
                    except tk.TclError as e:
                        print_func(f"[Error] Failed to display image {var_name}: {e}")
                else:
                    print_func(f"[Error] Image {var_name} not found or no canvas")
            else:
                print_func(f"[Syntax Error] Invalid DisplayImage syntax: {line}")

        # 📝 Show Text
        elif line.startswith("ShowText "):
            match = re.match(r'ShowText "([^"]+)" (\d+) (\d+)', line)
            if match:
                text, x, y = match.groups()
                x, y = int(x), int(y)
                if canvas:
                    try:
                        canvas.create_text(x, y, text=text, fill="white", font=("Arial", 16), anchor="nw")
                        print_func(f"Displayed text '{text}' at ({x}, {y})")
                    except tk.TclError as e:
                        print_func(f"[Error] Failed to display text: {e}")
                else:
                    print_func(f"[Error] No canvas for text display")
            else:
                print_func(f"[Syntax Error] Invalid ShowText syntax: {line}")

        # 🎵 Play Sound
        elif line.startswith("PlaySound "):
            match = re.match(r'PlaySound "([^"]+)"', line)
            if match:
                sound_path = resource_path(match.group(1))
                print_func(f"Attempting to play sound: {sound_path}")
                try:
                    pygame.mixer.init()
                    pygame.mixer.music.load(sound_path)
                    pygame.mixer.music.play()
                    print_func(f"Playing sound {sound_path}")
                except pygame.error as e:
                    print_func(f"[Error] Failed to play sound {sound_path}: {e}")
            else:
                print_func(f"[Syntax Error] Invalid PlaySound syntax: {line}")

        # 🖨️ Print
        elif line.startswith("Print "):
            match = re.match(r'Print "(.*)"', line)
            if match:
                print_func(match.group(1))
            else:
                expr = line.replace("Print ", "")
                try:
                    print_func(eval(expr, {}, variables))
                except Exception as e:
                    print_func(f"[Error evaluating expression: {e}]")

        # 💾 Variables
        elif line.startswith("Let "):
            match = re.match(r"Let (\w+) = (.*)", line)
            if match:
                var_name, value = match.groups()
                try:
                    variables[var_name] = eval(value, {}, variables)
                except Exception:
                    variables[var_name] = value.strip('"')
            else:
                print_func(f"[Syntax Error] Invalid Let statement: {line}")

        # 🔁 Repeat loop
        elif line.startswith("Repeat "):
            match = re.match(r"Repeat (\d+) Times:", line)
            if match:
                count = int(match.group(1))
                block_lines = []
                i += 1
                while i < total_lines and lines[i].startswith("    "):
                    block_lines.append(lines[i][4:])
                    i += 1
                for _ in range(count):
                    run_kuromi_code("\n".join(block_lines), print_func, debug_mode)
                continue
            else:
                print_func(f"[Syntax Error] Invalid Repeat syntax: {line}")

        # ⏳ Wait
        elif line.startswith("Wait "):
            match = re.match(r"Wait (\d+)", line)
            if match:
                time.sleep(int(match.group(1)) / 1000)  # Convert ms to seconds
            else:
                print_func(f"[Syntax Error] Invalid Wait syntax: {line}")

        # ❓ Unknown command
        else:
            print_func(f"[Unknown Command] {line}")

        i += 1

    # Update GUI in debug mode, run mainloop in build mode
    if debug_mode and root is not None:
        root.update()
    elif root is not None:
        root.mainloop()