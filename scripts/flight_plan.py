from geographiclib.geodesic import Geodesic
from requests import post
from math import atan, degrees
import json
import os

GOOGLE_API_KEY = os.environ['GOOGLE_API_KEY']

LOCATIONS = [
    [ 37.8385405, -121.5779735 ],
    [ 36.97455, -118.20957 ],
    [ 34.327119, -118.497649 ],
    [ 33.842041, -117.4366907 ],
    [ 34.148177, -115.120787 ],
    [ 33.705652, -115.628957 ],
    [ 33.809354, -115.44997 ],
    [ 34.2968629, -114.1407968 ]
]

CURRENT_LOCATION = [ 34.327119, -118.497649 ]


def latlon(pair):
    if "location" in pair:
        return {
            "latitude": pair["location"]["lat"],
            "longitude": pair["location"]["lng"],
            "elevation": pair["elevation"]
        }
    else:
        return { "latitude": pair[0], "longitude": pair[1] }

def get_bearing_and_pitch(point1, point2, scale = 100):
    geo = Geodesic.WGS84.Inverse(
        point1["latitude"],
        point1["longitude"],
        point2["latitude"],
        point2["longitude"]
    )
    dist = geo['s12']
    if dist == 0:
        return None
    bearing = geo['azi1']
    if bearing < 0:
        bearing += 360
    elevation_change = point1["elevation"] - point2["elevation"]
    pitch = degrees(atan(scale * elevation_change / dist))
    return bearing, pitch

def get_elevations():
    locations =  '|'.join([ '%f,%f' % (l[0], l[1]) for l in LOCATIONS ])
    response = post(
        'https://maps.googleapis.com/maps/api/elevation/json?locations=%s&key=%s' % (
            locations,
            GOOGLE_API_KEY
        )
    )
    return response.json()['results']

def format_location(location):
    coords = latlon(location);
    coords.update({ "elevation": location["elevation"] })
    return coords

def get_current(locations, threshold = .0001):
    latitude, longitude = CURRENT_LOCATION
    for l in locations:
        if abs(l["latitude"] - latitude) < threshold and abs(l["longitude"] - longitude) < threshold:
            return l

def annotate_location(current, location):
    bearing, pitch = get_bearing_and_pitch(current, location)
    location.update({ "bearing": bearing, "pitch": pitch})
    return location


locations_with_elevation = [format_location(l) for l in get_elevations()]
current_with_elevation = get_current(locations_with_elevation)
print('Current')
print(current_with_elevation)
plan = [annotate_location(current_with_elevation, l) for l in locations_with_elevation if l != current_with_elevation]
print('Plan')
for l in plan:
    print('Bearing: %f, Pitch: %f' % (l['bearing'], l['pitch']))
