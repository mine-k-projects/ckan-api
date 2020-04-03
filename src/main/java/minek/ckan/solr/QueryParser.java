package minek.ckan.solr;

import org.apache.commons.lang3.StringUtils;

import java.util.*;

public class QueryParser {

    protected static final String CRITERIA_VALUE_SEPARATOR = " ";
    protected static final String DELIMINATOR = ":";
    protected static final String NOT = "-";

    private static QueryParser instance = null;
    private Map<Criteria.Operation, PredicateProcessor> predicateProcessorMap = new HashMap<>();

    private QueryParser() {
        predicateProcessorMap.put(Criteria.Operation.EQUALS, new EqualsProcessor());
        predicateProcessorMap.put(Criteria.Operation.CONTAINS, new ContainsProcessor());
        predicateProcessorMap.put(Criteria.Operation.STARTS_WITH, new StartsWithProcessor());
        predicateProcessorMap.put(Criteria.Operation.ENDS_WITH, new EndsWithProcessor());
        predicateProcessorMap.put(Criteria.Operation.EXPRESSION, new ExpressionProcessor());
        predicateProcessorMap.put(Criteria.Operation.BETWEEN, new BetweenProcessor());
    }

    public static QueryParser getInstance() {
        if (instance == null) {
            instance = new QueryParser();
        }
        return instance;
    }

    public String createQuery(Criteria criteria) {
        StringBuilder sb = new StringBuilder();

        String field = criteria.getField();

        if (criteria.isNegating()) {
            sb.append(NOT);
        }
        sb.append(field);
        sb.append(DELIMINATOR);

        Set<Criteria.Predicate> predicates = criteria.getPredicates();

        if (predicates.isEmpty()) {
            sb.append("[* TO *]");
            return sb.toString();
        }

        boolean oneMorePredicate = predicates.size() > 1;
        if (oneMorePredicate) {
            sb.append("(");
        }

        StringBuilder predicateSb = new StringBuilder();
        for (Criteria.Predicate predicate : predicates) {
            if (predicateSb.length() > 0) {
                predicateSb.append(CRITERIA_VALUE_SEPARATOR);
            }
            Criteria.Operation key = predicate.getKey();
            PredicateProcessor predicateProcessor = predicateProcessorMap.get(key);
            predicateSb.append(predicateProcessor.process(field, predicate));
        }
        sb.append(predicateSb);

        if (oneMorePredicate) {
            sb.append(")");
        }

        return sb.toString();
    }

    interface PredicateProcessor {
        String process(String field, Criteria.Predicate predicate);
    }

    static class DefaultPredicateProcessor implements PredicateProcessor {

        protected static final String DOUBLEQUOTE = "\"";

        protected final Set<String> BOOLEAN_OPERATORS = new HashSet<>(Arrays.asList("NOT", "AND", "OR"));

        protected final String[] RESERVED_CHARS = {
                DOUBLEQUOTE, "+", "-", "&&", "||",
                "!", "(", ")", "{", "}",
                "[", "]", "^", "~", "*",
                "?", ":", "\\"
        };

        protected String[] RESERVED_CHARS_REPLACEMENT = {
                "\\" + DOUBLEQUOTE, "\\+", "\\-", "\\&\\&", "\\|\\|",
                "\\!", "\\(", "\\)", "\\{", "\\}",
                "\\[", "\\]", "\\^", "\\~", "\\*",
                "\\?", "\\:", "\\\\"
        };

        @Override
        public String process(String field, Criteria.Predicate predicate) {
            return (String) filterCriteriaValue(predicate.getValue());
        }

        protected Object filterCriteriaValue(Object criteriaValue) {
            String value = escapeCriteriaValue(criteriaValue.toString());
            return processWhiteSpaces(value);
        }

        private String escapeCriteriaValue(String criteriaValue) {
            return StringUtils.replaceEach(criteriaValue, RESERVED_CHARS, RESERVED_CHARS_REPLACEMENT);
        }

        private String processWhiteSpaces(String criteriaValue) {
            if (StringUtils.contains(criteriaValue, CRITERIA_VALUE_SEPARATOR) || BOOLEAN_OPERATORS.contains(criteriaValue)) {
                return DOUBLEQUOTE + criteriaValue + DOUBLEQUOTE;
            }
            return criteriaValue;
        }
    }

    static class EqualsProcessor extends DefaultPredicateProcessor {
    }

    static class ContainsProcessor extends DefaultPredicateProcessor {
        @Override
        public String process(String field, Criteria.Predicate predicate) {
            return Criteria.WILDCARD + filterCriteriaValue(predicate.getValue()) + Criteria.WILDCARD;
        }
    }

    static class StartsWithProcessor extends DefaultPredicateProcessor {
        @Override
        public String process(String field, Criteria.Predicate predicate) {
            return filterCriteriaValue(predicate.getValue()) + Criteria.WILDCARD;
        }
    }

    static class EndsWithProcessor extends DefaultPredicateProcessor {
        @Override
        public String process(String field, Criteria.Predicate predicate) {
            return Criteria.WILDCARD + filterCriteriaValue(predicate.getValue());
        }
    }

    static class ExpressionProcessor implements PredicateProcessor {
        @Override
        public String process(String field, Criteria.Predicate predicate) {
            return predicate.getValue().toString();
        }
    }

    static class BetweenProcessor extends DefaultPredicateProcessor {

        private static final String RANGE_OPERATOR = " TO ";

        @Override
        public String process(String field, Criteria.Predicate predicate) {
            Object[] value = (Object[]) predicate.getValue();
            Object lowerBound = value[0];
            Object upperBound = value[1];
            boolean includeLowerBound = (boolean) value[2];
            boolean includeUpperBound = (boolean) value[3];

            String fragment = "";
            fragment += includeLowerBound ? "[" : "{";
            fragment += lowerBound != null ? filterCriteriaValue(lowerBound) : Criteria.WILDCARD;
            fragment += RANGE_OPERATOR;
            fragment += upperBound != null ? filterCriteriaValue(upperBound) : Criteria.WILDCARD;
            fragment += includeUpperBound ? "]" : "}";

            return fragment;
        }
    }

}