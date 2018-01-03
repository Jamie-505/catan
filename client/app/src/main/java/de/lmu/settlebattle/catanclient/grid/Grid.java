package de.lmu.settlebattle.catanclient.grid;

import android.graphics.Point;

/**
 * *   * *   *
*   * *   * *
 * *   * *   *
*   * *   * *
 * *   * *   *
 * A grid of hex nodes with axial coordinates.
 */
public class Grid {

  public final int radius; //The radius of the grid - the count of rings around the central node
  public final int scale; //The radius of the single node in grid

  //Derived node properties
  public final int width; //The width of the single hexagon node
  public final int height; //The height of the single hexagon node
  public final int centerOffsetX; //Relative center coordinate within one node
  public final int centerOffsetY; //Relative center coordinate within one node

  public Cube[] nodes;

  /**
   * Construing a Grid with a set of cubes and scale
   * @param radius The count of rings around the central node
   * @param scale The radius of the hexagon in pixels
   */
  public Grid(int radius, int scale) {
    this.radius = radius;
    this.scale = scale;

    //Init derived node properties
    width = (int) (Math.sqrt(3) * scale);
    height = 2 * scale;
    centerOffsetX = width/2;
    centerOffsetY = height/2;

    //Init nodes
    generateHexagonalShape(radius);
  }

  public Point hexToPixel(Hex hex) {
    int x = 0;
    int y = 0;

    x = (int) (width * (hex.getX() + 0.5 * hex.getY()));
    y = (int) (scale * 1.5 * hex.getY());

    return new Point(x, y);
  }

  public Hex pixelToHex(float x, float y) {
    float q = (float) (Math.sqrt(3)/3 * x - 1/3 * y) / scale;
    float r = (2/3 * y) / scale;

    return new Hex(q, r);

    //TODO RECTANGLE
  }

  private void generateHexagonalShape(int radius) throws ArrayIndexOutOfBoundsException {
    nodes = new Cube[getNumberOfNodesInGrid(radius)];
    int i = 0;

    for (int x = -radius; x <= radius; x++) {
      for (int y = radius; y >= -radius; y--) {
        int z = -x-y;
        if (Math.abs(x) <= radius && Math.abs(y) <= radius && Math.abs(z) <= radius) {
          nodes[i++] = new Cube(x, y, z);
        }
      }
    }
  }

  /**
   * @return Number of hexagons inside of a hex or oddR rectangle shaped grid with the given radius
   */
  public static int getNumberOfNodesInGrid(int radius) {
    return (int) (3 * Math.pow(radius+1, 2) - 3 * (radius +1) + 1);
  }

  public static int getGridWidth(int radius, int scale) {
    return (int) ((2*radius + 1) * Math.sqrt(3) * scale);
  }

  public static int getGridHeight(int radius, int scale) {
    return (int) (scale * ((2*radius + 1) * 1.5 + 0.5));
  }
}
