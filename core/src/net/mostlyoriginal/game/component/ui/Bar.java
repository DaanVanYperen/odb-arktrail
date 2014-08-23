package net.mostlyoriginal.game.component.ui;

/**
 * @author Daan van Yperen
 */
public class Bar extends Label {

    public int value;
    public String animationId;

    public Bar(String text, String animationId, int value) {
        super(text);
        this.animationId = animationId;
        this.value = value;
    }
}
