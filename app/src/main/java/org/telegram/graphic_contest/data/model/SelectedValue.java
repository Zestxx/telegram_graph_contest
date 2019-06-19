package org.telegram.graphic_contest.data.model;


public class SelectedValue {

    private int firstIndex;
    private int secondIndex;

    public SelectedValue() {
        clear();
    }

    public void set(int firstIndex, final int secondIndex) {
        this.firstIndex = firstIndex;
        this.secondIndex = secondIndex;
    }

    public void set(final SelectedValue selectedValue) {
        this.firstIndex = selectedValue.firstIndex;
        this.secondIndex = selectedValue.secondIndex;
    }

    public void clear() {
        set(Integer.MIN_VALUE, Integer.MIN_VALUE);
    }


    public boolean isSet() {
        return firstIndex >= 0 && secondIndex >= 0;
    }

    public int getSecondIndex() {
        return secondIndex;
    }

}
