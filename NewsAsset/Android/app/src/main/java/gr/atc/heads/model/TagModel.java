package gr.atc.heads.model;

import java.io.Serializable;

/**
 * Created by kGiannakakis on 19/6/2014.
 */
public class TagModel implements Serializable, Comparable<TagModel> {

    private String name;
    private boolean selected;
    private long id;

    public TagModel(String name, long id) {
        this.name = name;
        this.id = id;
        selected = false;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Override
    public int compareTo(TagModel o) {
        return name.compareTo(o.getName());
    }
}
