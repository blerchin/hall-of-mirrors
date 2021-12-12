POLE_OD = 17;
BATTERY_WIDTH = 68.5;
BATTERY_DEPTH = 17;
CLIP_HEIGHT = 25;
THICKNESS = 2;
d1 = 0.01;
d2 = d1 * 2;
$fn = 128;

module battery_holder() {
  difference() {
    cube([BATTERY_WIDTH + 2 * THICKNESS, BATTERY_DEPTH + 2 * THICKNESS, CLIP_HEIGHT + THICKNESS]);
    translate([THICKNESS, THICKNESS, THICKNESS])
      cube([BATTERY_WIDTH, BATTERY_DEPTH, CLIP_HEIGHT + d1]);
  }
}

module clip() {
  difference() {
    cylinder(d=POLE_OD + THICKNESS * 2, h=CLIP_HEIGHT + THICKNESS);
    translate([0, 0, -d1])
      cylinder(d=POLE_OD, h=CLIP_HEIGHT + THICKNESS + d2);
    
  }
}

battery_holder();
translate([BATTERY_WIDTH / 2 + THICKNESS, -POLE_OD / 2, 0])
  clip();