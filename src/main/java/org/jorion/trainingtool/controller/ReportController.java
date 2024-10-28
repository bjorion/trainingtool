package org.jorion.trainingtool.controller;

import lombok.extern.slf4j.Slf4j;
import org.jorion.trainingtool.dto.ReportDTO;
import org.jorion.trainingtool.entity.Registration;
import org.jorion.trainingtool.entity.User;
import org.jorion.trainingtool.service.ExportService;
import org.jorion.trainingtool.service.RegistrationService;
import org.jorion.trainingtool.util.StrUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

@Slf4j
@Controller
public class ReportController extends AbstractController {

    private static final String SESSION_REPORT = "report";

    @Autowired
    private RegistrationService registrationService;

    @Autowired
    private ExportService exportService;

    /**
     * Display all training requests (paginated).
     * <ul>
     * <li>MEMBER do not have access to this page
     * <li>MANAGER will only see registrations of the MEMBERs for which they are the manager
     * <li>HR and TRAINING will see all registrations
     * </ul>
     *
     * @param model   the attribute holder for the page
     * @param request the HTTP request
     * @return "report"
     */
    @GetMapping("/report")
    public String showReport(Model model, HttpServletRequest request) {

        int page = StrUtils.getParameterAsInt(request, Constants.PAGE, 1) - 1;
        int size = StrUtils.getParameterAsInt(request, Constants.SIZE, Constants.SIZE_DEF);

        // User is a manager (or above)
        User user = findUser(request.getSession());
        PageRequest pr = PageRequest.of(page, size, Sort.by(Direction.DESC, "id"));
        Page<Registration> registrations = registrationService.findAllByManager(user.getUserName(), pr);

        request.getSession().removeAttribute(SESSION_REPORT);
        model.addAttribute(Constants.MD_REPORT, new ReportDTO());
        model.addAttribute(Constants.MD_REGISTRATIONS, registrations);
        model.addAttribute(Constants.MD_USER, user);
        return "report";
    }

    /**
     * Display training requests matching the selection criteria.
     *
     * @param report  the criteria to select the registrations
     * @param model   the attribute holder for the page
     * @param request the HTTP request
     * @return "report"
     */
    @GetMapping("/select-report")
    public String selectReport(ReportDTO report, Model model, HttpServletRequest request) {

        if (report.isEmpty()) {
            return showReport(model, request);
        }

        User user = findUser(request.getSession());
        // HR and training can see all registrations
        // Manager are restricted to the registrations of the member for which they are responsible
        report.setManagerName(user.isOffice() ? null : user.getUserName());
        report.setUserName(user.getUserName());
        Page<Registration> registrations = registrationService.findAllByExample(report);

        // Save the report into the session to be able to generate an export
        request.getSession().setAttribute(SESSION_REPORT, report);
        log.debug("selectReport {}", report);

        model.addAttribute(Constants.MD_REPORT, report);
        model.addAttribute(Constants.MD_REGISTRATIONS, registrations);
        model.addAttribute(Constants.MD_USER, user);
        return "report";
    }

    /**
     * Export a CSV file. Delimiter is the semi-column (standard in Europe), and encoding is ISO_8859 instead of UTF-8
     * to be sure XL is able to understand characters with accent. Possible improvement would be to let the user select
     * the delimiter character or the encoding.
     *
     * @param model   the attribute holder for the page
     * @param session the HTTP session
     * @return the CSV file
     */
    @GetMapping("/export-report")
    public ResponseEntity<Resource> exportReport(Model model, HttpSession session)
            throws IOException {

        // retrieve the report criteria from the session
        ReportDTO report = (ReportDTO) session.getAttribute(SESSION_REPORT);
        if (report == null) {
            report = new ReportDTO();
            User user = findUser(session);
            report.setManagerName(user.isOffice() ? null : user.getUserName());
            report.setUserName(user.getUserName());
        }
        String csv = exportService.exportRegistrationsToCSV(report, ExportService.CSV_DELIM);
        InputStreamResource resource = new InputStreamResource(new ByteArrayInputStream(csv.getBytes(StandardCharsets.ISO_8859_1)));

        return ResponseEntity
                .ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment;filename=registrations.csv")
                .contentType(MediaType.TEXT_PLAIN)
                .contentLength(csv.length())
                .body(resource);
    }

    /**
     * For experiment.
     */
    @GetMapping("/status")
    public String showStatus(Model model) {
        return "status";
    }
}
