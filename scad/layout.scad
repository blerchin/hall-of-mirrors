use <scad-utils/morphology.scad>
use <case.scad>
include <helpers.scad>
include <constants.scad>

SUPPORT_THICKNESS = 2;
//#2D array of positions (rotation, translation)
POSITIONS = [
  [ [134.648, 225.704, 99.3686],	 [49.3451, 105.663, 224.724] ],
  [ [297.307, 196.699, 265.857],	 [99.3866, 174.719, 148.235] ],
  [ [309.159, 98.7394, 312.317],	 [37.2874, 28.2925, 228.506] ],
  [ [314.855, 21.4972, 279.288],	 [158.367, 170.535, 146.508] ],
  [ [171.605, 354.204, 309.768],	 [118.408, 158.852, 74.9138] ]
];

DRAW_RANDOM = false;

function support_len(point, zpos) = CEIL_CLEARANCE + CUBE_SIZE - point[2] - zpos;

module support(point, zpos) {
  color("black")
  translate(point)
    cylinder(d = SUPPORT_THICKNESS, h = CEIL_CLEARANCE + CUBE_SIZE - point[2] - zpos);
}

module label(position_i, support_i, point, zpos) {
  pos_name = str(position_i);
  support_name = chr(support_i + 65);
  support_length = support_len(point, zpos);
  color("white")
  translate([ point[0], point[1], 0])
    union() {
      circle(2);
      translate([5, 0, 0])
      text(pos_name, size = 5);
      translate([10, 0, 0])
        text(support_name, size=3);
      translate([5, -4, 0])
        text(str(support_length), size=3);
    }
  sep = "\t";
  echo(str(pos_name, support_name, sep, support_length, sep, point));
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

function get_safe_translation_and_rotation(index, size) = DRAW_RANDOM ?
  (let (position = [rands(0, 360, 3), rands(0, size, 3)])
   is_safe(position[0], position[1]) ? position : get_safe_translation_and_rotation(index, size)
  ) : [ POSITIONS[index][0], POSITIONS[index][1] ];

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
