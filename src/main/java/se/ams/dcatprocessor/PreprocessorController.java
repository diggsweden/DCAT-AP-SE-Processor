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

package se.ams.dcatprocessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PropertiesLoaderUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.multipart.MultipartFile;

@Controller
class PreprocessorController {

	Resource resource = new ClassPathResource("application.properties");
	Properties properties;
	{
		try {
			properties = PropertiesLoaderUtils.loadProperties(resource);
		} catch (IOException e) {
			e.printStackTrace();
		}
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
		Manager manager = new Manager();
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
		Manager manager = new Manager();
		MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();

		if (create != null && create.equals("create")) {
			String result = "";

			/**
			 * Generate DCAT-AP-SE from files
			 */
			if(apiSpecification.isEmpty() && !apiFiles.isEmpty()) {

				results = manager.createFromList(apiFiles,model);

				/**
				 * Generate DCAT-AP-SE from string
				 */
			} else if(!apiSpecification.isEmpty()){

				try {
					apiSpecMap.put("apifile", apiSpecification);
					result = manager.createDcat(apiSpecMap);
				} catch (Exception e) {			//Catch and show processing errors in web-gui
					result = e.getMessage();
					results.add(new Result(null, result));
					e.printStackTrace();
				}
				results.add(new Result(null, result));
			}
			model.addAttribute("results", results);
		}
		return "index";
	}
	
	
}

