use <scad-utils/morphology.scad>
include <helpers.scad>
include <constants.scad>

SUPPORT_THICKNESS = 1;

module phone() {
  color("black")
  linear_extrude(PHONE_DEPTH)
  rounding(10)
    square([PHONE_WIDTH, PHONE_HEIGHT]);
}

module case_outer() {
  color("blue")
  linear_extrude(CASE_DEPTH)
  rounding(12)
    square([CASE_WIDTH, CASE_HEIGHT]);
}

module case_bool() {
  difference() {
    case_outer();
    translate([CASE_THICKNESS, CASE_THICKNESS, CASE_THICKNESS + d2])
      phone();
  }
}

module support(point, zpos) {
  color("silver")
  translate(point)
    cylinder(d = SUPPORT_THICKNESS, h = CEIL_CLEARANCE + CUBE_SIZE - point[2] - zpos);
}

module draw_supports(rotation, zpos) {
  for (point = CASE_ANCHOR_POINTS) {
    support(rotate_point(rotation, point), zpos);
  }
}

module draw_random(size = CUBE_SIZE) {
  rotation = rands(0, 360, 3);
  translation = rands(0, size, 3);
  if (is_safe(rotation, translation)) {
    translate(translation)
    union() {
      draw_supports(rotation, translation[2]);
      rotate(rotation)
        case_bool();
    }
  } else {
    //echo("out of bounds; could not draw");
    draw_random();
  }
}

color("lightsteelblue")
translate([0, 0, 1000 + CUBE_SIZE + CEIL_CLEARANCE])
  cube([CUBE_SIZE, CUBE_SIZE, 10]);
translate([0, 0, 1000])
for (i=[0:NUM_PHONES]) {
  draw_random();
}
