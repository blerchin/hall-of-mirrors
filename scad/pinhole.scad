use <scad-utils/morphology.scad>
use <nutsnbolts/cyl_head_bolt.scad>
include <helpers.scad>
include <constants.scad>
numToDraw = DRAW_RANDOM ? NUM_PHONES : len(POSITIONS);
positions = [for(i=[0:numToDraw - 1]) get_safe_translation_and_rotation(i, CUBE_SIZE)];

$fn = 128;

FLOOR_HEIGHT = 5;
PINHOLE_SIZE = 1;
PINHOLE_LENGTH = 5;
PINHOLE_SURROUND_DIA = 40;

module cavity(factor, margin=INSERT_MARGIN) {
  offset = (1-factor)/2 * CUBE_SIZE;
  translate([offset, offset, offset])
  scale(factor)
    holders();
  //base fits in here
  translate([CUBE_SIZE/2 - FLOOR_SIZE/2 + margin/2, CUBE_SIZE/2 - FLOOR_SIZE/2 + margin/2, -d2])
    cube([FLOOR_SIZE - margin, FLOOR_SIZE - margin, FLOOR_HEIGHT + INSERT_HEIGHT]);
}

module draw_camera(index, rotation, translation, with_fov) {
  echo(str("#", index, "\t ", rotation, "\t ", translation));
  translate(translation)
  difference() {
    rotate(rotation)
      union() {
        //for previewing angle
        //translate([0, 0, -5])
         // cylinder(d=PINHOLE_SIZE, h=10);
        translate([0, 0, .7])
          draw_aperture_ring(PINHOLE_SURROUND_DIA / 2 + 1, 2, false);
        #translate([0, 0, 0]) //can't be too close to outside of model for slicing reasons
          //projection cone
          cylinder(d1=0.1, d2=2 * PINHOLE_SURROUND_DIA / 3, h=8);
    }
  }
}

module apertures(dia, thickness) {
  positions = [0, 45, 90, 135, 180, 225, 270, 315];
  for(i = [0 : len(positions) - 1]) {
    rotate([0, 0, positions[i]]) {
      translate([0, dia/4, -0.1])
        cylinder(d=2/len(positions) * i, h=thickness + 1);
    }
  }
}

module draw_aperture_ring(dia = PINHOLE_SURROUND_DIA / 2, pin_dia = 1.9, with_apertures=true) {
  thickness = 2;
  translate([0, dia/4, -1 * thickness])
    difference() {
      cylinder(d=dia, h=thickness);
      if (with_apertures)
        apertures(dia, thickness);
    }
  //mounting pin
  translate([0, dia/4, -0.1])
    cylinder(d=pin_dia, h=2);
}

module draw_holder(margin = BOARD_HOLDER_MARGIN) {
  //this is where we draw the outer holders that will determine the hull
  cylinder(d=PINHOLE_SURROUND_DIA, h=PINHOLE_LENGTH);
}

module cameras(with_fov=false) {
  for (i=[0:len(positions) - 1]) {
    draw_camera(i, positions[i][0], positions[i][1], with_fov);
  }
}

module rings() {
  color("yellow")
  for (p=positions) {
    translate(p[1])
    rotate(p[0])
      draw_aperture_ring();
  }
}

module holders() {
  color("blue")
  hull(){
    for (p=positions) {
      translate(p[1])
      rotate(p[0])
        draw_holder();
    }
    floor();
  }
}

module floor() {
  translate([CUBE_SIZE/2 - FLOOR_SIZE/2, CUBE_SIZE/2 - FLOOR_SIZE/2, 0])
    cube([FLOOR_SIZE, FLOOR_SIZE, FLOOR_HEIGHT]);
}

*difference() {
  holders();
  cameras();
  cavity(CAVITY_SCALE);
}
draw_aperture_ring();
//cameras(with_fov=true);
