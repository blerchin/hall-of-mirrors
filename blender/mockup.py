bl_info = {
    "name": "Mockup Drone Photos",
    "blender": (2, 80, 0),
    "category": "Object",
}

import bpy
import math
import json
import os

DIRECTORY = "/Users/blerchin/workspace/hall-of-mirrors/images/layouts/7-bigtujunga/"
SCALE = .3
FRAME_RATIO = 1.78

FRAME_SIZE = 3;
LENS_FOV = math.radians(94)

def get_frame_width(dist, fov = LENS_FOV):
    return 2 * dist * math.tan(fov / 2)

def get_frame_dist(width, fov = LENS_FOV):
    return width / (2 * math.tan(fov / 2))

def draw(context):
        scene = context.scene
        
        with open(os.path.join(DIRECTORY, "positions.json")) as f:
            positions = json.loads(f.read())
        whitelist = []
        try:
            with open(os.path.join(DIRECTORY, "whitelist.txt")) as f:
                whitelist = [l.strip() for l in f.readlines()]
        except e:
            print("No whitelist found.")
        positions = [p for p in positions if p["name"] in whitelist]
        for p in positions:
            scene.cursor_location = (0, 0, 0)
            offset = [coord * SCALE for coord in p["offset"]]
            heading = math.radians(-p["heading"])
            pitch = math.radians(p["pitch"])
            bpy.ops.mesh.primitive_plane_add(radius=FRAME_SIZE)
            bpy.ops.transform.rotate(axis=(1,0,0), value=math.radians(90))
            bpy.ops.transform.resize(value=(1, 1, 1/FRAME_RATIO))
            context.object.name = p["name"]
            bpy.ops.transform.translate(value=offset)
            bpy.ops.transform.translate(value=(0, 5 * get_frame_dist(FRAME_SIZE), 0))
            scene.cursor_location = offset
            bpy.ops.object.origin_set(type="ORIGIN_CURSOR")
            bpy.ops.transform.rotate(axis=(1,0,0), value=pitch)
            bpy.ops.transform.rotate(axis=(0,0,1), value=heading)
            
            plane = context.object
            mat = bpy.data.materials.new(p["name"])
            mat.emit = 0.0
            mat.use_transparency = True
            mat.alpha = 0.7
            tex = bpy.data.textures.new(p["name"], type="IMAGE")
            img = bpy.data.images.load(os.path.join(DIRECTORY, p["name"]))
            tex.image = img
            slot = mat.texture_slots.add()
            slot.texture_coords = "ORCO"
            slot.texture = tex
            
            #UV unwrap
            bpy.ops.object.mode_set(mode="EDIT")
            bpy.ops.uv.smart_project()
            bpy.ops.object.mode_set(mode="OBJECT")

            #setup nodes for cycles
            mat.use_nodes = True
            nodes = mat.node_tree.nodes
            diffuse = nodes.get("Diffuse BSDF")
    
            if diffuse != None:
                nodes.remove(diffuse)
            node_output = nodes.get("Material Output")
            
            node_image = nodes.new(type="ShaderNodeTexImage")
            node_image.image = img
            node_glass = nodes.new(type="ShaderNodeBsdfGlass")
            
            links = mat.node_tree.links
            links.new(node_image.outputs[0], node_glass.inputs[0])
            links.new(node_glass.outputs[0], node_output.inputs[0])
            
            plane.data.materials.append(mat)
            

        bpy.ops.object.lamp_add(type="SUN", location=(0,0,50))
        context.object.data.energy = 10
        context.object.data.distance = 100
        bpy.ops.object.select_all(action="DESELECT")
        bpy.ops.object.select_by_type(type="MESH")
        bpy.ops.transform.translate(value=(50, 13, 50))
        bpy.ops.mesh.primitive_plane_add(radius=35, location=(0, 0, 0))
        bpy.ops.mesh.primitive_plane_add(radius=35, location=(0, 35, 35), rotation=(math.pi/2, 0, 0))
        bpy.ops.mesh.primitive_plane_add(radius=35, location=(35, 0, 35), rotation=(0, math.pi/2, 0))
        
        
class MockupDronePhotos(bpy.types.Operator):
    bl_idname = "scene.mockup_drone_photos"
    bl_label = "mocks up drone photos"
    bl_options = {'REGISTER', 'UNDO'}

    def execute(self, context):
        #draw(context)



        return {'FINISHED'}

def register():
    bpy.utils.register_class(MockupDronePhotos)

def unregister():
    bpy.utils.unregister_class(MockupDronePhotos)

if __name__ == "__main__":
    draw(bpy.context)
