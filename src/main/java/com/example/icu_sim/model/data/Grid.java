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
                Cell c = new Cell(x, y);
                // Ej: las 2 últimas columnas son UCI
                if(x >= width - 2) {
                    c.setIcuCell(true);
                    c.setIcuCapacity(4);
                }
                cells[x][y] = c;
            }
        }
    }

    public Cell getCell(int x, int y) {
        if(x >= 0 && x < width && y >= 0 && y < height) {
            return cells[x][y];
        }
        return null;
    }

    public int getWidth() { return width; }
    public int getHeight() { return height; }
    public Cell[][] getCells() { return cells; }
}
