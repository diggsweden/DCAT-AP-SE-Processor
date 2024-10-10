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
import java.io.StringWriter;
import java.util.*;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import io.quarkiverse.freemarker.TemplatePath;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

@Path("")
class PreprocessorController {


	/**
	 * REST API Endpoint for creating DCAT-AP-SE data in RDF/XML format
	 * 
	 * @param dir The directory location where the API-specifications are
	 * @return A String containing DCAT-AP-SE data in RDF/XML format or error message
	 */
	@GET()
	@Path("/dcat-generation/files/")
	@Produces("text/plain;charset=UTF-8")
	public String restEndpointProduceRdf(@QueryParam("dir") String dir) {
		Manager manager = new Manager();
		String result = "";

		try {
			result = manager.createDcatFromDirectory(Optional.ofNullable(dir).orElse("/apidef"));
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

	//TODO: Ta bort freemarker och använd en simpel vue application istället?
	/*@POST
	@Path("/dcat-generation/web/")
	@Produces(MediaType.TEXT_PLAIN)
	public String viewEndpoint(	@PathParam("apispecification") String apiSpecification,
								@PathParam("create") String create,
								Model model) {

		List<Result> results = new ArrayList<Result>();
		Manager manager = new Manager();
		MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();

		if (create != null && create.equals("create")) {
			String result = "";

			//Generate DCAT-AP-SE from files
			if(apiSpecification.isEmpty() && !apiFiles.isEmpty()) {

				results = manager.createFromList(apiFiles,model);


				//Generate DCAT-AP-SE from string
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
	}*/
	

	@Inject
	@TemplatePath("index.ftlh")
	Template hello;

	@GET
	@Produces(MediaType.TEXT_HTML)
	public String getIndex() throws TemplateException, IOException {
		StringWriter stringWriter = new StringWriter();
		hello.process(Map.of("name", "bob"), stringWriter);
		String result = stringWriter.toString();
		return result;
	}
}

