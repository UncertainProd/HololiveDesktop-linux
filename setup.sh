#!/bin/bash

# Move pre-built libs into the correct folder
echo "Moving pre-built libraries into the lib folder...."
cp -r prebuilt-libs/* Source\ Code\ Hololive\ EN\ Myth/lib/

echo "Fixing build.xml ..."
#        /-------------------1---------------------/-------------------2------------------------/
sed -i 's/\/opt\/jna_3_4_0_src\/contrib\/platform\/src/..\/jna_3_4_0_src\/contrib\/platform\/src/' Source\ Code\ Hololive\ EN\ Myth/build.xml
sed -i 's/\/opt\/netbeans_awtextras_src/..\/netbeans_awtextras_src/' Source\ Code\ Hololive\ EN\ Myth/build.xml

echo "Setup complete!"
echo "Now run the following commands to build the executable:"
echo "cd Source\ Code\ Hololive\ EN\ Myth"
echo "ant"
echo ""
echo ""
echo "In order to run the application run:"
echo "cd target/"
echo "# Unzip any of the zip files (except src.zip) like this:"
echo "unzip <name-of-the-zip> -d HololiveDesktopPet"
echo "cd HololiveDesktopPet/"
echo "java -jar HololiveEN\ Myth\ Shimeji-ee.jar"

