package bflow.contact.controllers;

import bflow.contact.dto.ContactRequest;
import bflow.contact.service.ContactService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/contact")
@RequiredArgsConstructor
public class ContactController {

    private final ContactService contactService;

    @PostMapping
    public ResponseEntity<Void> sendMessage(
            @Valid @RequestBody ContactRequest request
    ) {
        contactService.sendMessage(request);

        return ResponseEntity.accepted().build();
    }
}
