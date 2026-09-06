package bflow.contact.service;

import bflow.common.aws.service.EmailTemplateService;
import bflow.contact.dto.ContactRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class ContactService {

    /**
     * Service responsible for sending templated emails.
     */
    private final EmailTemplateService emailTemplateService;

    /**
     * Sends a contact message to the configured BFlow support address.
     *
     * @param request contact form request
     */
    public void sendMessage(final ContactRequest request) {
        emailTemplateService.sendContactMessage(
                request.name(),
                request.email(),
                request.subject(),
                request.message()
        );
    }
}
