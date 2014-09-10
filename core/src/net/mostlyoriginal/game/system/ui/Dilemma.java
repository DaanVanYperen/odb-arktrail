package net.mostlyoriginal.game.system.ui;

/**
 * Created by Daan on 10-9-2014.
 */
public class Dilemma {

    public String id;
    public String[] text;
    public Choice[] choices;

    public static class Choice {
        public String[] label;
        public String[] success;
        public String[] failure;

        /** Chance of success. If no failure set, always success! */
        public int chance = 50;
    }
}
