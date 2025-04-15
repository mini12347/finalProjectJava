package Service;

import com.itextpdf.text.*;
import com.itextpdf.text.pdf.*;

import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.Date;

public class FacturePDFGenerator {
    private static AutoEcoleInfosS autoEcoleInfos=new AutoEcoleInfosS();

    // Modern color scheme - matching the VehiculesPDFGenerator
    private static final BaseColor PRIMARY_COLOR = new BaseColor(41, 128, 185); // Blue
    private static final BaseColor SECONDARY_COLOR = new BaseColor(52, 73, 94); // Dark blue-gray
    private static final BaseColor ACCENT_COLOR = new BaseColor(46, 204, 113); // Green
    private static final BaseColor TEXT_COLOR = new BaseColor(44, 62, 80); // Dark slate
    private static final BaseColor LIGHT_GRAY = new BaseColor(236, 240, 241); // Light gray for alternating rows

    public static void generateFacture(String outputPath, String logoPath, String fontPath,
                                       String matricule, String description, String date,
                                       double montant, int kilometrage) throws Exception {

        Document document = new Document(PageSize.A4, 36, 36, 54, 36); // Better margins
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(outputPath));

        // Add page event for footer
        writer.setPageEvent(new FooterPageEvent());

        document.open();

        // Use default fonts instead of trying to load a custom font
        BaseFont baseFont;
        try {
            // Try to use the provided font if available
            if (fontPath != null && !fontPath.trim().isEmpty()) {
                baseFont = BaseFont.createFont(fontPath, BaseFont.IDENTITY_H, BaseFont.EMBEDDED);
            } else {
                // Use a standard font that's guaranteed to be available
                baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
            }
        } catch (Exception e) {
            // Fallback to Helvetica if there's any issue with the font
            baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
        }

        // Improved fonts
        Font titleFont = new Font(baseFont, 20, Font.BOLD, PRIMARY_COLOR);
        Font normalFont = new Font(baseFont, 10, Font.NORMAL, TEXT_COLOR);
        Font boldFont = new Font(baseFont, 11, Font.BOLD, SECONDARY_COLOR);
        Font headerFont = new Font(baseFont, 12, Font.BOLD, BaseColor.WHITE);
        Font detailLabelFont = new Font(baseFont, 11, Font.BOLD, PRIMARY_COLOR);
        Font detailValueFont = new Font(baseFont, 11, Font.NORMAL, TEXT_COLOR);
        Font footerFont = new Font(baseFont, 8, Font.ITALIC, SECONDARY_COLOR);

        // Create header with background
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

        // Add logo if available
        PdfPCell logoCell = new PdfPCell();
        try {
            if (logoPath != null && !logoPath.isEmpty()) {
                Image logo = Image.getInstance(logoPath);
                logo.scaleToFit(120, 120);
                logoCell.addElement(logo);
            }
        } catch (Exception e) {
            // Continue without logo if there's an issue
            System.err.println("Could not add logo: " + e.getMessage());
        }
        logoCell.setBorder(Rectangle.NO_BORDER);
        logoCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        headerTable.addCell(logoCell);

        // Add company info
        Paragraph info = new Paragraph();
        info.add(new Chunk("AUTO ÉCOLE\n", new Font(baseFont, 16, Font.BOLD, PRIMARY_COLOR)));
        info.add(new Chunk("\n", new Font(baseFont, 6)));
        info.add(new Chunk("Email: ", new Font(baseFont, 10, Font.BOLD, SECONDARY_COLOR)));
        info.add(new Chunk(autoEcoleInfos.getAutoEcole().getEmail() +"\n", normalFont));
        info.add(new Chunk("Tél: ", new Font(baseFont, 10, Font.BOLD, SECONDARY_COLOR)));
        info.add(new Chunk(String.valueOf(autoEcoleInfos.getAutoEcole().getNumtel())+"\n", normalFont));

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

        // Add title
        Paragraph title = new Paragraph("FACTURE DE RÉPARATION", titleFont);
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

        // Add date with icon-like prefix
        SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy");
        Paragraph dateP = new Paragraph();
        dateP.add(new Chunk("📅 ", new Font(baseFont, 12)));
        dateP.add(new Chunk("Date: " + formatter.format(new Date()), new Font(baseFont, 10, Font.ITALIC, SECONDARY_COLOR)));
        dateP.setAlignment(Element.ALIGN_RIGHT);
        dateP.setSpacingBefore(10);
        dateP.setSpacingAfter(20);
        document.add(dateP);

        // Create a card-like container for vehicle details
        PdfPTable detailsCard = new PdfPTable(2);
        detailsCard.setWidthPercentage(90);
        detailsCard.setWidths(new float[]{1f, 2f});
        detailsCard.setSpacingBefore(10);

        // Add a stylish header to the card
        PdfPCell cardHeader = new PdfPCell(new Phrase("Informations du Véhicule", new Font(baseFont, 12, Font.BOLD, BaseColor.WHITE)));
        cardHeader.setBackgroundColor(PRIMARY_COLOR);
        cardHeader.setColspan(2);
        cardHeader.setPadding(10);
        cardHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
        cardHeader.setBorderColor(PRIMARY_COLOR);
        detailsCard.addCell(cardHeader);

        // Add details in a more structured format
        addDetailRow(detailsCard, "Immatriculation", matricule, detailLabelFont, detailValueFont, LIGHT_GRAY);
        addDetailRow(detailsCard, "Kilométrage", kilometrage + " km", detailLabelFont, detailValueFont, LIGHT_GRAY);
        addDetailRow(detailsCard, "Date de réparation", date, detailLabelFont, detailValueFont, LIGHT_GRAY);

        document.add(detailsCard);
        document.add(new Paragraph("\n", normalFont));

        // Add repair details section
        Paragraph repairTitle = new Paragraph("Description de la réparation", new Font(baseFont, 12, Font.BOLD, PRIMARY_COLOR));
        repairTitle.setSpacingBefore(10);
        repairTitle.setSpacingAfter(5);
        document.add(repairTitle);

        // Add description in a styled box
        PdfPTable descriptionTable = new PdfPTable(1);
        descriptionTable.setWidthPercentage(90);
        PdfPCell descCell = new PdfPCell(new Phrase(description, normalFont));
        descCell.setPadding(10);
        descCell.setBorderColor(LIGHT_GRAY);
        descCell.setBackgroundColor(new BaseColor(250, 250, 250));
        descriptionTable.addCell(descCell);
        document.add(descriptionTable);
        document.add(new Paragraph("\n", normalFont));

        // Add cost table with improved styling
        Paragraph costTitle = new Paragraph("Détails des coûts", new Font(baseFont, 12, Font.BOLD, PRIMARY_COLOR));
        costTitle.setSpacingBefore(10);
        costTitle.setSpacingAfter(5);
        document.add(costTitle);

        PdfPTable table = new PdfPTable(2);
        table.setWidthPercentage(90);
        table.setWidths(new float[]{3f, 1f});

        PdfPCell cell1 = new PdfPCell(new Phrase("Description", headerFont));
        PdfPCell cell2 = new PdfPCell(new Phrase("Montant", headerFont));

        cell1.setBackgroundColor(PRIMARY_COLOR);
        cell2.setBackgroundColor(PRIMARY_COLOR);
        cell1.setPadding(8);
        cell2.setPadding(8);
        cell1.setHorizontalAlignment(Element.ALIGN_CENTER);
        cell2.setHorizontalAlignment(Element.ALIGN_CENTER);

        table.addCell(cell1);
        table.addCell(cell2);

        PdfPCell descriptionCell = new PdfPCell(new Phrase(description, normalFont));
        PdfPCell montantCell = new PdfPCell(new Phrase(String.format("%.2f DT", montant), normalFont));

        descriptionCell.setPadding(8);
        montantCell.setPadding(8);
        montantCell.setHorizontalAlignment(Element.ALIGN_RIGHT);

        table.addCell(descriptionCell);
        table.addCell(montantCell);

        document.add(table);

        // Add total with better styling
        PdfPTable totalTable = new PdfPTable(2);
        totalTable.setWidthPercentage(90);
        totalTable.setWidths(new float[]{3f, 1f});

        PdfPCell totalLabelCell = new PdfPCell(new Phrase("Total", new Font(baseFont, 12, Font.BOLD, SECONDARY_COLOR)));
        totalLabelCell.setBorder(Rectangle.NO_BORDER);
        totalLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalLabelCell.setPadding(8);

        PdfPCell totalValueCell = new PdfPCell(new Phrase(String.format("%.2f DT", montant), new Font(baseFont, 12, Font.BOLD, PRIMARY_COLOR)));
        totalValueCell.setBorder(Rectangle.BOX);
        totalValueCell.setBorderColor(PRIMARY_COLOR);
        totalValueCell.setBackgroundColor(LIGHT_GRAY);
        totalValueCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        totalValueCell.setPadding(8);

        totalTable.addCell(totalLabelCell);
        totalTable.addCell(totalValueCell);
        totalTable.setSpacingBefore(10);
        document.add(totalTable);

        // Add barcode for invoice tracking
        try {
            Barcode128 barcode = new Barcode128();
            barcode.setCode("INV-" + new SimpleDateFormat("yyyyMMdd").format(new Date()) + "-" + matricule);
            barcode.setCodeType(Barcode128.CODE128);
            Image barcodeImage = barcode.createImageWithBarcode(writer.getDirectContent(), null, null);
            barcodeImage.setAlignment(Element.ALIGN_CENTER);
            barcodeImage.scalePercent(80);
            barcodeImage.setSpacingBefore(30);

            Paragraph barcodeTitle = new Paragraph("Référence Facture", new Font(baseFont, 10, Font.ITALIC, SECONDARY_COLOR));
            barcodeTitle.setAlignment(Element.ALIGN_CENTER);
            barcodeTitle.setSpacingBefore(30);
            document.add(barcodeTitle);
            document.add(barcodeImage);
        } catch (Exception e) {
            // Ignore if barcode generation fails
        }

        // Add thank you message with better styling
        Paragraph footer = new Paragraph("Merci pour votre confiance!", new Font(baseFont, 12, Font.BOLD, ACCENT_COLOR));
        footer.setAlignment(Element.ALIGN_CENTER);
        footer.setSpacingBefore(20);
        document.add(footer);

        document.close();
    }

    private static void addDetailRow(PdfPTable table, String label, String value, Font labelFont, Font valueFont, BaseColor bgColor) {
        PdfPCell labelCell = new PdfPCell(new Phrase(label + ":", labelFont));
        labelCell.setPadding(8);
        labelCell.setBorderColor(bgColor);
        labelCell.setBackgroundColor(bgColor);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setPadding(8);
        valueCell.setBorderColor(bgColor);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(valueCell);
    }

    // Footer page event handler
    static class FooterPageEvent extends PdfPageEventHelper {
        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                BaseFont baseFont = BaseFont.createFont(BaseFont.HELVETICA, BaseFont.CP1252, BaseFont.NOT_EMBEDDED);
                Font footerFont = new Font(baseFont, 8, Font.ITALIC, new BaseColor(52, 73, 94));

                PdfContentByte cb = writer.getDirectContent();

                // Add a separator line above footer
                cb.setColorStroke(new BaseColor(236, 240, 241));
                cb.setLineWidth(1f);
                cb.moveTo(document.leftMargin(), document.bottom() - 20);
                cb.lineTo(document.right(), document.bottom() - 20);
                cb.stroke();

                // Footer text with icons
                String footerText = "📞 +216 71 123 456 | 📍 123 Rue Principale, Tunis";
                Phrase footer = new Phrase(footerText, footerFont);

                ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                        footer,
                        (document.right() - document.left()) / 2 + document.leftMargin(),
                        document.bottom() - 10, 0);

                // Page number with styling
                Phrase pageNumber = new Phrase("Page " + writer.getPageNumber(), footerFont);
                ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                        pageNumber,
                        document.right(),
                        document.bottom() - 10, 0);

                // Add copyright or generated text
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
                Phrase generated = new Phrase("Document généré le " + dateFormat.format(new Date()),
                        new Font(baseFont, 6, Font.ITALIC, new BaseColor(52, 73, 94)));
                ColumnText.showTextAligned(cb, Element.ALIGN_LEFT,
                        generated,
                        document.leftMargin(),
                        document.bottom() - 10, 0);
            } catch (Exception e) {
                // Ignore any errors in footer generation
            }
        }
    }
}
