ANDROID_SOURCE = \
	$(shell find app -not -path 'app/build/*' -not -path 'app/apps/*' -not -path 'app/src/main/assets/*' -not -path 'app/src/main/res/mipmap-hdpi/app_icon.png' -type f) \
	build.gradle gradle.properties settings.gradle \
	$(NULL)

JVM_ENV = ./jdk/jdk-11.0.20+8/bin/java
JAVE_HOME = ./jdk/jdk-11.0.20+8;
${JVM_ENV}:
	sh download_jdk.sh


./ad-filter/build/outputs/aar/ad-filter-release.aar: ${JVM_ENV} ./ad-filter/src/main/* ./ad-filter/build.gradle ./ad-filter/proguard-rules.pro
	export JAVA_HOME=$(JAVE_HOME) ./gradlew  :ad-filter:assembleRelease


./adblock-client/build/outputs/apk/adblock-client-release.aar: ${JVM_ENV} ./adblock-client/src/main/* ./adblock-client/build.gradle ./adblock-client/proguard-rules.pro
	export JAVA_HOME=$(JAVE_HOME) ./gradlew  :adblock-client:assembleRelease

clean: ${JVM_ENV}
	export JAVA_HOME=$(JAVE_HOME) ./gradlew clean



ad-filter: ./ad-filter/build/outputs/aar/ad-filter-release.aar

adblock-client: ./adblock-client/build/outputs/apk/adblock-client-release.aar

all: ad-filter adblock-client

# 发布到本地仓库
publish: ad-filter adblock-client
	./gradlew publish




