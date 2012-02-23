package at.ac.univie.mminf.qskos4j;

import java.io.IOException;
import java.util.Set;

import junit.framework.Assert;

import org.junit.Before;
import org.junit.Test;
import org.openrdf.OpenRDFException;
import org.openrdf.model.URI;
import org.openrdf.repository.RepositoryException;

public class LooseConceptsTest extends QSkosTestCase {

	private QSkos qSkosConcepts, qSkosComponents;
	
	@Before
	public void setUp() throws OpenRDFException, IOException {
		qSkosConcepts = setUpInstance("concepts.rdf");
		qSkosComponents = setUpInstance("components.rdf");
	}
	
	@Test
	public void testConceptsLooseConceptCount() throws RepositoryException {
		Set<URI> looseConcepts = qSkosConcepts.findLooseConcepts();
		Assert.assertEquals(5, looseConcepts.size());		
	}
	
	@Test
	public void testComponentsLooseConceptCount() throws RepositoryException {
		Set<URI> looseConcepts = qSkosComponents.findLooseConcepts(); 
		Assert.assertEquals(2, looseConcepts.size());		
	}
	
}