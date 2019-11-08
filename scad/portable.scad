use <scad-utils/morphology.scad>
use <nutsnbolts/cyl_head_bolt.scad>
use <case.scad>
include <helpers.scad>
include <constants.scad>

numToDraw = DRAW_RANDOM ? NUM_PHONES : len(POSITIONS);
positions = [for(i=[0:numToDraw - 1]) get_safe_translation_and_rotation(i, CUBE_SIZE)];

FLOOR_HEIGHT = 15;

module draw_phone(index, rotation, translation, with_fov) {
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

module draw_holder() {
  w = HOLDER_WIDTH;
  h = HOLDER_HEIGHT;
  d = HOLDER_DEPTH;
  foot = HOLDER_FOOT;
  translate([0, -foot, 0])
  translate([CASE_WIDTH/2 - w/2, 0, CASE_DEPTH/2 - d/2])
    cube([w, h + foot, d]);
}

module slot(id) {
  if (id) {
    color("black")
    translate([PHONE_WIDTH/2 - 5, PHONE_HEIGHT-15, PHONE_DEPTH + d])
    linear_extrude(1)
      text(str(id));
  }
  phone();

}
module field_of_view(length=70) {
  d2 = 2 * length * cos(PHONE_LENS_FIELD_OF_VIEW / 2);
  position_camera() {
    rotate([180, 0, 0])
      cylinder(d1 = PHONE_LENS_DIA, d2=d2, h=length);
  }
}

module phones(with_fov=false) {
  for (i=[0:len(positions) - 1]) {
    translate([0, 0, 0])
      draw_phone(i, positions[i][0], positions[i][1], with_fov);
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
  }
}

module tripod_mount() {
  translate([CUBE_SIZE/2 + 40, CUBE_SIZE/2, FLOOR_HEIGHT])
    rotate([180, 0, 0])
  screw("M6x20");
}

module floor() {
  cube([CUBE_SIZE + 100, CUBE_SIZE + 100, FLOOR_HEIGHT]);
}

difference() {
  holders();
  phones();
  tripod_mount();
}
//phones(with_fov=true);
