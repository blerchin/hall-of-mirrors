include <constants.scad>

function get_rotation_matricies(theta) = [
  [[1, 0, 0], [0, cos(theta), -sin(theta)], [0, sin(theta), cos(theta)]],
  [[cos(theta), 0, sin(theta)], [0, 1, 0], [-sin(theta), 0, cos(theta)]],
  [[cos(theta), -sin(theta), 0], [sin(theta), cos(theta), 0], [0, 0, 1]]
];

function get_rotation_matrix(axis, theta) = get_rotation_matricies(theta)[axis];

function rotate_point(rotation, point) =
  get_rotation_matrix(2, rotation[2]) *
  get_rotation_matrix(1, rotation[1]) *
  get_rotation_matrix(0, rotation[0]) *
  point;

function clamp_safe(point, translation) =
  let(pt = [for(i=[0, 2]) point[i] + translation[i]])
  [for(i=[0, 2])
    (pt[i] > CUBE_SIZE) ? point[i] - (pt[i] - CUBE_SIZE) :
    (pt[i] < 0) ? point[i] - pt[i] :
    point[i]
  ];

function is_safe_point(point, translation) =
  let(pt = [point[0] + translation[0], point[1] + translation[1], point[2] + translation[2]])
  pt[0] > 0 && pt[0] < CUBE_SIZE &&
  pt[1] > 0 && pt[1] < CUBE_SIZE &&
  pt[2] > 0 && pt[2] < CUBE_SIZE;

function is_safe(rotation, translation) =
  min([for(point = CASE_ANCHOR_POINTS) is_safe_point(rotate_point(rotation, point), translation) ? 1 : 0]) == 1;

function get_safe_translation_and_rotation(index, size) = DRAW_RANDOM ?
  (let (position = [rands(0, 360, 3), rands(0, size, 3)])
   is_safe(position[0], position[1]) ? position : get_safe_translation_and_rotation(index, size)
  ) : [ POSITIONS[index][0], POSITIONS[index][1] ];

module label(position_i, support_i, point, zpos, draw_text=true) {
  $fn = 64;
  pos_name = str(position_i);
  support_name = chr(support_i + 65);
  support_length = support_len(point, zpos);
  color("white")
  translate([ point[0], point[1], 0])
    union() {
      translate([-1.5, 0, 0])
        circle(1);
      translate([1.5, 0, 0])
        circle(1);
      if(draw_text) {
        translate([5, 0, 0])
        text(pos_name, size = 5);
        translate([10, 0, 0])
          text(support_name, size=3);
        translate([5, -4, 0])
          text(str(support_length), size=3);
      }
    }
  sep = "\t";
  echo(str(pos_name, support_name, sep, support_length, sep, point));
}
