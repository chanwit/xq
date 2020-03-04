./gradlew clean shadowJar
rm -rf bundle/jre
jdk-13.0.2+8/bin/jlink --no-header-files --no-man-pages --compress=2 --strip-debug --add-modules java.base,java.desktop,java.prefs,java.sql --output bundle/jre
cp build/libs/xq-1.0-SNAPSHOT-all.jar bundle/xq.jar
./warp-packer --arch linux-x64 --input_dir bundle --exec run.sh --output xq
