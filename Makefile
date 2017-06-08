CMD = javac
LIB_DIR = lib/
OUT_DIR = out/
SOURCE_DIR = src/
MAIN = src/fr/jgetmove/jgetmove/Main.java
APP_NAME = jGetMove.jar
MANIFEST = Manifest.mf

jgetmove : jgetmove-class
	jar cvfm $(APP_NAME) $(MANIFEST) $(MAIN_CP) -C $(OUT_DIR) $(LIB_DIR)

jgetmove-class :
	mkdir -p $(OUT_DIR)
	$(CMD) -extdirs $(LIB_DIR) -sourcepath $(SOURCE_DIR) $(MAIN) -d $(OUT_DIR)

clean:
	rm -f $(APP_NAME)
	rm -rf $(OUT_DIR)
