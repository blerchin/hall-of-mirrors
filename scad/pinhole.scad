use <scad-utils/morphology.scad>
use <nutsnbolts/cyl_head_bolt.scad>
include <helpers.scad>
include <constants.scad>
numToDraw = DRAW_RANDOM ? NUM_PHONES : len(POSITIONS);
positions = [for(i=[0:numToDraw - 1]) get_safe_translation_and_rotation(i, CUBE_SIZE)];

$fn = 128;

FLOOR_HEIGHT = 5;
PINHOLE_SIZE = 0.2;
PINHOLE_LENGTH = 5;
PINHOLE_SURROUND_DIA = 15;

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
        translate([0, 0, -5])
          #cylinder(d=PINHOLE_SIZE, h=PINHOLE_SURROUND_DIA);
        translate([0, 0, 0.5]) //can't be too close to outside of model for slicing reasons
          //projection cone
          #cylinder(d1=0.01, d2=2 * PINHOLE_SURROUND_DIA / 3, h=3);
        if(with_fov) {
          field_of_view();
        }
    }
  }
}

module draw_holder(margin = BOARD_HOLDER_MARGIN) {
  //this is where we draw the outer holders that will determine the hull
  cylinder(d=PINHOLE_SURROUND_DIA, h=PINHOLE_LENGTH);
}

module draw_slot(id = -1) {
  if (id != -1) {
    color("black")
    translate([4 , -3, 2.5])
    rotate([0, 180, 0])
    linear_extrude(0.5)
      text(str(id));
  }
  translate([0, 0, -d2])
    holder(margin=BOARD_HOLDER_MARGIN + 0.5, depth=70);
  //channel back to cavity
  translate([BOARD_WIDTH/2 - USB_MICROB_WIDTH/2, -USB_MICROB_LENGTH + d2, 0])
    cube([USB_CHANNEL_WIDTH, USB_MICROB_LENGTH, USB_CHANNEL_DEPTH]);
}
module field_of_view(length=70) {
  d2 = 2 * length * cos(PHONE_LENS_FIELD_OF_VIEW / 2);
  translate([BOARD_WIDTH/2, BOARD_HEIGHT/2, 0])
  rotate([180, 0, 0])
    cylinder(d1 = PHONE_LENS_DIA, d2=d2, h=length);
}

module cameras(with_fov=false) {
  for (i=[0:len(positions) - 1]) {
    draw_camera(i, positions[i][0], positions[i][1], with_fov);
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

module wire_channel() {
  translate([
    CUBE_SIZE/2 - FLOOR_SIZE/2 + USB_CHANNEL_WIDTH, 
    CUBE_SIZE/2 - FLOOR_SIZE/2 + USB_CHANNEL_WIDTH,
    -d2
     ])
    cylinder(d=USB_CHANNEL_WIDTH, 30);
}

module raspi() {
  translate([
    CUBE_SIZE/2 - RASPI_HEIGHT/2,
    CUBE_SIZE/2 - RASPI_WIDTH/2,
    -d2
     ])
  cube([RASPI_HEIGHT, RASPI_WIDTH, RASPI_LENGTH]);
}

difference() {
  holders();
  cameras();
  cavity(CAVITY_SCALE);
}
//cameras(with_fov=true);
