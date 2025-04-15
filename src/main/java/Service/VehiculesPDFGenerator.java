package Service;

import Entities.AutoEcole;
import Entities.Vehicule;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import com.lowagie.text.Image;
import java.awt.*;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class VehiculesPDFGenerator {

    // Modern color scheme
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); // Blue
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94); // Dark blue-gray
    private static final Color ACCENT_COLOR = new Color(46, 204, 113); // Green
    private static final Color TEXT_COLOR = new Color(44, 62, 80); // Dark slate
    private static final Color LIGHT_GRAY = new Color(236, 240, 241); // Light gray for alternating rows

    // Improved fonts
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD, PRIMARY_COLOR);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
    private static final Font CONTENT_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXT_COLOR);
    private static final Font SUB_HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, SECONDARY_COLOR);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, SECONDARY_COLOR);
    private static final Font DETAIL_LABEL_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, PRIMARY_COLOR);
    private static final Font DETAIL_VALUE_FONT = new Font(Font.HELVETICA, 11, Font.NORMAL, TEXT_COLOR);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

    public static void generateVehiculesPDF(List<Vehicule> vehicules, AutoEcole autoEcole, String filePath) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 54, 36); // Better margins
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            writer.setPageEvent(new AutoEcoleFooterPageEvent(autoEcole));

            document.open();

            addHeader(document, autoEcole);
            addVehiclesTable(document, vehicules);

            // Add summary information
            addSummarySection(document, vehicules);

        } catch (Exception e) {
            throw e;
        } finally {
            document.close();
        }
    }

    public static void generateSingleVehiculePDF(Vehicule vehicule, AutoEcole autoEcole, String filePath) throws Exception {
        Document document = new Document(PageSize.A4, 36, 36, 54, 36); // Better margins
        try {
            File file = new File(filePath);
            file.getParentFile().mkdirs();

            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(file));
            writer.setPageEvent(new AutoEcoleFooterPageEvent(autoEcole));

            document.open();

            addHeader(document, autoEcole);

            Paragraph title = new Paragraph("Détails du Véhicule", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(20);
            title.setSpacingAfter(20);
            document.add(title);

            // Create a card-like container for vehicle details
            PdfPTable detailsCard = new PdfPTable(2);
            detailsCard.setWidthPercentage(90);
            detailsCard.setWidths(new float[]{1f, 2f});
            detailsCard.setSpacingBefore(10);

            // Add a stylish header to the card
            PdfPCell cardHeader = new PdfPCell(new Phrase("Véhicule: " + vehicule.getMatricule(), SUB_HEADER_FONT));
            cardHeader.setBackgroundColor(PRIMARY_COLOR);
            cardHeader.setColspan(2);
            cardHeader.setPadding(10);
            cardHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            cardHeader.setBorderColor(PRIMARY_COLOR);
            detailsCard.addCell(cardHeader);

            // Add details in a more structured format
            addDetailRow(detailsCard, "Matricule", vehicule.getMatricule() != null ? vehicule.getMatricule() : "N/A");
            addDetailRow(detailsCard, "Date de mise en service", vehicule.getDatem() != null ? dateFormat.format(vehicule.getDatem()) : "N/A");
            addDetailRow(detailsCard, "Kilométrage", String.valueOf(vehicule.getKilometrage()));
            addDetailRow(detailsCard, "Type", vehicule.getType() != null ? vehicule.getType().toString() : "N/A");

            document.add(detailsCard);

            // Add a QR code or barcode for the vehicle
            try {
                Barcode128 barcode = new Barcode128();
                barcode.setCode(vehicule.getMatricule());
                barcode.setCodeType(Barcode128.CODE128);
                Image barcodeImage = barcode.createImageWithBarcode(writer.getDirectContent(), null, null);
                barcodeImage.setAlignment(Element.ALIGN_CENTER);
                barcodeImage.scalePercent(100);
                barcodeImage.setSpacingBefore(30);

                Paragraph barcodeTitle = new Paragraph("Code d'identification", SUB_HEADER_FONT);
                barcodeTitle.setAlignment(Element.ALIGN_CENTER);
                barcodeTitle.setSpacingBefore(30);
                document.add(barcodeTitle);
                document.add(barcodeImage);
            } catch (Exception e) {
                // Ignore if barcode generation fails
            }

        } catch (Exception e) {
            throw e;
        } finally {
            document.close();
        }
    }

    private static void addDetailRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label + ":", DETAIL_LABEL_FONT));
        labelCell.setPadding(8);
        labelCell.setBorderColor(LIGHT_GRAY);
        labelCell.setBackgroundColor(LIGHT_GRAY);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, DETAIL_VALUE_FONT));
        valueCell.setPadding(8);
        valueCell.setBorderColor(LIGHT_GRAY);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(valueCell);
    }

    private static void addHeader(Document document, AutoEcole autoEcole) throws DocumentException {
        try {
            // Create a header with background color
            PdfPTable headerBackground = new PdfPTable(1);
            headerBackground.setWidthPercentage(100);
            PdfPCell bgCell = new PdfPCell();
            bgCell.setBackgroundColor(LIGHT_GRAY);
            bgCell.setPadding(10);
            bgCell.setBorder(Rectangle.NO_BORDER);

            // Create the actual header content
            PdfPTable headerTable = new PdfPTable(2);
            headerTable.setWidthPercentage(100);
            headerTable.setWidths(new float[]{1f, 3f});

            Image logo;
            try {
                logo = Image.getInstance("C:\\Users\\minya\\IdeaProjects\\finalProjectJava2\\src\\main\\resources\\images\\111-removebg-preview.png");
                logo.scaleToFit(120, 120);
            } catch (Exception e) {
                logo = null;
            }

            PdfPCell logoCell = new PdfPCell();
            if (logo != null) {
                logoCell.addElement(logo);
            }
            logoCell.setBorder(Rectangle.NO_BORDER);
            logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            headerTable.addCell(logoCell);

            // Improved school info section
            Paragraph info = new Paragraph();
            info.add(new Chunk(autoEcole.getNom() + "\n", new Font(Font.HELVETICA, 16, Font.BOLD, PRIMARY_COLOR)));
            info.add(new Chunk("\n", new Font(Font.HELVETICA, 6)));
            info.add(new Chunk("Email: ", new Font(Font.HELVETICA, 10, Font.BOLD, SECONDARY_COLOR)));
            info.add(new Chunk(autoEcole.getEmail() + "\n", CONTENT_FONT));
            info.add(new Chunk("Tél: ", new Font(Font.HELVETICA, 10, Font.BOLD, SECONDARY_COLOR)));
            info.add(new Chunk(String.valueOf(autoEcole.getNumtel()), CONTENT_FONT));

            PdfPCell infoCell = new PdfPCell(info);
            infoCell.setBorder(Rectangle.NO_BORDER);
            infoCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            headerTable.addCell(infoCell);

            bgCell.addElement(headerTable);
            headerBackground.addCell(bgCell);
            document.add(headerBackground);

            // Add a separator line
            PdfPTable separator = new PdfPTable(1);
            separator.setWidthPercentage(100);
            PdfPCell separatorCell = new PdfPCell();
            separatorCell.setBorderWidthBottom(2f);
            separatorCell.setBorderColorBottom(PRIMARY_COLOR);
            separatorCell.setBorderWidthTop(0);
            separatorCell.setBorderWidthLeft(0);
            separatorCell.setBorderWidthRight(0);
            separatorCell.setPaddingTop(5);
            separatorCell.setPaddingBottom(5);
            separator.addCell(separatorCell);
            document.add(separator);

            // Add date with icon-like prefix
            Paragraph date = new Paragraph();
            date.add(new Chunk("📅 ", new Font(Font.ZAPFDINGBATS, 12)));
            date.add(new Chunk("Date: " + dateFormat.format(new Date()), new Font(Font.HELVETICA, 10, Font.ITALIC, SECONDARY_COLOR)));
            date.setAlignment(Element.ALIGN_RIGHT);
            date.setSpacingBefore(10);
            document.add(date);

            // Add title with underline
            Paragraph title = new Paragraph("Liste des Véhicules", TITLE_FONT);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingBefore(20);
            title.setSpacingAfter(5);
            document.add(title);

            // Add decorative underline
            PdfPTable titleUnderline = new PdfPTable(1);
            titleUnderline.setWidthPercentage(30);
            PdfPCell underlineCell = new PdfPCell();
            underlineCell.setBorderWidthBottom(3f);
            underlineCell.setBorderColorBottom(ACCENT_COLOR);
            underlineCell.setBorderWidthTop(0);
            underlineCell.setBorderWidthLeft(0);
            underlineCell.setBorderWidthRight(0);
            underlineCell.setPaddingBottom(10);
            titleUnderline.addCell(underlineCell);
            document.add(titleUnderline);

        } catch (Exception e) {
            throw new DocumentException("Erreur lors de l'ajout du header : " + e.getMessage());
        }
    }

    private static void addVehiclesTable(Document document, List<Vehicule> vehicules) throws DocumentException {
        PdfPTable table = new PdfPTable(4);
        table.setWidthPercentage(100);
        float[] columnWidths = {3f, 3f, 2f, 2f};
        table.setWidths(columnWidths);
        table.setSpacingBefore(20);

        addTableHeader(table);

        boolean alternate = false;
        for (Vehicule vehicule : vehicules) {
            // Alternate row colors for better readability
            Color bgColor = alternate ? LIGHT_GRAY : Color.WHITE;

            table.addCell(createCell(vehicule.getMatricule(), CONTENT_FONT, Element.ALIGN_LEFT, bgColor));
            table.addCell(createCell(vehicule.getDatem() != null ? dateFormat.format(vehicule.getDatem()) : "N/A",
                    CONTENT_FONT, Element.ALIGN_CENTER, bgColor));
            table.addCell(createCell(String.valueOf(vehicule.getKilometrage()),
                    CONTENT_FONT, Element.ALIGN_CENTER, bgColor));
            table.addCell(createCell(vehicule.getType() != null ? vehicule.getType().toString() : "N/A",
                    CONTENT_FONT, Element.ALIGN_CENTER, bgColor));

            alternate = !alternate;
        }

        document.add(table);
    }

    private static void addTableHeader(PdfPTable table) {
        String[] headers = {"Matricule", "Date mise en service", "Kilométrage", "Type"};

        for (String h : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(h, HEADER_FONT));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
    }

    private static PdfPCell createCell(String content, Font font, int alignment, Color bgColor) {
        PdfPCell cell = new PdfPCell(new Phrase(content, font));
        cell.setHorizontalAlignment(alignment);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        cell.setPadding(6);
        cell.setBackgroundColor(bgColor);
        return cell;
    }

    private static void addSummarySection(Document document, List<Vehicule> vehicules) throws DocumentException {
        if (vehicules.isEmpty()) return;

        document.add(Chunk.NEWLINE);

        Paragraph summaryTitle = new Paragraph("Résumé", SUB_HEADER_FONT);
        summaryTitle.setAlignment(Element.ALIGN_LEFT);
        summaryTitle.setSpacingBefore(20);
        summaryTitle.setSpacingAfter(10);
        document.add(summaryTitle);

        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setHorizontalAlignment(Element.ALIGN_LEFT);
        summaryTable.setWidths(new float[]{1.5f, 1f});

        // Count vehicles by type
        java.util.Map<String, Integer> typeCount = new java.util.HashMap<>();
        for (Vehicule v : vehicules) {
            String type = v.getType() != null ? v.getType().toString() : "Non défini";
            typeCount.put(type, typeCount.getOrDefault(type, 0) + 1);
        }

        // Add summary data
        addSummaryRow(summaryTable, "Nombre total de véhicules", String.valueOf(vehicules.size()));

        // Add type breakdown
        for (java.util.Map.Entry<String, Integer> entry : typeCount.entrySet()) {
            addSummaryRow(summaryTable, "Véhicules de type " + entry.getKey(), String.valueOf(entry.getValue()));
        }

        document.add(summaryTable);
    }

    private static void addSummaryRow(PdfPTable table, String label, String value) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label + ":", DETAIL_LABEL_FONT));
        labelCell.setPadding(5);
        labelCell.setBorder(Rectangle.NO_BORDER);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, DETAIL_VALUE_FONT));
        valueCell.setPadding(5);
        valueCell.setBorder(Rectangle.NO_BORDER);
        valueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        table.addCell(valueCell);
    }

    static class AutoEcoleFooterPageEvent extends PdfPageEventHelper {
        private final AutoEcole autoEcole;

        public AutoEcoleFooterPageEvent(AutoEcole autoEcole) {
            this.autoEcole = autoEcole;
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            PdfContentByte cb = writer.getDirectContent();

            // Add a separator line above footer
            PdfContentByte line = writer.getDirectContent();
            line.setColorStroke(LIGHT_GRAY);
            line.setLineWidth(1f);
            line.moveTo(document.leftMargin(), document.bottom() - 20);
            line.lineTo(document.right(), document.bottom() - 20);
            line.stroke();

            // Footer text with icons
            String footerText = "📞 " + autoEcole.getNumtel() + " | 📍 " + autoEcole.getAdresse();
            Phrase footer = new Phrase(footerText, FOOTER_FONT);

            ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                    footer,
                    (document.right() - document.left()) / 2 + document.leftMargin(),
                    document.bottom() - 10, 0);

            // Page number with styling
            Phrase pageNumber = new Phrase("Page " + writer.getPageNumber(), FOOTER_FONT);
            ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                    pageNumber,
                    document.right(),
                    document.bottom() - 10, 0);

            // Add copyright or generated text
            Phrase generated = new Phrase("Document généré le " + dateFormat.format(new Date()),
                    new Font(Font.HELVETICA, 6, Font.ITALIC, SECONDARY_COLOR));
            ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                    generated,
                    document.leftMargin(),
                    document.bottom() - 10, 0);
        }
    }
}
