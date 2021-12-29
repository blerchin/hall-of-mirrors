include <helpers.scad>
include <constants.scad>

$fn = 256;

WALL = 1;
INNER_WALL = 2 * WALL;
LENS_THRU_HOLE_DIA = 15;
slop = 0.1;
cube_size = FLOOR_SIZE + 2 * WALL;

module camera_box() {
  difference() {
    cube([cube_size, cube_size, cube_size - INNER_WALL]);
    translate([WALL - slop, WALL - slop, cube_size - INNER_WALL - FLOOR_HEIGHT])
      cube([FLOOR_SIZE + 2 * slop, FLOOR_SIZE + 2 * slop, FLOOR_HEIGHT]);
    translate([INNER_WALL, INNER_WALL, INNER_WALL])
      cube([FLOOR_SIZE - INNER_WALL, FLOOR_SIZE - INNER_WALL, FLOOR_SIZE + INNER_WALL]);
  }
}

difference() {
  camera_box();
  translate([cube_size / 2, cube_size / 2, -slop])
    cylinder(d=LENS_THRU_HOLE_DIA, h=10);
}