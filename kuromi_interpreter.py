import re
import time
import tkinter as tk
import pygame
import sys
import os

# Store PhotoImage objects to prevent garbage collection
image_references = []

def resource_path(relative_path):
    """Get absolute path to resource, works for dev and for PyInstaller."""
    try:
        base_path = sys._MEIPASS
    except AttributeError:
        base_path = os.path.abspath(".")
    full_path = os.path.join(base_path, relative_path)
    return full_path

def run_kuromi_code(code, print_func=print, debug_mode=False, root=None, canvas=None):
    global image_references
    variables = {}
    
    # Only create window if not provided
    created_window = False
    if root is None:
        root = tk.Tk()
        root.title("KuromiCore Game")
        root.geometry("800x600")
        
        # Set icon with multiple attempts to ensure it sticks
        icon_path = resource_path("assets/icon.ico")
        try:
            root.iconbitmap(default=icon_path)
            root.iconbitmap(icon_path)
            root.update_idletasks()
        except (tk.TclError, Exception) as e:
            print_func(f"[Warning] Could not set icon: {e}")
        
        canvas = tk.Canvas(root, width=800, height=600, bg="black")
        canvas.pack()
        created_window = True

    lines = code.splitlines()
    i = 0
    total_lines = len(lines)

    while i < total_lines:
        line = lines[i].strip()
        if not line or line.startswith("#") or line.startswith("//"):
            i += 1
            continue

        # 🎮 Game start
        if line.startswith("Kuromi <Start>") or line.startswith("Kuromi <Game Start>"):
            print_func("✨ Kuromi Game Started ✨")
            canvas.configure(bg="black")
            canvas.delete("all")
            if debug_mode:
                root.update()

        # 🛑 Game close
        elif line.startswith("Kuromi <Close>") or line.startswith("Kuromi <Close Game>"):
            print_func("🛑 Kuromi Game Closing...")
            if root and debug_mode:
                try:
                    root.quit()
                except:
                    pass
            return root, canvas

        # 🖼️ Load Image
        elif line.startswith("LoadImage "):
            match = re.match(r'LoadImage "(\w+)" "([^"]+)"', line)
            if match:
                var_name, img_path = match.groups()
                full_img_path = resource_path(f"assets/{img_path}")
                try:
                    img = tk.PhotoImage(file=full_img_path)
                    variables[var_name] = img
                    image_references.append(img)
                    print_func(f"✓ Loaded image: {var_name}")
                except (tk.TclError, Exception) as e:
                    print_func(f"[Error] Failed to load image {img_path}: {e}")
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
                        print_func(f"✓ Displayed {var_name} at ({x}, {y})")
                        if debug_mode:
                            root.update()
                    except (tk.TclError, Exception) as e:
                        print_func(f"[Error] Failed to display image {var_name}: {e}")
                else:
                    print_func(f"[Error] Image '{var_name}' not found")
            else:
                print_func(f"[Syntax Error] Invalid DisplayImage syntax: {line}")

        # 📝 Show Text
        elif line.startswith("ShowText "):
            # Match pattern: ShowText (alignment) "text" x y OR ShowText "text" x y
            # First try with alignment
            match = re.match(r'ShowText\s+\((\w+)\)\s+"([^"]+)"\s+(\d+)\s+(\d+)', line)
            if match:
                alignment, text, x, y = match.groups()
                x, y = int(x), int(y)
                alignment = alignment.lower()
            else:
                # Try without alignment (default to left)
                match = re.match(r'ShowText\s+"([^"]+)"\s+(\d+)\s+(\d+)', line)
                if match:
                    text, x, y = match.groups()
                    x, y = int(x), int(y)
                    alignment = "left"
                else:
                    print_func(f"[Syntax Error] Invalid ShowText syntax: {line}")
                    i += 1
                    continue
            
            # Determine anchor based on alignment
            if alignment == "centered" or alignment == "center":
                anchor = "center"
            elif alignment == "right":
                anchor = "e"  # East = right aligned
            elif alignment == "left":
                anchor = "w"  # West = left aligned
            else:
                anchor = "w"  # Default to left
            
            if canvas:
                try:
                    canvas.create_text(x, y, text=text, fill="white", 
                                     font=("Arial", 16), anchor=anchor)
                    align_msg = f" ({alignment})" if alignment != "left" else ""
                    print_func(f"✓ Displayed text{align_msg} at ({x}, {y})")
                    if debug_mode:
                        root.update()
                except (tk.TclError, Exception) as e:
                    print_func(f"[Error] Failed to display text: {e}")
            else:
                print_func(f"[Error] No canvas for text display")

        # 🎵 Play Sound
        elif line.startswith("PlaySound "):
            match = re.match(r'PlaySound "([^"]+)"', line)
            if match:
                sound_path = resource_path(f"assets/{match.group(1)}")
                try:
                    if not pygame.mixer.get_init():
                        pygame.mixer.init()
                    pygame.mixer.music.load(sound_path)
                    pygame.mixer.music.play()
                    print_func(f"♪ Playing sound: {match.group(1)}")
                except (pygame.error, Exception) as e:
                    print_func(f"[Error] Failed to play sound: {e}")
            else:
                print_func(f"[Syntax Error] Invalid PlaySound syntax: {line}")

        # 🎨 Draw Rectangle
        elif line.startswith("DrawRectangle "):
            match = re.match(r'DrawRectangle\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+"([^"]+)"', line)
            if match:
                x, y, width, height, color = match.groups()
                x, y, width, height = int(x), int(y), int(width), int(height)
                if canvas:
                    try:
                        canvas.create_rectangle(x, y, x + width, y + height, 
                                              fill=color, outline=color)
                        print_func(f"✓ Drew rectangle at ({x}, {y}) - {width}x{height} - {color}")
                        if debug_mode:
                            root.update()
                    except (tk.TclError, Exception) as e:
                        print_func(f"[Error] Failed to draw rectangle: {e}")
                else:
                    print_func(f"[Error] No canvas for drawing")
            else:
                print_func(f"[Syntax Error] Invalid DrawRectangle syntax: {line}")

        # 🎨 Draw Circle
        elif line.startswith("DrawCircle "):
            match = re.match(r'DrawCircle\s+(\d+)\s+(\d+)\s+(\d+)\s+"([^"]+)"', line)
            if match:
                x, y, radius, color = match.groups()
                x, y, radius = int(x), int(y), int(radius)
                if canvas:
                    try:
                        # Canvas draws ovals with bounding box
                        canvas.create_oval(x - radius, y - radius, 
                                         x + radius, y + radius, 
                                         fill=color, outline=color)
                        print_func(f"✓ Drew circle at ({x}, {y}) - radius {radius} - {color}")
                        if debug_mode:
                            root.update()
                    except (tk.TclError, Exception) as e:
                        print_func(f"[Error] Failed to draw circle: {e}")
                else:
                    print_func(f"[Error] No canvas for drawing")
            else:
                print_func(f"[Syntax Error] Invalid DrawCircle syntax: {line}")

        # 🎨 Draw Line
        elif line.startswith("DrawLine "):
            match = re.match(r'DrawLine\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+"([^"]+)"', line)
            if match:
                x1, y1, x2, y2, color = match.groups()
                x1, y1, x2, y2 = int(x1), int(y1), int(x2), int(y2)
                if canvas:
                    try:
                        canvas.create_line(x1, y1, x2, y2, fill=color, width=2)
                        print_func(f"✓ Drew line from ({x1}, {y1}) to ({x2}, {y2}) - {color}")
                        if debug_mode:
                            root.update()
                    except (tk.TclError, Exception) as e:
                        print_func(f"[Error] Failed to draw line: {e}")
                else:
                    print_func(f"[Error] No canvas for drawing")
            else:
                print_func(f"[Syntax Error] Invalid DrawLine syntax: {line}")

        # 🎨 Draw Triangle
        elif line.startswith("DrawTriangle "):
            match = re.match(r'DrawTriangle\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+(\d+)\s+"([^"]+)"', line)
            if match:
                x1, y1, x2, y2, x3, y3, color = match.groups()
                x1, y1, x2, y2, x3, y3 = int(x1), int(y1), int(x2), int(y2), int(x3), int(y3)
                if canvas:
                    try:
                        canvas.create_polygon(x1, y1, x2, y2, x3, y3, 
                                            fill=color, outline=color)
                        print_func(f"✓ Drew triangle - {color}")
                        if debug_mode:
                            root.update()
                    except (tk.TclError, Exception) as e:
                        print_func(f"[Error] Failed to draw triangle: {e}")
                else:
                    print_func(f"[Error] No canvas for drawing")
            else:
                print_func(f"[Syntax Error] Invalid DrawTriangle syntax: {line}")

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
                    print_func(f"[Error] Could not evaluate: {e}")

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
                    run_kuromi_code("\n".join(block_lines), print_func, debug_mode, root, canvas)
                continue
            else:
                print_func(f"[Syntax Error] Invalid Repeat syntax: {line}")

        # ⏳ Wait
        elif line.startswith("Wait "):
            match = re.match(r"Wait (\d+)", line)
            if match:
                wait_time = int(match.group(1)) / 1000
                time.sleep(wait_time)
                if debug_mode and root:
                    root.update()
            else:
                print_func(f"[Syntax Error] Invalid Wait syntax: {line}")

        # ❓ Unknown command
        else:
            print_func(f"[Unknown Command] {line}")

        i += 1

    # Only run mainloop if we created the window and in debug mode
    # Window stays open until "Kuromi <Close>" is called or manually closed
    if created_window and debug_mode:
        try:
            root.mainloop()
        except:
            pass  # Window was closed by Kuromi <Close> command
    
    return root, canvas