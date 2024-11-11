package cm.belrose.controller;

import cm.belrose.model.MT103Message;
import cm.belrose.service.MT103ConverterService1;
import com.fasterxml.jackson.core.JsonProcessingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/mt103")
public class MT103Controller1 {

    private final MT103ConverterService1 mt103ConverterService1;

    @Autowired
    public MT103Controller1(MT103ConverterService1 mt103ConverterService1) {
        this.mt103ConverterService1 = mt103ConverterService1;
    }

    /**
     * Endpoint to parse and convert an MT103 message to JSON format.
     *
     * @param mt103Message String containing the MT103 message.
     * @return JSON representation of the MT103 message.
     */
    @PostMapping("/convert1")
    public ResponseEntity<?> convertMT103ToJSON(@RequestHeader String mt103Message) {
        try {
            // Parse MT103 message
            MT103Message parsedMessage = mt103ConverterService1.parseMT103(mt103Message);

            // Convert parsed message to JSON
            String jsonResponse = mt103ConverterService1.convertToJSON(parsedMessage);

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