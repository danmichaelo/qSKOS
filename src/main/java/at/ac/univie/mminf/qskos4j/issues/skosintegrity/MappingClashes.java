package at.ac.univie.mminf.qskos4j.issues.skosintegrity;

import at.ac.univie.mminf.qskos4j.issues.Issue;
import at.ac.univie.mminf.qskos4j.report.CollectionReport;
import at.ac.univie.mminf.qskos4j.report.Report;
import at.ac.univie.mminf.qskos4j.util.Tuple;
import at.ac.univie.mminf.qskos4j.util.TupleQueryResultUtil;
import at.ac.univie.mminf.qskos4j.util.vocab.SparqlPrefix;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;

import java.util.Collection;
import java.util.HashSet;

/**
 * Finds <a href="https://github.com/cmader/qSKOS/wiki/Quality-Issues#wiki-Mapping_Clashes">Exact vs. Associative and Hierarchical Mapping Clashes</a>.
 */
public class MappingClashes extends Issue<Collection<Tuple<Value>>> {

    public MappingClashes() {
        super("mc",
              "Mapping Clashes",
              "Covers condition S46 from the SKOS reference document (Exact vs. Associative and Hierarchical Mapping Clashes)",
              IssueType.ANALYTICAL
        );
    }

    @Override
    protected Collection<Tuple<Value>> computeResult() throws OpenRDFException {
        TupleQuery query = repCon.prepareTupleQuery(QueryLanguage.SPARQL, createExVsAssMappingQuery());

        Collection<Tuple<Value>> valuePairs = TupleQueryResultUtil.createCollectionOfValuePairs(
            query.evaluate(),
            "concept1", "concept2");
        Collection<Tuple<Value>> distinctPairs = new HashSet<Tuple<Value>>();
        distinctPairs.addAll(valuePairs);

        return distinctPairs;
    }

    @Override
    protected Report generateReport(Collection<Tuple<Value>> preparedData) {
        return new CollectionReport<Tuple<Value>>(preparedData);
    }

    private String createExVsAssMappingQuery() {
        return SparqlPrefix.SKOS +
            "SELECT ?concept1 ?concept2 WHERE {" +
                "?concept1 (skos:exactMatch|^skos:exactMatch)+ ?concept2 ."+
                "?concept1 skos:broadMatch|skos:narrowMatch|skos:relatedMatch ?concept2 ." +
                "}";
    }
}
