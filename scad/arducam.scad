use <scad-utils/morphology.scad>
use <nutsnbolts/cyl_head_bolt.scad>
use <case.scad>
include <helpers.scad>
include <constants.scad>
numToDraw = DRAW_RANDOM ? NUM_PHONES : len(POSITIONS);
positions = [for(i=[0:numToDraw - 1]) get_safe_translation_and_rotation(i, CUBE_SIZE)];

$fn = 128;

FLOOR_HEIGHT = 5;
CAVITY_SCALE = 0.75;
HOLDER_MARGIN = 2;

module cavity(factor, margin=INSERT_MARGIN) {
  offset = (1-factor)/2 * CUBE_SIZE;
  translate([offset, offset, offset])
  scale(factor)
    holders();
  translate([CUBE_SIZE/2 - FLOOR_SIZE/2 + margin/2, CUBE_SIZE/2 - FLOOR_SIZE/2 + margin/2, -d2])
    cube([FLOOR_SIZE - margin, FLOOR_SIZE - margin, FLOOR_HEIGHT + INSERT_HEIGHT]);
}

module draw_camera(index, rotation, translation, with_fov) {
  echo(str("#", index, "\t ", rotation, "\t ", translation));
  translate(translation)
  difference() {
    rotate(rotation)
      union() {
        slot(index);
        if(with_fov) {
          field_of_view();
        }
    }
  }
}

module draw_holder(margin = HOLDER_MARGIN) {
  w = BOARD_WIDTH + margin;
  h = BOARD_HEIGHT + margin;
  d = BOARD_DEPTH + margin;
  translate([-margin/2, -margin/2, d2])
    union() {
      cube([w, h, d]);
    translate([BOARD_WIDTH/2 - USB_MICROB_WIDTH/2, -USB_MICROB_LENGTH + d2, 0])
      cube([USB_MICROB_WIDTH + margin, USB_MICROB_LENGTH + margin, BOARD_DEPTH]);
  }
}

module slot(id = -1) {
  if (id != -1) {
    color("black")
    translate([BOARD_WIDTH/2 + 4 , BOARD_HEIGHT/2 - 3, BOARD_DEPTH + 0.5])
    rotate([0, 180, 0])
    linear_extrude(0.5)
      text(str(id));
  }
  translate([0, 0, -70])
  union() {
    cube([BOARD_WIDTH, BOARD_HEIGHT, BOARD_DEPTH + 70]);
    //room for usb connection
    translate([BOARD_WIDTH/2 - USB_MICROB_WIDTH/2, -USB_MICROB_LENGTH + d2, 0])
      cube([USB_MICROB_WIDTH, USB_MICROB_LENGTH, BOARD_DEPTH + 70]);
  }
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
        union() {
          draw_holder();
        }
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
  wire_channel();
  //raspi();
  translate([0, 0, CUBE_SIZE - 5])
    cube([CUBE_SIZE, CUBE_SIZE, CUBE_SIZE]);
}
//cameras(with_fov=true);
