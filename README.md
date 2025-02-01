# A port of HololiveDesktop for linux
This is a port of [Hololive Desktop](https://github.com/NisenoShitsu/HololiveDesktop) by Niseno Shitsu (https://www.youtube.com/@nisenoshitsu) to linux. Parts of the code for this port are also taken from linux-shimeji (https://github.com/asdfman/linux-shimeji) but modified to fit this version.
It has been tested on linux-mint under Cinnamon using x11, although it _should_ work under [xwayland](https://www.linuxfromscratch.org/blfs/view/git/x/xwayland.html), if you are running wayland.

Make sure to use jdk-11 to run this program

This port is pretty much done, but is still a WIP for the most part. Some features like those which involve interacting with desktop windows are not implemented yet, but most of the features should work as of now.

Hololive Desktop pet is based on the [Shimeji-ee](https://kilkakon.com/shimeji) desktop pet program by Kilkakon.

## Getting the release version:
You can get them from the releases tab here on github. [Here](https://github.com/UncertainProd/HololiveDesktop-linux/releases)

## How to build from source:
In order to build from source, you will need Java (JDK and JRE) as well as Apache Ant installed (You'll probably have to install `git` too, if you don't have it). Now to set up the environment, run `setup.sh`. Now, just open the terminal inside the "Source Code Hololive EN Myth" folder and type in the command `ant` to compile the source files. The final builds will be in a folder called `target`, inside of which there are zip files like "HololiveEN Myth Shimeji-ee_1.0.9_*.zip". Those contain the final build. Unzip any of those (except `src.zip`) into a folder and run the application by running the command `java -jar HololiveEN\ Myth\ Shimeji-ee.jar` in a terminal in that folder.

# Original README:

# HololiveDesktop
Hololive Members inside your computer doing their stuff while you are working or doing your computer stuff =w=
==============================================
HOLOLIVEDESKTOP PET or Shimeji-ee by Niseno Shitsu
==============================================

HOW TO USE:
-----------------------------------
- Make sure Java is already installed.
- EXTRACT CONTENTS OF THIS ARCHIVE TO A FOLDER.
- Simply run Shimeji-ee.jar, Shimeji-ee.exe or Shimeji-ee.bat
- To look for options, Right-click the black colored hololive play icon as shown in the screenshots below in taskbar sytem stray
- You can enable/disable certain begaviors of each Hololive Talents or Choose and Summon one of them Hololive Members to your computer

-----------------------------------
SCREENSHOTS:
-----------------------------------
<img src="https://i.imgur.com/gkfcNGY.png" alt="HololiveDesktop Myth" width="1000">

-----------------------------------
WHERE TO DOWNLOAD?:
-----------------------------------
- Click on the "Releases" on the "About" section of this page located at the right side 
- Follow the instructions inside the "Releases" Page

-----------------------------------
SUPPORT THIS PROJECT BY
-----------------------------------
If you want to support me in making this project I will be glad to and very thankful so feel free to join me in my [Patreon] https://www.patreon.com/nisenoshitsu?utm_campaign=creatorshare_creator!
Or You can Donate On my [Ko-Fi] https://ko-fi.com/niseno_shitsu

ENJOY HOLOLIVE FANS!
