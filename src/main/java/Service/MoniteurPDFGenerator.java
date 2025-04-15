package Service;

import Entities.Moniteur;
import Entities.AutoEcole;
import Entities.Disponibility;
import Entities.Hours;
import DAO.AutoEcoleDAO;

import com.lowagie.text.*;
import com.lowagie.text.Font;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.sql.SQLException;
import java.time.DayOfWeek;
import java.util.List;
import java.util.Map;
import java.awt.Color;
import java.io.File;
import java.awt.Desktop;
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MoniteurPDFGenerator {
    private AutoEcoleDAO autoEcoleDAO;
    private static final String LOGO_PATH = "/images/111-removebg-preview.png"; // Chemin relatif au classpath

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
    private static final Font SUB_HEADER_FONT = new Font(Font.HELVETICA, 12, Font.BOLD, SECONDARY_COLOR);
    private static final Font FOOTER_FONT = new Font(Font.HELVETICA, 8, Font.ITALIC, SECONDARY_COLOR);
    private static final Font DETAIL_LABEL_FONT = new Font(Font.HELVETICA, 11, Font.BOLD, PRIMARY_COLOR);
    private static final Font DETAIL_VALUE_FONT = new Font(Font.HELVETICA, 11, Font.NORMAL, TEXT_COLOR);

    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

    public MoniteurPDFGenerator() throws SQLException {
        this.autoEcoleDAO = new AutoEcoleDAO();
    }

    public void generatePDF(List<Moniteur> moniteurs, String filePath) throws SQLException, IOException, DocumentException {
        // Récupérer les informations de l'auto-école
        AutoEcole autoEcole = autoEcoleDAO.getLastModifiedAutoEcole();
        if (autoEcole == null) {
            throw new SQLException("Impossible de récupérer les informations de l'auto-école");
        }

        // Création du document PDF avec de meilleures marges
        Document document = new Document(PageSize.A4, 36, 36, 54, 36);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

        // Ajout d'un event pour les en-têtes et pieds de page
        HeaderFooter event = new HeaderFooter(autoEcoleDAO, LOGO_PATH);
        writer.setPageEvent(event);

        document.open();

        // Titre du document avec style amélioré
        Paragraph title = new Paragraph("Liste des Moniteurs", TITLE_FONT);
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

        // Date d'impression avec icône
        SimpleDateFormat printDateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Paragraph dateString = new Paragraph();
        dateString.add(new Chunk("📅 ", new Font(Font.ZAPFDINGBATS, 12)));
        dateString.add(new Chunk("Date d'impression: " + printDateFormat.format(new Date()),
                new Font(Font.HELVETICA, 10, Font.ITALIC, SECONDARY_COLOR)));
        dateString.setAlignment(Element.ALIGN_RIGHT);
        dateString.setSpacingBefore(10);
        dateString.setSpacingAfter(15);
        document.add(dateString);

        // Tableau pour les moniteurs avec style amélioré
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Définir la largeur des colonnes
        float[] columnWidths = {1f, 1.5f, 1.5f, 2f, 2f, 1.5f, 2f, 2.5f};
        table.setWidths(columnWidths);

        // En-têtes du tableau avec style amélioré
        addTableHeader(table);

        // Données du tableau avec style amélioré
        boolean alternate = false;
        for (Moniteur moniteur : moniteurs) {
            addMoniteurToTable(table, moniteur, alternate);
            alternate = !alternate;
        }

        document.add(table);

        // Résumé avec style amélioré
        PdfPTable summaryTable = new PdfPTable(2);
        summaryTable.setWidthPercentage(60);
        summaryTable.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryTable.setSpacingBefore(20);

        PdfPCell summaryLabelCell = new PdfPCell(new Phrase("Nombre total de moniteurs:", DETAIL_LABEL_FONT));
        summaryLabelCell.setBorder(Rectangle.NO_BORDER);
        summaryLabelCell.setHorizontalAlignment(Element.ALIGN_RIGHT);
        summaryLabelCell.setPadding(5);

        PdfPCell summaryValueCell = new PdfPCell(new Phrase(String.valueOf(moniteurs.size()),
                new Font(Font.HELVETICA, 12, Font.BOLD, PRIMARY_COLOR)));
        summaryValueCell.setBorder(Rectangle.BOX);
        summaryValueCell.setBorderColor(PRIMARY_COLOR);
        summaryValueCell.setBackgroundColor(LIGHT_GRAY);
        summaryValueCell.setHorizontalAlignment(Element.ALIGN_CENTER);
        summaryValueCell.setPadding(5);

        summaryTable.addCell(summaryLabelCell);
        summaryTable.addCell(summaryValueCell);
        document.add(summaryTable);

        document.close();

        // Ouvrir le PDF après génération
        openPDF(filePath);
    }

    // Méthode pour ouvrir le PDF généré
    private void openPDF(String filePath) {
        try {
            File pdfFile = new File(filePath);
            if (pdfFile.exists()) {
                if (Desktop.isDesktopSupported()) {
                    Desktop.getDesktop().open(pdfFile);
                } else {
                    System.out.println("Desktop n'est pas supporté, impossible d'ouvrir automatiquement le PDF.");
                }
            } else {
                System.out.println("Le fichier PDF n'existe pas: " + filePath);
            }
        } catch (IOException e) {
            System.err.println("Erreur lors de l'ouverture du PDF: " + e.getMessage());
        }
    }

    private void addTableHeader(PdfPTable table) {
        String[] headers = {"CIN", "Nom", "Prénom", "Adresse", "Email", "Téléphone", "Date Naissance", "Disponibilités"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, HEADER_FONT));
            cell.setBackgroundColor(PRIMARY_COLOR);
            cell.setPadding(8);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
    }

    private void addMoniteurToTable(PdfPTable table, Moniteur moniteur, boolean alternate) {
        Color bgColor = alternate ? LIGHT_GRAY : Color.WHITE;

        // CIN
        PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(moniteur.getCIN()), CONTENT_FONT));
        cell.setPadding(6);
        cell.setBackgroundColor(bgColor);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        // Nom
        cell = new PdfPCell(new Phrase(moniteur.getNom(), CONTENT_FONT));
        cell.setPadding(6);
        cell.setBackgroundColor(bgColor);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        // Prénom
        cell = new PdfPCell(new Phrase(moniteur.getPrenom(), CONTENT_FONT));
        cell.setPadding(6);
        cell.setBackgroundColor(bgColor);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        // Adresse
        cell = new PdfPCell(new Phrase(moniteur.getAdresse(), CONTENT_FONT));
        cell.setPadding(6);
        cell.setBackgroundColor(bgColor);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        // Email
        cell = new PdfPCell(new Phrase(moniteur.getMail(), CONTENT_FONT));
        cell.setPadding(6);
        cell.setBackgroundColor(bgColor);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        // Téléphone
        cell = new PdfPCell(new Phrase(String.valueOf(moniteur.getNumTelephone()), CONTENT_FONT));
        cell.setPadding(6);
        cell.setBackgroundColor(bgColor);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        // Date de naissance
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = moniteur.getDateNaissance() != null ? format.format(moniteur.getDateNaissance()) : "";
        cell = new PdfPCell(new Phrase(dateStr, CONTENT_FONT));
        cell.setPadding(6);
        cell.setBackgroundColor(bgColor);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);

        // Disponibilités avec style amélioré
        Disponibility dispo = moniteur.getDisponibilite();
        StringBuilder dispoStr = new StringBuilder();
        if (dispo != null && dispo.getDaysOfWeek() != null) {
            Map<DayOfWeek, Hours> schedule = dispo.getDaysOfWeek();
            if (!schedule.isEmpty()) {
                for (Map.Entry<DayOfWeek, Hours> entry : schedule.entrySet()) {
                    dispoStr.append("• ")
                            .append(translateDayOfWeek(entry.getKey()))
                            .append(": ")
                            .append(entry.getValue().getStarthour())
                            .append("h-")
                            .append(entry.getValue().getEndhour())
                            .append("h\n");
                }
            }
        }
        cell = new PdfPCell(new Phrase(dispoStr.toString(), CONTENT_FONT));
        cell.setPadding(6);
        cell.setBackgroundColor(bgColor);
        cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
        table.addCell(cell);
    }

    private String translateDayOfWeek(DayOfWeek day) {
        switch (day) {
            case MONDAY:
                return "Lundi";
            case TUESDAY:
                return "Mardi";
            case WEDNESDAY:
                return "Mercredi";
            case THURSDAY:
                return "Jeudi";
            case FRIDAY:
                return "Vendredi";
            case SATURDAY:
                return "Samedi";
            case SUNDAY:
                return "Dimanche";
            default:
                return day.toString();
        }
    }

    // Classe interne pour gérer l'en-tête et le pied de page
    class HeaderFooter extends PdfPageEventHelper {
        private AutoEcoleDAO autoEcoleDAO;
        private String logoPath;
        private Image logo;

        public HeaderFooter(AutoEcoleDAO autoEcoleDAO, String logoPath) {
            this.autoEcoleDAO = autoEcoleDAO;
            this.logoPath = logoPath;
            try {
                // Obtenir le chemin absolu du logo à partir des ressources
                String absoluteLogoPath = getClass().getResource(logoPath).getPath();
                this.logo = Image.getInstance(absoluteLogoPath);
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
                    SimpleDateFormat genDateFormat = new SimpleDateFormat("dd/MM/yyyy");
                    Phrase generated = new Phrase("Document généré le " + genDateFormat.format(new Date()),
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
