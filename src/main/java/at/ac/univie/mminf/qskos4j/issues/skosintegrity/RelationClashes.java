package at.ac.univie.mminf.qskos4j.issues.skosintegrity;

import at.ac.univie.mminf.qskos4j.issues.HierarchyGraphBuilder;
import at.ac.univie.mminf.qskos4j.issues.Issue;
import at.ac.univie.mminf.qskos4j.report.CollectionReport;
import at.ac.univie.mminf.qskos4j.report.Report;
import at.ac.univie.mminf.qskos4j.util.Tuple;
import at.ac.univie.mminf.qskos4j.util.TupleQueryResultUtil;
import at.ac.univie.mminf.qskos4j.util.graph.NamedEdge;
import at.ac.univie.mminf.qskos4j.util.progress.MonitoredIterator;
import at.ac.univie.mminf.qskos4j.util.vocab.SparqlPrefix;
import org.jgrapht.Graph;
import org.jgrapht.alg.DijkstraShortestPath;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Value;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryConnection;

import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Finds <a href="https://github.com/cmader/qSKOS/wiki/Quality-Issues#wiki-Relation_Clashes">Associative vs. Hierarchical Relation Clashes</a>.
 */
public class RelationClashes extends Issue<Collection<Tuple<Value>>> {

    private HierarchyGraphBuilder hierarchyGraphBuilder;

    public RelationClashes(HierarchyGraphBuilder hierarchyGraphBuilder) {
        super("rc",
              "Relation Clashes",
              "Covers condition S27 from the SKOS reference document (Associative vs. Hierarchical Relation Clashes)",
              IssueType.ANALYTICAL
        );

        this.hierarchyGraphBuilder = hierarchyGraphBuilder;
    }

    @Override
    protected Collection<Tuple<Value>> computeResult() throws OpenRDFException {
        Graph<Value, NamedEdge> hierarchyGraph = hierarchyGraphBuilder.createGraph();

        Collection<Tuple<Value>> clashes = new HashSet<Tuple<Value>>();

        Iterator<Tuple<Value>> it = new MonitoredIterator<Tuple<Value>>(
                findRelatedConcepts(),
                progressMonitor);

        while (it.hasNext()) {
            Tuple<Value> conceptPair = it.next();
            try {
                if (pathExists(hierarchyGraph, conceptPair)) {
                    clashes.add(conceptPair);
                }
            }
            catch (IllegalArgumentException e) {
                // one of the concepts not in graph, no clash possible
            }
        }

        return clashes;
    }

    @Override
    protected Report generateReport(Collection<Tuple<Value>> preparedData) {
        return new CollectionReport<Tuple<Value>>(preparedData);
    }

    private Collection<Tuple<Value>> findRelatedConcepts() throws OpenRDFException {
        TupleQueryResult result = repCon.prepareTupleQuery(QueryLanguage.SPARQL, createRelatedConceptsQuery()).evaluate();
        return TupleQueryResultUtil.createCollectionOfValuePairs(result, "concept1", "concept2");
    }

    private String createRelatedConceptsQuery() {
        return SparqlPrefix.SKOS +
            "SELECT DISTINCT ?concept1 ?concept2 WHERE {" +
                "?concept1 skos:related|skos:relatedMatch ?concept2 ." +
            "}";
    }

    private boolean pathExists(Graph<Value, NamedEdge> hierarchyGraph, Tuple<Value> conceptPair) {
        if (new DijkstraShortestPath<Value, NamedEdge>(
                hierarchyGraph,
                conceptPair.getFirst(),
                conceptPair.getSecond()).getPathEdgeList() == null)
        {
            return new DijkstraShortestPath<Value, NamedEdge>(
                    hierarchyGraph,
                    conceptPair.getSecond(),
                    conceptPair.getFirst()).getPathEdgeList() != null;
        }
        return true;
    }

    @Override
    public void setRepositoryConnection(RepositoryConnection repCon) {
        hierarchyGraphBuilder.setRepositoryConnection(repCon);
        super.setRepositoryConnection(repCon);
    }
}
