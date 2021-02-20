include <constants.scad>
$fn = 128;

module holder(margin = BOARD_HOLDER_MARGIN, depth = BOARD_DEPTH) {
  w = BOARD_WIDTH + margin;
  h = BOARD_HEIGHT + margin;
  d = depth + margin;
  translate([-margin/2, -margin/2, d2 - depth])
    union() {
      cube([w, h, d]);
    translate([BOARD_WIDTH/2 - USB_MICROB_WIDTH/2, -USB_MICROB_LENGTH + d2, 0])
      cube([USB_MICROB_WIDTH + margin, USB_MICROB_LENGTH + margin, depth + margin]);
  }
}

module slot(id = -1, cutout_depth = 70) {
  translate([0, 0, -cutout_depth])
  union() {
    cube([BOARD_WIDTH, BOARD_HEIGHT, BOARD_DEPTH + cutout_depth]);
    //room for usb connection
    translate([BOARD_WIDTH/2 - USB_MICROB_WIDTH/2, -USB_MICROB_LENGTH + d2, 0])
      cube([USB_MICROB_WIDTH, USB_MICROB_LENGTH, BOARD_DEPTH + cutout_depth]);
    translate([BOARD_WIDTH/2, BOARD_HEIGHT/2, cutout_depth])
      cylinder(d=BOARD_LENS_DIA, h=cutout_depth);
  }
}

rotate([180, 0, 0])
difference() {
  translate([0, 0, BOARD_DEPTH])
    holder();
  slot();
}