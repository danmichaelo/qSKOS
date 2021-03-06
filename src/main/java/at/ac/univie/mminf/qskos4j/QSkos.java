package at.ac.univie.mminf.qskos4j;

import at.ac.univie.mminf.qskos4j.issues.HierarchyGraphBuilder;
import at.ac.univie.mminf.qskos4j.issues.Issue;
import at.ac.univie.mminf.qskos4j.issues.clusters.DisconnectedConceptClusters;
import at.ac.univie.mminf.qskos4j.issues.concepts.AuthoritativeConcepts;
import at.ac.univie.mminf.qskos4j.issues.concepts.InvolvedConcepts;
import at.ac.univie.mminf.qskos4j.issues.concepts.OrphanConcepts;
import at.ac.univie.mminf.qskos4j.issues.concepts.UndocumentedConcepts;
import at.ac.univie.mminf.qskos4j.issues.conceptscheme.ConceptSchemes;
import at.ac.univie.mminf.qskos4j.issues.conceptscheme.OmittedTopConcepts;
import at.ac.univie.mminf.qskos4j.issues.conceptscheme.TopConceptsHavingBroaderConcepts;
import at.ac.univie.mminf.qskos4j.issues.count.AggregationRelations;
import at.ac.univie.mminf.qskos4j.issues.count.SemanticRelations;
import at.ac.univie.mminf.qskos4j.issues.cycles.HierarchicalCycles;
import at.ac.univie.mminf.qskos4j.issues.inlinks.MissingInLinks;
import at.ac.univie.mminf.qskos4j.issues.labels.DisjointLabelsViolations;
import at.ac.univie.mminf.qskos4j.issues.labels.InconsistentPrefLabels;
import at.ac.univie.mminf.qskos4j.issues.labels.LexicalRelations;
import at.ac.univie.mminf.qskos4j.issues.labels.OverlappingLabels;
import at.ac.univie.mminf.qskos4j.issues.labels.util.ResourceLabelsCollector;
import at.ac.univie.mminf.qskos4j.issues.language.IncompleteLanguageCoverage;
import at.ac.univie.mminf.qskos4j.issues.language.OmittedOrInvalidLanguageTags;
import at.ac.univie.mminf.qskos4j.issues.outlinks.BrokenLinks;
import at.ac.univie.mminf.qskos4j.issues.outlinks.HttpURIs;
import at.ac.univie.mminf.qskos4j.issues.outlinks.HttpUriSchemeViolations;
import at.ac.univie.mminf.qskos4j.issues.outlinks.MissingOutLinks;
import at.ac.univie.mminf.qskos4j.issues.relations.SolelyTransitivelyRelatedConcepts;
import at.ac.univie.mminf.qskos4j.issues.relations.UnidirectionallyRelatedConcepts;
import at.ac.univie.mminf.qskos4j.issues.relations.ValuelessAssociativeRelations;
import at.ac.univie.mminf.qskos4j.issues.skosintegrity.MappingClashes;
import at.ac.univie.mminf.qskos4j.issues.skosintegrity.RelationClashes;
import at.ac.univie.mminf.qskos4j.issues.skosintegrity.UndefinedSkosResources;
import at.ac.univie.mminf.qskos4j.util.progress.IProgressMonitor;
import at.ac.univie.mminf.qskos4j.util.progress.StubProgressMonitor;
import at.ac.univie.mminf.qskos4j.util.vocab.RepositoryBuilder;
import org.openrdf.OpenRDFException;
import org.openrdf.repository.Repository;
import org.openrdf.repository.RepositoryConnection;
import org.openrdf.repository.RepositoryException;

import java.io.File;
import java.util.*;

/**
 * Main class intended for easy interaction with qSKOS. On instantiation an in-memory ("local") repository 
 * containing the passed controlled vocabulary is created which can be queried by calling the methods of this class. 
 * 
 * @author christian
 *
 */
public class QSkos {

    /**
     * Delay to avoid flooding "external" sources. This is used, e.g., when dereferencing lots of links or sending
     * many queryies to a SPARQL endpoint
     */
    private final static int EXT_ACCESS_MILLIS = 1500;

	private String baseURI;

    private BrokenLinks brokenLinks;
    private InvolvedConcepts involvedConcepts;
    private AuthoritativeConcepts authoritativeConcepts;
    private MissingInLinks missingInLinks;
    private ConceptSchemes conceptSchemes;
    private HttpURIs httpURIs;

    private List<Issue> registeredIssues = new ArrayList<Issue>();

    public QSkos(File file) throws RepositoryException {
        this();

        RepositoryBuilder repositoryBuilder = new RepositoryBuilder();
        Repository repo = repositoryBuilder.setUpFromFile(file, null, null);
        setRepositoryConnection(repo.getConnection());
    }

    public QSkos() {
        addStatisticalIssues();
        addAnalyticalIssues();
        addSkosIntegrityIssues();

        setProgressMonitor(new StubProgressMonitor());
    }

    private void addStatisticalIssues() {
        involvedConcepts = new InvolvedConcepts();
        authoritativeConcepts = new AuthoritativeConcepts(involvedConcepts);
        authoritativeConcepts.setBaseURI(baseURI);
        conceptSchemes = new ConceptSchemes();
        httpURIs = new HttpURIs();

        registeredIssues.add(involvedConcepts);
        registeredIssues.add(authoritativeConcepts);
        registeredIssues.add(new LexicalRelations(involvedConcepts));
        registeredIssues.add(new SemanticRelations());
        registeredIssues.add(new AggregationRelations());
        registeredIssues.add(conceptSchemes);
        registeredIssues.add(new at.ac.univie.mminf.qskos4j.issues.count.Collections());
        registeredIssues.add(httpURIs);
    }

    private void addAnalyticalIssues() {
        HierarchyGraphBuilder hierarchyGraphBuilder = new HierarchyGraphBuilder();

        registeredIssues.add(new OmittedOrInvalidLanguageTags());
        registeredIssues.add(new IncompleteLanguageCoverage(involvedConcepts));
        registeredIssues.add(new UndocumentedConcepts(authoritativeConcepts));
        registeredIssues.add(new OverlappingLabels(involvedConcepts));
        registeredIssues.add(new OrphanConcepts(involvedConcepts));
        registeredIssues.add(new DisconnectedConceptClusters(involvedConcepts));
        registeredIssues.add(new HierarchicalCycles(hierarchyGraphBuilder));
        registeredIssues.add(new ValuelessAssociativeRelations());
        registeredIssues.add(new SolelyTransitivelyRelatedConcepts());
        registeredIssues.add(new OmittedTopConcepts(conceptSchemes));
        registeredIssues.add(new TopConceptsHavingBroaderConcepts());

        missingInLinks = new MissingInLinks(authoritativeConcepts);
        missingInLinks.setQueryDelayMillis(EXT_ACCESS_MILLIS);
        registeredIssues.add(missingInLinks);

        registeredIssues.add(new MissingOutLinks(authoritativeConcepts));

        brokenLinks = new BrokenLinks(httpURIs);
        brokenLinks.setExtAccessDelayMillis(EXT_ACCESS_MILLIS);
        registeredIssues.add(brokenLinks);

        registeredIssues.add(new UndefinedSkosResources());
        registeredIssues.add(new UnidirectionallyRelatedConcepts());
        registeredIssues.add(new HttpUriSchemeViolations());
        registeredIssues.add(new RelationClashes(hierarchyGraphBuilder));
        registeredIssues.add(new MappingClashes());
    }

    private void addSkosIntegrityIssues() {
        ResourceLabelsCollector resourceLabelsCollector = new ResourceLabelsCollector();

        registeredIssues.add(new InconsistentPrefLabels(resourceLabelsCollector));
        registeredIssues.add(new DisjointLabelsViolations(resourceLabelsCollector));
    }

    public List<Issue> getAllIssues() {
        return registeredIssues;
    }

    public Collection<Issue> getIssues(String commaSeparatedIssueIDs)
    {
        if (commaSeparatedIssueIDs == null || commaSeparatedIssueIDs.isEmpty()) {
            return Collections.emptySet();
        }

        Collection<Issue> issues = new ArrayList<Issue>();
        StringTokenizer tokenizer = new StringTokenizer(commaSeparatedIssueIDs, ",");
        while (tokenizer.hasMoreElements()) {
            issues.add(findIssue(tokenizer.nextToken().trim()));
        }

        return issues;
    }

    private Issue findIssue(String issueId) {
        for (Issue issue : registeredIssues) {
            if (issue.getId().equalsIgnoreCase(issueId)) {
                return issue;
            }
        }

        throw new UnknownIssueIdException(issueId, generateSupportedIssueIdList());
    }

    private String generateSupportedIssueIdList() {
        String supportedIssueIds = "";
        Iterator<Issue> allIssuesIt = getAllIssues().iterator();
        while (allIssuesIt.hasNext()) {
            supportedIssueIds += allIssuesIt.next().getId() + (allIssuesIt.hasNext() ? ", " : "");
        }
        return supportedIssueIds;
    }

    public void setRepositoryConnection(RepositoryConnection repCon) {
        for (Issue issue : registeredIssues) {
            issue.setRepositoryConnection(repCon);
        }
    }

	/**
	 * Set an IProgressMonitor that is notified on changes in the evaluation progress for every managed issues.
	 * @param progressMonitor monitor instance to be notified
	 */
	public void setProgressMonitor(IProgressMonitor progressMonitor) {
        for (Issue issue : registeredIssues) {
            issue.setProgressMonitor(progressMonitor);
        }
	}
	
	/**
	 * Sets a delay time in milliseconds that must pass between accessing an external resource. This is intended to
     * avoid flooding of, e.g., vocabulary hosts or SPARQL endpoints with HTTP requests.
	 * 
	 * @param delayMillis delay time in milliseconds
	 */
    @SuppressWarnings("unused")
    public void setExtAccessDelayMillis(int delayMillis) {
        missingInLinks.setQueryDelayMillis(delayMillis);
        brokenLinks.setExtAccessDelayMillis(delayMillis);
	}
	
	/**
	 * Some methods in this class support investigating only a subset of the vocabulary and extrapolate the results
	 * to shorten evaluation time. Works for, e.g., finding broken links. 
	 * 
	 * @param subsetSizePercent percentage of the total resources to investigate.
	 */
	public void setSubsetSize(Float subsetSizePercent) {
        missingInLinks.setSubsetSize(subsetSizePercent);
        brokenLinks.setSubsetSize(subsetSizePercent);
	}

    /**
     * Sets a string that is used to identify if an URI is authoritative. This is required to, e.g., find all
     * out-links to distinguish between URIs in the vocabulary namespace and other resources on the Web.
     *
     * @param authResourceIdentifier a string, usually a substring of an URI in the vocabulary's namespace,
     * that uniquely identifies an authoritative URI.
     */
    public void setAuthResourceIdentifier(String authResourceIdentifier) {
        authoritativeConcepts.setAuthResourceIdentifier(authResourceIdentifier);
    }

    @SuppressWarnings("unused")
    public void setBaseURI(String baseURI) {
        this.baseURI = baseURI;
    }

    public void addSparqlEndPoint(String endpointUrl) throws OpenRDFException {
        missingInLinks.addSparqlEndPoint(endpointUrl);
    }

    // convenience methods

    public DisconnectedConceptClusters getDisconnectedConceptClusters() throws OpenRDFException {
        return (DisconnectedConceptClusters) getIssueInstanceByClassName(DisconnectedConceptClusters.class.getName());
    }

    public MappingClashes getMappingClashes() throws OpenRDFException {
        return (MappingClashes) getIssueInstanceByClassName(MappingClashes.class.getName());
    }

    public SemanticRelations getSemanticRelations() throws OpenRDFException {
        return (SemanticRelations) getIssueInstanceByClassName(SemanticRelations.class.getName());
    }

    public HttpURIs getHttpURIs() throws OpenRDFException {
        return (HttpURIs) getIssueInstanceByClassName(HttpURIs.class.getName());
    }

    public DisjointLabelsViolations getDisjointLabelsViolations() throws OpenRDFException {
        return (DisjointLabelsViolations) getIssueInstanceByClassName(DisjointLabelsViolations.class.getName());
    }

    public OverlappingLabels getOverlappingLabels() throws OpenRDFException {
        return (OverlappingLabels) getIssueInstanceByClassName(OverlappingLabels.class.getName());
    }

    public OmittedOrInvalidLanguageTags getOmittedOrInvalidLanguageTags() throws OpenRDFException {
        return (OmittedOrInvalidLanguageTags) getIssueInstanceByClassName(OmittedOrInvalidLanguageTags.class.getName());
    }

    public BrokenLinks getBrokenLinks() throws OpenRDFException {
        return (BrokenLinks) getIssueInstanceByClassName(BrokenLinks.class.getName());
    }

    public MissingInLinks getMissingInLinks() throws OpenRDFException {
        return (MissingInLinks) getIssueInstanceByClassName(MissingInLinks.class.getName());
    }

    public ConceptSchemes getConceptSchemes() throws OpenRDFException {
        return (ConceptSchemes) getIssueInstanceByClassName(ConceptSchemes.class.getName());
    }

    public InvolvedConcepts getInvolvedConcepts() throws OpenRDFException {
        return (InvolvedConcepts) getIssueInstanceByClassName(InvolvedConcepts.class.getName());
    }

    public AuthoritativeConcepts getAuthoritativeConcepts() throws OpenRDFException {
        return (AuthoritativeConcepts) getIssueInstanceByClassName(AuthoritativeConcepts.class.getName());
    }

    public UndocumentedConcepts getUndocumentedConcepts() throws OpenRDFException {
        return (UndocumentedConcepts) getIssueInstanceByClassName(UndocumentedConcepts.class.getName());
    }

    public UnidirectionallyRelatedConcepts getUnidirectionallyRelatedConcepts() throws OpenRDFException {
        return (UnidirectionallyRelatedConcepts) getIssueInstanceByClassName(UnidirectionallyRelatedConcepts.class.getName());
    }

    public AggregationRelations getAggregationRelations() throws OpenRDFException {
        return (AggregationRelations) getIssueInstanceByClassName(AggregationRelations.class.getName());
    }

    public OmittedTopConcepts getOmittedTopConcepts() throws OpenRDFException {
        return (OmittedTopConcepts) getIssueInstanceByClassName(OmittedTopConcepts.class.getName());
    }

    public LexicalRelations getLexicalRelations() throws OpenRDFException {
        return (LexicalRelations) getIssueInstanceByClassName(LexicalRelations.class.getName());
    }

    public OrphanConcepts getOrphanConcepts() throws OpenRDFException {
        return (OrphanConcepts) getIssueInstanceByClassName(OrphanConcepts.class.getName());
    }

    public ValuelessAssociativeRelations getValuelessAssociativeRelations() throws OpenRDFException {
        return (ValuelessAssociativeRelations) getIssueInstanceByClassName(ValuelessAssociativeRelations.class.getName());
    }

    public MissingOutLinks getMissingOutLinks() throws OpenRDFException {
        return (MissingOutLinks) getIssueInstanceByClassName(MissingOutLinks.class.getName());
    }

    public TopConceptsHavingBroaderConcepts getTopConceptsHavingBroaderConcepts() throws OpenRDFException {
        return (TopConceptsHavingBroaderConcepts) getIssueInstanceByClassName(TopConceptsHavingBroaderConcepts.class.getName());
    }

    public IncompleteLanguageCoverage getIncompleteLanguageCoverage() throws OpenRDFException {
        return (IncompleteLanguageCoverage) getIssueInstanceByClassName(IncompleteLanguageCoverage.class.getName());
    }

    public UndefinedSkosResources getUndefinedSkosResources() throws OpenRDFException {
        return (UndefinedSkosResources) getIssueInstanceByClassName(UndefinedSkosResources.class.getName());
    }

    public at.ac.univie.mminf.qskos4j.issues.count.Collections getCollections() throws OpenRDFException {
        return (at.ac.univie.mminf.qskos4j.issues.count.Collections) getIssueInstanceByClassName(
            at.ac.univie.mminf.qskos4j.issues.count.Collections.class.getName());
    }

    public HttpUriSchemeViolations getHttpUriSchemeViolations() throws OpenRDFException {
        return (HttpUriSchemeViolations) getIssueInstanceByClassName(HttpUriSchemeViolations.class.getName());
    }

    public SolelyTransitivelyRelatedConcepts getSolelyTransitivelyRelatedConcepts() throws OpenRDFException {
        return (SolelyTransitivelyRelatedConcepts) getIssueInstanceByClassName(SolelyTransitivelyRelatedConcepts.class.getName());
    }

    public HierarchicalCycles getHierarchicalCycles() throws OpenRDFException {
        return (HierarchicalCycles) getIssueInstanceByClassName(HierarchicalCycles.class.getName());
    }

    public InconsistentPrefLabels getInconsistentPrefLabels() throws OpenRDFException {
        return (InconsistentPrefLabels) getIssueInstanceByClassName(InconsistentPrefLabels.class.getName());
    }

    public RelationClashes getRelationClashes() throws OpenRDFException {
        return (RelationClashes) getIssueInstanceByClassName(RelationClashes.class.getName());
    }

    private Issue getIssueInstanceByClassName(String className) {
        for (Issue issue : registeredIssues) {
            if (issue.getClass().getName().equals(className)) return issue;
        }

        throw new IllegalArgumentException("No issue class '" +className+ "' found");
    }

}