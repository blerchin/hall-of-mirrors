bl_info = {
    "name": "Mockup Drone Photos",
    "blender": (2, 80, 0),
    "category": "Object",
}

import bpy
import math
import json
import os
from pprint import pprint

DIRECTORY = "/Users/blerchin/workspace/hall-of-mirrors/images/layouts/7-bigtujunga/"
SCALE = 26
FRAME_RATIO = 1.7

FRAME_SIZE = 120;
LENS_FOV = math.radians(94)
CEILING_HEIGHT = 12 * 12 * 25.4
WALL_RADIUS = CEILING_HEIGHT / 2

def get_frame_width(dist, fov = LENS_FOV, scale=SCALE):
    return 2 * dist * math.tan(fov / 2)

def get_frame_dist(width, fov = LENS_FOV):
    return width / (2 * math.tan(fov / 2))

def apply_boolean(ctx, positive, negative):
    ctx.scene.objects.active = positive
    boolean = positive.modifiers.new("Boolean", "BOOLEAN")
    boolean.object = negative
    boolean.operation = "DIFFERENCE"
    bpy.ops.object.modifier_apply(modifier="Boolean")
    

def draw(context):
        scene = context.scene
        for ob in scene.objects:
            bpy.context.scene.objects.unlink(ob)
            if ob.users==0: ob.user_clear()
        
        with open(os.path.join(DIRECTORY, "positions.json")) as f:
            positions = json.loads(f.read())
        whitelist_positions = []
        try:
            with open(os.path.join(DIRECTORY, "frames.csv")) as f:
                frames = [l.strip().split(',') for l in f.readlines()]
                for p in positions:
                    matches = [f for f in frames if f[0] == p["name"]]
                    if len(matches) > 0:
                        whitelist_positions.append(dict(p, **{"displacement": matches[0][1] }))         
        except:
            print("No whitelist found.")
            whitelist_positions = positions
            
        bpy.ops.mesh.primitive_plane_add(radius=WALL_RADIUS, location=(0, 0, CEILING_HEIGHT))
        ceiling = context.object
            
        for p in whitelist_positions:
            scene.cursor_location = (0, 0, 0)
            offset = [coord * SCALE for coord in p["offset"]]
            heading = math.radians(-p["heading"])
            pitch = math.radians(p["pitch"])
            displacement = float(p["displacement"]) if "displacement" in p else 100
            frame_size = get_frame_width(displacement)
            bpy.ops.mesh.primitive_plane_add(radius=frame_size/2)
            bpy.ops.transform.rotate(axis=(1,0,0), value=math.radians(90))
            bpy.ops.transform.resize(value=(1, 1, 1/FRAME_RATIO))
            context.object.name = p["name"]
            bpy.ops.transform.translate(value=offset)
            bpy.ops.transform.translate(value=(0, displacement, 0))
            scene.cursor_location = offset
            bpy.ops.object.origin_set(type="ORIGIN_CURSOR")
            bpy.ops.transform.rotate(axis=(1,0,0), value=pitch)
            bpy.ops.transform.rotate(axis=(0,0,1), value=heading)

            bpy.ops.transform.translate(value=(4000, 1500, 5000))
            bpy.ops.object.transform_apply(rotation=True, scale=True)
            plane = context.object
            world = plane.matrix_world.to_translation()
            
            #setup material for text
            black = bpy.data.materials.new(p["name"])
            black.use_nodes = True
            nodes = black.node_tree.nodes
            diffuse = nodes.get("Diffuse BSDF")
            diffuse.color = (0.0,0.0,0.0)

            index = 0
            for v in context.object.data.vertices:
                scene.cursor_location = (0, 0, 0)
                coords = (v.co[0] + world[0], v.co[1] + world[1], v.co[2] + world[2])
                height = CEILING_HEIGHT - coords[2] + 2
                bpy.ops.mesh.primitive_cylinder_add(
                    depth=height,
                    radius=0.5
                )
                bpy.ops.transform.translate(value=(coords[0], coords[1], coords[2] + height/2))
                apply_boolean(context, ceiling, context.object)
                
                bpy.ops.object.text_add(location=(coords[0], coords[1], CEILING_HEIGHT), radius=10, enter_editmode=False)
                label = p["name"].split('_')[1].split('.')[0]
                label += " %s" % chr(ord('a') + index)
                context.object.data.body = label
                context.object.data.materials.append(black)
                bpy.ops.object.convert(target='MESH')
                bpy.ops.object.mode_set(mode='EDIT')
                bpy.ops.mesh.select_mode(type='FACE')
                bpy.ops.mesh.select_all(action='SELECT')
                bpy.ops.mesh.extrude_region_move(
                    TRANSFORM_OT_translate={"value": (0,0,20)}
                )
                bpy.ops.object.mode_set(mode='OBJECT')
                bpy.ops.transform.translate(value=(0,0,-12))
                
                text = context.object
                apply_boolean(context, ceiling, text)
                context.scene.objects.active = text
                bpy.ops.object.delete()
                
                index += 1
                
                
            
            bpy.context.scene.objects.active = plane
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
            

        bpy.ops.object.lamp_add(type="SUN", location=(0,0,2000))
        context.object.data.energy = 10
        context.object.data.distance = 100
        bpy.ops.object.select_all(action="DESELECT")
        bpy.ops.mesh.primitive_plane_add(radius=WALL_RADIUS, location=(0, 0, 0))
        bpy.ops.mesh.primitive_plane_add(radius=WALL_RADIUS, location=(0, WALL_RADIUS, WALL_RADIUS), rotation=(math.pi/2, 0, 0))
        bpy.ops.mesh.primitive_plane_add(radius=WALL_RADIUS, location=(WALL_RADIUS, 0, WALL_RADIUS), rotation=(0, math.pi/2, 0))

            
        
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
