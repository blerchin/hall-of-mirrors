include <layouts/l6-playa.scad>

INCH = 25.4;
CUBE_SIZE = 110;
CEIL_CLEARANCE = 600;
NUM_PHONES = 3;
DRAW_RANDOM = false;


PANE_WIDTH = 10 * INCH;
PANE_HEIGHT = 8 * INCH;
PANE_THICKNESS = 2;

FRAME_PADDING = INCH / 4;

FRAME_WIDTH = PANE_WIDTH + 2 * FRAME_PADDING;
FRAME_HEIGHT = PANE_HEIGHT + 2 * FRAME_PADDING;
FRAME_THICKNESS = INCH / 4;


PHONE_HEIGHT = 147.8;
PHONE_WIDTH = 71.9;
PHONE_DEPTH = 9.1;
PHONE_SCREEN_TOP = 19; //wrt case top
PHONE_SCREEN_HEIGHT = 110;
PHONE_SCREEN_WIDTH = 68;
PHONE_LENS_DIA = 11.85;
PHONE_LENS_OFFSET_LEFT = 3;
PHONE_LENS_OFFSET_TOP = 3.65;
PHONE_LENS_FIELD_OF_VIEW = 81;
PHONE_POWER_BUTTON_TOP = 52;

CASE_THICKNESS = 1.5;
WALL_THICKNESS = 3;
CASE_HEIGHT = PHONE_HEIGHT + 2 * WALL_THICKNESS;
CASE_WIDTH = PHONE_WIDTH + 2 * WALL_THICKNESS;
CASE_DEPTH = PHONE_DEPTH + 2 * CASE_THICKNESS;

// new iteration using baby USB cams
BOARD_WIDTH = 33;
BOARD_HEIGHT = 33;
BOARD_DEPTH = 6;

USB_MICROB_WIDTH = 23;
USB_MICROB_LENGTH = 16;
USB_CHANNEL_WIDTH = 10;
USB_CHANNEL_LENGTH = 20;
USB_CHANNEL_DEPTH = 20;

RASPI_WIDTH = 50; // includes approximate USB clearance
RASPI_LENGTH = 70;
RASPI_HEIGHT = 30;

FLOOR_SIZE = 55;
INSERT_MARGIN = 7;
INSERT_HEIGHT = 20;

CASE_ANCHOR_OFFSET = 14;
CASE_ANCHOR_POINTS = [
  [0, 0, 0],
  [0, BOARD_HEIGHT, 0],
  [BOARD_WIDTH, BOARD_HEIGHT, 0],
  [BOARD_WIDTH, 0, 0]
];

PANE_ANCHOR_OFFSET = 0;
PANE_ANCHOR_POINTS = [
  [0, PANE_ANCHOR_OFFSET, 0],
  [0, PANE_HEIGHT - PANE_ANCHOR_OFFSET, 0],
  [PANE_WIDTH, PANE_HEIGHT - PANE_ANCHOR_OFFSET, 0],
  [PANE_WIDTH, PANE_ANCHOR_OFFSET, 0]
];

HOLDER_WIDTH = 100;
HOLDER_HEIGHT = 20;
HOLDER_DEPTH = 30;
HOLDER_FOOT = 10;


d = 0.01;
d2 = 2 * d;
wiggle = 1;
