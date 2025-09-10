package org.jorion.trainingtool.export;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVPrinter;
import org.jorion.trainingtool.registration.RegistrationService;
import org.jorion.trainingtool.report.ReportDTO;
import org.jorion.trainingtool.type.Provider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.io.IOException;
import java.io.StringWriter;

@Slf4j
@Service
@RequiredArgsConstructor
public class CsvService {

    public static final char CSV_DELIM = ';';

    private final RegistrationService registrationService;

    /**
     * Export the registrations to a CSV string.
     *
     * @param dto       a registration filter. Cannot be null.
     * @param delimiter the delimiter character for the CSV
     * @return a string containing the CSV export
     * @throws IOException if an I/O error occurs
     */
    public String exportRegistrationsToCSV(ReportDTO dto, char delimiter)
            throws IOException {

        log.debug("exportCsv {}", dto);
        Assert.notNull(dto, "ReportDTO cannot be null");

        var registrations = registrationService.findAllByExample(dto);
        var stringWriter = new StringWriter();
        var csvFormat = CSVFormat.DEFAULT.builder()
                .setHeader(RegistrationCsvHeaders.class)
                .setDelimiter(delimiter)
                .build();

        try (var csvPrinter = new CSVPrinter(stringWriter, csvFormat)) {
            registrations.forEach(r -> {
                try {
                    String providerTitle = r.getProvider().getTitle();
                    if (r.getProvider() == Provider.OTHER) {
                        providerTitle = r.getProviderOther();
                    }

                    csvPrinter.printRecord(
                            r.getStartDate(),
                            r.getEndDate(),
                            (r.getStartDate() != null) ? r.getStartDate().getMonth().getValue() : null,
                            r.getTotalHour(),
                            r.getTitle(),
                            providerTitle,
                            r.getProvider().getInternalExternal(),
                            r.getPrice(),
                            r.getMember().getPnr(),
                            r.getMember().getManagerName(),
                            r.getMember().getSector(),
                            r.getMember().getFullName(),
                            r.getMember().getFirstName(),
                            r.getMember().getLastName(),
                            (r.getPeriod() != null) ? r.getPeriod().getShortTitle() : null,
                            null,
                            null,
                            r.getSelfStudy(),
                            r.getMandatory(),
                            r.getCbaCompliant()
                    );
                } catch (IOException e) {
                    log.error("Could not export {}", r);
                }
            });
        }

        return stringWriter.toString();
    }

    enum RegistrationCsvHeaders {

        Start_Date, End_Date, Month, Tot_Hours, Training, Providers, IE, Price_EUR, Employee_ID, Supervisor, Sector,
        Name, Firstname, Lastname, Day_Evening, Classroom_office_hours, Classroom_after_office_hours, Self_study,
        Mandatory_training, CBA_Compliant

    }
}
