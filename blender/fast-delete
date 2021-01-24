import bpy

bl_info = {
    "name": "Fast Delete",
    "blender": (2, 79, 0),
    "category": "Object",
}


class FastDelete(bpy.types.Operator):
    bl_idname = "scene.fast_delete"
    bl_label = "Delete selected objects quickly"
    bl_options = {'REGISTER', 'UNDO'}

    def execute(self, context):
        for ob in context.selected_objects:
            context.scene.objects.unlink(ob)
            if ob.users==0: ob.user_clear()
    
        return {'FINISHED'}

def register():
    bpy.utils.register_class(FastDelete)

def unregister():
    bpy.utils.unregister_class(FastDelete)