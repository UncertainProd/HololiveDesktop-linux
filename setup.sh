#!/bin/bash

# pull jna version 3.4.0 (git submodules don't work with tags, but git clone does :/)
git clone --depth 1 --branch 3.4.0 https://github.com/java-native-access/jna.git jna_3_4_0_src

# cloning this cuz there's the awtextras package with AbosulteLayout.java inside it
git clone --depth 1 https://github.com/apache/netbeans.git

# the nimrod library does not exist on git (afaik), but we download it in zip form like so:
wget https://nilogonzalez.es/nimrodlf/data/nimrodlf-src.zip
unzip nimrodlf-src.zip -d nimrodlf-src
rm -rf nimrodlf-src.zip

