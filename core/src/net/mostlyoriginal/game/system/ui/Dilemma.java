package net.mostlyoriginal.game.system.ui;

/**
 * Created by Daan on 10-9-2014.
 */
public class Dilemma {

    public String id;
    public String[] text;
    public Choice[] choices;
    public String[] groups;

    /** crew required for all choices. */
    public String crew;

	public Dilemma() {
	}

	public static class Choice {
        public String[] label;
        public String[] success;
        public String[] failure;

		public Choice() {
		}

        /** crew required to make choice available. */
        public String crew;

        /** Chance of success. If no failure set, always success! */
        public int risk = 20;
    }

    public static enum DilemmaGroup {
        SCRIPTED,
        POSITIVE,
        NEGATIVE
    }
}
