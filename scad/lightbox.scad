use <scad-utils/morphology.scad>
use <case.scad>
include <helpers.scad>
include <constants.scad>

MAX_EXTENT = 430;
SUPPORT_THICKNESS = 2;
//SCALE_FACTOR = PANE_WIDTH / PHONE_SCREEN_WIDTH;
//#2D array of positions (rotation, translation)
DISPLAY_GUIDES = false;

function scale_vec(vec, amt) = [vec[0] * amt, vec[1] * amt, vec[2] * amt];

module label(id) {
  color("black")
  translate([0, 0, -PANE_THICKNESS])
  linear_extrude(PANE_THICKNESS * 2)
    text(str(id));
}

module draw(index, rotation, translation, width = PANE_WIDTH) {
  offset = get_frame_dist(FIELD_OF_VIEW, width);
  aspect_ratio = PANE_WIDTH / PANE_HEIGHT;
  height = width / aspect_ratio;
  translate(translation)
  union() {
    echo(str("#", index, "\t ", rotation, "\t ", translation));
    echo(str("offset: ", offset));
    if(DISPLAY_GUIDES) {
      color("red")
      rotate(rotation)
      union(){
        sphere(r=2);
        cylinder(r=1, h=1000);
        label(index);
      }
    }
    rotate(rotation)
    translate([0, 0, offset])
      union() {
        color("skyblue")
          cube([width + 1, height, PANE_THICKNESS + 1], center=true);
        color("black")
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
    width = POSITIONS[i][2] ? POSITIONS[i][2] : PANE_WIDTH;
    draw(i, rotation, translation, width);
  }
}

module terrain_extent(height = 300) {
  color("red")
  difference() {
    cube([10000, 10000, height], center=true);
    cube([MAX_EXTENT, MAX_EXTENT, height + 1], center=true);
  }
}
*translate([10, -50, 0])
terrain_extent();
if (TERRAIN) {
  difference() {
    translate(TERRAIN_OFFSET)
    rotate(TERRAIN_ROTATE)
    scale(TERRAIN_DISPLAY_SCALE)
      import(TERRAIN);
    *translate([40, 57, 0])
      terrain_extent();
    panes();
  }
}
panes();
