# PolyWorld

This is a polygon-based world generating module. It is based on the [map generating algorithm by Amit Patel of Red Blob Games](http://www-cs-students.stanford.edu/~amitp/game-programming/polygon-map-generation/).

![image1](images/2015-07-22_island.jpg "A screenshot of version 0.6.0")

The basic idea is to tesselate the terrain (using Fortune's algorithm) into a set of polygons.
Currently, only island worlds are supported.

### Acknowledgements

This module is loosely based on the Java implementation by [Connor Clark](https://github.com/Hoten/Java-Delaunay).

![image2](images/2014-05-06_original_work.png "A screenshot from the original renderer")

### License

This module is licensed under the [Apache 2.0 License](http://www.apache.org/licenses/LICENSE-2.0.html).
