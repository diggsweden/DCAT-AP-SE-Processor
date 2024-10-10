/*
 * This file is part of dcat-ap-se-processor.
 *
 * dcat-ap-se-processor is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * dcat-ap-se-processor is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with dcat-ap-se-processor.  If not, see <https://www.gnu.org/licenses/>.
 */

package se.ams.dcatprocessor.rdf;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.time.Period;
import java.time.temporal.TemporalAmount;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.collections4.MultiValuedMap;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Literal;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Resource;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.impl.SimpleNamespace;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;
import org.eclipse.rdf4j.model.vocabulary.DCAT;
import org.eclipse.rdf4j.model.vocabulary.DCTERMS;
import org.eclipse.rdf4j.model.vocabulary.FOAF;
import org.eclipse.rdf4j.model.vocabulary.LOCN;
import org.eclipse.rdf4j.model.vocabulary.PROV;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.VCARD4;
import org.eclipse.rdf4j.model.vocabulary.XSD;
import org.eclipse.rdf4j.rio.RDFHandler;
import org.eclipse.rdf4j.rio.Rio;
import org.eclipse.rdf4j.rio.helpers.BufferedGroupingRDFHandler;
import org.eclipse.rdf4j.rio.rdfxml.util.RDFXMLPrettyWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import se.ams.dcatprocessor.models.Catalog;
import se.ams.dcatprocessor.models.DataClass;
import se.ams.dcatprocessor.models.DataService;
import se.ams.dcatprocessor.models.DataSet;
import se.ams.dcatprocessor.models.Distribution;
import se.ams.dcatprocessor.models.FileStorage;
import se.ams.dcatprocessor.models.Organization;
import se.ams.dcatprocessor.rdf.namespace.ADMS;
import se.ams.dcatprocessor.rdf.namespace.ODRS;
import se.ams.dcatprocessor.rdf.namespace.SCHEMA;
import se.ams.dcatprocessor.rdf.namespace.SPDX;
import se.ams.dcatprocessor.rdf.validate.CardinalityValidator;
import se.ams.dcatprocessor.rdf.validate.InputType;
import se.ams.dcatprocessor.rdf.validate.MultipleURIValidator;
import se.ams.dcatprocessor.rdf.validate.SingleInputValidator;
import se.ams.dcatprocessor.rdf.validate.ValidationErrorStorage;
import se.ams.dcatprocessor.util.Util;

/**
 * @see <a href="https://docs.dataportal.se/dcat/sv/#intro">https://docs.dataportal.se/dcat/sv/#intro</a>
 * @author nacbr
 *
 */
public class RDFWorker {
	private static Logger logger = LoggerFactory.getLogger(RDFWorker.class);

	private Model model;
	
	/**
	 * Holds the file that is presently being processed
	 */
	private String currentFileName;

	private MultipleURIValidator multipleURIValidator;
	
	public RDFWorker() {
		multipleURIValidator = new MultipleURIValidator();
	}

	/**
	 * 
	 * Creates a RDF-file that according to DCAT-AP-SE format based on the data in Catalog and the list of
	 * FileStorage objects. The elements created from the data in the FileStorage objects are appended to
	 * the Catalog
	 * 
	 * @see <a href="https://docs.dataportal.se/dcat/sv/#intro">https://docs.dataportal.se/dcat/sv/#intro</a>
	 * 
	 * @param catalog - Contains the data for Catalog
	 * @param fileStorages - 	The list of FileStorage objects where each FileStorage object contains 
	 * 							data from one API-specification file
	 * @return The resulting RDF-file as an XML-string
	 * @throws DcatException - If there is an error processing data or creating the file
	 * @throws IOException - If there is an error processing data or creating the file
	 */
	public String createDcatFile(Catalog catalog, List<FileStorage> fileStorages) throws DcatException, IOException {

		createModel();
		
		ValidationErrorStorage validationErrorStorage = ValidationErrorStorage.getInstance();
		
		/**
		 * The Catalog data does not come from a file with a name we can access 
		 * so we use a generic name as an identifier for possible errors in this input data
		 */
		SingleInputValidator.getInstance().setCurrentFileName(catalog.fileName);
		CardinalityValidator.getInstance().setCurrentFileName(catalog.fileName);
		multipleURIValidator.setCurrentFileName(catalog.fileName);

		/**
		 * Returnparmeter from createCatalog is a list with IRI where
		 * IRI[0] = Catalog
		 * IRI[1] = Agent
		 */
		IRI[] catalogAndAgent = createCatalog(catalog);
		
		model.add(catalogAndAgent[0], RDF.TYPE, DCAT.CATALOG);
		
		model.add(catalogAndAgent[0], DCTERMS.PUBLISHER, catalogAndAgent[1]);
		
		for (FileStorage fileStorage : fileStorages) {
			
			currentFileName = fileStorage.fileName;
			
			/**
			 * Save validation errors under the correct filename
			 */
			SingleInputValidator.getInstance().setCurrentFileName(currentFileName);
			CardinalityValidator.getInstance().setCurrentFileName(currentFileName);
			multipleURIValidator.setCurrentFileName(currentFileName);
			
			/*
			 * Create DataSets and add them to the model and add the reference from Catalog
			 * to DataSet
			 */
			
			Util.checkNotNull(fileStorage.dcat_dataset, UNABLE_CREATE_MISSING_VALUES.replaceAll("X", "Catalog.dcat_dataset"));
			
			for (DataSet dataSet : fileStorage.dcat_dataset) {	
				IRI iriDataSet = createDataset(dataSet, catalogAndAgent[1]);
				model.add(catalogAndAgent[0], DCAT.HAS_DATASET, iriDataSet);
			}
			
			
			/*
			 * Create DataService and add them to the model and then 
			 * add the reference from Catalog to DataServices
			 */
			for (DataService dataService : fileStorage.dataService) {
				IRI dataServiceIRI = createDataService(dataService);
				model.add(catalogAndAgent[0], DCAT.DATA_SERVICE, dataServiceIRI);
			}
			
			
		}
		
		multipleURIValidator.validate();
		
		if(validationErrorStorage.hasValidationErrors()) {
			DcatException dcatException = new DcatException("Error creating DCAT-AP-SE due to validationerrors. See enclosed list");
			dcatException.setValidationResults(validationErrorStorage.getValidationErrors());
			throw dcatException;
		} else {
			return printModel(model);
		}
		
	}
	
	private void createModel() {
		model = new LinkedHashModel();
		//Add namespaces
		model.setNamespace(DCAT.NS);
		model.setNamespace(DCTERMS.NS);
		model.setNamespace(FOAF.NS);
		model.setNamespace(VCARD4.NS);
		model.setNamespace(RDF.NS);
		model.setNamespace(XSD.NS);
		model.setNamespace(LOCN.NS);
		model.setNamespace(SCHEMA.NS);
		model.setNamespace(PROV.NS);
		model.setNamespace(ADMS.NS);
		model.setNamespace(ODRS.NS);
		model.setNamespace(SPDX.NS);
		model.setNamespace(new SimpleNamespace("dcatap", "http://data.europa.eu/r5r#"));
	}

	/**
	 * Generates a RDF/XML string from the model
	 * 
	 * @return The generated string
	 */
	private String printModel(Model model) {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		Rio.write(model, new BufferedGroupingRDFHandler((RDFHandler) new RDFXMLPrettyWriter(bos)));
		// Rio.write(model, new RDFXMLPrettyWriter(bos)); //Not so pretty output with this printer
		return bos.toString(Charset.forName("UTF-8"));
	}


	/*
	 * Predefined errormessage
	 */
	private final String UNABLE_CREATE_MISSING_VALUES = this.getClass() + " : Unable to create DCAT. Reason: X is a mandatory but is missing"; 


	/**
	 * Creates a catalog from data contained in the catalog object
	 * @see <a href="http://www.w3.org/ns/dcat#Catalog">Catalog</a>
	 * @param catalog - Containing the data
	 * @return IRI[] - A list of IRI with two objects [0] Catalog(IRI) [1] Agent(IRI)
	 * @throws DcatException - If there is an error during processing
	 * @throws IOException - If there is an IOException
	 */
	private IRI[] createCatalog(Catalog catalog) throws DcatException, IOException{
	
		IRI[] catalogAndAgent = new IRI[2];
		
		Util.checkNotNull(catalog, UNABLE_CREATE_MISSING_VALUES.replaceAll("X", "Catalog"));
		
		/**
		 * Special check that subject exist and is valid URI because this is a stopping error
		 */
		checkSubject("Catalog.about", catalog.about); 
		
		Util.checkNotNull(catalog.publisher, UNABLE_CREATE_MISSING_VALUES.replaceAll("X", "Catalog.publisher"));
		
		/**
		 * Check that the keys for inputvalues:
		 * 1. Are defined in the specification
		 * 2. Occur within the range allowed in the specification
		 */
		List<String> checkedElsewhere = List.of("dcat:dataset", "dcterms:publisher"); //Items that will not be checked now
		CardinalityValidator.getInstance().validate(DcatClass.CATALOG, catalog.dcData, checkedElsewhere);
		
		IRI catalogIRI = createIri(catalog.about);
		
		catalogAndAgent[0] = catalogIRI;
		
		IRI agentIRI = createAgent(catalog.publisher);
		catalogAndAgent[1] = agentIRI;
		
		addToModel(model, catalogIRI, catalog.dcData);
				
		/*
		 * Add the Rights...if it exist
		 */
		addRights(catalogIRI, catalog.rights);
		
		/*
		 * Add locations as anonymous nodes
		 */
		addNodes(catalogIRI, DCTERMS.SPATIAL, DCTERMS.LOCATION, catalog.spatial);
		
		return catalogAndAgent;
	}
	
	/**
	 * Creates a Agent with all its data @see <a href="http://xmlns.com/foaf/0.1/Agent">Agent</a>
	 * @param agent - The object containing the data for Agent
	 * @return - The Agent
	 */
	private IRI createAgent(DataClass agent) {
		/**
		 * Special check that subject exist and is valid URI because this is a stopping error
		 */
		checkSubject("Agent.about", agent.about); 
		
		/**
		 * Check that the keys for inputvalues:
		 * 1. Are defined in the specification
		 * 2. Occur within the range allowed in the specification
		 */
		CardinalityValidator.getInstance().validate(DcatClass.AGENT, agent.dcData, List.of());

		IRI agentIRI = SimpleValueFactory.getInstance().createIRI(agent.about);

		model.add(agentIRI, RDF.TYPE, FOAF.AGENT);

		addToModel(model, agentIRI, agent.dcData);

		return agentIRI;
	}

	/**
	 * Creates a Dataset with all its data and creates a link from
	 * the dataset to the Publisher
	 * @see <a href="http://www.w3.org/ns/dcat#Dataset">Dataset</a>
	 * @param dataSet The object containing the data for Dataset
	 * @param agentIRI - The Agent to link to
	 * @return - The Dataset
	 */
	private IRI createDataset(DataSet dataSet, IRI agentIRI) throws IOException {
		/**
		 * Special check that subject exist and is valid URI because this is a stopping error
		 */
		checkSubject("DataSet.about", dataSet.about);
				
		/**
		 * Check that the keys for inputvalues:
		 * 1. Are defined in the specification
		 * 2. Occur within the range allowed in the specification
		 */
		List<String> checkedElsewhere = List.of("dcterms:publisher", "dcterms:creator"); //Items that will not be checked now
		CardinalityValidator.getInstance().validate(DcatClass.DATASET, dataSet.dcData, checkedElsewhere);
		
	
		IRI dataSetIRI =  createIri(dataSet.about);
		model.add(dataSetIRI, RDF.TYPE, DCAT.DATASET);
		
		//Add all values to dataset
		addToModel(model, dataSetIRI, dataSet.dcData);

		//Add the reference to the external element publisher
		model.add(dataSetIRI, DCTERMS.PUBLISHER, agentIRI);

		/*
		 * Add creator
		 */
		if ((dataSet.creator != null) && !dataSet.creator.equals(null)) {
			IRI CreatorIRI = createAgent(dataSet.creator);
			model.add(dataSetIRI, DCTERMS.CREATOR, CreatorIRI);
		}

		
		/*
		 * Add locations as anonymous nodes
		 */
		addNodes(dataSetIRI, DCTERMS.SPATIAL, DCTERMS.LOCATION, dataSet.spatial);
		
		/*
		 * Add temporals as anonymous nodes
		 */
		addNodes(dataSetIRI, DCTERMS.TEMPORAL, DCTERMS.PERIOD_OF_TIME, dataSet.temporals);
		
		/*
		 * Add offers as anonymous nodes
		 */
		addNodes(dataSetIRI, SCHEMA.OFFERS, SCHEMA.OFFER, dataSet.offers);		
	
		/*
		 * Add the Uppfyller...conformsTo as anonymous nodes
		 */
		addNodes(dataSetIRI, DCTERMS.CONFORMS_TO, DCTERMS.STANDARD, dataSet.conformsTo);
		
		/*
		 * Add the Documentation...foaf:page as anonymous nodes
		 */
		addNodes(dataSetIRI, FOAF.PAGE, FOAF.DOCUMENT, dataSet.documents);
		
		/*
		 * Add the Relations...dcat:qualifiedRelation as anonymous nodes
		 */
		addNodes(dataSetIRI, DCAT.QUALIFIED_RELATION, DCAT.RELATIONSHIP, dataSet.qualifiedRelations);
		
		/*
		 * Add Provenance...dcterms:provenance as anonymous nodes
		 */
		addNodes(dataSetIRI, DCTERMS.PROVENANCE_STATEMENT, DCTERMS.PROVENANCE, dataSet.provenances);
		
		/*
		 * Add the Övrig aktör ...prov:qualifiedAttribution as anonymous nodes
		 * Different from other anonymous nodes since it has an Agent(complex type)
		 */
		for (DataClass dataClass : dataSet.otherAgents) {
			List<Resource> anonymousNodesList = addNodes(dataSetIRI, PROV.QUALIFIED_ATTRIBUTION, PROV.ATTRIBUTION, List.of(dataClass));
			
			IRI tmpAgentIRI = createAgent(dataClass.agent);
			
			model.add(anonymousNodesList.get(0), PROV.AGENT_PROP, tmpAgentIRI);
		}
		
		
		/*
		 * Create ContactPoints and add them to the model and then 
		 * add the reference from DataSet to ContactPoints
		 */
		for (Organization org : dataSet.organizations) {
			IRI organizationIRI = createOrganization(org);
			model.add(dataSetIRI, DCAT.CONTACT_POINT, organizationIRI);
		}

		/*
		 * Create Distributions and add them to the model and then 
		 * add the reference from DataSet to Distributions
		 */
		for (Distribution distribution : dataSet.dcat_distribution) {
			IRI distributionIRI = createDistribution(distribution);
			model.add(dataSetIRI, DCAT.HAS_DISTRIBUTION, distributionIRI);
		}

		return dataSetIRI;
	}
	
	private void addRights(IRI parentIRI, DataClass rights) {
		
		if(Util.isNullOrEmpty(rights)) {
			return;
		}
		
		/**
		 * Create the rights node as anonymous nodes with reference to parent node
		 */
		List<Resource> rightsNodesList = addNodes(parentIRI, DCTERMS.RIGHTS, ODRS.RIGHTS_STATEMENT, List.of(rights));
		
		/**
		 * Create the license documents as anonymous nodes with reference to the Rights node
		 */
		addNodes(rightsNodesList.get(0), ODRS.COPYRIGHT_STATEMENT, DCTERMS.LICENSE_DOCUMENT, rights.licenseDocuments);
		
		/*
		 * Create Agents(publishers) and add them to the model and 
		 * then add the reference from DataService to Agents(publishers) 
		 */
		for (DataClass agent : rights.agents) {
			IRI agentIRI = createAgent(agent);
			model.add(rightsNodesList.get(0), ODRS.COPYRIGHT_HOLDER, agentIRI);
		}

	}
			
	/**
	 * Creates a Distribution with all its data
	 * @see <a href="http://www.w3.org/ns/dcat#Distribution">Distribution</a>
	 * @param distribution The object containing the data for Distribution
	 * @return The Distribution
	 */
	private IRI createDistribution(Distribution distribution) throws IOException {
		/**
		 * Special check that subject exist and is valid URI because this is a stopping error
		 */
		checkSubject("Distribution.about", distribution.about);

		/**
		 * Check that the keys for inputvalues:
		 * 1. Are defined in the specification
		 * 2. Occur within the range allowed in the specification
		 */
		List<String> checkedElsewhere = List.of("dcat:accessService"); //Items that will not be checked now
		CardinalityValidator.getInstance().validate(DcatClass.DISTRIBUTION, distribution.dcData, checkedElsewhere);
		
		IRI distributionIRI = createIri(distribution.about);
		model.add(distributionIRI, RDF.TYPE, DCAT.DISTRIBUTION);
		
		//Add all values to Organization
		addToModel(model, distributionIRI, distribution.dcData);
		
		/*
		 * Add the Uppfyller...conformsTo as anonymous nodes
		 */
		addNodes(distributionIRI, DCTERMS.CONFORMS_TO, DCTERMS.STANDARD, distribution.conformsTo);

		/*
		 * Add the licensedocuments as anonymous nodes
		 */
		addNodes(distributionIRI, DCTERMS.LICENSE, DCTERMS.LICENSE_DOCUMENT, distribution.licenseDocuments);
		
		for (DataClass dataClass : distribution.licenseDocuments) {
			
		}
		
		/*
		 * Add the Documentation...foaf:page as anonymous nodes
		 */
		addNodes(distributionIRI, FOAF.PAGE, FOAF.DOCUMENT, distribution.documents);
		
		/*
		 * Add the Rights...if it exist
		 */
		addRights(distributionIRI, distribution.rights);
		
		/*
		 * Add CheckSum...if it exist
		 */
		if(Util.isNotNullOrEmpty(distribution.checksum)) {
			addNodes(distributionIRI, SPDX.CHECKSUMS, SPDX.CHECKSUM, List.of(distribution.checksum));
		}
		
		/*
		 * Create DataServices and add them to the model and 
		 * then add the reference from Distribution to DataServices 
		 */
		for (DataService dataService : distribution.dataServices) {
			IRI dataServiceIRI = createDataService(dataService);
			model.add(distributionIRI, DCAT.DATA_SERVICE, dataServiceIRI);
		}
				
		return distributionIRI;
	}
	
	/**
	 * Creates a Dataservice with all its data
	 * @see <a href="http://www.w3.org/ns/dcat#DataService">DataService</a>
	 * @param dataService - The object containing the data for dataService
	 * @return DataService - The Dataservice
	 */
	private IRI createDataService(DataService dataService) throws IOException {
		/**
		 * Special check that subject exist and is valid URI because this is a stopping error
		 */		
		checkSubject("Dataservice.about", dataService.about);
				
		/**
		 * Check that the keys for inputvalues:
		 * 1. Are defined in the specification
		 * 2. Occur within the range allowed in the specification
		 */
		List<String> checkedElsewhere = List.of("dcterms:publisher", "dcat:contactPoint"); //Items that will not be checked now
		CardinalityValidator.getInstance().validate(DcatClass.DATASERVICE, dataService.dcData, checkedElsewhere);
		
		IRI dataServiceIRI = createIri(dataService.about);
		model.add(dataServiceIRI, RDF.TYPE, DCAT.DATA_SERVICE);
		
		//Add all simple values to Dataservice
		addToModel(model, dataServiceIRI, dataService.dcData);
		
		/*
		 * Add the Uppfyller...conformsTo as anonymous nodes
		 */
		addNodes(dataServiceIRI, DCTERMS.CONFORMS_TO, DCTERMS.STANDARD, dataService.conformsTo);
		
		/*
		 * Add the licensedocuments as anonymous nodes
		 */
		addNodes(dataServiceIRI, DCTERMS.LICENSE, DCTERMS.LICENSE_DOCUMENT, dataService.licenseDocuments);
		
		/*
		 * Add the Documentation...foaf:page as anonymous nodes
		 */
		addNodes(dataServiceIRI, FOAF.PAGE, FOAF.DOCUMENT, dataService.documents);
		
		/*
		 * Create Agents(publishers) and add them to the model and 
		 * then add the reference from DataService to Agents(publishers) 
		 */
		for (DataClass agent : dataService.agents) {
			IRI agentIRI = createAgent(agent);
			model.add(dataServiceIRI, DCTERMS.PUBLISHER, agentIRI);
		}

		/*
		 * Create Organizations and add them to the model and 
		 * then add the reference from DataService to Organizations 
		 */
		for (Organization organization : dataService.organizations) {
			IRI organizationIRI = createOrganization(organization);
			model.add(dataServiceIRI, DCAT.CONTACT_POINT, organizationIRI);
		}
		
		return dataServiceIRI;
	}
	
	/**
	 * Creates a ContactPoint with all its data
	 * @see <a href="http://www.w3.org/ns/dcat#Organization">Organization</a>
	 * @param org - The object containing the data for Organization
	 * @return - The Organization
	 */
	private IRI createOrganization(Organization org) throws IOException {
		/**
		 * Special check that subject exist and is valid URI because this is a stopping error
		 */
		checkSubject("ContactPoint.about", org.about);
		
		/**
		 * Check that the keys for inputvalues:
		 * 1. Are defined in the specification
		 * 2. Occur within the range allowed in the specification
		 */
		List<String> checkedElsewhere = List.of("vcard:hasTelephone", "vcard:hasAddress"); //Items that will not be checked now
		CardinalityValidator.getInstance().validate(DcatClass.ORGANISATION, org.dcData, checkedElsewhere);
				
		IRI organizationIRI = createIri(org.about);
		model.add(organizationIRI, RDF.TYPE, VCARD4.ORGANIZATION);

		//Add all simple values to Organization
		addToModel(model, organizationIRI, org.dcData);

		/*
		 * Add addresses as anonymous nodes
		 */
		addNodes(organizationIRI, VCARD4.HAS_ADDRESS, VCARD4.ADDRESS, org.adress);
		
		/*
		 * Add phones as anonymous nodes
		 */
		addNodes(organizationIRI, VCARD4.HAS_TELEPHONE, VCARD4.VOICE, org.phone);
		
		return organizationIRI;
	}
	
	
	/**
	 * Creates a node with an anonymous link or an URI depending on if an URI-link is submitted
	 * @param parentIRI - Reference to parent node
	 * @param parentIRINodeTypeRef - Parent node reference type
	 * @param nodeType - Type of node
	 * @param dataClasses - Placeholder for data to be added to this node
	 * @return The list of created BNodes
	 */
	private List<Resource> addNodes(Resource parentIRI, IRI parentIRINodeTypeRef, IRI nodeType, List<DataClass> dataClasses) {
		List<Resource> resources = new ArrayList<>();
		
		ValueFactory valueFactory = SimpleValueFactory.getInstance();
		
		for (DataClass dataClass : dataClasses) {
			Resource resource;
			if(Util.isNullOrEmpty(dataClass.about)) {
				resource = valueFactory.createBNode();			//Create an anonymous node	
			} else {
				/**
				 * Special check that subject exist and is valid URI because this is a stopping error
				 */
				checkSubject(parentIRINodeTypeRef.getLocalName(), dataClass.about);
				resource = createIri(dataClass.about);			//Otherwise create a node with an URI as reference
			}
			
			model.add(parentIRI, parentIRINodeTypeRef, resource);	//Add reference from parent IRI
			model.add(resource, RDF.TYPE, nodeType);
			addDataToNode(resource, dataClass.dcData);	//Add data to node
			
			resources.add(resource);
		}
		
		return resources;
	}
	
	/**
	 * Adds the values in valueMap to the node
	 * @param resource - The node to add value to
	 * @param valueMap - Containing the data to be added
	 */
	private void addDataToNode(Resource resource, MultiValuedMap<String, String> valueMap) {
		
		ValueFactory valueFactory = SimpleValueFactory.getInstance();
		
		Set<String> keySet = valueMap.keySet();
		
		for (String key : keySet) {
		
			IRI iri = VocabularyStringToIRI.getIRI(key);
			
			if (Util.isNotNullOrEmpty(iri)) {
			
				Collection<String> values = valueMap.get(key);
				
				for (String value : values) {
					
					SingleInputValidator.getInstance().validateData(key, value);
				
					if (Util.isURI(value) ) { // Check if its an URI and create a resource
						model.add(resource, iri, valueFactory.createIRI(value));
					} else if (isLanguageValue(value)) { // Check if it's a language specific string, Ex en:Some text in english
						String[] split = value.split(SUN);
						model.add(resource, iri, valueFactory.createLiteral(split[1], split[0]));
					} else {
						Literal dateLiteral = getDateValue(value);
						if (dateLiteral != null) { 					//Datevalue
							model.add(resource, iri, dateLiteral);
						} else if(isPhoneValue(key)) {	//Phone value...needs to handled separately from a URI
							model.add(resource, iri, valueFactory.createIRI(value));
						} else {// All other values....for now
							model.add(resource, iri, valueFactory.createLiteral(value));	
						}
					}
				}
			}
		}
	}
	

	/**
	 * Wrappermethod for creating an IRI and at the same time save the URI
	 * for later validation
	 * @param uriString - The URI
	 * @return IRI
	 */
	private IRI createIri(String uriString) {
		multipleURIValidator.addUri(uriString);
		return SimpleValueFactory.getInstance().createIRI(uriString);
	}
	
	/**
	 * Checks that the given string has the format of a languange specific string
	 * That it has a format like this: "en:Description in English"
	 * @param testIfLanguage - The string to test
	 * @return T/F depending of the test
	 */
	private boolean isLanguageValue(String testIfLanguage) {
		if(Util.isNullOrEmpty(testIfLanguage)) {
			return false;
		}
		
		String[] split = testIfLanguage.split(SUN);
		if(split.length == 2) {
			if(Util.isNullOrEmpty(split[0]) || Util.isNullOrEmpty(split[1])) {
				return false;
			}
			return (split[0].length() == 2 || split[0].length() == 3) && split[1].length() >= 1;
		}
		
		return false;
	}
	
	//TODO: Improve this and remove hardcoded value
	//Constants for multiple use
	private final String VCARD_HAS_VALUE = "vcard:hasValue";
	
	private boolean isPhoneValue(String key) {
		return key.equals(VCARD_HAS_VALUE);
	}

	/**
	 * Converts a string to a BigDecimal object if possible. Otherwise returns null
	 * @param key - Used to get the allowable InputType(s)
	 * @param value - The value to be converted
	 * @return - The resulting BigDecimal object or null 
	 */
	private BigDecimal getBigDecimal(String key, String value) {
	    if (Util.isNullOrEmpty(key) || Util.isNullOrEmpty(value)) {
	        return null; 
	    }
	    
	    List<InputType> inputTypes = SingleInputValidator.getInstance().getInputTypes(key);
	    
	    if(inputTypes.contains(InputType.INTEGER) || inputTypes.contains(InputType.DECIMAL)) {
	    	try {
			    return new BigDecimal(value);
			    /**
			     * No need to logg error here.
			     * Its has been already checked in SingleInputValidator
			     */
			} catch (NumberFormatException e) {
				return null;
			}
	    }

	    return null;
	}

	/**
	 * Converts a string to a TemporalAmount object if possible. Otherwise returns null
	 * @param key - Used to get the allowable InputType(s)
	 * @param value - The value to be converted
	 * @return - The resulting TemporalAmount object or null 
	 */
	private TemporalAmount getTemporalAmount(String key, String value) {
	    if (Util.isNullOrEmpty(key) || Util.isNullOrEmpty(value)) {
	        return null; 
	    }

	    List<InputType> inputTypes = SingleInputValidator.getInstance().getInputTypes(key);
	    
	    if(inputTypes.contains(InputType.DURATION)) {
	    	try {
			    return Period.parse(value);
			    /**
			     * No need to logg error here.
			     * Its has been already checked in SingleInputValidator
			     */
			} catch (NumberFormatException e) {
				return null;
			}
	    }

	    return null;

	}
	
	private final String INVALID_URI = this.getClass() + " : Unable to create DCAT. Reason: X is not a valid URI";
	
	/**
	 * Checks that a value exists and is a valid URI
	 * @param subjectKey - Name of the value
	 * @param subjectURI - The value itself
	 * @throws DcatException - Thrown if null or invalid
	 */
	private void checkSubject(String subjectKey, String subjectURI) throws DcatException {
		Util.checkNotNull(subjectURI, UNABLE_CREATE_MISSING_VALUES.replaceAll("X", subjectKey));
		if(!Util.isURI(subjectURI)) {
			throw new DcatException(INVALID_URI.replaceAll("X", subjectKey + "=" + subjectURI));
		}
	}
	
	/**
	 * Special delimiter for language-prefixed strings since it's not commonly used
	 */
	private final String SUN = "¤";
	
	/**
	 * Adds all the values in the valueMap to the model inside the sectionIRI(subject)
	 * @param model - The model to add the values to
	 * @param sectionIRI - The section where the values are added
	 * @param valueMap - The map containing the values
	 */
	/**
	 * @param model
	 * @param sectionIRI
	 * @param valueMap
	 */
	private void addToModel(Model model, IRI sectionIRI, MultiValuedMap<String, String> valueMap) {
		ValueFactory valueFactory = SimpleValueFactory.getInstance();
		Set<String> keySet = valueMap.keySet();
		
		for (String key : keySet) {

			IRI iri = VocabularyStringToIRI.getIRI(key);

			if (Util.isNotNullOrEmpty(iri)) {

				Collection<String> values = valueMap.get(key);

				for (String value : values) {

					SingleInputValidator.getInstance().validateData(key, value);
					
					/**
					 * First check if its a numeric value...otherwise it might be interpreted as date value further down
					 */
					BigDecimal bigDecimal = getBigDecimal(key, value);
					if(Util.isNotNullOrEmpty(bigDecimal)) {
						model.add(sectionIRI, iri, valueFactory.createLiteral(bigDecimal));
					} else {
						TemporalAmount temporalAmount = getTemporalAmount(key, value);
						if (Util.isNotNullOrEmpty(temporalAmount)) {
							model.add(sectionIRI, iri, valueFactory.createLiteral(temporalAmount));
						} else if (Util.isURI(value)) { // Check if its an URI and create a resource
							model.add(sectionIRI, iri, valueFactory.createIRI(value));
						} else if (isLanguageValue(value)) { // Check if it's a language specific string, Ex en¤Some text in english
							String[] split = value.split(SUN);
							model.add(sectionIRI, iri, valueFactory.createLiteral(split[1], split[0]));
						} else {
							Literal dateLiteral = getDateValue(value);
							if(dateLiteral != null) {	//Datevalue
								model.add(sectionIRI, iri, dateLiteral);
							} else {					//All other values....for now
								model.add(sectionIRI, iri, valueFactory.createLiteral(value));
							}
								
						}

					}
				}

			}
		}

	}
	
	// Regexp pattern used for date-format checking
	private final Pattern XSD_DATE_TIME_PATTERN = Pattern.compile("[1|2]{1}[0-9]{3}[-]{1}[0-9]{2}[-]{1}[0-9]{2}[T]{1}[0-9]{2}[:]{1}[0-9]{2}[:]{1}[0-9]{2}"); // YYYY-MM-DDThh:mm:ss
	private final Pattern XSD_DATE_PATTERN = Pattern.compile("[1|2]{1}[0-9]{3}[-]{1}[0-9]{2}[-]{1}[0-9]{2}"); // YYYY-MM-DD
	private final Pattern XSD_GYEAR_PATTERN = Pattern.compile("[0-9]{4}"); // YYYY

	/**
	 * Method validates a string against the supplied pattern
	 * 
	 * @param pattern - To validate against
	 * @param test    - The string to validate
	 * @return - T/F depending of the outcome
	 */
	private boolean validateAgainstPattern(Pattern pattern, String test) {
		return pattern.matcher(test).matches();
	}

	/**
	 * Converts a string value to a properly formatted DATE-Literal depending of the format of the
	 * string IF the string is a valid date. Otherwise null is returned
	 * @param value - The value to test
	 * @return - Date-formatted literal or null
	 */
	private Literal getDateValue(String value) {
		if(value == null) {
			return null;
		}
		
		ValueFactory valueFactory = SimpleValueFactory.getInstance();
		
		if (validateAgainstPattern(XSD_DATE_PATTERN, value)) {				//Datevalue YYYY-MM-DD
			return valueFactory.createLiteral(value, XSD.DATE); 
		} else if (validateAgainstPattern(XSD_DATE_TIME_PATTERN, value)) {	//Datevalue YYYY-MM-DDThh:mm:ss
			return valueFactory.createLiteral(value, XSD.DATETIME); 
		} else if (validateAgainstPattern(XSD_GYEAR_PATTERN, value)) {		//Datevalue YYYY
			return valueFactory.createLiteral(value, XSD.GYEAR);
		} else {
			return null;
		}
		
	}
	
	
//	
//	/**
//	 * Creates a anonymous node in the .RDF document
//	 * @param parentIRI - Reference to parent node
//	 * @param parentIRINodeTypeRef - Parent node reference type
//	 * @param nodeType - Type of node
//	 * @param dataClasses - Placeholder for data to be added to this node
//	 * @return The list of created BNodes
//	 */
//	private List<BNode> addAnonymousNodes(@Nonnull Resource parentIRI, @Nonnull IRI parentIRINodeTypeRef, @Nullable IRI nodeType, @Nonnull List<DataClass> dataClasses) {
//		List<BNode> bNodes = new ArrayList<>();
//		
//		ValueFactory valueFactory = SimpleValueFactory.getInstance();
//		
//		for (DataClass dataClass : dataClasses) {
//			BNode bNode = valueFactory.createBNode();			//Create an anonymous node
//			model.add(parentIRI, parentIRINodeTypeRef, bNode);	//Add reference from parent IRI
//			if(nodeType != null) {
//				model.add(bNode, RDF.TYPE, nodeType);	//Add nodetype explicitly if required
//				bNodes.add(bNode);
//			}
//			addDataToBnode(bNode, dataClass.dcData);	//Add data to node
//		}
//		
//		return bNodes;
//	}
	
}
