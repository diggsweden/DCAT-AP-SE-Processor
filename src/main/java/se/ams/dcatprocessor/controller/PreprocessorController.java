// SPDX-FileCopyrightText: 2022 Agency for Digital Government
//
// SPDX-License-Identifier: EUPL-1.2

package se.ams.dcatprocessor.controller;
import se.ams.dcatprocessor.processor.Manager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
class PreprocessorController {

	private final ObjectProvider<Manager> managerProvider;
	Resource resource = new ClassPathResource("application.properties");
	Properties properties;
	{
		try {
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

  	public PreprocessorController(ObjectProvider<Manager> managerProvider) {
        this.managerProvider = managerProvider;
    }

    @GetMapping("/")
    public String index(Model model) {
        return "index";
    }
 
	/**
	 * REST API Endpoint for creating DCAT-AP-SE data in RDF/XML format
	 * 
	 * @param dir The directory location where the API-specifications are
	 * @return A String containing DCAT-AP-SE data in RDF/XML format or error message
	 */
	@RequestMapping(value="/dcat-generation/files/", method = RequestMethod.GET, produces = "text/plain;charset=UTF-8")
	@ResponseBody
	public String restEndpointProduceRdf(@RequestParam(name = "dir", defaultValue = "/apidef") String dir) {
		Manager manager = managerProvider.getObject();
		String result = "";

		try {
			result = manager.createDcatFromDirectory(dir);
		} catch (Exception e) {
			result = e.getMessage();
		}
		return result;
	}

	/**
	 * View endpoint - Access from web-gui
	 * 
	 * @param apiSpecification	The Api-specification as string
	 * @param create		    The name of the "create" button in web-gui
	 * @param apiFiles			List of api-definitions files
	 * @param model				The Model according to Spring MVC pattern
	 * @return					The index page with the result added
	 */
	@PostMapping("/dcat-generation/web/") 
	public String viewEndpoint(	@RequestParam(name = "apispecification", required = false) String apiSpecification, 
								@RequestParam(name = "create", required = true) String create,
								@RequestParam(name = "apifile", required = false) List<MultipartFile> apiFiles,
								Model model) {
		
		List<Result> results = new ArrayList<Result>();
		MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();

		if (create != null && create.equals("create")) {
			Manager manager = managerProvider.getObject();
			String result = "";

			// Generate DCAT-AP-SE from files
			if(apiSpecification.isEmpty() && !apiFiles.isEmpty()) {
				results = manager.createFromList(apiFiles,model);
		
			// Generate DCAT-AP-SE from string
			} else if(!apiSpecification.isEmpty()){
				try {
					apiSpecMap.put("apifile", apiSpecification);
					result = manager.createDcat(apiSpecMap);
				
				//Catch and show processing errors in web-gui
				} catch (Exception e) {
					result = e.getMessage();
					e.printStackTrace();
				}
				results.add(new Result(result));
			}
			model.addAttribute("results", results);
		}
		return "index";
	}
}
