package solver;

import java.util.List;
import java.util.ArrayList;
import java.util.Objects;

public class Vehicle {
    private int id;
    private int startX;
    private  int startY;
    private int length;
    private boolean isHorizontal;

    public Vehicle(int id, int startX, int startY, int length, boolean isHorizontal) {
        this.id = id;
        this.length = length;
        this.startX = startX;
        this.startY = startY;
        this.isHorizontal = isHorizontal;
    }

    public List<int[]> occupiedCells() {
        List<int[]> cells = new ArrayList<>();
        int x = startX;
        int y = startY;

        for (int i = 0; i < length; i++) {
            cells.add(new int[]{y, x});
            if (isHorizontal) {
                x++;
            } else {
                y++;
            }
        }

        return cells;
    }


    public int getId() {
        return id;
    }

    public int getStartX() {
        return startX;
    }

    public int getStartY() {
        return startY;
    }

    public void setStartX(int x) {
        startX = x;
    }

    public void setStartY(int y) {
        startY = y;
    }

    public int getLength() {
        return length;
    }

    public boolean getIsHorizontal() {
        return isHorizontal;
    }

    @Override
    public Vehicle clone() {
        return new Vehicle(id, startX, startY, length, isHorizontal);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Vehicle)) return false;
        Vehicle other = (Vehicle) obj;
        return this.id == other.id &&
                this.startX == other.startX &&
                this.startY == other.startY &&
                this.length == other.length &&
                this.isHorizontal == other.isHorizontal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, startX, startY, length, isHorizontal);
    }

}
