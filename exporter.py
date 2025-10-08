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
    """
    Find the real Python interpreter, not the bundled exe.
    """
    # If running from source, sys.executable is fine
    if not getattr(sys, 'frozen', False):
        return sys.executable
    
    # If bundled, we need to find python.exe
    # Try common locations
    possible_paths = [
        'python.exe',  # In PATH
        'python3.exe',
        r'C:\Python311\python.exe',
        r'C:\Python310\python.exe',
        r'C:\Python39\python.exe',
        r'C:\Python38\python.exe',
        r'C:\Python37\python.exe',
        os.path.join(os.environ.get('LOCALAPPDATA', ''), 'Programs', 'Python', 'Python311', 'python.exe'),
        os.path.join(os.environ.get('LOCALAPPDATA', ''), 'Programs', 'Python', 'Python310', 'python.exe'),
    ]
    
    # Try to find python in PATH first
    try:
        result = subprocess.run(['python', '--version'], 
                              capture_output=True, text=True, timeout=5)
        if result.returncode == 0:
            return 'python'
    except:
        pass
    
    # Check each possible path
    for path in possible_paths:
        try:
            if os.path.exists(path):
                result = subprocess.run([path, '--version'], 
                                      capture_output=True, text=True, timeout=5)
                if result.returncode == 0:
                    return path
        except:
            continue
    
    return None

def get_interpreter_code():
    """
    Get the interpreter code, either from file or from the bundled module.
    """
    # First, try to import it as a module (works in both dev and bundled)
    try:
        import kuromi_interpreter
        import inspect
        return inspect.getsource(kuromi_interpreter)
    except:
        pass
    
    # Try to read from file (dev environment)
    try:
        with open("kuromi_interpreter.py", "r", encoding="utf-8") as f:
            return f.read()
    except FileNotFoundError:
        pass
    
    # Try to get from _MEIPASS (PyInstaller bundle)
    try:
        if hasattr(sys, '_MEIPASS'):
            interpreter_path = os.path.join(sys._MEIPASS, "kuromi_interpreter.py")
            with open(interpreter_path, "r", encoding="utf-8") as f:
                return f.read()
    except:
        pass
    
    return None

def export_game_to_exe(kuromi_code, output_name, assets_dir="assets"):
    """
    Export a Kuromi game to a standalone EXE.
    
    Args:
        kuromi_code: The .KUROMI game code as a string
        output_name: Name for the output EXE (without .exe extension)
        assets_dir: Path to assets folder
    
    Returns:
        tuple: (success: bool, exe_path: str, error_message: str)
    """
    
    # Find Python executable
    python_exe = find_python_executable()
    if python_exe is None:
        return False, None, "Could not find Python installation! Please ensure Python is installed and in your PATH."
    
    # Get the interpreter source code
    interpreter_code = get_interpreter_code()
    
    if interpreter_code is None:
        return False, None, "Could not find kuromi_interpreter.py! Make sure it's in the same folder."
    
    # Find assets directory
    if hasattr(sys, '_MEIPASS'):
        # Running as bundled app
        bundled_assets = os.path.join(sys._MEIPASS, "assets")
        if os.path.exists(bundled_assets):
            assets_dir = bundled_assets
    
    # Check if assets exist
    if not os.path.exists(assets_dir):
        # Try to find assets in common locations
        possible_locations = [
            "assets",
            os.path.join(os.getcwd(), "assets"),
            os.path.join(os.path.dirname(sys.argv[0]), "assets")
        ]
        
        for location in possible_locations:
            if os.path.exists(location):
                assets_dir = location
                break
    
    # Create temporary directory for build
    temp_dir = tempfile.mkdtemp()
    src_path = os.path.join(temp_dir, f"game_{output_name}.py")
    
    # Escape the user's game code
    escaped_code = kuromi_code.replace("\\", "\\\\").replace('"""', '\\"\\"\\"')
    
    # Generate standalone game file
    standalone_game = f'''{interpreter_code}

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
    
    # Write the standalone game file
    try:
        with open(src_path, "w", encoding="utf-8") as f:
            f.write(standalone_game)
    except Exception as e:
        shutil.rmtree(temp_dir, ignore_errors=True)
        return False, None, f"Could not write temporary game file: {e}"
    
    # Set up output directory
    builds_dir = os.path.join(os.getcwd(), "dist", "builds")
    try:
        os.makedirs(builds_dir, exist_ok=True)
    except Exception as e:
        shutil.rmtree(temp_dir, ignore_errors=True)
        return False, None, f"Could not create builds directory: {e}"
    
    # Set up build command
    spec_dir = os.path.join(temp_dir, "spec")
    os.makedirs(spec_dir, exist_ok=True)
    
    # Create tkinter hook file
    hook_dir = os.path.join(temp_dir, "hooks")
    os.makedirs(hook_dir, exist_ok=True)
    hook_file = os.path.join(hook_dir, "hook-tkinter.py")
    
    hook_content = """# PyInstaller hook for tkinter
from PyInstaller.utils.hooks import collect_data_files, collect_submodules

datas = collect_data_files('tkinter')
hiddenimports = collect_submodules('tkinter')
hiddenimports += ['_tkinter']
"""
    
    with open(hook_file, "w") as f:
        f.write(hook_content)
    
    # Build the PyInstaller command
    cmd = [
        python_exe,
        "-m", "PyInstaller",
        "--onefile",
        "--noconsole",
        "--clean",
        "--noconfirm",
        "--name", output_name,
        "--distpath", builds_dir,
        "--workpath", os.path.join(temp_dir, "build"),
        "--specpath", spec_dir,
        # Add runtime hooks for tkinter
        "--runtime-tmpdir", "."
    ]
    
    # Add hidden imports
    cmd.extend(["--hidden-import", "pygame"])
    cmd.extend(["--hidden-import", "tkinter"])
    cmd.extend(["--hidden-import", "pygame.mixer"])
    cmd.extend(["--hidden-import", "_tkinter"])
    
    # Collect all dependencies - CRITICAL for tkinter to work
    cmd.extend(["--collect-all", "pygame"])
    cmd.extend(["--collect-all", "tkinter"])
    cmd.extend(["--collect-data", "tkinter"])
    
    # Add icon if exists
    icon_file = os.path.join(assets_dir, "icon.ico")
    if os.path.exists(icon_file):
        cmd.extend(["--icon", icon_file])
    
    # Add assets
    separator = ";" if sys.platform.startswith("win") else ":"
    assets_to_bundle = ["icon.ico", "splash_bg.png", "startup.wav"]
    
    for asset_name in assets_to_bundle:
        asset_path = os.path.join(assets_dir, asset_name)
        if os.path.exists(asset_path):
            abs_asset = os.path.abspath(asset_path)
            cmd.extend(["--add-data", f"{abs_asset}{separator}assets"])
    
    cmd.append(src_path)
    
    # Run PyInstaller
    try:
        # Run with shell to avoid console window
        startupinfo = None
        if sys.platform.startswith('win'):
            startupinfo = subprocess.STARTUPINFO()
            startupinfo.dwFlags |= subprocess.STARTF_USESHOWWINDOW
            startupinfo.wShowWindow = subprocess.SW_HIDE
        
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
            # Copy assets folder next to EXE for easy access
            final_assets = os.path.join(builds_dir, "assets")
            if not os.path.exists(final_assets) and os.path.exists(assets_dir):
                try:
                    shutil.copytree(assets_dir, final_assets)
                except:
                    pass
            
            # Clean up temp directory
            shutil.rmtree(temp_dir, ignore_errors=True)
            
            return True, exe_path, None
        else:
            # Build failed
            error_lines = []
            
            if result.stderr:
                # Parse PyInstaller errors
                for line in result.stderr.split('\n'):
                    if 'error' in line.lower() or 'failed' in line.lower():
                        error_lines.append(line.strip())
            
            if error_lines:
                error_msg = "PyInstaller errors:\n" + "\n".join(error_lines[:10])
            else:
                error_msg = f"Build failed. PyInstaller output:\n{result.stderr[:500]}"
            
            # Clean up temp directory
            shutil.rmtree(temp_dir, ignore_errors=True)
            
            return False, None, error_msg
            
    except subprocess.TimeoutExpired:
        shutil.rmtree(temp_dir, ignore_errors=True)
        return False, None, "Build timed out (took longer than 5 minutes)"
    except FileNotFoundError:
        shutil.rmtree(temp_dir, ignore_errors=True)
        return False, None, f"PyInstaller not found! Run this command first:\n{python_exe} -m pip install pyinstaller"
    except Exception as e:
        shutil.rmtree(temp_dir, ignore_errors=True)
        return False, None, f"Build error: {str(e)}"


if __name__ == "__main__":
    # Test export
    test_code = """Kuromi <Start>
ShowText "Hello from exported game!" 400 300
"""
    
    print("Testing exporter...")
    success, exe_path, error = export_game_to_exe(test_code, "TestGame")
    
    if success:
        print(f"✓ Export successful: {exe_path}")
    else:
        print(f"✗ Export failed: {error}")