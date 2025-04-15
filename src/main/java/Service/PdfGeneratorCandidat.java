package Service;

import Entities.Candidat;
import Entities.AutoEcole;
import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.awt.Color;
import Service.AutoEcoleInfosS;

public class PdfGeneratorCandidat {
    private final AutoEcoleInfosS autoEcoleService = new AutoEcoleInfosS();

    // Modern color scheme - matching the other PDF generators
    private static final Color PRIMARY_COLOR = new Color(41, 128, 185); // Blue
    private static final Color SECONDARY_COLOR = new Color(52, 73, 94); // Dark blue-gray
    private static final Color ACCENT_COLOR = new Color(46, 204, 113); // Green
    private static final Color TEXT_COLOR = new Color(44, 62, 80); // Dark slate
    private static final Color LIGHT_GRAY = new Color(236, 240, 241); // Light gray for alternating rows

    // Improved fonts
    private static final Font TITLE_FONT = new Font(Font.HELVETICA, 20, Font.BOLD, PRIMARY_COLOR);
    private static final Font HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, Color.WHITE);
    private static final Font CONTENT_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXT_COLOR);
    private static final Font SUB_HEADER_FONT = new Font(Font.HELVETICA, 14, Font.BOLD, SECONDARY_COLOR);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, SECONDARY_COLOR);
    private static final Font DETAIL_LABEL_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, PRIMARY_COLOR);
    private static final Font DETAIL_VALUE_FONT = new Font(Font.HELVETICA, 11, Font.NORMAL, TEXT_COLOR);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
    private static final SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");

    public PdfGeneratorCandidat() throws SQLException {
    }

    /**
     * Génère un PDF contenant les données d'un candidat avec l'en-tête et
     * le pied de page contenant les informations de l'auto-école
     *
     * @param candidate Le candidat dont les données seront incluses dans le PDF
     * @param filePath Le chemin où enregistrer le fichier PDF
     * @throws IOException En cas d'erreur lors de la création du fichier
     */
    public void generateCandidatePdf(Candidat candidate, String filePath) throws IOException {
        Document document = new Document(PageSize.A4, 36, 36, 54, 36); // Better margins

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

            // Ajouter l'event pour en-tête et pied de page
            AutoEcoleHeaderFooter event = new AutoEcoleHeaderFooter();
            writer.setPageEvent(event);

            document.open();

            // Ajout du titre avec style amélioré
            Paragraph title = new Paragraph("Fiche Candidat", TITLE_FONT);
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

            // Add candidate ID and date with icons
            PdfPTable candidateInfoTable = new PdfPTable(2);
            candidateInfoTable.setWidthPercentage(100);
            candidateInfoTable.setSpacingBefore(15);
            candidateInfoTable.setSpacingAfter(15);

            // Candidate ID on left
            Paragraph candidateId = new Paragraph();
            candidateId.add(new Chunk("🪪 ", new Font(Font.ZAPFDINGBATS, 12)));
            candidateId.add(new Chunk("CIN: " + candidate.getCIN(),
                    new Font(Font.HELVETICA, 11, Font.BOLD, SECONDARY_COLOR)));

            PdfPCell candidateIdCell = new PdfPCell(candidateId);
            candidateIdCell.setBorder(Rectangle.NO_BORDER);
            candidateIdCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            candidateInfoTable.addCell(candidateIdCell);

            // Date on right
            Paragraph dateInfo = new Paragraph();
            dateInfo.add(new Chunk("📅 ", new Font(Font.ZAPFDINGBATS, 12)));
            String birthDate = candidate.getDateNaissance() != null ?
                    dateFormat.format(candidate.getDateNaissance()) : "Non spécifiée";
            dateInfo.add(new Chunk("Date de naissance: " + birthDate,
                    new Font(Font.HELVETICA, 11, Font.ITALIC, SECONDARY_COLOR)));

            PdfPCell dateCell = new PdfPCell(dateInfo);
            dateCell.setBorder(Rectangle.NO_BORDER);
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            candidateInfoTable.addCell(dateCell);

            document.add(candidateInfoTable);

            // Create a card-like container for candidate details
            PdfPTable detailsCard = new PdfPTable(1);
            detailsCard.setWidthPercentage(100);
            detailsCard.setSpacingBefore(10);

            // Add a stylish header to the card
            PdfPCell cardHeader = new PdfPCell(new Phrase("Informations Personnelles", HEADER_FONT));
            cardHeader.setBackgroundColor(PRIMARY_COLOR);
            cardHeader.setPadding(10);
            cardHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            cardHeader.setBorderColor(PRIMARY_COLOR);
            detailsCard.addCell(cardHeader);

            // Create details table with improved styling
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            float[] columnWidths = {30f, 70f};
            infoTable.setWidths(columnWidths);

            // Add candidate details with alternating row colors
            boolean alternate = true;

            addDetailRow(infoTable, "Nom", candidate.getNom(), alternate);
            alternate = !alternate;

            addDetailRow(infoTable, "Prénom", candidate.getPrenom(), alternate);
            alternate = !alternate;

            addDetailRow(infoTable, "Email", candidate.getMail(), alternate);
            alternate = !alternate;

            addDetailRow(infoTable, "Téléphone", String.valueOf(candidate.getNumTelephone()), alternate);
            alternate = !alternate;

            if (candidate.getAdresse() != null && !candidate.getAdresse().isEmpty()) {
                addDetailRow(infoTable, "Adresse", candidate.getAdresse(), alternate);
                alternate = !alternate;
            }

            // Add the info table to the card
            PdfPCell infoTableCell = new PdfPCell(infoTable);
            infoTableCell.setBorderColor(LIGHT_GRAY);
            infoTableCell.setPadding(0);
            detailsCard.addCell(infoTableCell);

            document.add(detailsCard);

            // Add barcode for candidate identification
            try {
                Barcode128 barcode = new Barcode128();
                barcode.setCode("CAND-" + candidate.getCIN());
                barcode.setCodeType(Barcode128.CODE128);
                Image barcodeImage = barcode.createImageWithBarcode(writer.getDirectContent(), null, null);
                barcodeImage.setAlignment(Element.ALIGN_CENTER);
                barcodeImage.scalePercent(80);
                barcodeImage.setSpacingBefore(30);

                Paragraph barcodeTitle = new Paragraph("Référence Candidat", new Font(Font.HELVETICA, 10, Font.ITALIC, SECONDARY_COLOR));
                barcodeTitle.setAlignment(Element.ALIGN_CENTER);
                barcodeTitle.setSpacingBefore(20);
                document.add(barcodeTitle);
                document.add(barcodeImage);
            } catch (Exception e) {
                // Ignore if barcode generation fails
            }

            // Ajout de la date de génération avec style amélioré
            Paragraph dateGeneration = new Paragraph();
            dateGeneration.add(new Chunk("Document généré le " + dateTimeFormat.format(new Date()),
                    new Font(Font.HELVETICA, 10, Font.ITALIC, SECONDARY_COLOR)));
            dateGeneration.setAlignment(Element.ALIGN_RIGHT);
            dateGeneration.setSpacingBefore(20);
            document.add(dateGeneration);

        } catch (Exception e) {
            throw new IOException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        } finally {
            document.close();
        }
    }

    /**
     * Génère un PDF contenant une liste de candidats avec l'en-tête et
     * le pied de page contenant les informations de l'auto-école
     *
     * @param candidates La liste des candidats
     * @param filePath Le chemin où enregistrer le fichier PDF
     * @throws IOException En cas d'erreur lors de la création du fichier
     */
    public void generateCandidatesList(List<Candidat> candidates, String filePath) throws IOException {
        Document document = new Document(PageSize.A4.rotate(), 36, 36, 54, 36); // Format paysage avec meilleures marges

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

            // Ajouter l'event pour en-tête et pied de page
            AutoEcoleHeaderFooter event = new AutoEcoleHeaderFooter();
            writer.setPageEvent(event);

            document.open();

            // Ajout du titre avec style amélioré
            Paragraph title = new Paragraph("Liste des Candidats", TITLE_FONT);
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

            // Add date with icon
            Paragraph dateInfo = new Paragraph();
            dateInfo.add(new Chunk("📅 ", new Font(Font.ZAPFDINGBATS, 12)));
            dateInfo.add(new Chunk("Date d'impression: " + dateTimeFormat.format(new Date()),
                    new Font(Font.HELVETICA, 10, Font.ITALIC, SECONDARY_COLOR)));
            dateInfo.setAlignment(Element.ALIGN_RIGHT);
            dateInfo.setSpacingBefore(10);
            dateInfo.setSpacingAfter(15);
            document.add(dateInfo);

            // Création du tableau avec style amélioré
            PdfPTable table = new PdfPTable(5); // 5 colonnes
            table.setWidthPercentage(100);

            // Définition des largeurs de colonnes
            float[] columnWidths = {1.5f, 2.5f, 2.5f, 4f, 2.5f};
            table.setWidths(columnWidths);

            // Ajout des en-têtes avec style amélioré
            addTableHeader(table, new String[]{"CIN", "Nom", "Prénom", "Email", "Téléphone"});

            // Ajout des données avec alternating rows
            boolean alternate = false;
            for (Candidat candidate : candidates) {
                Color bgColor = alternate ? LIGHT_GRAY : Color.WHITE;

                PdfPCell cinCell = new PdfPCell(new Phrase(String.valueOf(candidate.getCIN()), CONTENT_FONT));
                cinCell.setBackgroundColor(bgColor);
                cinCell.setPadding(6);
                cinCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(cinCell);

                PdfPCell nomCell = new PdfPCell(new Phrase(candidate.getNom(), CONTENT_FONT));
                nomCell.setBackgroundColor(bgColor);
                nomCell.setPadding(6);
                nomCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(nomCell);

                PdfPCell prenomCell = new PdfPCell(new Phrase(candidate.getPrenom(), CONTENT_FONT));
                prenomCell.setBackgroundColor(bgColor);
                prenomCell.setPadding(6);
                prenomCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(prenomCell);

                PdfPCell emailCell = new PdfPCell(new Phrase(candidate.getMail(), CONTENT_FONT));
                emailCell.setBackgroundColor(bgColor);
                emailCell.setPadding(6);
                emailCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(emailCell);

                PdfPCell telCell = new PdfPCell(new Phrase(String.valueOf(candidate.getNumTelephone()), CONTENT_FONT));
                telCell.setBackgroundColor(bgColor);
                telCell.setPadding(6);
                telCell.setVerticalAlignment(Element.ALIGN_MIDDLE);
                table.addCell(telCell);

                alternate = !alternate;
            }

            document.add(table);

            // Résumé avec style amélioré
            PdfPTable summaryTable = new PdfPTable(2);
            summaryTable.setWidthPercentage(60);
            summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryTable.setSpacingBefore(20);

            PdfPCell summaryLabelCell = new PdfPCell(new Phrase("Nombre total de candidats:", DETAIL_LABEL_FONT));
            summaryLabelCell.setBorder(Rectangle.NO_BORDER);
            summaryLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            summaryLabelCell.setPadding(5);

            PdfPCell summaryValueCell = new PdfPCell(new Phrase(String.valueOf(candidates.size()),
                    new Font(Font.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR)));
            summaryValueCell.setBorder(Rectangle.BOX);
            summaryValueCell.setBorderColor(PRIMARY_COLOR);
            summaryValueCell.setBackgroundColor(LIGHT_GRAY);
            summaryValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
            summaryValueCell.setPadding(5);

            summaryTable.addCell(summaryLabelCell);
            summaryTable.addCell(summaryValueCell);
            document.add(summaryTable);

        } catch (Exception e) {
            throw new IOException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        } finally {
            document.close();
        }
    }

    /**
     * Ajoute les en-têtes au tableau avec style amélioré
     *
     * @param table Le tableau auquel ajouter les en-têtes
     * @param headers Les en-têtes à ajouter
     */
    private void addTableHeader(PdfPTable table, String[] headers) {
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
    }

    /**
     * Ajoute une ligne de détail au tableau avec style amélioré
     *
     * @param table Le tableau auquel ajouter la ligne
     * @param label Le libellé du champ
     * @param value La valeur du champ
     * @param alternate Indique si la ligne doit avoir une couleur alternée
     */
    private void addDetailRow(PdfPTable table, String label, String value, boolean alternate) {
        Color bgColor = alternate ? LIGHT_GRAY : Color.WHITE;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, DETAIL_LABEL_FONT));
        labelCell.setPadding(8);
        labelCell.setBorderColor(LIGHT_GRAY);
        labelCell.setBackgroundColor(bgColor);
        labelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        table.addCell(labelCell);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, DETAIL_VALUE_FONT));
        valueCell.setPadding(8);
        valueCell.setBorderColor(LIGHT_GRAY);
        valueCell.setBackgroundColor(bgColor);
        valueCell.setHorizontalAlignment(Element.ALIGN_LEFT);
        table.addCell(valueCell);
    }

    /**
     * Classe interne pour gérer l'en-tête et le pied de page des documents PDF
     */
    private class AutoEcoleHeaderFooter extends PdfPageEventHelper {
        private AutoEcole autoEcole;

        public AutoEcoleHeaderFooter() {
            try {
                // Récupérer les informations de l'auto-école
                this.autoEcole = autoEcoleService.getAutoEcole();
            } catch (Exception e) {
                System.err.println("Erreur lors de la récupération des informations de l'auto-école: " + e.getMessage());
                // Créer un objet Auto-École par défaut si impossible de récupérer les données
                this.autoEcole = new AutoEcole("Auto-École", 0, "info@auto-ecole.com", "Adresse non disponible", null);
            }
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();

                // Créer un fond gris clair pour l'en-tête
                cb.setColorFill(LIGHT_GRAY);
                cb.rectangle(document.left(), document.top(), document.right() - document.left(), 50);
                cb.fill();

                if (autoEcole != null) {
                    // Informations de l'auto-école avec style amélioré
                    Font schoolNameFont = new Font(Font.HELVETICA, 16, Font.BOLD, PRIMARY_COLOR);
                    Font infoFont = new Font(Font.HELVETICA, 10, Font.NORMAL, SECONDARY_COLOR);

                    // Nom de l'auto-école
                    Phrase schoolName = new Phrase(autoEcole.getNom(), schoolNameFont);
                    ColumnText.showTextAligned(
                            cb,
                            Element.ALIGN_CENTER,
                            schoolName,
                            (document.right() - document.left()) / 2 + document.left(),
                            document.top() + 30,
                            0);

                    // Email
                    Phrase emailPhrase = new Phrase("Email: " + autoEcole.getEmail(), infoFont);
                    ColumnText.showTextAligned(
                            cb,
                            Element.ALIGN_CENTER,
                            emailPhrase,
                            (document.right() - document.left()) / 2 + document.left(),
                            document.top() + 15,
                            0);

                    // Add a line separator
                    cb.setColorStroke(PRIMARY_COLOR);
                    cb.setLineWidth(2f);
                    cb.moveTo(document.left(), document.top() - 10);
                    cb.lineTo(document.right(), document.top() - 10);
                    cb.stroke();
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout de l'en-tête: " + e.getMessage());
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                if (autoEcole != null) {
                    PdfContentByte cb = writer.getDirectContent();

                    // Add a line separator
                    cb.setColorStroke(LIGHT_GRAY);
                    cb.setLineWidth(1f);
                    cb.moveTo(document.left(), document.bottom() + 30);
                    cb.lineTo(document.right(), document.bottom() + 30);
                    cb.stroke();

                    // Add footer text with icons
                    String footerText = "📞 " + autoEcole.getNumtel() + " | 📍 " + autoEcole.getAdresse();
                    Phrase footerPhrase = new Phrase(footerText, FOOTER_FONT);

                    // Position footer text
                    ColumnText.showTextAligned(
                            cb,
                            Element.ALIGN_CENTER,
                            footerPhrase,
                            (document.right() - document.left()) / 2 + document.left(),
                            document.bottom() + 15,
                            0);

                    // Add page number
                    Phrase pageNumber = new Phrase("Page " + writer.getPageNumber(), FOOTER_FONT);
                    ColumnText.showTextAligned(
                            cb,
                            Element.ALIGN_RIGHT,
                            pageNumber,
                            document.right(),
                            document.bottom() + 15,
                            0);

                    // Add generation date
                    Phrase generated = new Phrase("Document généré le " + dateFormat.format(new Date()),
                            new Font(Font.HELVETICA, 6, Font.ITALIC, SECONDARY_COLOR));
                    ColumnText.showTextAligned(
                            cb,
                            Element.ALIGN_LEFT,
                            generated,
                            document.left(),
                            document.bottom() + 15,
                            0);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout du pied de page: " + e.getMessage());
            }
        }
    }
}
