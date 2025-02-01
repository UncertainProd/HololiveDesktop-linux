#!/bin/bash

echo "Pulling JNA version 3.4.0 from git... (from: https://github.com/java-native-access/jna.git)"
# pull jna version 3.4.0 (git submodules don't work with tags, but git clone does :/)
git clone --depth 1 --branch 3.4.0 https://github.com/java-native-access/jna.git jna_3_4_0_src

echo "Downloading and extracting nimrod from https://nilogonzalez.es/nimrodlf/data/nimrodlf-src.zip ..."
# the nimrod library does not exist on git (afaik), but we download it in zip form like so:
wget https://nilogonzalez.es/nimrodlf/data/nimrodlf-src.zip
unzip nimrodlf-src.zip
rm -rf nimrodlf-src.zip

# patch jna(...).WindowUtils.java to avoid a warning
echo "Patching WindowUtils.java from JNA ..."
patch ./jna_3_4_0_src/contrib/platform/src/com/sun/jna/platform/WindowUtils.java jna_patch.patch

echo "Setup complete. You should be ready to build now"
