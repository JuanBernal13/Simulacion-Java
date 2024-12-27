// backend/src/main/java/com/example/icu_sim/model/Grid.java
package com.example.icu_sim.model.data;

public class Grid {
    private int width;
    private int height;
    private Cell[][] cells;

    public Grid(int width, int height) {
        this.width = width;
        this.height = height;
        this.cells = new Cell[width][height];

        // Inicializar celdas
        for(int x = 0; x < width; x++) {
            for(int y = 0; y < height; y++) {
                cells[x][y] = new Cell(x, y);
            }
        }
    }

    public Cell getCell(int x, int y) {
        if(x >= 0 && x < width && y >= 0 && y < height) {
            return cells[x][y];
        }
        return null;
    }

    // Getters y Setters
    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public Cell[][] getCells() {
        return cells;
    }

    public void setCells(Cell[][] cells) {
        this.cells = cells;
    }
}
