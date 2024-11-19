  
  
https://www.morling.dev/blog/building-openjdk-from-source-on-macos/



```bash
## 1
sdk install java 23.0.1-tem

## 2
git apply << EOF
--- a/make/autoconf/flags-cflags.m4
+++ b/make/autoconf/flags-cflags.m4
@@ -337,9 +337,9 @@ AC_DEFUN([FLAGS_SETUP_OPTIMIZATION],
       C_O_FLAG_HIGHEST="-O3 -finline-functions"
       C_O_FLAG_HI="-O3 -finline-functions"
     else
-      C_O_FLAG_HIGHEST_JVM="-O3"
-      C_O_FLAG_HIGHEST="-O3"
-      C_O_FLAG_HI="-O3"
+      C_O_FLAG_HIGHEST_JVM="-O1"
+      C_O_FLAG_HIGHEST="-O1"
+      C_O_FLAG_HI="-O1"
     fi
     C_O_FLAG_NORM="-O2"
     C_O_FLAG_DEBUG_JVM="-O0"
EOF

## 3. auto conf
brew install autoconf

```

## build

```bash
## clone
git clone https://git.openjdk.org/jdk
cd jdk

## run config
bash configure

## make image
make images

## verify
./build/macosx-aarch64-server-release/jdk/bin/java --version

```


## Log: configure

```bash
$ bash configure
configure: Configuration created at Tue Nov 19 17:17:44 CST 2024.
checking for basename... /usr/bin/basename
checking for dirname... /usr/bin/dirname
checking for file... /usr/bin/file
checking for ldd... no
checking for echo... echo [builtin]
checking for tr... /usr/bin/tr
checking for uname... /usr/bin/uname
checking for wc... /usr/bin/wc
checking for grep that handles long lines and -e... /usr/bin/grep
checking for egrep... /usr/bin/grep -E
checking for a sed that does not truncate output... /usr/bin/sed
checking for locale... /usr/bin/locale
checking for cygpath... [not found]
checking for wslpath... [not found]
checking for cmd.exe... [not found]
checking build system type... aarch64-apple-darwin24.1.0
checking host system type... aarch64-apple-darwin24.1.0
checking target system type... aarch64-apple-darwin24.1.0
checking openjdk-build os-cpu... macosx-aarch64
checking openjdk-target os-cpu... macosx-aarch64

... 

config.status: creating /Users/rickhwang/Repos/oss/jdk/build/macosx-aarch64-server-release/bootcycle-spec.gmk
config.status: creating /Users/rickhwang/Repos/oss/jdk/build/macosx-aarch64-server-release/buildjdk-spec.gmk
config.status: creating /Users/rickhwang/Repos/oss/jdk/build/macosx-aarch64-server-release/compare.sh
config.status: creating /Users/rickhwang/Repos/oss/jdk/build/macosx-aarch64-server-release/Makefile

====================================================
The existing configuration has been successfully updated in
/Users/rickhwang/Repos/oss/jdk/build/macosx-aarch64-server-release
using default settings.

Configuration summary:
* Name:           macosx-aarch64-server-release
* Debug level:    release
* HS debug level: product
* JVM variants:   server
* JVM features:   server: 'cds compiler1 compiler2 dtrace epsilongc g1gc jfr jni-check jvmci jvmti management parallelgc serialgc services shenandoahgc vm-structs zgc'
* OpenJDK target: OS: macosx, CPU architecture: aarch64, address length: 64
* Version string: 24-internal-adhoc.rickhwang.jdk (24-internal)
* Source date:    1732007868 (2024-11-19T09:17:48Z)

Tools summary:
* Boot JDK:       openjdk version "23.0.1" 2024-10-15 OpenJDK Runtime Environment Temurin-23.0.1+11 (build 23.0.1+11) OpenJDK 64-Bit Server VM Temurin-23.0.1+11 (build 23.0.1+11, mixed mode, sharing) (at /Users/rickhwang/.sdkman/candidates/java/current)
* Toolchain:      clang (clang/LLVM from Xcode 16.1)
* Sysroot:        /Applications/Xcode.app/Contents/Developer/Platforms/MacOSX.platform/Developer/SDKs/MacOSX15.1.sdk
* C Compiler:     Version 16.0.0 (at /usr/bin/clang)
* C++ Compiler:   Version 16.0.0 (at /usr/bin/clang++)

Build performance summary:
* Build jobs:     10
* Memory limit:   65536 MB

WARNING: The result of this configuration has overridden an older
configuration. You *should* run 'make clean' to make sure you get a
proper build. Failure to do so might result in strange build problems.

The following warnings were produced. Repeated here for convenience:
WARNING: C.UTF-8 locale not found, using C locale

```



## build

```bash
$ make images
Building target 'images' in configuration 'macosx-aarch64-server-release'
Updating hotspot/variant-server/tools/adlc/adlc due to 1 file(s)
Compiling up to 213 files for BUILD_jdk.javadoc.interim
Creating support/modules_libs/java.base/jrt-fs.jar
Compiling up to 3476 files for java.base
Updating support/src.zip
Updating support/modules_libs/java.base/server/libjvm.dylib due to 50 file(s)
Compiling up to 10 files for java.instrument
Compiling up to 15 files for java.scripting
Compiling up to 146 files for jdk.charsets
Compiling up to 15 files for jdk.attach
Compiling up to 2743 files for java.desktop
Compiling up to 213 files for jdk.javadoc
Compiling up to 917 files for jdk.hotspot.agent

...

Creating support/demos/image/jfc/CodePointIM/CodePointIM.jar
Creating support/demos/image/jfc/FileChooserDemo/FileChooserDemo.jar
Creating support/demos/image/jfc/Font2DTest/Font2DTest.jar
Creating support/demos/image/jfc/J2Ddemo/J2Ddemo.jar
Creating support/demos/image/jfc/SwingSet2/SwingSet2.jar
Creating support/demos/image/jfc/Metalworks/Metalworks.jar
Creating support/demos/image/jfc/Notepad/Notepad.jar
Creating support/demos/image/jfc/Stylepad/Stylepad.jar
Creating support/demos/image/jfc/SampleTree/SampleTree.jar
Creating support/demos/image/jfc/TableExample/TableExample.jar
Creating support/demos/image/jfc/TransparentRuler/TransparentRuler.jar
Creating support/classlist.jar
Creating jdk.jlink.jmod
Creating java.base.jmod
Creating jdk image
Creating CDS archive for jdk image for server
Creating CDS-NOCOOPS archive for jdk image for server
Creating CDS-COH archive for jdk image for server
Creating CDS-NOCOOPS-COH archive for jdk image for server
Stopping javac server
Finished building target 'images' in configuration 'macosx-aarch64-server-release'

```



## verify

```bash
$ ./build/macosx-aarch64-server-release/jdk/bin/java --version
openjdk 24-internal 2025-03-18
OpenJDK Runtime Environment (build 24-internal-adhoc.rickhwang.jdk)
OpenJDK 64-Bit Server VM (build 24-internal-adhoc.rickhwang.jdk, mixed mode)
```