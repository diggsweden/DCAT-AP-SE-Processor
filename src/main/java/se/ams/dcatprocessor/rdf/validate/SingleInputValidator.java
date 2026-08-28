// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.rdf.validate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Pattern;

import org.eclipse.rdf4j.model.vocabulary.GEO;
import org.jspecify.annotations.NonNull;

import se.ams.dcatprocessor.rdf.DcatException;
import se.ams.dcatprocessor.rdf.validate.ValidationError.ErrorType;
import se.ams.dcatprocessor.specification.DcatProperty;
import se.ams.dcatprocessor.specification.DcatSpecification;
import se.ams.dcatprocessor.util.Util;

/**
 * Class for validating all the values put into the DCAT-AP specification file
 * 
 * @author nacbr
 *
 */
public class SingleInputValidator {
	
	private static SingleInputValidator instance;

	private DcatSpecification specification;
	
	//Map from an InputType to regex
	private HashMap<InputType, Pattern> inputTypeToRegexMap;
	
	//Map for the key string of the inputvalue to an InputType
	private HashMap<String, List<InputType>> keyToInputTypeMap;

	// The name of the file currently being validated
	private String currentFileName;
	
	//  Predefined error messages
	private static final String ERROR_CURRENT_FILENAME_NOT_SET = "Error validating input data. Reason: Filename for the file being validated is not set";
	private static final String ERROR_KEY_OR_VALUE_IS_NULL = "Error validating type: Input %s is null";
	private static final String ERROR_NO_CORRESPONDING_INPUT_TYPE = "Error validating type: Key %s is not defined";
	
	/**
	 * dcat:hadRole is validated against resourceRole.json and userRole.json in the converter, not here. 
	 * The specification states two patterns for it, one per class, this check has no class context and can't determine which pattern to use.
	 */
	private static final Set<String> PATTERN_EXCLUDED_PROPERTIES = Set.of("dcat:hadRole"); 
	
	private static final String PHONE_PROPERTY = "vcard:hasValue";
	private static final Map<String, InputType> INPUT_TYPE_BY_DATATYPE = Map.of(
        "http://www.w3.org/2001/XMLSchema#date", InputType.DATE,
        "http://www.w3.org/2001/XMLSchema#dateTime", InputType.DATETIME,
        "http://www.w3.org/2001/XMLSchema#gYear", InputType.GYEAR,
        "http://www.w3.org/2001/XMLSchema#nonNegativeInteger", InputType.NONNEGATIVEINTEGER,
        	GEO.WKT_LITERAL.stringValue(), InputType.WKTLITERAL,
        "xsd:decimal", InputType.DECIMAL,
        "xsd:duration", InputType.DURATION);

	/**
	 * Properties where the specification has a complete set of allowed values to validate against.
	 * A property is left out when it offers a free alternative, when it has more than one list, 
	 * when the list differs per class (validation has no class context), or when dataportal.se accepts values the list does not contain.
	 */
	private static final Set<String> CLOSED_VALUE_LISTS = Set.of(
		"dcatap:hvdCategory",
		"dcat:theme",
		"dcat:themeTaxonomy",
		"dcterms:accessRights",
		"dcterms:language",
		"adms:status",
		"rdf:type");
			
	private SingleInputValidator(DcatSpecification specification) {
		this.specification = specification;
		mapInputTypeToRegex();
		loadInputTypeDefinitions();
	}

	public static SingleInputValidator getInstance() {
		if(instance == null) {
			instance = new SingleInputValidator(new DcatSpecification());	
		}
		return instance;
	}
	
	public static void resetInstance(){
		instance = null;
	} 

	/**
	 * Checks a value against what the specification.
	 * Three things are checked: the pattern the specification states for the
	 * property, the list of allowed values where it states one, and the kind of
	 * value the property takes.
	 * @param key - The key
	 * @param value - The value
	 * @return T/F depending of the result
	 * @throws DcatException - If there is an error during validation
	 */
	public boolean validateData(@NonNull String key, @NonNull String value) throws DcatException {

		Util.checkNotNull(currentFileName, getClass() + " " + ERROR_CURRENT_FILENAME_NOT_SET);
		Util.checkNotNull(key, ERROR_KEY_OR_VALUE_IS_NULL.formatted("key"));
		Util.checkNotNull(value, ERROR_KEY_OR_VALUE_IS_NULL.formatted("value"));

		List<InputType> inputTypes = keyToInputTypeMap.get(key);
		Util.checkNotNull(inputTypes, ERROR_NO_CORRESPONDING_INPUT_TYPE.formatted(key));

		if (!matchesPattern(key, value)) {
			return reportError(ErrorType.ILLEGAL_FORMAT, key, value);
		}
		if (!isAllowedValue(key, value)) {
			return reportError(ErrorType.UNKNOWN_VALUE, key, value);
		}

		if (!matchesInputType(key, value)) {
			return reportError(ErrorType.ILLEGAL_FORMAT, key, value);
		}
		return true;
	}

	private boolean reportError(ErrorType errorType, String key, String value) {
		ValidationErrorStorage.getInstance().setValidationError(currentFileName, new ValidationError(errorType, currentFileName, key, value));
		return false;
	}

	private boolean matchesPattern(String key, String value) {
		if (PATTERN_EXCLUDED_PROPERTIES.contains(key)) {
			return true;
		}
		String pattern = specification.patternFor(key);
		if (pattern == null) {
			return true;
		}

		String candidate = value;
		
		if(PHONE_PROPERTY.equals(key)){
			// Converter.addPhone applies the "tel:" prefix, while the pattern describes the number without it.
			candidate = value.replace("tel:", "");
		}

		return candidate.matches(pattern);
	}

	/** Check value against predefined chocies */
	private boolean isAllowedValue(String key, String value) {
		if (!CLOSED_VALUE_LISTS.contains(key)) {
			return true;
		}
		List<String> choices = specification.choicesFor(key);

		return choices.isEmpty() || choices.contains(value);
	}

	/**
	 * A key can have several input types, and the value needs to satisfy one of them.
	 * vcard:hasValue is left out: the value carries the "tel:" prefix, 
	 * and the URI check would reject a number the matchesPattern() already accepted.
	 */
	private boolean matchesInputType(String key, String value) {
		if (PHONE_PROPERTY.equals(key)) {
			return true;
		}
		for (InputType inputType : keyToInputTypeMap.get(key)) {
			if (inputType.equals(InputType.ANYURI)) {
				return Util.isURI(value);
			}
			if (inputTypeToRegexMap.get(inputType).matcher(value).matches()) {
				return true;
			}
		}
		return false;
	}

	public String getCurrentFileName() {
		return currentFileName;
	}
	public void setCurrentFileName(String fileName) {
		currentFileName = fileName;
	}
	
	/**
	 * Maps an InputType to a regex(Pattern)
	 */
	private void mapInputTypeToRegex() {
		//TODO: Improve REGEX Check that they cover all OUR cases...verify with tests
		//Add the patterns to XSD-Types
		inputTypeToRegexMap = new HashMap<InputType, Pattern>();
		inputTypeToRegexMap.put(InputType.STRING, Pattern.compile(".*"));
		inputTypeToRegexMap.put(InputType.INTEGER, Pattern.compile("^\\d{1,10}$"));
		inputTypeToRegexMap.put(InputType.NONNEGATIVEINTEGER, Pattern.compile("^\\d+$"));
		inputTypeToRegexMap.put(InputType.DECIMAL, Pattern.compile("^[0-9]+([\\.][0-9]+)?$"));
		inputTypeToRegexMap.put(InputType.DATE, Pattern.compile("[1|2]{1}[0-9]{3}[-]{1}[0-9]{2}[-]{1}[0-9]{2}"));
		inputTypeToRegexMap.put(InputType.DATETIME, Pattern.compile("[1|2]{1}[0-9]{3}[-]{1}[0-9]{2}[-]{1}[0-9]{2}[T]{1}[0-9]{2}[:]{1}[0-9]{2}[:]{1}[0-9]{2}"));
		inputTypeToRegexMap.put(InputType.GYEAR, Pattern.compile("[0-9]{4}"));
		inputTypeToRegexMap.put(InputType.DURATION, Pattern.compile("P(?:(?:\\d+D|\\d+M(?:\\d+D)?|\\d+Y(?:\\d+M(?:\\d+D)?)?)(?:T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S))?|T(?:\\d+H(?:\\d+M(?:\\d+S)?)?|\\d+M(?:\\d+S)?|\\d+S)|\\d+W)"));
		inputTypeToRegexMap.put(InputType.WKTLITERAL, Pattern.compile("^(?:<[^>\\s]+>\\s++)?(?i:POINT|LINESTRING|POLYGON|MULTIPOINT|MULTILINESTRING|MULTIPOLYGON|GEOMETRYCOLLECTION)(?:\\s++(?:Z|M|ZM))?\\s*+\\(.*\\)$"));
	}
	
	private void loadInputTypeDefinitions() {
	    keyToInputTypeMap = new HashMap<>();
	    for (String property : specification.properties()) {
	        keyToInputTypeMap.put(property, inputTypesFor(property));
	    }
	}

	private List<InputType> inputTypesFor(String property) {
	    List<InputType> types = new ArrayList<>();
	    for (DcatProperty node : specification.nodesFor(property)) {
	        addInputType(node, types);
	    }
	    return types;
	}

	/**
     * Translates one node into the InputType(s) used by the regex validation.
     */
    private void addInputType(DcatProperty node, List<InputType> types) {
        String nodetype = node.getNodetype();
        if (nodetype == null) {
            return;
        }

        switch (nodetype) {
			case "URI", "RESOURCE" -> add(InputType.ANYURI, types);
            case "LITERAL", "ONLY_LITERAL", "LANGUAGE_LITERAL" -> add(InputType.STRING, types);
            case "DATATYPE_LITERAL" -> {
                for (String datatype : node.getDatatype()) {
                    add(INPUT_TYPE_BY_DATATYPE.get(datatype), types);
                }
            }
            default -> throw new DcatException("Unknown nodetype in specification: " + nodetype);
        }
    }

    private void add(InputType type, List<InputType> types) {
        if (type != null && !types.contains(type)) {
            types.add(type);
        }
    }
}
