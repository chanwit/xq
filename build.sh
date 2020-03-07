./gradlew clean shadowJar
rm -rf bundle-linux/jre
rm -rf bundle-mac/jre
jdk-13.0.2+8-linux/bin/jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.desktop,java.prefs,java.sql --output bundle-linux/jre
jdk-13.0.2+8-linux/bin/jlink --module-path=jdk-13.0.2+8-mac/Contents/Home/jmods --no-header-files --no-man-pages --compress=2 --add-modules java.base,java.desktop,java.prefs,java.sql --output bundle-mac/jre

cp build/libs/xq-1.0-SNAPSHOT-all.jar bundle-linux/xq.jar
cp build/libs/xq-1.0-SNAPSHOT-all.jar bundle-mac/xq.jar

mkdir -p bundle-linux/jre/lib/compressedrefs
cp jdk-13.0.2+8-linux/lib/compressedrefs/libj9shr29.so bundle-linux/jre/lib/compressedrefs/libj9shr29.so

./warp-packer --arch linux-x64 --input_dir bundle-linux --exec run.sh --output xq-linux  && cp xq-linux ~/.local/bin/xq
./warp-packer --arch macos-x64 --input_dir bundle-mac   --exec run.sh --output xq-darwin