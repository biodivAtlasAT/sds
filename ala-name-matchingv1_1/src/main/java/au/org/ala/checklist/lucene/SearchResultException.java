
package au.org.ala.checklist.lucene;

import java.util.List;

import au.org.ala.checklist.lucene.model.NameSearchResult;

/**
 *  @see HomonymException
 * @author Natasha
 */
public class SearchResultException extends Exception {
    protected List<NameSearchResult> results;
    public SearchResultException(String msg){
        super(msg);
    }
    public SearchResultException(String msg,List<NameSearchResult> results){
        this(msg);
        this.results = results;
    }
    public List<NameSearchResult> getResults(){
        return results;
    }
}
