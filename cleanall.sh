rm -rf jna_3_4_0_src
rm -rf nimrodlf-src-1.2d
rm -rf target
rm -rf ./Source Code Hololive EN Myth/lib/*
sed -i 's/<!-- <src path="..\/jna_3_4_0_src\/contrib\/platform\/src" \/> -->/<src path="..\/jna_3_4_0_src\/contrib\/platform\/src" \/>/' Source\ Code\ Hololive\ EN\ Myth/build.xml 
sed -i 's/<src path="\/opt\/jna_3_4_0_src\/contrib\/platform\/src" \/>/<!-- <src path="\/opt\/jna_3_4_0_src\/contrib\/platform\/src" \/> -->/' Source\ Code\ Hololive\ EN\ Myth/build.xml 

sed -i 's/<!-- <src path="..\/netbeans_awtextras_src" \/> -->/<src path="..\/netbeans_awtextras_src" \/>/' Source\ Code\ Hololive\ EN\ Myth/build.xml 
sed -i 's/<src path="\/opt\/netbeans_awtextras_src" \/>/<!-- <src path="\/opt\/netbeans_awtextras_src" \/> -->/' Source\ Code\ Hololive\ EN\ Myth/build.xml 

