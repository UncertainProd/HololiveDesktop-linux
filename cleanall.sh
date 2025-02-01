#!/bin/bash

rm -rf jna_3_4_0_src
rm -rf nimrodlf-src-1.2d
rm -rf ./Source Code Hololive EN Myth/target/
rm -rf ./Source Code Hololive EN Myth/lib/*

#        /-------------------1---------------------/-------------------2------------------------/
sed -i 's/..\/jna_3_4_0_src\/contrib\/platform\/src/\/opt\/jna_3_4_0_src\/contrib\/platform\/src/' Source\ Code\ Hololive\ EN\ Myth/build.xml
sed -i 's/..\/netbeans_awtextras_src/\/opt\/netbeans_awtextras_src/' Source\ Code\ Hololive\ EN\ Myth/build.xml

