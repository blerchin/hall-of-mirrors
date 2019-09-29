include <constants.scad>

FRAME_INSET = INCH / 8;
TONGUE_THICKNESS = PANE_THICKNESS / 2;

module tongue(width = INCH / 8) {
  difference() {
    translate([width, width, 0])
      cube([FRAME_WIDTH - 2 * width, FRAME_HEIGHT -  2 * width, TONGUE_THICKNESS]);
    translate([FRAME_PADDING, FRAME_PADDING, -d])
      cube([PANE_WIDTH, PANE_HEIGHT, TONGUE_THICKNESS + d2]);
  }
}

module frame_half() {
  difference() {
    cube([FRAME_WIDTH, FRAME_HEIGHT, FRAME_THICKNESS / 2]);
    translate([FRAME_PADDING + FRAME_INSET, FRAME_PADDING + FRAME_INSET, -d])
      cube([
        PANE_WIDTH - 2 * FRAME_INSET,
        PANE_HEIGHT - 2 * FRAME_INSET,
        FRAME_THICKNESS / 2 + d2
      ]);
      translate([FRAME_PADDING, FRAME_PADDING, FRAME_THICKNESS / 2 - PANE_THICKNESS / 2 + d])
        cube([PANE_WIDTH, PANE_HEIGHT, PANE_THICKNESS / 2]);
  }
}

frame_half();
translate([0, 0, FRAME_THICKNESS/2])
  tongue();

translate([FRAME_WIDTH + FRAME_PADDING, 0, 0])
  difference() {
    frame_half();
    translate([0, 0, FRAME_THICKNESS / 2 - TONGUE_THICKNESS + d])
      tongue();
  }
