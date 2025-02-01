#!/bin/bash

rm -rf ./Source Code Hololive EN Myth/target/
rm -rf ./Source Code Hololive EN Myth/lib/*

sed -i 's/\/opt\/jna_3_4_0_src\/contrib\/platform\/src/..\/jna_3_4_0_src\/contrib\/platform\/src/' Source\ Code\ Hololive\ EN\ Myth/build.xml
sed -i 's/\/opt\/netbeans_awtextras_src/..\/netbeans_awtextras_src/' Source\ Code\ Hololive\ EN\ Myth/build.xml

