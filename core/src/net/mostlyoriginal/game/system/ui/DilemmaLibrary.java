package net.mostlyoriginal.game.system.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Repository for all dilemma. */
public class DilemmaLibrary {
    public Dilemma[] dilemmas;

	public DilemmaLibrary() {
    }

    Map<String, List<Dilemma>> grouped = new HashMap<>();

    /** Return dilemma, or <code>null</code> if empty. */
    public Dilemma getById( String id )
    {
        for (Dilemma dilemma : dilemmas) {
            if ( dilemma.id != null && dilemma.id.equals(id)) return dilemma;
        }
        return null;
    }

    /** Map dilemma to groups */
    public void assignToGroups() {
        for (Dilemma dilemma : dilemmas) {
            if (dilemma.groups != null) {
                for (String group : dilemma.groups) {
                    addToGroup(dilemma, group);
                }
            }
        }
    }

    private void addToGroup(Dilemma dilemma, String group) {
        getGroup(group).add(dilemma);
    }

    public List<Dilemma> getGroup(String group) {
        List<Dilemma> list = grouped.get(group);
        if ( list == null )
        {
            list = new ArrayList<>();
            grouped.put(group, list);
        }
        return list;
    }
}
