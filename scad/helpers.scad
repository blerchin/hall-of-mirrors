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

module scale_for_svg() {
  MM_PER_IN = 25.4;
  DPI = 96;
  scale(DPI / MM_PER_IN) {
    children();
  }
}
