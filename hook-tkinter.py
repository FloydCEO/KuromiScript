# hook-tkinter.py
# PyInstaller hook to properly bundle tkinter

from PyInstaller.utils.hooks import collect_data_files, collect_submodules

datas = collect_data_files('tkinter')
hiddenimports = collect_submodules('tkinter')
hiddenimports += ['_tkinter']