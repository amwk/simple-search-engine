package pl.awlodarz.simplesearchengine.api;

import com.hazelcast.core.HazelcastInstance;
import io.swagger.annotations.Api;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

@Api(tags = "Document API")
@RestController
@RequestMapping("/api")
public class Controller {

    private final Logger logger = LoggerFactory.getLogger(Controller.class);
    private final HazelcastInstance hazelcastInstance;

    @Autowired
    public Controller(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    @PostMapping(value = "/document", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String addDocument(@PathVariable(required = true) Long id) {
        return id.toString();
    }

    @GetMapping(value = "/document/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String getDocument(@PathVariable(required = true) Long id) {
        return id.toString();
    }

    @PutMapping(value = "/document/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String updateDocument(@PathVariable(required = true) Long id) {
        return id.toString();
    }

    @DeleteMapping(value = "/document/{id}", produces = MediaType.APPLICATION_JSON_VALUE)
    public @ResponseBody String deleteDocument(@PathVariable(required = true) Long id) {
        return id.toString();
    }
}