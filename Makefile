.PHONY: build install release clean help

help:
	@echo "Available targets:"
	@echo "  build    - Build the debug APK"
	@echo "  install  - Install the debug APK to connected device"
	@echo "  release  - Build, install, and push tagged APK to phone downloads"
	@echo "  clean    - Clean the build directories"

build:
	./gradlew assembleDebug

install:
	adb install -r app/build/outputs/apk/debug/app-debug.apk

release:
	bash scripts/release.sh

clean:
	./gradlew clean
