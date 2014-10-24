#
# Copyright 2009 Cedric Priscal
# 
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
# http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License. 
#

LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

OPENCV_LIB_TYPE        := STATIC
OPENCV_INSTALL_MODULES := on
OPENCV_CAMERA_MODULES  := off

include /home/helios/Dev/OpenCV-2.4.8.2-Tegra-sdk/sdk/native/jni/OpenCV-tegra3.mk

LOCAL_MODULE    := object_tracking
LOCAL_SRC_FILES := jni_prog_00.cpp
LOCAL_SRC_FILES += SerialPort.c
LOCAL_LDLIBS +=  -llog -ldl

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

TARGET_PLATFORM := android-3
LOCAL_MODULE    := serial_port
LOCAL_SRC_FILES := SerialPort.c
LOCAL_LDLIBS    := -llog

include $(BUILD_SHARED_LIBRARY)
