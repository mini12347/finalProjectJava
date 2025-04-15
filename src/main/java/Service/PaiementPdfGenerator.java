package Service;

import Entities.AutoEcole;
import Entities.Paiement;
import Entities.ParFacilite;
import DAO.AutoEcoleDAO;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;

import java.awt.Color;
import java.awt.Desktop;
import java.io.File;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class PaiementPdfGenerator {

    private AutoEcoleDAO autoEcoleDAO;
    private static final String LOGO_PATH = "C:\\Users\\souma\\finalProjectJava\\src\\main\\resources\\images\\111-removebg-preview.png";

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
    private static final Font NOTE_FONT = new Font(Font.HELVETICA, 10, Font.NORMAL, TEXT_COLOR);
    private static final Font NOTE_TITLE_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, SECONDARY_COLOR);

    public PaiementPdfGenerator() throws SQLException {
        this.autoEcoleDAO = new AutoEcoleDAO();
    }

    public String generatePaiementRecu(Paiement paiement) {
        String destination = "paiement_" + paiement.getIdPaiement() + "_" + paiement.getIdClient() + ".pdf";
        try {
            // Initialize document with better margins
            Document document = new Document(PageSize.A4, 36, 36, 54, 36);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destination));

            // Add event handlers for header and footer
            HeaderFooter event = new HeaderFooter(autoEcoleDAO, LOGO_PATH);
            writer.setPageEvent(event);

            document.open();

            // Add title with improved styling
            Paragraph title = new Paragraph("Reçu de Paiement", TITLE_FONT);
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

            // Add receipt number and date with icon
            PdfPTable receiptInfoTable = new PdfPTable(2);
            receiptInfoTable.setWidthPercentage(100);
            receiptInfoTable.setSpacingBefore(15);
            receiptInfoTable.setSpacingAfter(15);

            // Receipt number on left
            Paragraph receiptNumber = new Paragraph();
            receiptNumber.add(new Chunk("🧾 ", new Font(Font.ZAPFDINGBATS, 12)));
            receiptNumber.add(new Chunk("Reçu N°: " + paiement.getIdPaiement(),
                    new Font(Font.HELVETICA, 11, Font.BOLD, SECONDARY_COLOR)));

            PdfPCell receiptNumberCell = new PdfPCell(receiptNumber);
            receiptNumberCell.setBorder(Rectangle.NO_BORDER);
            receiptNumberCell.setHorizontalAlignment(Element.ALIGN_LEFT);
            receiptInfoTable.addCell(receiptNumberCell);

            // Date on right
            Paragraph dateInfo = new Paragraph();
            dateInfo.add(new Chunk("📅 ", new Font(Font.ZAPFDINGBATS, 12)));
            dateInfo.add(new Chunk("Date: " + paiement.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")),
                    new Font(Font.HELVETICA, 11, Font.ITALIC, SECONDARY_COLOR)));

            PdfPCell dateCell = new PdfPCell(dateInfo);
            dateCell.setBorder(Rectangle.NO_BORDER);
            dateCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
            receiptInfoTable.addCell(dateCell);

            document.add(receiptInfoTable);

            // Create a card-like container for payment details
            PdfPTable detailsCard = new PdfPTable(1);
            detailsCard.setWidthPercentage(100);
            detailsCard.setSpacingBefore(10);

            // Add a stylish header to the card
            PdfPCell cardHeader = new PdfPCell(new Phrase("Informations du Paiement", HEADER_FONT));
            cardHeader.setBackgroundColor(PRIMARY_COLOR);
            cardHeader.setPadding(10);
            cardHeader.setHorizontalAlignment(Element.ALIGN_CENTER);
            cardHeader.setBorderColor(PRIMARY_COLOR);
            detailsCard.addCell(cardHeader);

            // Create payment info table with improved styling
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            float[] columnWidths = {30f, 70f};
            infoTable.setWidths(columnWidths);

            // Add payment details with alternating row colors
            boolean alternate = true;
            addTableRow(infoTable, "CIN Client", String.valueOf(paiement.getIdClient()), alternate);
            alternate = !alternate;

            addTableRow(infoTable, "Date", paiement.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")), alternate);
            alternate = !alternate;

            addTableRow(infoTable, "Heure", paiement.getTime().toString(), alternate);
            alternate = !alternate;

            addTableRow(infoTable, "Description", paiement.getDescription(), alternate);
            alternate = !alternate;

            // Highlight the amount with special styling
            PdfPCell amountLabelCell = new PdfPCell(new Phrase("Montant Total", DETAIL_LABEL_FONT));
            amountLabelCell.setPadding(8);
            amountLabelCell.setBackgroundColor(alternate ? LIGHT_GRAY : Color.WHITE);

            PdfPCell amountValueCell = new PdfPCell(new Phrase(String.format("%.2f TND", paiement.getMontant()),
                    new Font(Font.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR)));
            amountValueCell.setPadding(8);
            amountValueCell.setBackgroundColor(alternate ? LIGHT_GRAY : Color.WHITE);

            infoTable.addCell(amountLabelCell);
            infoTable.addCell(amountValueCell);
            alternate = !alternate;

            // Add payment status with color-coded styling
            PdfPCell statusLabelCell = new PdfPCell(new Phrase("État", DETAIL_LABEL_FONT));
            statusLabelCell.setPadding(8);
            statusLabelCell.setBackgroundColor(alternate ? LIGHT_GRAY : Color.WHITE);

            Color statusColor = paiement.getEtat().equalsIgnoreCase("Payé") ? ACCENT_COLOR : Color.RED;
            PdfPCell statusValueCell = new PdfPCell(new Phrase(paiement.getEtat(),
                    new Font(Font.HELVETICA, 11, Font.BOLD, statusColor)));
            statusValueCell.setPadding(8);
            statusValueCell.setBackgroundColor(alternate ? LIGHT_GRAY : Color.WHITE);

            infoTable.addCell(statusLabelCell);
            infoTable.addCell(statusValueCell);

            // Add the info table to the card
            PdfPCell infoTableCell = new PdfPCell(infoTable);
            infoTableCell.setBorderColor(LIGHT_GRAY);
            infoTableCell.setPadding(0);
            detailsCard.addCell(infoTableCell);

            document.add(detailsCard);
            document.add(Chunk.NEWLINE);

            // Add payment details if it's a "Par Facilité" payment with improved styling
            if (paiement.getParFacilite() != null) {
                ParFacilite parFacilite = paiement.getParFacilite();

                Paragraph faciliteTitle = new Paragraph("Détails de Paiement Par Facilité", SUB_HEADER_FONT);
                faciliteTitle.setSpacingBefore(15);
                faciliteTitle.setSpacingAfter(10);
                document.add(faciliteTitle);

                // Create a styled table for installment details
                PdfPTable faciliteTable = new PdfPTable(2);
                faciliteTable.setWidthPercentage(90);
                faciliteTable.setWidths(columnWidths);
                faciliteTable.setHorizontalAlignment(Element.ALIGN_CENTER);

                // Add down payment with special styling
                PdfPCell downPaymentLabelCell = new PdfPCell(new Phrase("Acompte", DETAIL_LABEL_FONT));
                downPaymentLabelCell.setPadding(8);
                downPaymentLabelCell.setBackgroundColor(LIGHT_GRAY);

                PdfPCell downPaymentValueCell = new PdfPCell(new Phrase(String.format("%.2f TND", parFacilite.getAccompte()),
                        new Font(Font.HELVETICA, 11, Font.BOLD, PRIMARY_COLOR)));
                downPaymentValueCell.setPadding(8);
                downPaymentValueCell.setBackgroundColor(LIGHT_GRAY);

                faciliteTable.addCell(downPaymentLabelCell);
                faciliteTable.addCell(downPaymentValueCell);

                // Add installments if any with improved formatting
                List<Double> montants = parFacilite.getMontans();
                if (!montants.isEmpty()) {
                    PdfPCell installmentsLabelCell = new PdfPCell(new Phrase("Tranches Restantes", DETAIL_LABEL_FONT));
                    installmentsLabelCell.setPadding(8);
                    installmentsLabelCell.setBackgroundColor(Color.WHITE);

                    StringBuilder tranches = new StringBuilder();
                    for (int i = 0; i < montants.size(); i++) {
                        tranches.append("• Tranche ").append(i + 1).append(": ")
                                .append(String.format("%.2f", montants.get(i))).append(" TND");
                        if (i < montants.size() - 1) {
                            tranches.append("\n");
                        }
                    }

                    PdfPCell installmentsValueCell = new PdfPCell(new Phrase(tranches.toString(), DETAIL_VALUE_FONT));
                    installmentsValueCell.setPadding(8);
                    installmentsValueCell.setBackgroundColor(Color.WHITE);

                    faciliteTable.addCell(installmentsLabelCell);
                    faciliteTable.addCell(installmentsValueCell);
                } else {
                    PdfPCell installmentsLabelCell = new PdfPCell(new Phrase("Tranches Restantes", DETAIL_LABEL_FONT));
                    installmentsLabelCell.setPadding(8);
                    installmentsLabelCell.setBackgroundColor(Color.WHITE);

                    PdfPCell installmentsValueCell = new PdfPCell(new Phrase("Toutes les tranches ont été payées",
                            new Font(Font.HELVETICA, 11, Font.ITALIC, ACCENT_COLOR)));
                    installmentsValueCell.setPadding(8);
                    installmentsValueCell.setBackgroundColor(Color.WHITE);

                    faciliteTable.addCell(installmentsLabelCell);
                    faciliteTable.addCell(installmentsValueCell);
                }

                document.add(faciliteTable);
            }

            // Add payment conditions and notes with improved styling
            Paragraph conditions = new Paragraph("Conditions de paiement:", NOTE_TITLE_FONT);
            conditions.setSpacingBefore(20);
            conditions.setSpacingAfter(10);
            document.add(conditions);

            // Create a styled box for notes
            PdfPTable notesTable = new PdfPTable(1);
            notesTable.setWidthPercentage(90);

            PdfPCell notesCell = new PdfPCell();
            notesCell.setPadding(10);
            notesCell.setBorderColor(LIGHT_GRAY);
            notesCell.setBackgroundColor(new Color(250, 250, 250));

            Paragraph notesList = new Paragraph();
            notesList.add(new Chunk("• Ce reçu est la preuve de votre paiement à l'auto-école.\n", NOTE_FONT));
            notesList.add(new Chunk("• En cas de paiement par facilité, tous les versements doivent être effectués selon le calendrier convenu.\n", NOTE_FONT));
            notesList.add(new Chunk("• Aucun remboursement ne sera accordé après le début des cours.", NOTE_FONT));

            notesCell.addElement(notesList);
            notesTable.addCell(notesCell);
            document.add(notesTable);

            // Add barcode for payment tracking
            try {
                Barcode128 barcode = new Barcode128();
                barcode.setCode("PAY-" + paiement.getIdPaiement() + "-" + paiement.getIdClient());
                barcode.setCodeType(Barcode128.CODE128);
                Image barcodeImage = barcode.createImageWithBarcode(writer.getDirectContent(), null, null);
                barcodeImage.setAlignment(Element.ALIGN_CENTER);
                barcodeImage.scalePercent(80);
                barcodeImage.setSpacingBefore(30);

                Paragraph barcodeTitle = new Paragraph("Référence Paiement", new Font(Font.HELVETICA, 10, Font.ITALIC, SECONDARY_COLOR));
                barcodeTitle.setAlignment(Element.ALIGN_CENTER);
                barcodeTitle.setSpacingBefore(20);
                document.add(barcodeTitle);
                document.add(barcodeImage);
            } catch (Exception e) {
                // Ignore if barcode generation fails
            }

            // Add thank you message with better styling
            Paragraph thankYou = new Paragraph("Merci pour votre confiance!", new Font(Font.HELVETICA, 12, Font.BOLD, ACCENT_COLOR));
            thankYou.setAlignment(Element.ALIGN_CENTER);
            thankYou.setSpacingBefore(20);
            document.add(thankYou);

            document.close();

            // Ouvrir le PDF après sa génération
            openPDF(destination);

            return destination;

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    /**
     * Ouvre le fichier PDF généré avec l'application par défaut du système
     * @param filePath Chemin vers le fichier PDF
     */
    private void openPDF(String filePath) {
        try {
            if (Desktop.isDesktopSupported()) {
                File pdfFile = new File(filePath);
                if (pdfFile.exists()) {
                    Desktop.getDesktop().open(pdfFile);
                }
            } else {
                System.out.println("Le bureau n'est pas supporté sur cette plateforme.");
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Impossible d'ouvrir le fichier PDF: " + e.getMessage());
        }
    }

    private void addTableRow(PdfPTable table, String label, String value, boolean alternate) {
        Color bgColor = alternate ? LIGHT_GRAY : Color.WHITE;

        PdfPCell labelCell = new PdfPCell(new Phrase(label, DETAIL_LABEL_FONT));
        labelCell.setBorderColor(LIGHT_GRAY);
        labelCell.setBackgroundColor(bgColor);
        labelCell.setPadding(8);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, DETAIL_VALUE_FONT));
        valueCell.setBorderColor(LIGHT_GRAY);
        valueCell.setBackgroundColor(bgColor);
        valueCell.setPadding(8);

        table.addCell(labelCell);
        table.addCell(valueCell);
    }

    // Inner class to handle header and footer
    class HeaderFooter extends PdfPageEventHelper {
        private AutoEcoleDAO autoEcoleDAO;
        private String logoPath;
        private Image logo;

        public HeaderFooter(AutoEcoleDAO autoEcoleDAO, String logoPath) {
            this.autoEcoleDAO = autoEcoleDAO;
            this.logoPath = logoPath;
            try {
                this.logo = Image.getInstance(logoPath);
                // Redimensionner le logo si nécessaire
                this.logo.scaleToFit(120, 80); // Ajustez ces valeurs selon la taille souhaitée
            } catch (Exception e) {
                e.printStackTrace();
                this.logo = null;
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

                // Ajouter le logo
                if (logo != null) {
                    float logoX = document.left() + 10;
                    float logoY = document.top() + 5;
                    logo.setAbsolutePosition(logoX, logoY);
                    cb.addImage(logo);
                }

                AutoEcole autoEcole = autoEcoleDAO.getLastModifiedAutoEcole();
                if (autoEcole != null) {
                    // Informations de l'auto-école avec style amélioré
                    Font schoolNameFont = new Font(Font.HELVETICA, 16, Font.BOLD, PRIMARY_COLOR);
                    Font infoFont = new Font(Font.HELVETICA, 10, Font.NORMAL, SECONDARY_COLOR);

                    // Nom de l'auto-école
                    Phrase schoolName = new Phrase(autoEcole.getNom(), schoolNameFont);
                    ColumnText.showTextAligned(
                            cb,
                            Element.ALIGN_RIGHT,
                            schoolName,
                            document.right() - 10,
                            document.top() + 30,
                            0);

                    // Email
                    Phrase emailPhrase = new Phrase("Email: " + autoEcole.getEmail(), infoFont);
                    ColumnText.showTextAligned(
                            cb,
                            Element.ALIGN_RIGHT,
                            emailPhrase,
                            document.right() - 10,
                            document.top() + 15,
                            0);

                    // Téléphone
                    Phrase phonePhrase = new Phrase("Tél: " + autoEcole.getNumtel(), infoFont);
                    ColumnText.showTextAligned(
                            cb,
                            Element.ALIGN_RIGHT,
                            phonePhrase,
                            document.right() - 10,
                            document.top(),
                            0);

                    // Add a line separator
                    cb.setColorStroke(PRIMARY_COLOR);
                    cb.setLineWidth(2f);
                    cb.moveTo(document.left(), document.top() - 10);
                    cb.lineTo(document.right(), document.top() - 10);
                    cb.stroke();
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                AutoEcole autoEcole = autoEcoleDAO.getLastModifiedAutoEcole();
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
                    LocalDate currentDate = LocalDate.now();
                    DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
                    Phrase generated = new Phrase("Document généré le " + currentDate.format(dateFormatter),
                            new Font(Font.HELVETICA, 6, Font.ITALIC, SECONDARY_COLOR));
                    ColumnText.showTextAligned(
                            cb,
                            Element.ALIGN_LEFT,
                            generated,
                            document.left(),
                            document.bottom() + 15,
                            0);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}

