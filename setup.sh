#!/bin/bash

# Move pre-built libs into the correct folder
echo "Moving pre-built libraries into the lib folder...."
cp -r prebuilt-libs/ Source\ Code\ Hololive\ EN\ Myth/lib

echo "Setup complete!"
echo "Now run the following commands to build the executable:"
echo "cd Source\ Code\ Hololive\ EN\ Myth"
echo "ant"
echo ""
echo ""

