#exporter.py

"""
KuromiCore Exporter - Builds standalone EXE files from .KUROMI games
"""
import os
import sys
import tempfile
import subprocess
import shutil

def find_python_executable():
    """Find the real Python interpreter, not the bundled exe."""
    if not getattr(sys, 'frozen', False):
        return sys.executable
    
    try:
        result = subprocess.run(['python', '--version'], 
                              capture_output=True, text=True, timeout=5)
        if result.returncode == 0:
            return 'python'
    except:
        pass
    
    possible_paths = [
        'python.exe',
        r'C:\Python311\python.exe',
        r'C:\Python310\python.exe',
        r'C:\Python39\python.exe',
    ]
    
    for path in possible_paths:
        try:
            if os.path.exists(path) or path == 'python.exe':
                result = subprocess.run([path, '--version'], 
                                      capture_output=True, text=True, timeout=5)
                if result.returncode == 0:
                    return path
        except:
            continue
    
    return None

def get_interpreter_code():
    """Get the interpreter code from various sources."""
    try:
        import kuromi_interpreter
        import inspect
        return inspect.getsource(kuromi_interpreter)
    except:
        pass
    
    try:
        with open("kuromi_interpreter.py", "r", encoding="utf-8") as f:
            return f.read()
    except:
        pass
    
    try:
        if hasattr(sys, '_MEIPASS'):
            interpreter_path = os.path.join(sys._MEIPASS, "kuromi_interpreter.py")
            with open(interpreter_path, "r", encoding="utf-8") as f:
                return f.read()
    except:
        pass
    
    return None

def export_game_to_exe(kuromi_code, output_name, assets_dir="assets"):
    """Export a Kuromi game to a standalone EXE."""
    
    python_exe = find_python_executable()
    if python_exe is None:
        return False, None, "Could not find Python! Ensure Python is installed and in PATH."
    
    interpreter_code = get_interpreter_code()
    if interpreter_code is None:
        return False, None, "Could not find kuromi_interpreter.py!"
    
    # Find assets
    if hasattr(sys, '_MEIPASS'):
        bundled_assets = os.path.join(sys._MEIPASS, "assets")
        if os.path.exists(bundled_assets):
            assets_dir = bundled_assets
    
    if not os.path.exists(assets_dir):
        for location in ["assets", os.path.join(os.getcwd(), "assets")]:
            if os.path.exists(location):
                assets_dir = location
                break
    
    # Create temp directory
    temp_dir = tempfile.mkdtemp()
    src_path = os.path.join(temp_dir, f"game_{output_name}.py")
    spec_path = os.path.join(temp_dir, f"{output_name}.spec")
    
    # Escape game code
    escaped_code = kuromi_code.replace("\\", "\\\\").replace('"""', '\\"\\"\\"')
    
    # Generate standalone game file with tkinter fix
    standalone_game = f'''{interpreter_code}

# Standalone KuromiCore Game
import tkinter as tk
import time
import os
import sys

# Fix for PyInstaller tkinter issues
if getattr(sys, 'frozen', False):
    os.environ['TCL_LIBRARY'] = os.path.join(sys._MEIPASS, 'tcl')
    os.environ['TK_LIBRARY'] = os.path.join(sys._MEIPASS, 'tk')

if __name__ == "__main__":
    root = tk.Tk()
    root.title("KuromiCore Game")
    root.geometry("800x600")
    
    try:
        root.iconbitmap(resource_path("assets/icon.ico"))
    except:
        pass
    
    canvas = tk.Canvas(root, width=800, height=600, bg="#4B0082")
    canvas.pack()
    
    canvas.create_text(400, 300, text="Made with KuromiCore", 
                      fill="white", font=("Arial", 24, "bold"))
    root.update()
    time.sleep(2)
    
    canvas.delete("all")
    canvas.configure(bg="black")
    
    game_code = """{escaped_code}"""
    
    run_kuromi_code(game_code, debug_mode=True, root=root, canvas=canvas)
'''
    
    try:
        with open(src_path, "w", encoding="utf-8") as f:
            f.write(standalone_game)
    except Exception as e:
        shutil.rmtree(temp_dir, ignore_errors=True)
        return False, None, f"Could not write game file: {e}"
    
    # Create output directory
    builds_dir = os.path.join(os.getcwd(), "dist", "builds")
    os.makedirs(builds_dir, exist_ok=True)
    
    # Get Python's tcl/tk directories
    try:
        import tkinter
        tcl_dir = os.path.join(os.path.dirname(tkinter.__file__), 'tcl')
        tk_dir = os.path.join(os.path.dirname(tkinter.__file__), 'tk')
        
        # Alternative locations
        if not os.path.exists(tcl_dir):
            python_root = os.path.dirname(sys.executable if not getattr(sys, 'frozen', False) else python_exe)
            tcl_dir = os.path.join(python_root, 'tcl')
        if not os.path.exists(tk_dir):
            python_root = os.path.dirname(sys.executable if not getattr(sys, 'frozen', False) else python_exe)
            tk_dir = os.path.join(python_root, 'tk')
    except:
        tcl_dir = None
        tk_dir = None
    
    # Create spec file with comprehensive tkinter support
    spec_content = f"""# -*- mode: python ; coding: utf-8 -*-
import os

block_cipher = None

a = Analysis(
    [r'{src_path.replace(chr(92), chr(92)*2)}'],
    pathex=[],
    binaries=[],
    datas=[],
    hiddenimports=['pygame', 'pygame.mixer', '_tkinter', 'tkinter', 'tkinter.ttk', 'tkinter.constants', 'tkinter.filedialog'],
    hookspath=[],
    hooksconfig={{}},
    runtime_hooks=[],
    excludes=[],
    win_no_prefer_redirects=False,
    win_private_assemblies=False,
    cipher=block_cipher,
    noarchive=False,
)

# Add TCL/TK data files
"""
    
    # Add tcl/tk directories to spec
    if tcl_dir and os.path.exists(tcl_dir):
        spec_content += f"""
import glob
tcl_files = []
for root, dirs, files in os.walk(r'{tcl_dir.replace(chr(92), chr(92)*2)}'):
    for file in files:
        file_path = os.path.join(root, file)
        arc_path = os.path.join('tcl', os.path.relpath(file_path, r'{tcl_dir.replace(chr(92), chr(92)*2)}'))
        tcl_files.append((arc_path, file_path, 'DATA'))
a.datas += tcl_files
"""
    
    if tk_dir and os.path.exists(tk_dir):
        spec_content += f"""
tk_files = []
for root, dirs, files in os.walk(r'{tk_dir.replace(chr(92), chr(92)*2)}'):
    for file in files:
        file_path = os.path.join(root, file)
        arc_path = os.path.join('tk', os.path.relpath(file_path, r'{tk_dir.replace(chr(92), chr(92)*2)}'))
        tk_files.append((arc_path, file_path, 'DATA'))
a.datas += tk_files
"""
    
    # Add assets to spec
    assets_to_bundle = ["icon.ico", "splash_bg.png", "startup.wav"]
    
    spec_content += "\n# Add game assets\n"
    for asset_name in assets_to_bundle:
        asset_path = os.path.join(assets_dir, asset_name)
        if os.path.exists(asset_path):
            abs_asset = os.path.abspath(asset_path).replace("\\", "\\\\")
            spec_content += f"a.datas += [(r'assets\\{asset_name}', r'{abs_asset}', 'DATA')]\n"
    
    spec_content += f"""
pyz = PYZ(a.pure, a.zipped_data, cipher=block_cipher)

exe = EXE(
    pyz,
    a.scripts,
"""
    
    # Add icon to exe section
    icon_file = os.path.join(assets_dir, "icon.ico")
    if os.path.exists(icon_file):
        abs_icon = os.path.abspath(icon_file).replace("\\", "\\\\")
        spec_content += f"    icon=r'{abs_icon}',\n"
    
    spec_content += f"""    exclude_binaries=True,
    name='{output_name}',
    debug=False,
    bootloader_ignore_signals=False,
    strip=False,
    upx=True,
    console=False,
    disable_windowed_traceback=False,
    argv_emulation=False,
    target_arch=None,
    codesign_identity=None,
    entitlements_file=None,
)

coll = COLLECT(
    exe,
    a.binaries,
    a.zipfiles,
    a.datas,
    strip=False,
    upx=True,
    upx_exclude=[],
    name='{output_name}',
)
"""
    
    # Write spec file
    with open(spec_path, "w") as f:
        f.write(spec_content)
    
    # Run PyInstaller with spec file
    try:
        startupinfo = None
        if sys.platform.startswith('win'):
            startupinfo = subprocess.STARTUPINFO()
            startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
            startupinfo.wShowWindow = subprocess.SW_HIDE
        
        cmd = [
            python_exe,
            "-m", "PyInstaller",
            "--clean",
            "--noconfirm",
            "--distpath", builds_dir,
            spec_path
        ]
        
        result = subprocess.run(
            cmd,
            capture_output=True,
            text=True,
            timeout=300,
            startupinfo=startupinfo,
            creationflags=subprocess.CREATE_NO_WINDOW if sys.platform.startswith('win') else 0
        )
        
        # Check if EXE was created
        exe_path = os.path.join(builds_dir, f"{output_name}.exe")
        
        if os.path.exists(exe_path):
            # Copy assets next to EXE
            final_assets = os.path.join(builds_dir, "assets")
            if not os.path.exists(final_assets) and os.path.exists(assets_dir):
                try:
                    shutil.copytree(assets_dir, final_assets)
                except:
                    pass
            
            shutil.rmtree(temp_dir, ignore_errors=True)
            return True, exe_path, None
        else:
            error_lines = []
            if result.stderr:
                for line in result.stderr.split('\n'):
                    if 'error' in line.lower() or 'failed' in line.lower():
                        error_lines.append(line.strip())
            
            if error_lines:
                error_msg = "Build errors:\n" + "\n".join(error_lines[:10])
            else:
                error_msg = f"Build failed:\n{result.stderr[:500]}"
            
            shutil.rmtree(temp_dir, ignore_errors=True)
            return False, None, error_msg
            
    except subprocess.TimeoutExpired:
        shutil.rmtree(temp_dir, ignore_errors=True)
        return False, None, "Build timed out (5 minutes)"
    except FileNotFoundError:
        shutil.rmtree(temp_dir, ignore_errors=True)
        return False, None, f"PyInstaller not found!\nRun: {python_exe} -m pip install pyinstaller"
    except Exception as e:
        shutil.rmtree(temp_dir, ignore_errors=True)
        return False, None, f"Build error: {str(e)}"


if __name__ == "__main__":
    test_code = """Kuromi <Start>
ShowText "Hello from exported game!" 400 300
"""
    
    print("Testing exporter...")
    success, exe_path, error = export_game_to_exe(test_code, "TestGame")
    
    if success:
        print(f"✓ Export successful: {exe_path}")
    else:
        print(f"✗ Export failed: {error}")