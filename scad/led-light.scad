use <scad-utils/morphology.scad>
use <nutsnbolts/cyl_head_bolt.scad>
use <case.scad>
include <helpers.scad>
include <constants.scad>

numToDraw = DRAW_RANDOM ? NUM_PHONES : len(POSITIONS);
positions = [for(i=[0:numToDraw - 1]) get_safe_translation_and_rotation(i, CUBE_SIZE)];

SCALE_FACTOR = 1/2;
THICKNESS = 3;
INNER_SCALE_FACTOR = SCALE_FACTOR - THICKNESS / CUBE_SIZE;
LIGHT_DEPTH = 25;
LIGHT_DIA = 5.5;

module draw_phone(index, rotation, translation) {
  echo(str("#", index, "\t ", rotation, "\t ", translation));
  translate(translation)
  rotate(rotation)
    cube([PHONE_WIDTH, PHONE_HEIGHT, 0.1]);
}

module phones(scale_factor = SCALE_FACTOR) {
  for (i=[0:len(positions) - 1]) {
    scale(scale_factor)
      draw_phone(i, positions[i][0], positions[i][1]);
  }
}


module draw_light(rotation, translation) {
  translate(translation)
  rotate(rotation)
    translate([0, 0, THICKNESS + d])
      light_base();
}

module light_base() {
  position_camera() {
    rotate([180, 0, 0])
      cylinder(d = LIGHT_DIA / SCALE_FACTOR, h=LIGHT_DEPTH);
  }
}

module lights() {
  scale(SCALE_FACTOR)
  for (pos = positions) {
    draw_light(pos[0], pos[1]);
  }

}

//lights();
color("blue")
difference() {
  hull() {
    phones(SCALE_FACTOR);
  }
  translate([THICKNESS / 2, THICKNESS / 2, THICKNESS / 2])
  hull() {
    phones(INNER_SCALE_FACTOR);
  }
  lights();
}
