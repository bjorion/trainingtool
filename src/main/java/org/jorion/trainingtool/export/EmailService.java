package org.jorion.trainingtool.export;

import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jorion.trainingtool.ldap.LdapService;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.jorion.trainingtool.user.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring6.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private static final String LINK_REGISTRATION_ID = "registration?id=";
    private static final String MAIL_BASEDIR = "mails/";
    private static final String MAIL_ASSIGNED = "req-assigned.html";
    private static final String MAIL_SUBMITTED = "req-submitted.html";
    private static final String MAIL_APPROVED_BY_PROVIDER = "req-approved-by-provider.html";
    private static final String MAIL_REFUSED = "req-refused.html";
    private static final String MAIL_SENT_BACK = "req-sent-back.html";

    @Setter
    @Value("${app.mail.sendMail}")
    private boolean sendMail;

    @Value("${app.mail.address.training}")
    private String trainingMailAddress;

    @Value("${app.mail.address.hr}")
    private String mgtMailAddress;

    @Setter
    @Value("${app.mail.address.from}")
    private String fromAddress;

    @Setter
    @Value("${app.server.domain}")
    private String serverDomain;

    @Setter
    @Value("${server.servlet.context-path}")
    private String serverContext;

    private final JavaMailSender javaMailSender;
    private final SpringTemplateEngine templateEngine;
    private final LdapService ldapService;

    /**
     * Method to prepare the email.
     *
     * @throws MessagingException Failure to send the mail
     */
    private static void prepareEmail(
            MimeMessageHelper msgHelper, String from, String to,
            String subject, String htmlTemplate)
            throws MessagingException {

        Assert.notNull(from, "From address cannot be null");
        Assert.notNull(to, "To address cannot be null");

        msgHelper.setFrom(from);
        msgHelper.setTo(to.split(","));
        msgHelper.setSubject(subject);

        // true = text/html
        msgHelper.setText(htmlTemplate, true);

        // javaMailSender.send(msg);
    }

    /**
     * Method to prepare email with attachment.
     */
    private static void prepareEmailWithAttachment(
            MimeMessageHelper msgHelper, String from, String to, String subject, String htmlTemplate,
            String attachmentFileName, String attachmentFilePath)
            throws MessagingException {

        prepareEmail(msgHelper, from, to, subject, htmlTemplate);
        msgHelper.addAttachment(attachmentFileName, new ClassPathResource(attachmentFilePath));
    }

    static String getTemplate(RegistrationEvent regEvent, RegistrationStatus regStatus) {

        String template = MAIL_BASEDIR;
        switch (regEvent) {
            case ASSIGN:
                template += MAIL_ASSIGNED;
                break;
            case SUBMIT:
                String mail = MAIL_SUBMITTED;
                if (regStatus == RegistrationStatus.APPROVED_BY_PROVIDER) {
                    mail = MAIL_APPROVED_BY_PROVIDER;
                }
                template += mail;
                break;
            case REFUSE:
                template += MAIL_REFUSED;
                break;
            case SEND_BACK:
                template += MAIL_SENT_BACK;
                break;
            default:
                template = null;
                break;
        }
        return template;
    }

    /**
     * return the full application path (e.g. <i><a href="http://localhost:8080/">...</a></i>
     * or <i>https://www.example.org/trainingtool/</i>).
     * This method takes care of the trailing slashes.
     *
     * @param domain  the application domain - trailing slash is optional (ex: <a href="https://www.example.org">...</a>)
     * @param context the application context - should start with a slash (ex: /trainingtool)
     * @return the full application path, ending with a slash
     */
    static String getServerContextPath(String domain, String context) {

        var sb = new StringBuilder(domain);
        if (sb.charAt(sb.length() - 1) == '/') {
            sb.deleteCharAt(sb.length() - 1);
        }
        sb.append(context);
        if (sb.charAt(sb.length() - 1) != '/') {
            sb.append('/');
        }
        return sb.toString();
    }

    /**
     * Build and send a mail.
     *
     * @param dto information injected into the mail template
     * @throws MessagingException Failure to send the mail
     */
    public void sendEmail(UpdateEventDTO dto)
            throws MessagingException {

        if (!sendMail) {
            log.info("Mailing not enabled");
            return;
        }

        // select the mail template
        String template = getTemplate(dto.getRegEvent(), dto.getRegStatus());
        if (template == null) {
            log.warn("No mail will be sent for event [{}]", dto.getRegEvent());
            return;
        }

        // find the recipient addresses
        var mailInfo = getMailInfo(dto);
        var recipient = mailInfo.recipient();
        var toAddress = mailInfo.toAddress();

        if (StringUtils.isBlank(toAddress)) {
            log.debug("Mail not sent because the 'to' address is empty. Recipient [{}]", recipient);
            return;
        }

        // input for the template
        var link = getServerContextPath(this.serverDomain, this.serverContext) + LINK_REGISTRATION_ID + dto.getRegId();
        var model = new HashMap<String, Object>();
        model.put("recipient", recipient);
        model.put("dto", dto);
        model.put("reqLink", link);

        var mail = this.buildMail(template, model);
        log.info("Send mail from [{}] to [{}], template [{}]", fromAddress, toAddress, template);
        this.doSendEmail(fromAddress, toAddress, mail);
    }

    /**
     * Send email.
     */
    protected void doSendEmail(String from, String to, String htmlTemplate)
            throws MessagingException {

        var msg = javaMailSender.createMimeMessage();
        // true = multipart message
        var msgHelper = new MimeMessageHelper(msg, true);
        prepareEmail(msgHelper, from, to, "TrainingTool registration request", htmlTemplate);
        javaMailSender.send(msg);
    }

    /**
     * Send an email with attachment.
     *
     * @throws MessagingException Failure to send the mail
     */
    @SuppressWarnings("unused")
    protected void doSendEmailWithAttachment(String from, String to, String subject, String htmlTemplate,
                                             String attachmentFileName, String attachmentFilePath)
            throws MessagingException {

        var msg = javaMailSender.createMimeMessage();
        // true = multipart message
        var msgHelper = new MimeMessageHelper(msg, true);
        prepareEmailWithAttachment(msgHelper, from, to, subject, htmlTemplate, attachmentFileName, attachmentFilePath);
        javaMailSender.send(msg);
    }

    /**
     * Get the email html message from a html template.
     */
    protected String buildMail(String filename, Map<String, Object> model) {

        var ctx = new Context(java.util.Locale.ENGLISH, model);
        return templateEngine.process(filename, ctx);
    }

    protected MailInfo getMailInfo(UpdateEventDTO dto) {

        // find the recipient addresses
        String toAddress = null;
        String recipient = "(recipient)";
        switch (dto.getRegStatus()) {
            case DRAFT, APPROVED_BY_PROVIDER:
                recipient = dto.getMemberFirstname();
                toAddress = dto.getMemberEmail();
                break;
            case SUBMITTED_TO_MANAGER:
                User manager = (dto.getManager() != null) ? ldapService.searchByName(dto.getManager()) : null;
                recipient = (manager != null) ? manager.getFullName() : null;
                toAddress = (manager != null) ? manager.getEmail() : null;
                break;
            case SUBMITTED_TO_HR:
                recipient = "Management";
                toAddress = mgtMailAddress;
                break;
            case SUBMITTED_TO_TRAINING:
                recipient = "BeTraining Team";
                toAddress = trainingMailAddress;
                break;
            case SUBMITTED_TO_PROVIDER:
                recipient = "Provider";
                break;
            default:
                if (RegistrationStatus.REFUSED_SET.contains(dto.getRegStatus()) ||
                        dto.getRegEvent() == RegistrationEvent.SEND_BACK) {
                    recipient = dto.getMemberFirstname();
                    toAddress = dto.getMemberEmail();
                }
                break;
        }
        return new MailInfo(recipient, toAddress);
    }

    protected record MailInfo(String recipient, String toAddress) {
    }

}
