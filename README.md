# PolyWorld

This is a polygon-based world generating module. It is based on the [map generating algorithm by Amit Patel of Red Blob Games](http://www-cs-students.stanford.edu/~amitp/game-programming/polygon-map-generation/).

![image1](images/2015-07-22_island.jpg "A screenshot of version 0.6.0")

The basic idea is to tesselate the terrain (using Fortune's algorithm) into a set of polygons.
Currently, only island worlds are supported. An island world is created as follows:

**Step 1:** Since the terrain is infinite, it needs to be partitioned into finite areas first:

![step1](images/step1_partition.png "An arbitrarily sized area in the world")

**Step 2:** The area is tesselated into polygons. The algorithms behind this are approximate Poisson disc sampling to generate a well-behaved set of sampling points, Fortune's algorithm to compute the initial Voronoi diagram and Lloyd's relaxation to regularize the polygon shapes.

![step2](images/step2_tesselation.png "An arbitrarily sized area in the world")

**Step 3:** Based on a random noise function such as Perlin noise, water and land vertices are defined. Starting at the border of the rectangle, the height of the island increases towards the center. Lake areas are flattened afterwards.

![step3](images/step3_elevation.png "The generated height map of the island")

**Step 4:** Since the elevation gradient is monotonously increasing, rivers can start at any corner of the map, flow downhill
and always reach either a lake or the ocean.

![step4](images/step4_rivers.png "The generated rivers always flow downwards until they hit the ocean")

**Step 5:** Based on height and the distance to rivers, a map of moisture is generated.

![step5](images/step5_moisture.png "The generated height map of the island")

**Step 6:** Each of the regions is assigned to exactly one biome as defined by Whittaker.

![step6](images/step6_biomes.png "The generated biomes of the island")

**Step 7:** Each of the biomes has its unique distribution of flowers, mushrooms and other small plants.

![step6](images/step7_flora.png "The generated flora of the island")

**Step 8:** Same goes for different tree types.

![step8](images/step8_trees.png "The fully generated island")

### Acknowledgements

This module is loosely based on the Java implementation by [Connor Clark](https://github.com/Hoten/Java-Delaunay).

### License

This module is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
