package gr.atc.heads.model;

import java.io.Serializable;

/**
*
* @author Panagiotis Kokkinakis
*/
@SuppressWarnings("serial")
public class Tag implements Serializable
{
	long id;
    String name;

    public Tag(long id, String name) {
        this.id = id;
        this.name = name;
    }

	public String getName() {
		return name;
	}

	public long getId() {
		return id;
	}
}
