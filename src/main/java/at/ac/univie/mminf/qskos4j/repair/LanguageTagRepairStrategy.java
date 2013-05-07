package at.ac.univie.mminf.qskos4j.repair;

import org.openrdf.model.Literal;
import org.openrdf.model.Statement;
import org.openrdf.model.Value;
import org.openrdf.model.impl.LiteralImpl;
import org.openrdf.model.impl.StatementImpl;
import org.openrdf.repository.RepositoryException;
import org.openrdf.repository.RepositoryResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class LanguageTagRepairStrategy extends RepairStrategy {

    private final Logger logger = LoggerFactory.getLogger(LanguageTagRepairStrategy.class);

    private Collection<Statement> invalidLangTagStatements;
    private Map<String, Long> languageStats;
    private String defaultLanguageTag = "";

    public LanguageTagRepairStrategy(Collection<Statement> invalidLangTagStatements) {
        this.invalidLangTagStatements = invalidLangTagStatements;
    }

    @Override
    public List<RepairChoice> getRepairChoices() {
        List<RepairChoice> repairChoices = new ArrayList<RepairChoice>();

        try {
            findDefaultLanguageTag();
            List<String> orderedLanguageTags = getOrderedLangTags();

            for (Statement statement : invalidLangTagStatements) {
                for (String languageTag : orderedLanguageTags) {
                    repairChoices.add(new LanguageRepairChoice(statement, languageTag));
                }
            }
        }
        catch (Exception e) {
            logger.info("Cannot find possible language tags");
        }

        return repairChoices;
    }

    private void findDefaultLanguageTag() throws RepositoryException {
        buildLangStats();
        if (languageStats.isEmpty()) throw new DefaultLanguageTagNotFoundException();
        defaultLanguageTag = languageStats.entrySet().iterator().next().getKey();
    }

    private void buildLangStats() throws RepositoryException {
        HashMap<String, Long> map = new HashMap<String, Long>();
        languageStats = new TreeMap<String, Long>(new ValueComparator(map));

        RepositoryResult<Statement> result = repCon.getStatements(null, null, null, false);
        while (result.hasNext()) {
            Statement statement = result.next();
            Value object = statement.getObject();
            if (object instanceof Literal && ((Literal) object).getLanguage() != null) {
                addLanguage(map, ((Literal) object).getLanguage());
            }
        }

        languageStats.putAll(map);
    }

    private void addLanguage(HashMap<String, Long> map, String langTag) {
        Long langTagCount = languageStats.get(langTag);
        if (langTagCount == null) {
            map.put(langTag, (long) 1);
        }
        else {
            map.put(langTag, langTagCount.longValue() + 1);
        }
    }

    private List<String> getOrderedLangTags() {
        List<String> orderedLangTags = new ArrayList<String>();
        orderedLangTags.addAll(languageStats.keySet());
        if (!orderedLangTags.contains(defaultLanguageTag)) {
            orderedLangTags.add(0, defaultLanguageTag);
        }

        return orderedLangTags;
    }

    public void setDefaultLanguage(String defaultLanguageTag) {
        this.defaultLanguageTag = defaultLanguageTag;
    }

    private class DefaultLanguageTagNotFoundException extends RuntimeException {
    }

    private class ValueComparator implements Comparator<String> {

        Map<String, Long> base;

        public ValueComparator(Map<String, Long> base) {
            this.base = base;
        }

        @Override
        public int compare(String key1, String key2) {
            Long val1 = base.get(key1);
            Long val2 = base.get(key2);

            if (val1 != null && val2 != null && val1 >= val2) {
                return -1;
            }
            else {
                return 1;
            }
        }

    }

    private class LanguageRepairChoice implements RepairChoice {

        private Statement statement;
        private String langTag;

        public LanguageRepairChoice(Statement statement, String langTag) {
            this.statement = statement;
            this.langTag = langTag;
        }

        @Override
        public void performRepair() {
            Literal oldLiteral = (Literal) statement.getObject();
            Literal newLiteral = new LiteralImpl(oldLiteral.getLabel(), langTag);

            try {
                repCon.remove(statement);
                repCon.add(new StatementImpl(statement.getSubject(), statement.getPredicate(), newLiteral));
            }
            catch (RepositoryException e) {
                throw new RepairFailedException(e);
            }
        }

        @Override
        public String getMessage() {
            return "Set language tag '" +langTag+ "' for literal '" +statement.getObject()+ "'?";
        }
    }
}
