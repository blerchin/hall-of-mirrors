import bpy
import os

#filepath = bpy.data.filepath
#directory = os.path.dirname(filepath)
DEFAULT_DISPLACEMENT = 100
directory = "/Users/blerchin/workspace/hall-of-mirrors/images/layouts/7-bigtujunga/"
 
scene = bpy.context.scene
with open(os.path.join(directory, "frames.csv"), 'w') as f:
    for o in scene.objects:
        if "DJI" in o.name:
            f.write('%s,%i\n' % (o.name, DEFAULT_DISPLACEMENT))
        