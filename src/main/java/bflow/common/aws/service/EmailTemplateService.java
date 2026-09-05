package bflow.common.aws.service;

import bflow.budget.DTO.BudgetResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.Year;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

/**
 * Service for sending email notifications with templated content.
 */
@Service
@RequiredArgsConstructor
public final class EmailTemplateService {

    /** Template engine for rendering email templates. */
    private final TemplateEngine templateEngine;
    /** Service for sending emails via AWS SES. */
    private final SesEmailService sesEmailService;

    /** Frontend URL for email links. */
    @Value("${app.frontend-url}")
    private String frontendUrl;

    /** Support email address for customer inquiries. */
    @Value("${support.email}")
    private String supportEmail;

    /** URL for the application logo in emails. */
    @Value("${app.email.logo-url}")
    private String logoUrl;

    /** Expiration time in minutes for password reset tokens. */
    @Value("${security.password-reset.expiration-minutes}")
    private Integer resetExpirationMinutes;

    /** Expiration time in hours for email verification tokens. */
    @Value("${security.email-verification.expiration-hours}")
    private Integer verificationExpirationHours;

    /**
     * Sends a password reset email to the user.
     * @param toEmail the recipient email address.
     * @param userName the user's name for personalization.
     * @param token the password reset token.
     */
    public void sendPasswordResetEmail(
            final String toEmail,
            final String userName,
            final String token
    ) {

        String resetUrl =
                frontendUrl
                        + "/reset-password?token="
                        + token;

        Context context = new Context();

        context.setVariable("userName", userName);
        context.setVariable("resetUrl", resetUrl);
        context.setVariable("minutes", resetExpirationMinutes);
        context.setVariable("year", Year.now().getValue());
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("logoUrl", logoUrl);

        String html = templateEngine.process(
                "forgot-password",
                context
        );

        sesEmailService.sendEmail(
                toEmail,
                "Reset your BFlow password",
                html
        );
    }

    /**
     * Sends an email verification email to the specified recipient.
     * @param toEmail the recipient email address.
     * @param userName the user's name for personalization.
     */
    public void sendEmailVerificationEmail(
            final String toEmail,
            final String userName
            //final String token
    ) {

        String verificationUrl =
                frontendUrl
                        + "/api/auth/verify-email?token=";
                        //+ token;

        Context context = new Context();

        context.setVariable("userName", userName);
        context.setVariable("verificationUrl", verificationUrl);
        context.setVariable("year", Year.now().getValue());
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("logoUrl", logoUrl);

        String html = templateEngine.process(
                "email-verification",
                context
        );

        sesEmailService.sendEmail(
                toEmail,
                "Verify your BFlow email",
                html
        );
    }

    /**
     * Send a renewal reminder email for an upcoming subscription renewal.
     *
     * @param toEmail recipient email address
     * @param userName recipient display name
     * @param planName the subscription plan name
     * @param amount the renewal amount
     * @param renewalDate the renewal date to display
     * @param checkoutUrl checkout URL for the renewal flow
     */
    public void sendRenewalReminderEmail(
        final String toEmail,
        final String userName,
        final String planName,
        final String amount,
        final String renewalDate,
        final String checkoutUrl
    ) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("planName", planName);
        context.setVariable("amount", amount);
        context.setVariable("renewalDate", renewalDate);
        context.setVariable("checkoutUrl", checkoutUrl);
        context.setVariable("year", Year.now().getValue());
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("logoUrl", logoUrl);

        String html = templateEngine.process("renewal-reminder", context);
        sesEmailService.sendEmail(
                toEmail, "Your " + planName + " plan is renewing soon", html
        );
    }

    /**
     * Sends a wallet collaboration invitation email.
     *
     * The email contains the inviter's name, the wallet name,
     * an invitation link, and the invitation expiration date.
     *
     * @param toEmail the recipient email address
     * @param inviterName the name of the user sending the invitation
     * @param walletName the name of the shared wallet
     * @param token the invitation token used to build the invitation URL
     * @param expiresAt the invitation expiration timestamp
     */
    public void sendWalletInvitationEmail(
            final String toEmail,
            final String inviterName,
            final String walletName,
            final String token,
            final Instant expiresAt
    ) {

        String invitationUrl =
                frontendUrl.replaceAll("/+$", "")
                        + "/invitations/"
                        + token;

        String formattedExpiresAt =
                DateTimeFormatter.ofPattern(
                                "MMMM d, yyyy 'at' h:mm a z",
                                Locale.ENGLISH
                        )
                        .format(expiresAt.atZone(ZoneOffset.UTC));

        Context context = new Context();

        context.setVariable("inviterName", inviterName);
        context.setVariable("walletName", walletName);
        context.setVariable("invitationUrl", invitationUrl);
        context.setVariable("expiresAt", formattedExpiresAt);
        context.setVariable("year", Year.now().getValue());
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("logoUrl", logoUrl);

        String html = templateEngine.process(
                "wallet-invitation",
                context
        );

        sesEmailService.sendEmail(
                toEmail,
                inviterName + " invited you to collaborate on a wallet",
                html
        );
    }

    /**
     * Sends a notification email when a recurring transaction fails to
     * execute (e.g. insufficient wallet balance).
     *
     * @param toEmail recipient email address
     * @param userName recipient display name
     * @param transactionTitle title of the recurring transaction
     * @param amount the transaction amount
     * @param attempts number of consecutive failed attempts so far
     * @param deactivated whether the recurring transaction was
     *        auto-deactivated after reaching the failure threshold
     * @param reason short description of why the execution failed
     */
    public void sendRecurringFailedEmail(
            final String toEmail,
            final String userName,
            final String transactionTitle,
            final BigDecimal amount,
            final int attempts,
            final boolean deactivated,
            final String reason
    ) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("transactionTitle", transactionTitle);
        context.setVariable("amount", amount);
        context.setVariable("attempts", attempts);
        context.setVariable("deactivated", deactivated);
        context.setVariable("reason", reason);
        context.setVariable("manageUrl",
                frontendUrl.replaceAll("/+$", "") + "/recurring");
        context.setVariable("year", Year.now().getValue());
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("logoUrl", logoUrl);

        String html = templateEngine.process(
                "recurring-transaction-failed", context);

        String subject = deactivated
                ? "Action needed: \"" + transactionTitle + "\" was paused"
                : "We couldn't process \"" + transactionTitle + "\"";

        sesEmailService.sendEmail(toEmail, subject, html);
    }

    /**
     * Sends a celebratory email to one member of a shared wallet when
     * that wallet stays within budget as a team for the period.
     *
     * @param toEmail recipient email address
     * @param userName recipient display name
     * @param walletName the shared wallet's name
     * @param budget the final budget figures for the completed period
     */
    public void sendBudgetGroupSuccessEmail(
            final String toEmail,
            final String userName,
            final String walletName,
            final BudgetResponse budget
    ) {
        Context context = new Context();
        context.setVariable("userName", userName);
        context.setVariable("walletName", walletName);
        context.setVariable("budgetLimit", budget.getBudgetLimit());
        context.setVariable("spent", budget.getSpent());
        context.setVariable("percentage", budget.getPercentage());
        context.setVariable("manageUrl",
                frontendUrl.replaceAll("/+$", "") + "/budgets");
        context.setVariable("year", Year.now().getValue());
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("logoUrl", logoUrl);

        String html = templateEngine.process(
                "budget-group-success", context);

        sesEmailService.sendEmail(
                toEmail,
                walletName + " stayed within budget \uD83C\uDF89",
                html
        );
    }

    /**
     * Sends a contact form message to the configured support email.
     *
     * @param senderName name of the person submitting the form
     * @param senderEmail email address of the person submitting the form
     * @param subject subject provided by the sender
     * @param message message provided by the sender
     */
    public void sendContactMessage(
            final String senderName,
            final String senderEmail,
            final String subject,
            final String message
    ) {
        Context context = new Context();

        context.setVariable("senderName", senderName);
        context.setVariable("senderEmail", senderEmail);
        context.setVariable("subject", subject);
        context.setVariable("message", message);
        context.setVariable("year", Year.now().getValue());
        context.setVariable("supportEmail", supportEmail);
        context.setVariable("logoUrl", logoUrl);

        String html = templateEngine.process(
                "contact-message",
                context
        );

        sesEmailService.sendEmail(
                supportEmail,
                "[BFlow Contact] " + subject,
                html
        );
    }
}
