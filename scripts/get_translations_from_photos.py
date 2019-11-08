import os
import exifread
from geopy import distance

DIRECTORY = os.path.join(os.getcwd(), 'images/layouts/4-water/castaic/');

def _get_if_exist(data, key):
    if key in data:
        return data[key]

    return None

def ratio_to_float(value):
    return float(value.num) / float(value.den)

def get_offset(point_a, point_b):
    lat_a, lon_a, alt_a = point_a
    lat_b, lon_b, alt_b = point_b
    x = distance.distance((lat_a, lon_a), (lat_a, lon_b)).meters
    if lon_a > lon_b:
        x = 0 - x
    if lat_a > lat_b:
        x = 0 - x

    y = distance.distance((lat_a, lon_a), (lat_b, lon_a)).meters
    z = alt_b - alt_a
    return (x, y, z)

def convert_to_degrees(value):
    """
    Helper function to convert the GPS coordinates stored in the EXIF to degress in float format
    :param value:
    :type value: exifread.utils.Ratio
    :rtype: float
    """
    d = ratio_to_float(value.values[0])
    m = ratio_to_float(value.values[1])
    s = ratio_to_float(value.values[2])

    return d + (m / 60.0) + (s / 3600.0)

def get_exif_location(exif_data):
    """
    Returns the latitude and longitude, if available, from the provided exif_data (obtained through get_exif_data above)
    """
    lat = None
    lon = None
    alt = None

    gps_latitude = _get_if_exist(exif_data, 'GPS GPSLatitude')
    gps_latitude_ref = _get_if_exist(exif_data, 'GPS GPSLatitudeRef')
    gps_longitude = _get_if_exist(exif_data, 'GPS GPSLongitude')
    gps_longitude_ref = _get_if_exist(exif_data, 'GPS GPSLongitudeRef')
    gps_altitude = _get_if_exist(exif_data, 'GPS GPSAltitude')

    if gps_latitude and gps_latitude_ref and gps_longitude and gps_longitude_ref:
        lat = convert_to_degrees(gps_latitude)
        if gps_latitude_ref.values[0] != 'N':
            lat = 0 - lat

        lon = convert_to_degrees(gps_longitude)
        if gps_longitude_ref.values[0] != 'E':
            lon = 0 - lon

    if gps_altitude:
        alt = ratio_to_float(gps_altitude.values[0])

    return lat, lon, alt

def get_file_coords(fname):
    f = open(os.path.join(DIRECTORY, fname), 'rb')

    # Return Exif tags
    tags = exifread.process_file(f)
    return get_exif_location(tags)

coords = [ get_file_coords(fname) for fname in os.listdir(DIRECTORY) if 'DNG' in fname]
offsets = [ get_offset(coords[0], c) for c in coords ]

for o in offsets:
    print('[ %f, %f, %f ]' % o)
