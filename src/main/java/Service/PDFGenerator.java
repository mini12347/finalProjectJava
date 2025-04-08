package Service;

import Entities.AutoEcole;
import Entities.Hours;
import com.lowagie.text.*;
import com.lowagie.text.pdf.PdfPCell;
import com.lowagie.text.pdf.PdfPTable;
import com.lowagie.text.pdf.PdfWriter;

import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

public class PDFGenerator {

    public static void generateAutoEcolePDF(AutoEcole autoEcole, String filePath) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // Titre
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18, Font.NORMAL);
        Paragraph title = new Paragraph("Informations Auto-École", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Informations de base
        Font sectionFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14, Font.NORMAL);
        Font contentFont = FontFactory.getFont(FontFactory.HELVETICA, 12, Font.NORMAL);

        // Section Informations Générales
        Paragraph infoSection = new Paragraph("Informations Générales", sectionFont);
        infoSection.setSpacingAfter(10);
        document.add(infoSection);

        // Ajouter les informations générales
        document.add(new Paragraph("Nom: " + autoEcole.getNom(), contentFont));
        document.add(new Paragraph("Téléphone: " + autoEcole.getNumtel(), contentFont));
        document.add(new Paragraph("Email: " + autoEcole.getEmail(), contentFont));
        document.add(new Paragraph("Adresse: " + autoEcole.getAdresse(), contentFont));
        document.add(Chunk.NEWLINE);

        // Section Horaires
        Paragraph scheduleSection = new Paragraph("Horaires d'ouverture", sectionFont);
        scheduleSection.setSpacingAfter(10);
        document.add(scheduleSection);

        // Tableau pour les horaires
        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // En-têtes du tableau
        PdfPCell cell1 = new PdfPCell(new Paragraph("Jour", contentFont));
        PdfPCell cell2 = new PdfPCell(new Paragraph("Heure d'ouverture", contentFont));
        PdfPCell cell3 = new PdfPCell(new Paragraph("Heure de fermeture", contentFont));

        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell3.setHorizontalAlignment(Element.ALIGN_CENTER);

        table.addCell(cell1);
        table.addCell(cell2);
        table.addCell(cell3);

        // Ajouter les horaires
        Map<DayOfWeek, Hours> schedule = autoEcole.getHoraire().getDaysOfWeek();
        for (DayOfWeek day : DayOfWeek.values()) {
            String dayName = day.getDisplayName(TextStyle.FULL, Locale.FRANCE);
            if (schedule.containsKey(day)) {
                Hours hours = schedule.get(day);
                table.addCell(new PdfPCell(new Paragraph(dayName, contentFont)));
                table.addCell(new PdfPCell(new Paragraph(hours.getStarthour() + "h00", contentFont)));
                table.addCell(new PdfPCell(new Paragraph(hours.getEndhour() + "h00", contentFont)));
            } else {
                table.addCell(new PdfPCell(new Paragraph(dayName, contentFont)));
                table.addCell(new PdfPCell(new Paragraph("Fermé", contentFont)));
                table.addCell(new PdfPCell(new Paragraph("Fermé", contentFont)));
            }
        }

        document.add(table);

        // Pied de page
        Paragraph footer = new Paragraph("Document généré le " + java.time.LocalDate.now(), contentFont);
        footer.setAlignment(Element.ALIGN_CENTER);
        document.add(footer);

        document.close();
    }
}