package cm.belrose.controller;

import cm.belrose.model.MT103Message;
import cm.belrose.service.MT103ConverterService;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/mt103")
public class MT103Controller {

    private final MT103ConverterService mt103ConverterService;

    @Autowired
    public MT103Controller(MT103ConverterService mt103ConverterService) {
        this.mt103ConverterService = mt103ConverterService;
    }

    /**
     * Endpoint to parse and convert an MT103 message to JSON format.
     *
     * @param mt103Message String containing the MT103 message.
     * @return JSON representation of the MT103 message.
     */
    @PostMapping("/convert")
    public ResponseEntity<?> convertMT103ToJSON(@RequestHeader String mt103Message) {
        try {
            // Parse MT103 message
            MT103Message parsedMessage = mt103ConverterService.parseMT103(mt103Message);

            // Convert parsed message to JSON
            String jsonResponse = mt103ConverterService.convertToJSON(parsedMessage);

            return ResponseEntity.ok(jsonResponse);
        } catch (JsonProcessingException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error converting MT103 message to JSON");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Invalid MT103 message format");
        }
    }
}