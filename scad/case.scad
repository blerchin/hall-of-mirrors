use <scad-utils/morphology.scad>
include <constants.scad>
include <helpers.scad>

EXIT_LENGTH = 10;
CLEAT_WIDTH = 2.5;
CLEAT_HEIGHT = 1.5;
CLEAT_DIST = 3;

$fn = 128;

module phone() {
  //color("black")
  linear_extrude(PHONE_DEPTH)
  rounding(5)
    square([PHONE_WIDTH, PHONE_HEIGHT]);
}

module position_camera() {
  translate([
    PHONE_WIDTH - PHONE_LENS_OFFSET_LEFT - PHONE_LENS_DIA/2,
    PHONE_HEIGHT + CASE_THICKNESS - PHONE_LENS_OFFSET_TOP - PHONE_LENS_DIA / 2,
    -d
    ])
    children();
}

module phone_camera() {
    cylinder(d=PHONE_LENS_DIA, h=CASE_THICKNESS + d2);
}


module phone_exit() {
  translate([(CASE_WIDTH - PHONE_WIDTH) / 2, -1, (CASE_DEPTH - PHONE_DEPTH) / 2])
  cube([PHONE_WIDTH, EXIT_LENGTH, PHONE_DEPTH]);
}

module phone_screen() {
  translate([(CASE_WIDTH - PHONE_SCREEN_WIDTH) / 2,  CASE_HEIGHT - PHONE_SCREEN_TOP - PHONE_SCREEN_HEIGHT , PHONE_DEPTH])
  linear_extrude(PHONE_DEPTH)
    square([PHONE_SCREEN_WIDTH, PHONE_SCREEN_HEIGHT]);
}

module case_outer() {
  color("blue")
  linear_extrude(CASE_DEPTH)
  rounding(8)
    square([CASE_WIDTH, CASE_HEIGHT]);
}

module power_button_access() {
  v = [WALL_THICKNESS + d2, 14, 4];
  translate([CASE_WIDTH - WALL_THICKNESS - d, CASE_HEIGHT - PHONE_POWER_BUTTON_TOP - v[1], CASE_DEPTH / 2])
    translate([0, 0, -2])
    cube(v);
}

module cleat() {
  translate([-d, -CLEAT_DIST/2 - CLEAT_WIDTH/2, -CLEAT_HEIGHT / 2])
    union() {
      cube([WALL_THICKNESS + d2, CLEAT_WIDTH, CLEAT_HEIGHT]);
      translate([0, CLEAT_WIDTH + CLEAT_DIST, 0])
        cube([WALL_THICKNESS + d, CLEAT_WIDTH, CLEAT_HEIGHT]);
    }
}

module position_cleats() {
  for (position = CASE_ANCHOR_POINTS) {
    translate([0, 0, CASE_DEPTH/2])
    translate(position)
      cleat();
  }
}

module position_phone_in_case() {
  translate([WALL_THICKNESS, WALL_THICKNESS, CASE_THICKNESS])
    phone();
}

module case_bool() {
  difference() {
    case_outer();
    position_phone_in_case();
    phone_screen();
    phone_exit();
    position_camera() {
      phone_camera();
    };
    position_cleats();
    power_button_access();
  }
}

case_bool();
