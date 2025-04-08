package Service;

import Entities.AutoEcole;
import Entities.Vehicule;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VehiculesGeneratePDF {
    private static final Font titleFont = new Font(Font.HELVETICA, 16, Font.BOLD, new Color(64, 64, 64));
    private static final Font headerFont = new Font(Font.HELVETICA, 12, Font.BOLD, new Color(255, 255, 255));
    private static final Font contentFont = new Font(Font.HELVETICA, 10, Font.NORMAL, new Color(0, 0, 0));
    private static final Font subHeaderFont = new Font(Font.HELVETICA, 10, Font.BOLD, new Color(64, 64, 64));
    private static final Font footerFont = new Font(Font.HELVETICA, 8, Font.ITALIC, new Color(128, 128, 128));
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public static void generateVehiculesPDF(List<Vehicule> vehicules, AutoEcole autoEcole, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs(); // Create directories if they don't exist

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            document.open();

            // Add header with auto-école info
            addHeader(document, autoEcole);

            // Add table with vehicles data
            addVehiclesTable(document, vehicules);

            // Add footer with auto-école info and page numbers
            AutoEcoleFooterPageEvent footerEvent = new AutoEcoleFooterPageEvent(autoEcole);
            writer.setPageEvent(footerEvent);

            document.close();
        } catch (Exception e) {
            document.close();
            throw e;
        }
    }

    public static void generateSingleVehiculePDF(Vehicule vehicule, AutoEcole autoEcole, String filePath) throws Exception {
        Document document = new Document(PageSize.A4);
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs(); // Create directories if they don't exist

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));

            // Add footer with auto-école info and page numbers
            AutoEcoleFooterPageEvent footerEvent = new AutoEcoleFooterPageEvent(autoEcole);
            writer.setPageEvent(footerEvent);

            document.open();

            // Add header with auto-école info
            addHeader(document, autoEcole);

            // Add vehicle details
            Paragraph title = new Paragraph("Détails du Véhicule", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Add vehicle details
            PdfPTable table = new PdfPTable(2);
            table.setWidthPercentage(100);
            table.setSpacingBefore(10f);
            table.setSpacingAfter(10f);

            addTableRow(table, "Matricule:", vehicule.getMatricule());
            addTableRow(table, "Date de mise en service:", vehicule.getDatem() != null ? dateFormat.format(vehicule.getDatem()) : "N/A");
            addTableRow(table, "Kilométrage:", String.valueOf(vehicule.getKilometrage()));
            addTableRow(table, "Type:", vehicule.getType() != null ? vehicule.getType().toString() : "N/A");

            document.add(table);

            document.close();
        } catch (Exception e) {
            document.close();
            throw e;
        }
    }

    private static void addHeader(Document document, AutoEcole autoEcole) throws DocumentException {
        // Add auto-école name and email in header
        PdfPTable headerTable = new PdfPTable(2);
        headerTable.setWidthPercentage(100);

        // Auto-école name and email
        Paragraph nameEmail = new Paragraph();
        nameEmail.add(new Chunk(autoEcole.getNom() + "\n", subHeaderFont));
        nameEmail.add(new Chunk("Email: " + autoEcole.getEmail(), contentFont));

        PdfPCell leftCell = new PdfPCell(nameEmail);
        leftCell.setBorder(Rectangle.NO_BORDER);
        leftCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTable.addCell(leftCell);

        // Date
        Paragraph date = new Paragraph("Date: " + dateFormat.format(new Date()), contentFont);
        PdfPCell rightCell = new PdfPCell(date);
        rightCell.setBorder(Rectangle.NO_BORDER);
        rightCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        headerTable.addCell(rightCell);

        document.add(headerTable);

        // Add title
        Paragraph title = new Paragraph("Liste des Véhicules", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingBefore(20);
        title.setSpacingAfter(20);
        document.add(title);
    }

    private static void addVehiclesTable(Document document, List<Vehicule> vehicules) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        float[] columnWidths = {3f, 3f, 2f, 2f};
        table.setWidths(columnWidths);

        // Add table headers
        addTableHeader(table);

        // Add table content
        for (Vehicule vehicule : vehicules) {
            table.addCell(createCell(vehicule.getMatricule(), contentFont, Element.ALIGN_LEFT));
            table.addCell(createCell(vehicule.getDatem() != null ? dateFormat.format(vehicule.getDatem()) : "N/A", contentFont, Element.ALIGN_CENTER));
            table.addCell(createCell(String.valueOf(vehicule.getKilometrage()), contentFont, Element.ALIGN_CENTER));
            table.addCell(createCell(vehicule.getType() != null ? vehicule.getType().toString() : "N/A", contentFont, Element.ALIGN_CENTER));
        }

        document.add(table);
    }

    private static void addTableHeader(PdfPTable table) {
        PdfPCell headerCell = new PdfPCell();
        headerCell.setBackgroundColor(new Color(62, 72, 84));
        headerCell.setPadding(5);

        headerCell.setPhrase(new Phrase("Matricule", headerFont));
        table.addCell(headerCell);

        headerCell.setPhrase(new Phrase("Date mise en service", headerFont));
        table.addCell(headerCell);

        headerCell.setPhrase(new Phrase("Kilométrage", headerFont));
        table.addCell(headerCell);

        headerCell.setPhrase(new Phrase("Type", headerFont));
        table.addCell(headerCell);
    }

    private static PdfPCell createCell(String content, Font font, int alignment) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(alignment);
        cell.setPadding(5);
        return cell;
    }

    private static void addTableRow(PdfPTable table, String label, String value) {
        table.addCell(createCell(label, headerFont, Element.ALIGN_LEFT));
        table.addCell(createCell(value, contentFont, Element.ALIGN_LEFT));
    }

    static class AutoEcoleFooterPageEvent extends PdfPageEventHelper {
        private AutoEcole autoEcole;

        public AutoEcoleFooterPageEvent(AutoEcole autoEcole) {
            this.autoEcole = autoEcole;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // Add footer with auto-école phone and address
            String footerText = "Tél: " + autoEcole.getNumtel() + " | Adresse: " + autoEcole.getAdresse();
            Phrase footer = new Phrase(footerText, footerFont);

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);

            // Add page number
            Phrase pageNumber = new Phrase("Page " + writer.getPageNumber(), footerFont);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    pageNumber,
                    document.right(),
                    document.bottom() - 10, 0);
        }
    }
}