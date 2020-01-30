import os
import exifread
import csv
import json
from geopy import distance
from math import cos, sin, radians
from geographiclib.geodesic import Geodesic
from dateutil import tz
from datetime import datetime, timedelta

DIRECTORY = os.path.join(os.getcwd(), 'images/layouts/7-bigtujunga');

def _get_if_exist(data, key):
    if key in data:
        return data[key]

    return None

def ratio_to_float(value):
    return float(value.num) / float(value.den)

def get_offset(point_a, point_b):
    lat_a, lon_a, alt_a = point_a
    lat_b, lon_b, alt_b = point_b
    geo = Geodesic.WGS84.Inverse(
        lat_a,
        lon_a,
        lat_b,
        lon_b
    )
    dist = geo['s12']
    angle = geo['azi1'] + 90
    x = -1 * cos(radians(angle)) * dist
    y = sin(radians(angle)) * dist
    z = alt_b - alt_a
    return (x, y, z)

def get_logtime(log):
    return datetime.strptime(log["datetime(utc)"], "%Y-%m-%d %H:%M:%S").replace(tzinfo=tz.tzutc())


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



class DronePhotoPositioner:
    def __init__(self, dir = DIRECTORY):
        self.logged_photos = []
        self.files = [ fname for fname in os.listdir(DIRECTORY) if 'jpg' in fname]
        self.files.sort()

        self.get_flight_data()

    def get_positions(self):
        positions = [ self.get_file_position(fname) for fname in self.files]
        return [ dict(p, **{ "offset": get_offset(positions[0]["coords"], p["coords"]) }) for p in positions ]

    def print_positions(self):
        print(self.logged_photos)
        for p in self.get_positions():
            data = list(p["offset"])
            data.append(p["heading"])
            data.append(p["pitch"])
            print('[ %f, %f, %f ] [%f, %f]' % tuple(data))

    def get_exif_time(self, exif_data):
        return str(_get_if_exist(exif_data, 'EXIF DateTimeOriginal'))

    def get_exif_location(self, exif_data):
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

    def get_gimbal_heading_pitch(self, coords, time):
        #!! this will fail if DST changed since capture
        tzname = datetime.now(tz.tzlocal()).tzname()
        timestring = "%s %s" % (time, tzname)
        timestamp = datetime.strptime(timestring, "%Y:%m:%d %H:%M:%S %Z").astimezone(tz.tzutc())

        closest_log = None
        for log in self.logged_photos:
            logtime = get_logtime(log)
            closest_time = get_logtime(closest_log) if closest_log else None
            if closest_log == None or abs(logtime - timestamp) < abs(closest_time - timestamp):
                closest_log = log

        if closest_log == None or abs(timestamp - get_logtime(closest_log)) > timedelta(seconds=2):
            return (0, 0)
        return (
            float(closest_log["gimbal_heading(degrees)"]),
            float(closest_log["gimbal_pitch(degrees)"])
        )


    def get_file_position(self, fname):
        f = open(os.path.join(DIRECTORY, fname), 'rb')

        # Return Exif tags
        tags = exifread.process_file(f)
        lat, lon, alt = self.get_exif_location(tags)
        print('%s, %f, %f' % (fname, lat, lon))
        coords = self.get_exif_location(tags)
        time = self.get_exif_time(tags)
        gimbal = self.get_gimbal_heading_pitch(coords, time)
        return {
            "coords": coords,
            "name": fname,
            "time": time,
            "heading": gimbal[0],
            "pitch": gimbal[1]
        }


    def get_flight_data(self):
        try:
            with open(os.path.join(DIRECTORY, 'flight-data.csv'), 'r') as f:
                data = csv.DictReader(f)
                for row in data:
                    if int(row['isPhoto']) == 1:
                        self.logged_photos.append(row)
        except:
            print('No flight data found.')

if __name__ == "__main__":
    positioner = DronePhotoPositioner()
    positioner.print_positions()
    with open(os.path.join(DIRECTORY, "positions.json"), "w") as f:
        f.write(json.dumps(positioner.get_positions()))
