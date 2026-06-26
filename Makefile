# ============================================================================
# Makefile for Fruity Frank / Pac-Man project
# ============================================================================

# ----------------------------------------------------------------------------
# Tools
# ----------------------------------------------------------------------------
JAVAC      := javac
JAVA       := java
JAR        := jar
JAVACC     := javacc
RM         := rm -rf
MKDIR      := mkdir -p
CP         := cp

# ----------------------------------------------------------------------------
# Directories
# ----------------------------------------------------------------------------
SRC_ENGINE    := src-engine
SRC_FRANK     := src-Frank
SRC_PACMAN    := src-pacman
SRC_PARSER    := src-parser

CLASSES_DIR   := bin/classes
JAR_DIR       := bin
JAR_FILE      := $(JAR_DIR)/fruity-frank.jar

# ----------------------------------------------------------------------------
# Classpath & dependencies
# ----------------------------------------------------------------------------
GIVEN_JAR    := given.jar
LIB_DIR      := lib
JUNIT_API    := $(LIB_DIR)/junit-jupiter-api-6.1.0.jar

# Check if JUnit API is present
JUNIT_AVAILABLE := $(shell [ -f $(JUNIT_API) ] && echo yes)

ifeq ($(JUNIT_AVAILABLE),yes)
    # JUnit found: compile everything
    CLASSPATH   := $(GIVEN_JAR):$(JUNIT_API):$(LIB_DIR)/*
    FIND_DIRS   := $(SRC_ENGINE) $(SRC_FRANK) $(SRC_PACMAN) $(SRC_PARSER)
    SOURCES     := $(shell find $(FIND_DIRS) -name "*.java")
else
    # JUnit missing: exclude src-pacman and the test directory inside engine
    CLASSPATH   := $(GIVEN_JAR):$(LIB_DIR)/*
    FIND_DIRS   := $(SRC_ENGINE) $(SRC_FRANK) $(SRC_PARSER)
    SOURCES     := $(shell find $(FIND_DIRS) -name "*.java" | grep -v "$(SRC_ENGINE)/engine/tests/")
endif

# ----------------------------------------------------------------------------
# Parser generation (no stamp file – runs every time)
# ----------------------------------------------------------------------------
PARSER_DIR     := $(SRC_PARSER)/gal/parser
PARSER_JJ      := $(PARSER_DIR)/parser.jj

# ----------------------------------------------------------------------------
# Main class
# ----------------------------------------------------------------------------
MAIN_CLASS := frank.game.GameApp

# ----------------------------------------------------------------------------
# Targets
# ----------------------------------------------------------------------------

.PHONY: all parser compile jar run run-dev test clean help

all: jar

# ----------------------------------------------------------------------------
# Parser generation – runs javacc every time you compile
# ----------------------------------------------------------------------------
parser: $(PARSER_JJ)
	@echo "Generating parser with JavaCC..."
	$(JAVACC) -OUTPUT_DIRECTORY=$(PARSER_DIR) $(PARSER_JJ)

# ----------------------------------------------------------------------------
# Compilation (depends on parser being generated)
# ----------------------------------------------------------------------------
compile: parser $(CLASSES_DIR)
	@echo "Compiling sources..."
	$(JAVAC) -d $(CLASSES_DIR) -cp "$(CLASSPATH)" $(SOURCES)

$(CLASSES_DIR):
	$(MKDIR) $(CLASSES_DIR)

# ----------------------------------------------------------------------------
# JAR packaging
# ----------------------------------------------------------------------------
jar: compile
	@echo "Creating JAR file $(JAR_FILE)..."
	echo "Main-Class: $(MAIN_CLASS)" > $(CLASSES_DIR)/manifest.mf
	echo "Class-Path: $(GIVEN_JAR)" >> $(CLASSES_DIR)/manifest.mf
	$(JAR) cfm $(JAR_FILE) $(CLASSES_DIR)/manifest.mf -C $(CLASSES_DIR) .
	$(JAR) uf $(JAR_FILE) -C . sprites
	$(JAR) uf $(JAR_FILE) game.cfg
	@echo "JAR created: $(JAR_FILE)"

# ----------------------------------------------------------------------------
# Run the game – copies given.jar to JAR_DIR and runs from project root
# ----------------------------------------------------------------------------
run: jar
	@# Copy given.jar to the same directory as the JAR for Class-Path resolution
	@if [ ! -f $(JAR_DIR)/$(GIVEN_JAR) ]; then \
		echo "Copying $(GIVEN_JAR) to $(JAR_DIR)/"; \
		$(CP) $(GIVEN_JAR) $(JAR_DIR)/; \
	fi
	@echo "Running Fruity Frank (resources loaded from current directory)..."
	$(JAVA) -jar $(JAR_FILE)

# ----------------------------------------------------------------------------
# Run directly from classpath (no JAR)
# ----------------------------------------------------------------------------
run-dev: compile
	$(JAVA) -cp "$(CLASSPATH):$(CLASSES_DIR)" $(MAIN_CLASS)

# ----------------------------------------------------------------------------
# Run JUnit tests (EngineTest) – only if JUnit console is present
# ----------------------------------------------------------------------------
JUNIT_CONSOLE := $(LIB_DIR)/junit-platform-console-standalone-6.1.0.jar

test: compile
	@if [ ! -f $(JUNIT_CONSOLE) ]; then \
		echo "ERROR: JUnit console launcher not found at $(JUNIT_CONSOLE)."; \
		echo "Please download it from:"; \
		echo "  https://repo1.maven.org/maven2/org/junit/platform/junit-platform-console-standalone/6.1.0/"; \
		echo "Skipping tests."; \
		exit 0; \
	fi
	@if [ "$(JUNIT_AVAILABLE)" != "yes" ]; then \
		echo "JUnit API not found, tests were not compiled. Please place $(JUNIT_API) in $(LIB_DIR)/"; \
		echo "Skipping tests."; \
		exit 0; \
	fi
	@echo "Running JUnit tests..."
	$(JAVA) -jar $(JUNIT_CONSOLE) execute \
		--classpath "$(CLASSES_DIR):$(GIVEN_JAR)" \
		--select-class engine.tests.EngineTest

# ----------------------------------------------------------------------------
# Clean up
# ----------------------------------------------------------------------------
clean:
	$(RM) $(CLASSES_DIR) $(JAR_DIR)/*.jar
	$(RM) $(JAR_DIR)/$(GIVEN_JAR)
	# Remove any stray generated files from project root (if any)
	$(RM) ParseException.java Parser.java ParserConstants.java \
	       ParserTokenManager.java SimpleCharStream.java Token.java TokenMgrError.java
	@echo "Cleaned build artifacts."

# ----------------------------------------------------------------------------
# Help
# ----------------------------------------------------------------------------
help:
	@echo "Available targets:"
	@echo "  all          - build JAR (default)"
	@echo "  parser       - generate parser sources (requires JavaCC)"
	@echo "  compile      - compile all sources"
	@echo "  jar          - create executable JAR"
	@echo "  run          - run the game using the JAR (uses current directory for resources)"
	@echo "  run-dev      - run the game using classpath (no JAR)"
	@echo "  test         - run EngineTest with JUnit 6 (skips if JUnit missing)"
	@echo "  clean        - remove build artifacts"
	@echo ""
	@echo "Dependencies:"
	@echo "  - Java 17+"
	@echo "  - JavaCC (for parser generation)"
	@echo "  - JUnit 6 API JAR in $(LIB_DIR)/ (optional, needed for src-pacman & tests)"
	@echo "  - JUnit 6 Console Launcher JAR in $(LIB_DIR)/ (optional, for tests)"
	@echo ""
	@echo "Note: The game expects resources (sprites/, game.cfg, gal_fsm/) in the current working directory."
