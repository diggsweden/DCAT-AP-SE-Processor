package se.ams.dcatprocessor.controller;

import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import se.ams.dcatprocessor.processor.Manager;

@RestController
@RequestMapping("/api/dcat")
public class DcatController {
   
    private final ObjectProvider<Manager> managerProvider;
    private static final Logger logger = LoggerFactory.getLogger(DcatController.class);

    public DcatController(ObjectProvider<Manager> managerProvider) {
        this.managerProvider = managerProvider;
    }

    @PostMapping(value = "/generate", consumes = MediaType.TEXT_PLAIN_VALUE, produces = MediaType.TEXT_PLAIN_VALUE)
    public ResponseEntity<String> generate(@RequestBody String spec) {
        Manager manager = managerProvider.getObject();
        MultiValuedMap<String, String> apiSpecMap = new ArrayListValuedHashMap<>();
        apiSpecMap.put("apifile", spec);

        try {
            String result = manager.createDcat(apiSpecMap);
            if(isRdf(result)){
                return ResponseEntity.ok(result);
            }
            return ResponseEntity.unprocessableContent().body(result);

        } catch (Exception e) {
            logger.error("DCAT generation failed", e);
            return ResponseEntity.status(HttpStatus.UNPROCESSABLE_CONTENT).body(e.getMessage());
        }
    }

    private static boolean isRdf(String result) {
        if (result == null) return false;
        
        String trimmed = result.stripLeading();
        return trimmed.startsWith("<?xml") || trimmed.startsWith("<rdf:RDF");
    }
}
