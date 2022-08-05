use <scad-utils/morphology.scad>
use <nutsnbolts/cyl_head_bolt.scad>
use <threads-scad/threads.scad>
include <helpers.scad>
include <constants.scad>
numToDraw = DRAW_RANDOM ? NUM_PHONES : len(POSITIONS);
positions = [for(i=[0:numToDraw - 1]) get_safe_translation_and_rotation(i, CUBE_SIZE)];

$fn = 256;


PINHOLE_SIZE = 1;
PINHOLE_LENGTH = 10;
PINHOLE_SURROUND_DIA = 25;
THREAD_HEIGHT = 3.5;

module cavity(factor, margin=INSERT_MARGIN) {
  holders([0, 0, -THREAD_HEIGHT], false);
  //base fits in here
  *translate([-FLOOR_SIZE/2 + margin/2, -FLOOR_SIZE/2 + margin/2, -d2])
    cube([FLOOR_SIZE - margin, FLOOR_SIZE - margin, FLOOR_HEIGHT + INSERT_HEIGHT]);
}

module draw_c_mount(height) {
  thirty_two_tpi = .794;
  ScrewThread(INCH, height, pitch=thirty_two_tpi);
}

module draw_m42_mount(height) {
  ScrewThread(MOUNT_DIAMETER, height, pitch=1);
}

module draw_camera(index, rotation, translation, with_fov) {
  union(){
    #sphere(r=1);
    translate([0, 0, -20 *THREAD_HEIGHT])
      cylinder(d=MOUNT_DIAMETER, h=30 *THREAD_HEIGHT);
    translate([0, 0, THREAD_HEIGHT - 0.1])
      cylinder(d=1.2*INCH, 6 * THREAD_HEIGHT); //clearance for focus ring
  }
  *draw_m42_mount(thread_height);
}


module draw_holder(offset) {
  //this is where we draw the outer holders that will determine the hull
  translate(offset)
    cylinder(d=MOUNT_DIAMETER, h=THREAD_HEIGHT);
}

module cameras(with_fov=false) {
  with_locations() {
    draw_camera();
  }
}

module holders(offset=[0, 0, 0], draw_floor = true) {
  color("blue")
  hull(){
    with_locations() {
      draw_holder(offset);
    }
    if (draw_floor) {
      floor();
    }
  }
}

module with_locations() {
  for (i=[0:len(POSITIONS) - 1]) {
    translate(POSITIONS[i][1])
      rotate(POSITIONS[i][0])
        translate([FLANGE_FOCAL_DISTANCE, 0, 0])
            rotate([0, 90, 0])
              children();
  }
}

module floor() {
  translate([-FLOOR_SIZE/2, -FLOOR_SIZE/2, 0])
    cube([FLOOR_SIZE, FLOOR_SIZE, FLOOR_HEIGHT]);
}

difference() {
  holders();
  cameras();
  *translate([0, 0, -3])
    #cavity(CAVITY_SCALE);
}