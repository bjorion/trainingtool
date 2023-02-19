package org.jorion.trainingtool.service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jorion.trainingtool.dto.UpdateEventDTO;
import org.jorion.trainingtool.entity.User;
import org.jorion.trainingtool.type.RegistrationEvent;
import org.jorion.trainingtool.type.RegistrationStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.thymeleaf.context.Context;
import org.thymeleaf.spring5.SpringTemplateEngine;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
public class EmailService {

    private static final String LINK_REGISTRATION_ID = "registration?id=";
    private static final String MAIL_BASEDIR = "mails/";
    private static final String MAIL_ASSIGNED = "req-assigned.html";
    private static final String MAIL_SUBMITTED = "req-submitted.html";
    private static final String MAIL_APPROVED_BY_PROVIDER = "req-approved-by-provider.html";
    private static final String MAIL_REFUSED = "req-refused.html";
    private static final String MAIL_SENT_BACK = "req-sent-back.html";

    @Value("${app.mail.sendMail}")
    private boolean sendMail;

    @Value("${app.mail.address.training}")
    private String trainingMailAddress;

    @Value("${app.mail.address.hr}")
    private String mgtMailAddress;

    @Value("${app.mail.address.from}")
    private String fromAddress;

    @Value("${app.server.domain}")
    private String serverDomain;

    @Value("${server.servlet.context-path}")
    private String serverContext;

    @Autowired
    private JavaMailSender javaMailSender;

    @Autowired
    private SpringTemplateEngine templateEngine;

    @Autowired
    private LdapService ldapService;

    /**
     * Method to prepare the email.
     *
     * @throws MessagingException Failure to send the mail
     */
    private static MimeMessageHelper prepareEmail(
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
        return msgHelper;
    }

    /**
     * Method to prepare email with attachment.
     */
    private static MimeMessageHelper prepareEmailWithAttachment(
            MimeMessageHelper msgHelper, String from, String to, String subject, String htmlTemplate,
            String attachmentFileName, String attachmentFilePath)
            throws MessagingException {

        MimeMessageHelper msgHelperWithAttachment = prepareEmail(msgHelper, from, to, subject, htmlTemplate);
        msgHelper.addAttachment(attachmentFileName, new ClassPathResource(attachmentFilePath));
        return msgHelperWithAttachment;
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
     * return the full application path (eg. <i>http://localhost:8080/</i> or <i>https://www.example.org/trainingtool/</i>).
     * This method takes care of the trailing slashes.
     *
     * @param domain  the application domain - trailing slash is optional (ex: https://www.example.org)
     * @param context the application context - should start with a slash (ex: /trainingtool)
     * @return the full application path, ending with a slash
     */
    static String getServerContextPath(String domain, String context) {

        StringBuilder sb = new StringBuilder(domain);
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
        String[] infos = getMailInfo(dto);
        String recipient = infos[0], toAddress = infos[1];

        if (StringUtils.isBlank(toAddress)) {
            log.debug("Mail not sent because the 'to' address is empty. Recipient [{}]", recipient);
            return;
        }

        // input for the template
        String link = getServerContextPath(this.serverDomain, this.serverContext) + LINK_REGISTRATION_ID + dto.getRegId();
        HashMap<String, Object> model = new HashMap<>();
        model.put("recipient", recipient);
        model.put("dto", dto);
        model.put("reqLink", link);

        String mail = this.buildMail(template, model);
        log.info("Send mail from [{}] to [{}], template [{}]", fromAddress, toAddress, template);
        this.doSendEmail(fromAddress, toAddress, "TrainingTool registration request", mail);
        return;
    }

    /**
     * Send email.
     */
    protected void doSendEmail(String from, String to, String subject, String htmlTemplate)
            throws MessagingException {

        MimeMessage msg = javaMailSender.createMimeMessage();
        // true = multipart message
        MimeMessageHelper msgHelper = new MimeMessageHelper(msg, true);
        prepareEmail(msgHelper, from, to, subject, htmlTemplate);
        javaMailSender.send(msg);
    }

    /**
     * Send an email with attachment.
     *
     * @throws MessagingException Failure to send the mail
     */
    protected void doSendEmailWithAttachment(String from, String to, String subject, String htmlTemplate, String attachmentFileName, String attachmentFilePath)
            throws MessagingException {

        MimeMessage msg = javaMailSender.createMimeMessage();
        // true = multipart message
        MimeMessageHelper msgHelper = new MimeMessageHelper(msg, true);
        prepareEmailWithAttachment(msgHelper, from, to, subject, htmlTemplate, attachmentFileName, attachmentFilePath);
        javaMailSender.send(msg);
    }

    /**
     * Get the email html message from a html template.
     */
    protected String buildMail(String filename, Map<String, Object> model) {

        Context ctx = new Context(java.util.Locale.ENGLISH, model);
        return templateEngine.process(filename, ctx);
    }

    // TODO use record
    protected String[] getMailInfo(UpdateEventDTO dto) {

        // find the recipient addresses
        String toAddress = null;
        String recipient = "(recipient)";
        switch (dto.getRegStatus()) {
            case DRAFT:
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
                toAddress = null; // we don't send a mail to the provider
                break;
            case APPROVED_BY_PROVIDER:
                recipient = dto.getMemberFirstname();
                toAddress = dto.getMemberEmail();
                break;
            default:
                if (RegistrationStatus.REFUSED_SET.contains(dto.getRegStatus()) || dto.getRegEvent() == RegistrationEvent.SEND_BACK) {
                    recipient = dto.getMemberFirstname();
                    toAddress = dto.getMemberEmail();
                }
                break;
        }
        return new String[]{recipient, toAddress};
    }

    public void setSendMail(boolean sendMail) {
        this.sendMail = sendMail;
    }

    public void setFromAddress(String fromAddress) {
        this.fromAddress = fromAddress;
    }

    public void setServerDomain(String serverDomain) {
        this.serverDomain = serverDomain;
    }

    public void setServerContext(String serverContext) {
        this.serverContext = serverContext;
    }
}
