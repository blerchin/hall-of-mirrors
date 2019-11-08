SCAD_DIR = ./scad
STL_DIR = ./stl
SVG_DIR = ./svg


$(STL_DIR)/%:
	openscad $(SCAD_DIR)/$(basename $(notdir $@)).scad -o $@

$(SVG_DIR)/%:
	openscad $(SCAD_DIR)/$(basename $(notdir $@)).scad -o $@

$(STL_DIR):
	@echo "Creating STL_DIR $(STL_DIR)"
	mkdir -p $@

$(SVG_DIR):
	@echo "Creating SVG_DIR $(SVG_DIR)"
	mkdir -p $@

all: | $(STL_DIR) $(SVG_DIR) \
	$(STL_DIR)/case.stl \
	$(STL_DIR)/lightbox.stl \
	$(STL_DIR)/portable.stl \
	$(SVG_DIR)/frame.svg \
	$(SVG_DIR)/layout.svg

.PHONY: all
