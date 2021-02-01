
use <nutsnbolts/cyl_head_bolt.scad>
include <constants.scad>

$fn = 128;
SLOP = 0.5;
    
w = FLOOR_SIZE-INSERT_MARGIN - SLOP;
l = FLOOR_SIZE-INSERT_MARGIN - SLOP;

module wire_channel() {
  translate([
    USB_CHANNEL_WIDTH/2 + 1,
    USB_CHANNEL_WIDTH/2 + 1,
    -d2
     ])
    cylinder(d=USB_CHANNEL_WIDTH, 30);
}

module tripod_mount() {
  translate([w/2, w/2, -d2])
    rotate([180, 0, 0])
    screw("M6x10", $fn=128);
}

difference() {
  cube([w, l, INSERT_HEIGHT - SLOP]);
  tripod_mount();
  wire_channel();
}