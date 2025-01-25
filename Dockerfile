# I used docker to compile the program in isolation from other random stuff on my system.
# This is probably a bit overkill, 
# and you can probably just install "apache ant" on your system and build this program.
# It'll probably be something like this (don't copy the backticks --> ``):
# `cd 'Source Code Hololive EN Myth'`
# `ant`
# (The code above is not tested, but it'll probably work :p)

# If you _do_ want to build this program from docker, then follow the instructions here to download docker: https://docs.docker.com/engine/install/
# Use the command `sudo docker build -t <tag-name> .` from the same directory as this file to start build process
# Then use `sudo docker run -it <tag-name> /bin/bash`. This will give you a bash shell inside the container,
# Now just run the command `ant` to build the whole project. (Don't close or exit bash yet)
# 
# Then, in a separate terminal, get the container id of the one you just used to build the program
# And then run `sudo docker cp <container-id>:/opt/ShimejiSourceCode/target <whatever-folder-you-want>` and go into that folder
# Then unzip "HololiveEN Myth Shimeji-ee_1.0.9_Professional.zip" into some folder
# Lastly, cd into that folder, then run `java -jar 'HololiveEN Myth Shimeji-ee.jar'`
# That should run the program :)

# This process is pretty long and complicated as of now, so I'll probably try simplifying it later :p

# FROM frekele/ant:1.10.3-jdk8
FROM frekele/ant:1.10.3-jdk8@sha256:2c67c175a3906a0879072dfc38e87d6364538dc73f1e7a5e456c672a8f2418b4

# Setup stuff
RUN sed -i s/deb.debian.org/archive.debian.org/g /etc/apt/sources.list
RUN sed -i 's|security.debian.org|archive.debian.org|g' /etc/apt/sources.list
RUN sed -i '/stretch-updates/d' /etc/apt/sources.list

RUN ["apt", "update"]
RUN ["apt", "install", "-y", "build-essential", "libx11-dev", "libxt-dev"]

# trying to build jna from source
ADD ./jna_3_4_0_src /opt/jna_3_4_0_src
ADD ./nimrodlf-src-1.2d /opt/nimrodlf-src-1.2d
ADD ./netbeans_awtextras_src /opt/netbeans_awtextras_src

# Compile the dependencies
# JNA:
WORKDIR /opt/jna_3_4_0_src
RUN ["ant", "dist"]

# Nimrod:
WORKDIR /opt/nimrodlf-src-1.2d
RUN ant

# make src directory
ADD ["./Source Code Hololive EN Myth/", "/opt/ShimejiSourceCode/"]

# move JNA
RUN ["mv", "/opt/jna_3_4_0_src/build-d64/jna.jar", "/opt/ShimejiSourceCode/lib/"]
# move Nimrod
RUN ["mv", "/opt/nimrodlf-src-1.2d/dist/nimrodlf.jar", "/opt/ShimejiSourceCode/lib/"]


WORKDIR /opt/ShimejiSourceCode

# run this in the container in bash
# CMD ["ant"]
