package org.jorion.trainingtool.services;

import java.io.IOException;
import java.io.StringWriter;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import org.jorion.trainingtool.dto.ReportDTO;
import org.jorion.trainingtool.entities.Registration;
import org.jorion.trainingtool.types.Provider;

/**
 * Export Service.
 */
@Service
public class ExportService
{
    // --- Constants ---
    private static final Logger LOG = LoggerFactory.getLogger(ExportService.class);

    public static final char CSV_DELIM = ';';

    // --- Variables ---
    @Autowired
    private RegistrationService registrationService;

    // -- Enum ---
    enum RegistrationCsvHeaders
    {
        // @formatter:off
        Start_Date, End_Date, Month, Tot_Hours, Training, Providers, IE, Price_EUR, Employee_ID, Supervisor, Sector, 
        Name, Firstname, Lastname, Day_Evening, Classroom_office_hours, Classroom_after_office_hours, Self_study, 
        Mandatory_training, CBA_Compliant;
        // @formatter:on
    }

    // --- Methods ---
    /**
     * Export the registrations to a CSV string.
     * 
     * @param dto a registration filter. Cannot be null.
     * @param delimiter the delimiter character for the CSV
     * @return a string containing the CSV export
     * @throws IOException if an I/O error occurs
     */
    public String exportRegistrationsToCSV(ReportDTO dto, char delimiter)
            throws IOException
    {
        LOG.debug("exportCsv {}", dto);
        Assert.notNull(dto, "ReportDTO cannot be null");
        Page<Registration> registrations = registrationService.findAllByExample(dto);

        StringWriter sw = new StringWriter();
        try (CSVPrinter printer = new CSVPrinter(sw, CSVFormat.DEFAULT.withHeader(RegistrationCsvHeaders.class).withDelimiter(delimiter))) {
            registrations.forEach(r -> {
                try {
                    String providerTitle = r.getProvider().getTitle();
                    if (r.getProvider() == Provider.OTHER) {
                        providerTitle = r.getProviderOther();
                    }

                    // @formatter:off
                    printer.printRecord(
                            r.getStartDate(), 
                            r.getEndDate(), 
                            (r.getStartDate() != null) ? r.getStartDate().getMonth().getValue(): null,
                            r.getTotalHour(), 
                            r.getTitle(), 
                            providerTitle, 
                            r.getProvider().getInternalExternal(),
                            r.getPrice(),
                            r.getMember().getPnr(),
                            r.getMember().getManagername(), 
                            r.getMember().getSector(),
                            r.getMember().getFullname(), 
                            r.getMember().getFirstname(), 
                            r.getMember().getLastname(),
                            (r.getPeriod() != null) ? r.getPeriod().getShortTitle() : null,
                            null,
                            null,
                            r.getSelfStudy(),
                            r.getMandatory(),
                            r.getCbaCompliant() 
                    );
                    // @formatter:on
                }
                catch (IOException e) {
                    LOG.error("Could not export {}", r);
                }
            });
        }

        return sw.toString();
    }
}
