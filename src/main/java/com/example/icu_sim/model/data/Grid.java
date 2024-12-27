// backend/src/main/java/com/example/icu_sim/model/data/Grid.java
package com.example.icu_sim.model.data;

public class Grid {
    private int width;
    private int height;
    private Cell[][] cells;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];

        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                Cell cell = new Cell(x, y);

                // Ejemplo: dos últimas columnas como UCI
                if(x > width - 3) {
                    cell.setIcuCell(true);
                    cell.setIcuCapacity(5); // cada celda UCI con 5 camas
                }
                cells[x][y] = cell;
            }
        }
    }

    public Cell getCell(int x, int y) {
        if(x >= 0 && x < width && y >= 0 && y < height) {
            return cells[x][y];
        }
        return null;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public Cell[][] getCells() {
        return cells;
    }
}
