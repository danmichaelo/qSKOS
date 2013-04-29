package at.ac.univie.mminf.qskos4j.issues.language;

import at.ac.univie.mminf.qskos4j.report.CollectionReport;
import at.ac.univie.mminf.qskos4j.report.Report;
import org.openrdf.model.Literal;
import org.openrdf.model.Resource;
import org.openrdf.model.Statement;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class LanguageTagReport extends Report {

    private Collection<Statement> data;
    private Map<Resource, Collection<Literal>> literalsForResource;

	LanguageTagReport(Collection<Statement> data) {
		this.data = data;
	}

    @Override
    protected void generateTextReport(BufferedWriter writer, ReportStyle style) throws IOException
    {
        createLiteralsForResourceMap();

        switch (style) {
            case SHORT:
                new CollectionReport<Resource>(literalsForResource.keySet()).generateReport(writer, ReportFormat.TXT, ReportStyle.SHORT);
                break;

            case EXTENSIVE:
                writer.write(generateExtensiveReport());
                break;
        }
    }

	private String generateExtensiveReport() {
        StringBuilder extensiveReport = new StringBuilder();
		
		for (Resource resource : literalsForResource.keySet()) {
			Collection<Literal> affectedLiterals = literalsForResource.get(resource);
			
			extensiveReport.append("resource: '").append(resource).append("', affected literals: ").append(affectedLiterals.toString()).append("\n");
		}
		
		return extensiveReport.toString();
	}

    private void createLiteralsForResourceMap() {
        literalsForResource = new HashMap<Resource, Collection<Literal>>();

        for (Statement statement : data) {
            Resource subject = statement.getSubject();
            Literal object = (Literal) statement.getObject();
            Collection<Literal> literals = literalsForResource.get(subject);

            if (literals == null) {
                literals = new ArrayList<Literal>();
                literalsForResource.put(subject, literals);
            }

            literals.add(object);
        }
    }

}
