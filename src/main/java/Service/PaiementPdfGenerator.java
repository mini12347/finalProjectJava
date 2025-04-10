package Service;

import Entities.AutoEcole;
import Entities.Paiement;
import Entities.ParFacilite;
import DAO.AutoEcoleDAO;

import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.PdfWriter;

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
    private static final String LOGO_PATH =  "C:\\Users\\souma\\finalProjectJava\\src\\main\\resources\\images\\111-removebg-preview.png"; // Chemin corrigé

    public PaiementPdfGenerator() throws SQLException {
        this.autoEcoleDAO = new AutoEcoleDAO();
    }

    public String generatePaiementRecu(Paiement paiement) {
        String destination = "paiement_" + paiement.getIdPaiement() + "_" + paiement.getIdClient() + ".pdf";
        try {
            // Initialize document
            Document document = new Document(PageSize.A4);
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(destination));

            // Add event handlers for header and footer
            HeaderFooter event = new HeaderFooter(autoEcoleDAO, LOGO_PATH);
            writer.setPageEvent(event);

            document.setMargins(50, 50, 130, 70); // left, right, top, bottom (increased top margin for logo)
            document.open();

            // Add title
            Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Reçu de Paiement", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Create payment info table
            PdfPTable infoTable = new PdfPTable(2);
            infoTable.setWidthPercentage(100);
            float[] columnWidths = {30f, 70f};
            infoTable.setWidths(columnWidths);

            addTableRow(infoTable, "N° Paiement", String.valueOf(paiement.getIdPaiement()));
            addTableRow(infoTable, "CIN Client", String.valueOf(paiement.getIdClient()));
            addTableRow(infoTable, "Date", paiement.getDate().format(DateTimeFormatter.ofPattern("dd/MM/yyyy")));
            addTableRow(infoTable, "Heure", paiement.getTime().toString());
            addTableRow(infoTable, "Description", paiement.getDescription());
            addTableRow(infoTable, "Montant Total", String.format("%.2f", paiement.getMontant()) + " TND");
            addTableRow(infoTable, "État", paiement.getEtat());

            document.add(infoTable);
            document.add(Chunk.NEWLINE);

            // Add payment details if it's a "Par Facilité" payment
            if (paiement.getParFacilite() != null) {
                ParFacilite parFacilite = paiement.getParFacilite();

                Font faciliteFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 14);
                Paragraph faciliteTitle = new Paragraph("Détails de Paiement Par Facilité", faciliteFont);
                faciliteTitle.setSpacingBefore(10);
                faciliteTitle.setSpacingAfter(10);
                document.add(faciliteTitle);

                PdfPTable faciliteTable = new PdfPTable(2);
                faciliteTable.setWidthPercentage(100);
                faciliteTable.setWidths(columnWidths);

                addTableRow(faciliteTable, "Acompte", String.format("%.2f", parFacilite.getAccompte()) + " TND");

                // Add installments if any
                List<Double> montants = parFacilite.getMontans();
                if (!montants.isEmpty()) {
                    StringBuilder tranches = new StringBuilder();
                    for (int i = 0; i < montants.size(); i++) {
                        tranches.append("Tranche ").append(i + 1).append(": ")
                                .append(String.format("%.2f", montants.get(i))).append(" TND");
                        if (i < montants.size() - 1) {
                            tranches.append("\n");
                        }
                    }
                    addTableRow(faciliteTable, "Tranches Restantes", tranches.toString());
                } else {
                    addTableRow(faciliteTable, "Tranches Restantes", "Toutes les tranches ont été payées");
                }

                document.add(faciliteTable);
            }

            // Add payment conditions and notes
            Font conditionsFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12);
            Paragraph conditions = new Paragraph("Conditions de paiement:", conditionsFont);
            conditions.setSpacingBefore(15);
            conditions.setSpacingAfter(5);
            document.add(conditions);

            Font noteFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
            Paragraph note1 = new Paragraph("- Ce reçu est la preuve de votre paiement à l'auto-école.", noteFont);
            Paragraph note2 = new Paragraph("- En cas de paiement par facilité, tous les versements doivent être effectués selon le calendrier convenu.", noteFont);
            Paragraph note3 = new Paragraph("- Aucun remboursement ne sera accordé après le début des cours.", noteFont);

            document.add(note1);
            document.add(note2);
            document.add(note3);

            // Add generation date and time
            LocalDate currentDate = LocalDate.now();
            java.time.LocalTime currentTime = java.time.LocalTime.now();
            DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
            DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");

            Font dateFont = FontFactory.getFont(FontFactory.HELVETICA_OBLIQUE, 10);
            Paragraph dateInfo = new Paragraph(
                    "Document généré le " + currentDate.format(dateFormatter) + " " +
                            currentTime.format(timeFormatter), dateFont);
            dateInfo.setAlignment(Element.ALIGN_RIGHT);
            dateInfo.setSpacingBefore(20);
            document.add(dateInfo);

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

    private void addTableRow(PdfPTable table, String label, String value) {
        Font labelFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 11);
        Font valueFont = FontFactory.getFont(FontFactory.HELVETICA, 11);

        PdfPCell labelCell = new PdfPCell(new Phrase(label, labelFont));
        labelCell.setBorderColor(Color.LIGHT_GRAY);
        labelCell.setPadding(5);

        PdfPCell valueCell = new PdfPCell(new Phrase(value, valueFont));
        valueCell.setBorderColor(Color.LIGHT_GRAY);
        valueCell.setPadding(5);

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
                this.logo.scaleToFit(150, 100); // Ajustez ces valeurs selon la taille souhaitée
            } catch (Exception e) {
                e.printStackTrace();
                this.logo = null;
            }
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                PdfContentByte cb = writer.getDirectContent();

                // Ajouter le logo centré en haut de la page
                if (logo != null) {
                    float logoWidth = logo.getScaledWidth();
                    float centerX = (document.right() - document.left()) / 2 + document.left();
                    float logoX = centerX - (logoWidth / 2);
                    float logoY = document.top() + 30; // Position au-dessus de l'en-tête

                    logo.setAbsolutePosition(logoX, logoY);
                    cb.addImage(logo);
                }

                AutoEcole autoEcole = autoEcoleDAO.getLastModifiedAutoEcole();
                if (autoEcole != null) {
                    // Add header title
                    Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
                    Phrase headerTitle = new Phrase(autoEcole.getNom(), titleFont);

                    // Add email
                    Font emailFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
                    Phrase emailPhrase = new Phrase(" | Email: " + autoEcole.getEmail(), emailFont);

                    // Combine title and email in one line
                    Paragraph headerInfo = new Paragraph();
                    headerInfo.add(headerTitle);
                    headerInfo.add(emailPhrase);

                    // Add the header to the document (below the logo)
                    ColumnText.showTextAligned(
                            writer.getDirectContent(),
                            Element.ALIGN_CENTER,
                            headerInfo,
                            (document.right() - document.left()) / 2 + document.left(),
                            document.top() + 10,
                            0);

                    // Add a line separator
                    cb.setLineWidth(1f);
                    cb.moveTo(document.left(), document.top() - 5);
                    cb.lineTo(document.right(), document.top() - 5);
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
                    // Add a line separator
                    PdfContentByte cb = writer.getDirectContent();
                    cb.setLineWidth(1f);
                    cb.moveTo(document.left(), document.bottom() + 30);
                    cb.lineTo(document.right(), document.bottom() + 30);
                    cb.stroke();

                    // Add footer text
                    Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
                    Phrase footerPhrase = new Phrase(
                            autoEcole.getNom() + " | Adresse: " + autoEcole.getAdresse() +
                                    " | Tél: " + autoEcole.getNumtel(), footerFont);

                    // Position footer text
                    ColumnText.showTextAligned(
                            writer.getDirectContent(),
                            Element.ALIGN_CENTER,
                            footerPhrase,
                            (document.right() - document.left()) / 2 + document.left(),
                            document.bottom() + 15,
                            0);

                    // Add page number
                    Phrase pageNumber = new Phrase("Page " + writer.getPageNumber(), footerFont);
                    ColumnText.showTextAligned(
                            writer.getDirectContent(),
                            Element.ALIGN_RIGHT,
                            pageNumber,
                            document.right(),
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