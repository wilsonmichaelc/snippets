import open3d
import json
import os
import numpy

def run_dataset(base_path):
    """
        Remove points fromt the 3D Model PLY file that are outside the cameras bounds.

        Args:
            base_path: base path to source directory
    """

    odm_georef_model_file = f'{base_path}/odm/entwine_pointcloud/ept-sources/odm_georeferenced_model.json'
    geojson_file = f'{base_path}/odm/odm_report/shots.geojson'
    ply_file = f'{base_path}/odm/opensfm/undistorted/openmvs/scene_dense_dense_filtered.ply'
    ply_out = f'{base_path}/ply'

    if not os.path.exists(ply_out):
            os.makedirs(ply_out)

    minx = float('inf')
    miny = float('inf')
    minz = float('inf')
    maxx = float('-inf')
    maxy = float('-inf')
    maxz = float('-inf')

    offset = { "X": None, "Y": None }

    # Read in the offsets from the odm_georeferenced_model file
    with open(odm_georef_model_file) as georef:
        g = json.load(georef)
        for s in g["schema"]:
            if s["name"] == "X":
                offset["X"] = s["offset"]
            if s["name"] == "Y":
                offset["Y"] = s["offset"]

    # Read in camera positions from geojson file and calculate the min/max x,y,z 
    with open(geojson_file) as geojson:
        data = json.load(geojson)
        print(len(data["features"]))
        for feature in data["features"]:
            coords = feature["properties"]["translation"]
            x = coords[0]-offset["X"]
            y = coords[1]-offset["Y"]
            z = coords[2]
            if (x < minx): minx = x
            if (y < miny): miny = y
            if (z < minz): minz = z
            if (x > maxx): maxx = x
            if (y > maxy): maxy = y
            if (z > maxz): maxz = z


#######################################################################################




####
#### First Iteration
####
# Read point cloud from PLY
pcd = o3d.io.read_point_cloud(ply_file)

# Crop the point cloud
bbox = o3d.geometry.AxisAlignedBoundingBox(min_bound=(minx, miny, minz), max_bound=(maxx, maxy, maxz))
cropped = pcd.crop(bbox)

# Write the new point cloud
o3d.io.write_point_cloud(f'{ply_out}/cropped.ply', cropped, False, True, False)



####
#### Second Iteration
####
# The bounding box
bbox = open3d.geometry.AxisAlignedBoundingBox(min_bound=(minx, miny, minz), max_bound=(maxx, maxy, maxz))

# Read point cloud from PLY
pcd = open3d.io.read_point_cloud(ply_file)

# Crop the point cloud
pcd = pcd.crop(bbox)

# Remove NaN & Infinite points
pcd.remove_non_finite_points(True, True)

# New cloud
cropped = open3d.geometry.PointCloud()
cropped.points = open3d.utility.Vector3dVector(numpy.asarray(pcd.points))

# Visualize
# open3d.visualization.draw_geometries([cropped])

# Write the new point cloud
open3d.io.write_point_cloud(f'{ply_out}/cropped.ply', cropped, False, True, False)




####
#### Third Iteration
####
# The bounding box
bbox = open3d.geometry.AxisAlignedBoundingBox(min_bound=(minx, miny, minz), max_bound=(maxx, maxy, maxz))

# Read point cloud from PLY
pcd = open3d.io.read_point_cloud(ply_file)

# Crop the point cloud
pcd = pcd.crop(bbox)

# Remove NaN & Infinite points
pcd.remove_non_finite_points(True, True)

# New cloud
cropped = open3d.geometry.PointCloud()
cropped.points = pcd.points

# Visualize
# open3d.visualization.draw_geometries([cropped])

# Write the new point cloud
open3d.io.write_point_cloud(f'{ply_out}/cropped.ply', cropped, False, True, False)
