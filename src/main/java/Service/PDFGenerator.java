package Service;

import Entities.AutoEcole;
import Entities.Hours;
import com.lowagie.text.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.format.TextStyle;
import java.util.Locale;
import java.util.Map;

public class PDFGenerator {

    private static final String LOGO_PATH = "C:\\Users\\minya\\IdeaProjects\\finalProjectJava2\\src\\main\\resources\\images\\111-removebg-preview.png";

    // Style
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185);
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94);
    private static final Color ACCENT_COLOR = new Color(46, 204, 113);
    private static final Color TEXT_COLOR = new Color(44, 62, 80);
    private static final Color LIGHT_GRAY = new Color(236, 240, 241);

    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD, PRIMARY_COLOR);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
    private static final Font SECTION_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, SECONDARY_COLOR);
    private static final Font CONTENT_FONT = new Font(Font.HELVETICA, 11, Font.NORMAL, TEXT_COLOR);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, SECONDARY_COLOR);

    public static void generateAutoEcolePDF(AutoEcole autoEcole, String filePath) throws IOException, DocumentException {
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter.getInstance(document, new FileOutputStream(filePath));
        document.open();

        // === HEADER avec logo à gauche ===
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);
        headerTable.setWidths(new float[]{1f, 3f});

        Image logo = null;
        try {
            logo = Image.getInstance(LOGO_PATH);
            logo.scaleToFit(100, 60);
        } catch (Exception e) {
            System.err.println("Erreur chargement logo : " + e.getMessage());
        }

        PdfPCell logoCell = new PdfPCell();
        if (logo != null) {
            logoCell.addElement(logo);
        }
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTable.addCell(logoCell);

        Paragraph info = new Paragraph();
        info.add(new Chunk(autoEcole.getNom() + "\n", SECTION_FONT));
        info.add(new Chunk("Email : " + autoEcole.getEmail(), CONTENT_FONT));

        PdfPCell infoCell = new PdfPCell(info);
        infoCell.setBorder(Rectangle.NO_BORDER);
        infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        infoCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        headerTable.addCell(infoCell);

        document.add(headerTable);
        document.add(Chunk.NEWLINE);

        // === TITLE ===
        Paragraph title = new Paragraph("Informations de l'Auto-École", TITLE_FONT);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(10);
        document.add(title);

        PdfPTable underline = new PdfPTable(1);
        underline.setWidthPercentage(30);
        PdfPCell lineCell = new PdfPCell();
        lineCell.setBorderWidthBottom(3f);
        lineCell.setBorderColorBottom(ACCENT_COLOR);
        lineCell.setBorder(Rectangle.NO_BORDER);
        lineCell.setPaddingBottom(10);
        underline.addCell(lineCell);
        document.add(underline);

        // === INFOS GÉNÉRALES ===
        Paragraph section1 = new Paragraph("Informations Générales", SECTION_FONT);
        section1.setSpacingBefore(20);
        section1.setSpacingAfter(10);
        document.add(section1);

        document.add(new Paragraph("📛 Nom : " + autoEcole.getNom(), CONTENT_FONT));
        document.add(new Paragraph("📞 Téléphone : " + autoEcole.getNumtel(), CONTENT_FONT));
        document.add(new Paragraph("✉️ Email : " + autoEcole.getEmail(), CONTENT_FONT));
        document.add(new Paragraph("📍 Adresse : " + autoEcole.getAdresse(), CONTENT_FONT));
        document.add(Chunk.NEWLINE);

        // === HORAIRES ===
        Paragraph section2 = new Paragraph("Horaires d'ouverture", SECTION_FONT);
        section2.setSpacingBefore(10);
        section2.setSpacingAfter(10);
        document.add(section2);

        PdfPTable table = new PdfPTable(3);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setWidths(new float[]{3f, 3f, 3f});

        String[] headers = {"Jour", "Heure d'ouverture", "Heure de fermeture"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setPadding(8);
            table.addCell(cell);
        }

        Map<DayOfWeek, Hours> schedule = autoEcole.getHoraire().getDaysOfWeek();
        boolean alternate = true;
        for (DayOfWeek day : DayOfWeek.values()) {
            Hours hours = schedule.get(day);
            String jour = day.getDisplayName(TextStyle.FULL, Locale.FRANCE);
            String ouverture = (hours != null) ? hours.getStarthour() + "h00" : "Fermé";
            String fermeture = (hours != null) ? hours.getEndhour() + "h00" : "Fermé";

            Color bgColor = alternate ? LIGHT_GRAY : Color.WHITE;

            addScheduleCell(table, jour, bgColor);
            addScheduleCell(table, ouverture, bgColor);
            addScheduleCell(table, fermeture, bgColor);

            alternate = !alternate;
        }

        document.add(table);

        // === FOOTER ===
        Paragraph footer = new Paragraph("Document généré le " + java.time.LocalDate.now(), FOOTER_FONT);
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);

        document.close();
        openPDF(filePath);
    }

    private static void addScheduleCell(PdfPTable table, String content, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(content, CONTENT_FONT));
        cell.setBackgroundColor(bgColor);
        cell.setPadding(6);
        cell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(cell);
    }

    private static void openPDF(String filePath) {
        try {
            File pdfFile = new File(filePath);
            if (pdfFile.exists() && Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(pdfFile);
            }
        } catch (IOException e) {
            System.err.println("Erreur ouverture PDF : " + e.getMessage());
        }
    }
}
