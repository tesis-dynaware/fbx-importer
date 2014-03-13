# Source and build dirs (relative to this makefile)
SRC_DIR       = src/main/cpp/
BUILD_DIR     = build/
OBJECTS_DIR   = $(BUILD_DIR)objects/
LIBS_DIR      = $(BUILD_DIR)libs/

# Compiler (must run vcvarsx86_amd64.bat before running make)
CC            = cl
LINK          = link
CC_FLAGS      = /c /MT "/I$(FBX_SDK_HOME)include" "/I$(JAVA_HOME)/include" "/I$(JAVA_HOME)/include/win32" /nologo
LINK_FLAGS    = /DLL /nologo
LINK_FLAGS_32 = /libpath:"$(FBX_SDK_HOME)lib/vs2012/x86/release" /libpath:"$(WIN_SDK_HOME)Lib" AdvAPI32.lib libfbxsdk-mt.lib
LINK_FLAGS_64 = /libpath:"$(FBX_SDK_HOME)lib/vs2012/x64/release" /libpath:"$(WIN_SDK_HOME)Lib/x64" AdvAPI32.lib libfbxsdk-mt.lib

ifeq ($(OS_ARCH),x86_amd64)
	LINK_FLAGS += $(LINK_FLAGS_64)
else
	LINK_FLAGS += $(LINK_FLAGS_32)
endif

all: jfbxlib

jfbxlib: $(SRC_DIR)JFbxLib.cpp

	test -d $(BUILD_DIR) || mkdir $(BUILD_DIR)
	test -d $(LIBS_DIR)  || mkdir $(LIBS_DIR)
	test -d $(OBJECTS_DIR)  || mkdir $(OBJECTS_DIR)
			
	$(CC) $(CC_FLAGS) "/Fo$(OBJECTS_DIR)JFbxLib.obj" "$(SRC_DIR)JFbxLib.cpp"
	
	$(LINK) $(LINK_FLAGS) /OUT:"$(LIBS_DIR)jfbxlib.dll" "$(OBJECTS_DIR)JFbxLib.obj" 

	-rm $(LIBS_DIR)jfbxlib.exp $(LIBS_DIR)jfbxlib.lib 

clean:
	-rm $(LIBS_DIR)jfbxlib.dll