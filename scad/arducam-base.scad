
use <nutsnbolts/cyl_head_bolt.scad>
include <constants.scad>

$fn = 128;
SLOP = 0.5;
    
w = FLOOR_SIZE-INSERT_MARGIN - SLOP;
l = FLOOR_SIZE-INSERT_MARGIN - SLOP;

module wire_channel(size=USB_CHANNEL_WIDTH + 5) {
  cube([size, size, 30]);
}

module tripod_mount() {
  translate([w/2, w/2, -d2])
    rotate([180, 0, 0])
    screw("M6x10", $fn=128);
}

difference() {
  cube([w, l, INSERT_HEIGHT - 5 - SLOP]);
  
  translate([INSERT_MARGIN/2, INSERT_MARGIN/2, INSERT_MARGIN/2])
    cube([w - INSERT_MARGIN, l - INSERT_MARGIN, INSERT_HEIGHT]);
  tripod_mount();
  wire_channel();
}