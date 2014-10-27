package gr.atc.heads.io;

import java.util.List;

import gr.atc.heads.model.HeadsPoint;

/**
 * Created by kGiannakakis on 10/6/2014.
 */
public interface HeadsSearchListener {

    void searchFailed(String message);
    void searchCompleted(List<HeadsPoint> results);
}
