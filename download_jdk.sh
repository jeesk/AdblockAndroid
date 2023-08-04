#!/bin/bash
java_path="./jdk/jdk-11.0.20+8/bin/java"
mkdir -p jdk
if [ -x "$java_path" ]; then
  echo "Java is already installed."
else
  # 下载 Microsoft JDK 11.0.20
  download_url="https://download.visualstudio.microsoft.com/download/pr/7a653ca9-494b-4be3-88cd-798fdcf45d03/28bcc66bd0385b880c069207aadb8adf/microsoft-jdk-11.0.20-linux-x64.tar.gz"
  echo "Downloading Microsoft JDK 11.0.20..."
  wget $download_url -O ./jdk/microsoft-jdk-11.0.20-linux-x64.tar.gz && pwd
  cd ./jdk && tar -xf microsoft-jdk-11.0.20-linux-x64.tar.gz && rm -rf microsoft-jdk-11.0.20-linux-x64.tar.gz
  echo "Java installation complete."
fi
