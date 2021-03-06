package at.ac.univie.mminf.qskos4j.issues.language;

import at.ac.univie.mminf.qskos4j.issues.Issue;
import at.ac.univie.mminf.qskos4j.issues.concepts.InvolvedConcepts;
import at.ac.univie.mminf.qskos4j.report.Report;
import at.ac.univie.mminf.qskos4j.util.progress.MonitoredIterator;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.URI;
import org.openrdf.model.Value;
import org.openrdf.query.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds all concepts with incomplete language coverage (<a href="https://github.com/cmader/qSKOS/wiki/Quality-Issues#wiki-Incomplete_Language_Coverage">Incomplete Language Coverage</a>
 */
public class IncompleteLanguageCoverage extends Issue<Map<Value, Collection<String>>> {

    private final Logger logger = LoggerFactory.getLogger(IncompleteLanguageCoverage.class);

	private Map<Value, Collection<String>> languageCoverage, incompleteLanguageCoverage;
	private Set<String> distinctLanguages;
    private InvolvedConcepts involvedConcepts;

    public IncompleteLanguageCoverage(InvolvedConcepts involvedConcepts) {
        super(involvedConcepts,
            "ilc",
            "Incomplete Language Coverage",
            "Finds concepts lacking description in languages that are present for other concepts",
            IssueType.ANALYTICAL
        );

        this.involvedConcepts = involvedConcepts;
    }

    @Override
    protected Map<Value, Collection<String>> computeResult() throws OpenRDFException {
		incompleteLanguageCoverage = new HashMap<Value, Collection<String>>();
		
		checkLanguageCoverage();
		generateIncompleteLanguageCoverageMap();
		
		return incompleteLanguageCoverage;
	}

    @Override
    protected Report generateReport(Map<Value, Collection<String>> preparedData) {
        return new IncompleteLangCovReport(incompleteLanguageCoverage);
    }

    private void checkLanguageCoverage() throws OpenRDFException
	{
		languageCoverage = new HashMap<Value, Collection<String>>();
		
		Iterator<URI> it = new MonitoredIterator<URI>(involvedConcepts.getResult(), progressMonitor);
		while (it.hasNext()) {
            Value concept = it.next();

            try {
			    TupleQuery query = repCon.prepareTupleQuery(QueryLanguage.SPARQL, createLanguageLiteralQuery(concept));
                addToLanguageCoverageMap(concept, query.evaluate());
            }
            catch (OpenRDFException e) {
                logger.error("Error finding languages for concept '" +concept+ "'");
            }
		}
	}
	
	private String createLanguageLiteralQuery(Value concept) {
		return "SELECT DISTINCT ?literal "+
			"WHERE {" +
			"<"+concept+"> ?p ?literal ."+
			"FILTER isLiteral(?literal) "+
			"FILTER langMatches(lang(?literal), \"*\")" +
			"}";
	}
	
	private void addToLanguageCoverageMap(Value concept, TupleQueryResult result)
		throws QueryEvaluationException 
	{
		while (result.hasNext()) {
			BindingSet queryResult = result.next();
			Literal literal = (Literal) queryResult.getValue("literal");
			
			String language = literal.getLanguage();
			addToLanguageCoverageMap(concept, language);
			addToDistinctLanguages(language);
		}
	}
	
	private void addToLanguageCoverageMap(Value value, String langTag) {
		Collection<String> langTags = languageCoverage.get(value);
		if (langTags == null) {
			langTags = new HashSet<String>();
			languageCoverage.put(value, langTags);
		}
		langTags.add(langTag);
	}
	
	private void addToDistinctLanguages(String langTag) {
		if (distinctLanguages == null) {
			distinctLanguages = new HashSet<String>();
		}
		distinctLanguages.add(langTag);
	}
	
	private void generateIncompleteLanguageCoverageMap() {
		incompleteLanguageCoverage = new HashMap<Value, Collection<String>>();
		
		for (Value resource : languageCoverage.keySet()) {
			Collection<String> coveredLanguages = languageCoverage.get(resource);
			Collection<String> notCoveredLanguages = getNotCoveredLanguages(coveredLanguages);
			if (!notCoveredLanguages.isEmpty()) {
				incompleteLanguageCoverage.put(resource, notCoveredLanguages);
			}
		}
	}
	
	private Collection<String> getNotCoveredLanguages(Collection<String> coveredLanguages) {
		Set<String> ret = new HashSet<String>(distinctLanguages);
		ret.removeAll(coveredLanguages);
		return ret;
	}

}
