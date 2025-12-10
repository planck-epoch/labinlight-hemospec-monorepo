import sys

from cx_Freeze import setup, Executable
from Forms import __build_name__
from Forms import __version__


# base="Win32GUI" should be used only for Windows GUI app
base = "Win32GUI" if sys.platform == "win32" else None

executables = [Executable("ISC_PY_Program.py",
                          base=base,
                          target_name=__build_name__ + ".exe",
                          icon="ISC_Logo.ico",
                          copyright="Copyright (C) 2018 InnoSpectra Corporation",
                          )]

# Dependencies are automatically detected, but it might need fine-tuning.
build_exe_options = {
    "build_exe": "build/" + __build_name__ + "_v" + __version__,
    "includes": ["Forms.config_ui",
                 "Forms.devicelist",
                 "Forms.devices_ui",
                 "Forms.main_ui",
                 "Forms.mainwindow",
                 "Forms.scanconfigdialog",
                 "SDK.device",
                 "SDK.scan",
                 "SDK.scanconfig"
                 ],
    "include_files": ["SDK/hidapi.dll",
                      "SDK/iscpy.cp311-win32.pyd",
                      "SDK/iscpy.pyd",
                      "SDK/libdlpspec.dll",
                      "SDK/lmdfu.dll",
                      "SDK/lmusbdll.dll",
                      "ISC_Logo.ico"
                      ]
}

setup(
    name=__build_name__,
    version=__version__,
    description="Demonstration ISC Python SDK",
    options={"build_exe": build_exe_options},
    executables=executables
)
