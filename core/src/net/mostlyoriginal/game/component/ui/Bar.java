package net.mostlyoriginal.game.component.ui;

/**
 * @author Daan van Yperen
 */
public class Bar extends Label {

    public int value;
    public final String animationIdEmpty;
    public int valueEmpty;
    public String animationId;

    public Bar(String text, String animationId, int value, String animationIdEmpty, int valueEmpty) {
        super(text);
        this.animationId = animationId;
        this.value = value;
        this.animationIdEmpty = animationIdEmpty;
        this.valueEmpty = valueEmpty;
    }
}
