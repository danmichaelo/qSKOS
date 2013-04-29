package at.ac.univie.mminf.qskos4j.issues.language;

import at.ac.univie.mminf.qskos4j.issues.Issue;
import at.ac.univie.mminf.qskos4j.report.Report;
import at.ac.univie.mminf.qskos4j.util.TupleQueryResultUtil;
import at.ac.univie.mminf.qskos4j.util.vocab.SkosOntology;
import at.ac.univie.mminf.qskos4j.util.vocab.SparqlPrefix;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;
import org.openrdf.model.URI;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.query.*;
import org.openrdf.repository.RepositoryConnection;

import java.util.*;

/**
* Finds <a href="https://github.com/cmader/qSKOS/wiki/Quality-Issues#wiki-Omitted_or_Invalid_Language_Tags">Omitted or Invalid Language Tags</a>.
*/
public class OmittedOrInvalidLanguageTags extends Issue<Collection<Statement>> {

    private Collection<Statement> affectedStatements;
    private Map<String, Boolean> checkedLanguageTags;

    public OmittedOrInvalidLanguageTags() {
        super("oilt",
              "Omitted or Invalid Language Tags",
              "Finds omitted or invalid language tags of text literals",
              IssueType.ANALYTICAL
        );
    }

    @Override
    protected Collection<Statement> prepareData() throws OpenRDFException {
        affectedStatements = new ArrayList<Statement>();

        TupleQueryResult result = repCon.prepareTupleQuery(QueryLanguage.SPARQL, createMissingLangTagQuery()).evaluate();
        generateMissingLangTagMap(result);

        return affectedStatements;
	}

    @Override
    protected Report prepareReport(Collection<Statement> preparedData) {
        return new LanguageTagReport(affectedStatements);
    }

    private String createMissingLangTagQuery() throws OpenRDFException
    {
		return SparqlPrefix.SKOS +" "+ SparqlPrefix.SKOSXL +" "+ SparqlPrefix.RDFS +
			"SELECT ?s ?literal ?textProp "+
			"WHERE {" +
				"?s ?textProp ?literal . " +

				"FILTER isLiteral(?literal) " +
                createSkosTextualPropertiesFilter()+
			"}";
	}

    private String createSkosTextualPropertiesFilter() throws OpenRDFException
    {
        RepositoryConnection skosRepConn = SkosOntology.getInstance().getRepository().getConnection();
        try {
            TupleQuery skosTextPropQuery = skosRepConn.prepareTupleQuery(QueryLanguage.SPARQL, createSkosTextualPropertiesQuery());
            return TupleQueryResultUtil.getFilterForBindingName(skosTextPropQuery.evaluate(), "textProp");
        }
        finally {
            skosRepConn.close();
        }
    }

    private String createSkosTextualPropertiesQuery() {
        return SparqlPrefix.SKOS +" "+  SparqlPrefix.RDFS +
            "SELECT ?textProp WHERE {" +
                "{?textProp rdfs:subPropertyOf* rdfs:label}" +
                "UNION" +
                "{?textProp rdfs:subPropertyOf* skos:note}" +
            "}";
    }

	private void generateMissingLangTagMap(TupleQueryResult result)
		throws QueryEvaluationException 
	{
        checkedLanguageTags = new HashMap<String, Boolean>();
		
		while (result.hasNext()) {
			BindingSet queryResult = result.next();
            Resource subject = (Resource) queryResult.getValue("s");
            URI predicate = (URI) queryResult.getValue("textProp");
            Literal literal = (Literal) queryResult.getValue("literal");

            if (literal.getDatatype() == null) {
				String langTag = literal.getLanguage();			
				if (langTag == null || !isValidLangTag(langTag)) {
                    affectedStatements.add(new StatementImpl(subject, predicate, literal));
				}
			}
		}
	}
	
	private boolean isValidLangTag(String langTag) {
        Boolean validTag = checkedLanguageTags.get(langTag);

        if (validTag == null) {
            validTag = isSyntacticallyCorrect(langTag) && hasIsoLanguage(langTag);
            checkedLanguageTags.put(langTag, validTag);
        }

        return validTag;
    }

    private boolean isSyntacticallyCorrect(String langTag) {
        try {
            new Locale.Builder().setLanguageTag(langTag);
        }
        catch (IllformedLocaleException e) {
            return false;
        }

        return true;
    }

    private boolean hasIsoLanguage(String langTag) {
        Locale locale = new Locale.Builder().setLanguageTag(langTag).build();

        boolean hasIsoLanguage = false;
        for (String isoLanguage : Locale.getISOLanguages()) {
            if (isoLanguage.equalsIgnoreCase(locale.getLanguage())) {
                hasIsoLanguage = true;
                break;
            }
        }

        return hasIsoLanguage;
	}

}
