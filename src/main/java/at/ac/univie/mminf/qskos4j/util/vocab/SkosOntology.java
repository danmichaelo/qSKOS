package at.ac.univie.mminf.qskos4j.util.vocab;

import at.ac.univie.mminf.qskos4j.util.TupleQueryResultUtil;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.model.impl.URIImpl;
import org.openrdf.query.QueryLanguage;
import org.openrdf.query.TupleQuery;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.sail.SailRepository;
import org.openrdf.rio.RDFFormat;
import org.openrdf.sail.memory.MemoryStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.Iterator;

public class SkosOntology {

    private final static Logger logger = LoggerFactory.getLogger(SkosOntology.class);

    public enum HierarchyType {BROADER, NARROWER}

    public final static URI[] SKOS_BROADER_PROPERTIES = {
            new URIImpl(SparqlPrefix.SKOS.getNameSpace() + "broader"),
            new URIImpl(SparqlPrefix.SKOS.getNameSpace() + "broaderTransitive"),
            new URIImpl(SparqlPrefix.SKOS.getNameSpace() + "broadMatch")
    };

    public final static URI[] SKOS_NARROWER_PROPERTIES = {
            new URIImpl(SparqlPrefix.SKOS.getNameSpace() + "narrower"),
            new URIImpl(SparqlPrefix.SKOS.getNameSpace() + "narrowerTransitive"),
            new URIImpl(SparqlPrefix.SKOS.getNameSpace() + "narrowMatch")
    };

    public final static URI[] SKOS_ASSOCIATIVE_PROPERTIES = {
            new URIImpl(SparqlPrefix.SKOS.getNameSpace() + "related"),
            new URIImpl(SparqlPrefix.SKOS.getNameSpace() + "relatedMatch")
    };

    private final String SKOS_GRAPH_URL = "http://www.w3.org/2009/08/skos-reference/skos.rdf";
    private final String SKOS_BASE_URI = "http://www.w3.org/2004/02/skos/core";

    private static SkosOntology ourInstance = new SkosOntology();
    private static Repository skosRepo;

    public static SkosOntology getInstance() {
        if (skosRepo == null) {
            try {
                ourInstance.createSkosRepo();
            }
            catch (Exception e) {
                logger.error("Error creating SKOS repository", e);
            }
        }
        return ourInstance;
    }

    private void createSkosRepo() throws OpenRDFException, IOException {
        skosRepo = new SailRepository(new MemoryStore());
        skosRepo.initialize();

        RepositoryConnection repCon = skosRepo.getConnection();
        try {
            repCon.add(new URL(SKOS_GRAPH_URL),
                        SKOS_BASE_URI,
                        RDFFormat.RDFXML,
                        new URIImpl(SKOS_GRAPH_URL));
        }
        finally {
            repCon.close();
        }
    }

    private SkosOntology() {
    }

    public String getSubPropertiesOfSemanticRelationsFilter(String bindingName) throws OpenRDFException
    {
        RepositoryConnection repCon = skosRepo.getConnection();
        try {
            TupleQuery tupleQuery = repCon.prepareTupleQuery(
                QueryLanguage.SPARQL,
                createSubPropertiesOfSemanticRelationsQuery(bindingName));

            return TupleQueryResultUtil.getFilterForBindingName(tupleQuery.evaluate(), bindingName);
        }
        finally {
            repCon.close();
        }
    }

    private String createSubPropertiesOfSemanticRelationsQuery(String bindingName) {
        return SparqlPrefix.SKOS +" "+ SparqlPrefix.RDFS +
            "SELECT ?" +bindingName+ " WHERE {" +
                "?" +bindingName+ " rdfs:subPropertyOf+ skos:semanticRelation" +
            "}";
    }

    public String getHierarchicalPropertiesPath(HierarchyType hierarchyType)
    {
        Iterator<URI> broaderIt = Arrays.asList(SkosOntology.SKOS_BROADER_PROPERTIES).iterator();
        Iterator<URI> narrowerIt = Arrays.asList(SkosOntology.SKOS_NARROWER_PROPERTIES).iterator();

        switch (hierarchyType) {
            case BROADER:
                return concatWithOrOperator(broaderIt, false) +"|"+ concatWithOrOperator(narrowerIt, true);

            case NARROWER:
                return concatWithOrOperator(narrowerIt, false) +"|"+ concatWithOrOperator(broaderIt, true);

            default:
                return "";
        }
    }

    private String concatWithOrOperator(Iterator<URI> iterator, boolean addInversePrefix) {
        String concatedEntries = "";
        while (iterator.hasNext()) {
            concatedEntries += (addInversePrefix ? "^" : "") +"<"+ iterator.next() +">"+ (iterator.hasNext() ? "|" : "");
        }
        return concatedEntries;
    }

    public HierarchyType getPredicateHierarchyType(URI predicate)
    {
        if (Arrays.asList(SkosOntology.SKOS_BROADER_PROPERTIES).contains(predicate)) return HierarchyType.BROADER;
        if (Arrays.asList(SkosOntology.SKOS_NARROWER_PROPERTIES).contains(predicate)) return HierarchyType.NARROWER;

        throw new IllegalArgumentException("Predicate not a hierarchical property");
    }

    public Repository getRepository() {
        return skosRepo;
    }

}
