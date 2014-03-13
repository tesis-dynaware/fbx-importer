@echo off

if not defined JAVA_HOME (
	set "JAVA_HOME=C:/Program Files/Java/jdk_1.8.0/"
	@echo JAVA_HOME environment variable not found
)

if not defined WIN_SDK_HOME (
	set "WIN_SDK_HOME=C:/Program Files/Microsoft SDKs/Windows/v7.1/"
	@echo WIN_SDK_HOME environment variable not found
)

if not defined VSINSTALLDIR (
	set "VSINSTALLDIR=C:/Program Files (x86)/Microsoft Visual Studio 11.0/"
	@echo VSINSTALLDIR environment variable not found
)

if not defined FBX_SDK_HOME (
	set "FBX_SDK_HOME=C:/Program Files/Autodesk/FBX/FBX SDK/2014.1/"
	@echo FBX_SDK_HOME environment variable not found
)

@echo Setting environment variables for Visual C++, %OS_ARCH%
call "%VSINSTALLDIR%\VC\vcvarsall.bat" %OS_ARCH%

@echo Executing make
make CYGWIN=nodosfilewarning