use <scad-utils/morphology.scad>
use <case.scad>
include <helpers.scad>
include <constants.scad>

numToDraw = DRAW_RANDOM ? NUM_PHONES : len(POSITIONS);
positions = [for(i=[0:numToDraw - 1]) get_safe_translation_and_rotation(i, CUBE_SIZE)];

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

module draw_holder(foot=40) {
  w = 100;
  h = 70;
  d = 40;
  translate([0, -foot, 0])
  translate([CASE_WIDTH/2 - w/2, 0, CASE_DEPTH/2 - d/2])
    cube([w, h + foot, d]);
}

module slot(id, extra = 10) {
  if (id) {
    color("black")
    translate([CASE_WIDTH/2, CASE_HEIGHT-10, CASE_DEPTH + d])
    linear_extrude(1)
      text(str(id));
  }
  linear_extrude(CASE_DEPTH + wiggle)
  rounding(8)
    square([CASE_WIDTH + wiggle, CASE_HEIGHT + wiggle + extra]);

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
difference() {
  holders();
  phones();
}
phones(with_fov=true);
