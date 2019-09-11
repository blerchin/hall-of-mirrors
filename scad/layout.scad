use <scad-utils/morphology.scad>
use <case.scad>
include <helpers.scad>
include <constants.scad>

SUPPORT_THICKNESS = 2;
//#2D array of positions (rotation, translation)

function support_len(point, zpos) = CEIL_CLEARANCE + CUBE_SIZE - point[2] - zpos;

module support(point, zpos) {
  color("black")
  translate(point)
    cylinder(d = SUPPORT_THICKNESS, h = CEIL_CLEARANCE + CUBE_SIZE - point[2] - zpos);
}

module draw_supports(rotation, zpos, index, label_only = false) {
  for (p = [0 : len(CASE_ANCHOR_POINTS) - 1]) {
    point = CASE_ANCHOR_POINTS[p];
    position = rotate_point(rotation, point);
    if (label_only) {
      label(index, p, position, zpos);
    } else {
      support(position, zpos);
    }
  }
}

module draw_plan(index, rotation, translation) {
  translate([ translation[0], translation[1], 0])
    draw_supports(rotation, translation[2], index, true);
}

module draw(index, rotation, translation) {
  translate(translation)
  difference() {
    union() {
      draw_supports(rotation, translation[2], index);
      echo(str("#", index, "\t ", rotation, "\t ", translation));
      rotate(rotation)
        case_bool();
    }
    rotate(rotation) {
      position_phone_in_case();
    }
  }
}

numToDraw = DRAW_RANDOM ? NUM_PHONES : len(POSITIONS);
for (i=[0:numToDraw - 1]) {
  position = get_safe_translation_and_rotation(i, CUBE_SIZE);
  translate([0, 0, -400])
    draw(i, position[0], position[1]);
  draw_plan(i, position[0], position[1]);
}
