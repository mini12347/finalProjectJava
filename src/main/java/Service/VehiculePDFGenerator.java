package Service;

import Entities.TypeP;
import Entities.Vehicule;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class VehiculePDFGenerator {
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 16, Font.BOLD);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD);
    private static final Font NORMAL_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL);

    public static void generatePDF(Vehicule vehicule, String filePath) {
        try {
            Document document = new Document(PageSize.A4);
            PdfWriter.getInstance(document, new FileOutputStream(filePath));
            document.open();

            // Add title
            Paragraph title = new Paragraph("Fiche Technique du Véhicule", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            document.add(title);
            document.add(Chunk.NEWLINE);

            // Add vehicle information table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);

            // Split the matricule as done in the controller
            String numMatricule = "";
            String anneeMatricule = "";
            try {
                numMatricule = vehicule.getMatricule().substring(0, vehicule.getMatricule().indexOf("ت"));
                anneeMatricule = vehicule.getMatricule().substring(vehicule.getMatricule().indexOf("س")+1, vehicule.getMatricule().length());
            } catch (Exception e) {
                numMatricule = vehicule.getMatricule();
            }

            // Add vehicle basic information
            addTableRow(infoTable, "Matricule", numMatricule + " ت " + anneeMatricule);

            SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
            addTableRow(infoTable, "Date de mise en service", dateFormat.format(vehicule.getDatem()));
            addTableRow(infoTable, "Kilométrage actuel", String.valueOf(vehicule.getKilometrage()) + " km");
            addTableRow(infoTable, "Type de véhicule", vehicule.getType() != null ? vehicule.getType().toString() : "N/A");

            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Add maintenance information
            Paragraph maintenanceTitle = new Paragraph("Informations de maintenance", HEADER_FONT);
            document.add(maintenanceTitle);
            document.add(Chunk.NEWLINE);

            PdfPTable maintenanceTable = new PdfPTable(2);
            maintenanceTable.setWidthPercentage(100);

            // Calculate maintenance dates as in the controller
            int currentYear = LocalDate.now().getYear();

            // Date de visite technique
            try {
                int numMatriculeParsed = Integer.parseInt(numMatricule.trim());
                if (numMatriculeParsed % 2 == 0 && vehicule.getType() != TypeP.MOTO) {
                    addTableRow(maintenanceTable, "Date limite vignette", "5 mars " + currentYear);
                } else {
                    addTableRow(maintenanceTable, "Date limite vignette", "5 avril " + currentYear);
                }
            } catch (NumberFormatException e) {
                addTableRow(maintenanceTable, "Date limite vignette", "Erreur immatriculation");
            }

            // Calculate date for technical visit and insurance
            try {
                LocalDate dateMiseEnService = vehicule.getDatem().toInstant()
                        .atZone(java.time.ZoneId.systemDefault())
                        .toLocalDate();

                int ageVehicule = currentYear - dateMiseEnService.getYear();
                LocalDate dateProchaineVisite;

                if (ageVehicule < 4) {
                    dateProchaineVisite = dateMiseEnService.plusYears(4);
                } else if (ageVehicule < 10) {
                    dateProchaineVisite = dateMiseEnService.plusYears((ageVehicule / 2) * 2 + 2);
                } else {
                    dateProchaineVisite = LocalDate.now().plusMonths(6);
                }

                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
                addTableRow(maintenanceTable, "Date prochaine visite technique", dateProchaineVisite.format(formatter));

                LocalDate dateAssurance;
                if(LocalDate.of(LocalDate.now().getYear(), dateMiseEnService.getMonthValue(), dateMiseEnService.getDayOfMonth()).isAfter(LocalDate.now())) {
                    dateAssurance = LocalDate.of(LocalDate.now().getYear(), dateMiseEnService.getMonthValue(), dateMiseEnService.getDayOfMonth());
                } else {
                    dateAssurance = LocalDate.of(LocalDate.now().getYear()+1, dateMiseEnService.getMonthValue(), dateMiseEnService.getDayOfMonth());
                }

                addTableRow(maintenanceTable, "Date renouvellement assurance", dateAssurance.format(formatter));

                int prochaineVidange = 10000 - (vehicule.getKilometrage() % 10000);
                addTableRow(maintenanceTable, "Kilométrage avant prochaine vidange", prochaineVidange + " km");

            } catch (Exception e) {
                addTableRow(maintenanceTable, "Erreur de calcul", e.getMessage());
            }

            document.add(maintenanceTable);
            document.add(Chunk.NEWLINE);

            // Footer with generation date
            Paragraph footer = new Paragraph("Document généré le " +
                    LocalDate.now().format(DateTimeFormatter.ofPattern("dd-MM-yyyy")), NORMAL_FONT);
            footer.setAlignment(Element.ALIGN_RIGHT);
            document.add(footer);

            document.close();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void addTableRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label, HEADER_FONT));
        labelCell.setBorderWidth(0.5f);
        labelCell.setPadding(5);
        labelCell.setBackgroundColor(new Color(211, 211, 211)); // Light gray

        PdfPCell valueCell = new PdfPCell(new Phrase(value, NORMAL_FONT));
        valueCell.setBorderWidth(0.5f);
        valueCell.setPadding(5);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }
}