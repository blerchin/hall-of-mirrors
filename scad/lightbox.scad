use <scad-utils/morphology.scad>
use <case.scad>
include <helpers.scad>
include <constants.scad>

MAX_EXTENT = 300;
SUPPORT_THICKNESS = 2;
//SCALE_FACTOR = PANE_WIDTH / PHONE_SCREEN_WIDTH;
SCALE_FACTOR = 1;
//#2D array of positions (rotation, translation)

function support_len(point, zpos) = CEIL_CLEARANCE + (SCALE_FACTOR * CUBE_SIZE) - point[2] - zpos;

function scale_vec(vec, amt) = [vec[0] * amt, vec[1] * amt, vec[2] * amt];

module support(point, zpos) {
  color("black")
  translate(point)
    cylinder(d = SUPPORT_THICKNESS, h = CEIL_CLEARANCE + (SCALE_FACTOR * CUBE_SIZE) - point[2] - zpos);
}

module draw_supports(rotation, zpos, index, label_only = false) {
  for (p = [0 : len(PANE_ANCHOR_POINTS) - 1]) {
    point = PANE_ANCHOR_POINTS[p];
    position = rotate_point(rotation, point);
    if (label_only) {
      label(index, p, position, zpos, draw_text=true);
    } else {
      support(position, zpos);
    }
  }
}

module draw_plan(index, rotation, translation) {
  translate([ translation[0], translation[1], 0])
    draw_supports(rotation, translation[2], index, true);
}

module label(id) {
  color("black")
  translate([0, 0, -PANE_THICKNESS])
  linear_extrude(PANE_THICKNESS * 2)
    text(str(id));
}

module draw(index, rotation, translation) {
  translate(translation)
  union() {
    echo(str("#", index, "\t ", rotation, "\t ", translation));
    rotate(rotation)
      union() {
        cube([PANE_WIDTH, PANE_HEIGHT, PANE_THICKNESS + 1], center=true);
        label(index);
      }
  }
}

module panes() {
  numToDraw = DRAW_RANDOM ? NUM_PHONES : len(POSITIONS);
  translate([0, 0, PANE_HEIGHT / 2])
  for (i=[0:numToDraw - 1]) {
    position = get_safe_translation_and_rotation(i, CUBE_SIZE);
    rotation = position[0];
    translation = scale_vec(position[1], SCALE_FACTOR);
    draw(i, rotation, translation);
  }
}

module terrain_extent() {
  difference() {
    cube(10000, center = true);
    cube(MAX_EXTENT, center=true);
  }
}

if (TERRAIN) {
  difference() {
    translate(TERRAIN_OFFSET)
    rotate(TERRAIN_ROTATE)
    scale(TERRAIN_SCALE)
      import(TERRAIN);
    *translate([0, 60, 0])
      terrain_extent();
    panes();
  }
}
panes();
