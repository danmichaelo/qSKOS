package at.ac.univie.mminf.qskos4j.issues;

import org.jgrapht.graph.DirectedMultigraph;
import org.openrdf.OpenRDFException;
import org.openrdf.model.Resource;
import org.openrdf.model.Value;
import org.openrdf.query.BindingSet;
import org.openrdf.query.MalformedQueryException;
import org.openrdf.query.QueryEvaluationException;
import org.openrdf.query.TupleQueryResult;
import org.openrdf.repository.RepositoryException;

import at.ac.univie.mminf.qskos4j.util.graph.NamedEdge;
import at.ac.univie.mminf.qskos4j.util.vocab.SparqlPrefix;
import at.ac.univie.mminf.qskos4j.util.vocab.VocabRepository;

public class HierarchyGraph {

	private enum HierarchyStyle {BROADER, NARROWER}
	
	private final String skosBroaderProperties = "skos:broader, skos:broaderTransitive, skos:broadMatch";
	private final String skosNarrowerProperties = "skos:narrower, skos:narrowerTransitive, skos:narrowMatch";	

	private DirectedMultigraph<Resource, NamedEdge> graph = new DirectedMultigraph<Resource, NamedEdge>(NamedEdge.class);
	private VocabRepository vocabRepository;
	
	public HierarchyGraph(VocabRepository vocabRepository) 
		throws OpenRDFException
	{
		this.vocabRepository = vocabRepository;
	}

	public DirectedMultigraph<Resource, NamedEdge> createGraph() throws OpenRDFException 
	{
		addResultsToGraph(findTriples(HierarchyStyle.BROADER), false);
		addResultsToGraph(findTriples(HierarchyStyle.NARROWER), true);
		
		return graph;
	}
	
	private TupleQueryResult findTriples(HierarchyStyle hierarchyStyle)
		throws RepositoryException, MalformedQueryException, QueryEvaluationException 
	{
		String skosHierarchyProperties = null;
		switch (hierarchyStyle) {
		case BROADER:
			skosHierarchyProperties = skosBroaderProperties;
			break;
		case NARROWER:
			skosHierarchyProperties = skosNarrowerProperties;
		}
		
		String query = createHierarchicalGraphQuery(skosHierarchyProperties);
		return vocabRepository.query(query);
	}
	
	private String createHierarchicalGraphQuery(String skosHierarchyProperties) {
		String query = SparqlPrefix.SKOS +" "+ SparqlPrefix.RDFS +
			"SELECT DISTINCT ?resource ?otherResource "+
			"FROM <" +vocabRepository.getVocabContext()+ "> "+
			"FROM <" +vocabRepository.SKOS_GRAPH_URL+ "> "+

			"WHERE {?resource ?hierarchyRelation ?otherResource . "+
				"?hierarchyRelation rdfs:subPropertyOf* ?skosHierarchyRelation . "+
				"FILTER (?skosHierarchyRelation IN (" +skosHierarchyProperties+ "))}";
				
		return query;
	}
	
	private void addResultsToGraph(TupleQueryResult result, boolean invertEdges) 
		throws QueryEvaluationException
	{	
		while (result.hasNext()) {
			BindingSet bindingSet = result.next();
			
			addToGraph(
				bindingSet.getValue("resource"), 
				bindingSet.getValue("otherResource"), 
				invertEdges);
		}
	}
	
	private void addToGraph(
		Value resource, 
		Value otherResource,
		boolean invertEdges) 
	{
		Resource resourceNode = (Resource) resource;		
		graph.addVertex(resourceNode);
		
		Resource otherNode = (Resource) otherResource;
		graph.addVertex(otherNode);

		if (invertEdges) {
			graph.addEdge(otherNode, resourceNode, new NamedEdge());
		}
		else {
			graph.addEdge(resourceNode, otherNode, new NamedEdge());
		}
	}
	
}
