package Service;

import Entities.Moniteur;
import Entities.AutoEcole;
import Entities.Disponibility;
import Entities.Hours;
import DAO.AutoEcoleDAO;

import com.lowagie.text.*;
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

    public MoniteurPDFGenerator() throws SQLException {
        this.autoEcoleDAO = new AutoEcoleDAO();
    }

    public void generatePDF(List<Moniteur> moniteurs, String filePath) throws SQLException, IOException, DocumentException {
        // Récupérer les informations de l'auto-école
        AutoEcole autoEcole = autoEcoleDAO.getLastModifiedAutoEcole();
        if (autoEcole == null) {
            throw new SQLException("Impossible de récupérer les informations de l'auto-école");
        }

        // Création du document PDF
        Document document = new Document(PageSize.A4);
        PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

        // Ajout d'un event pour les en-têtes et pieds de page
        HeaderFooter event = new HeaderFooter(autoEcoleDAO, LOGO_PATH);
        writer.setPageEvent(event);

        document.setMargins(50, 50, 130, 70); // left, right, top, bottom (increased top margin for logo)
        document.open();

        // Titre du document
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16);
        Paragraph title = new Paragraph("Liste des Moniteurs", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Date d'impression
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10);
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
        Paragraph dateString = new Paragraph("Date d'impression: " + dateFormat.format(new Date()), normalFont);
        dateString.setAlignment(Element.ALIGN_RIGHT);
        dateString.setSpacingAfter(15);
        document.add(dateString);

        // Tableau pour les moniteurs
        PdfPTable table = new PdfPTable(8);
        table.setWidthPercentage(100);
        table.setSpacingBefore(10f);
        table.setSpacingAfter(10f);

        // Définir la largeur des colonnes
        float[] columnWidths = {1f, 1.5f, 1.5f, 2f, 2f, 1.5f, 2f, 2.5f};
        table.setWidths(columnWidths);

        // En-têtes du tableau
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10);
        addTableHeader(table, headerFont);

        // Données du tableau
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9);
        for (Moniteur moniteur : moniteurs) {
            addMoniteurToTable(table, moniteur, cellFont);
        }

        document.add(table);

        // Pied de page avec nombre total
        Paragraph total = new Paragraph("Nombre total de moniteurs: " + moniteurs.size(), normalFont);
        total.setAlignment(Element.ALIGN_RIGHT);
        total.setSpacingBefore(10);
        document.add(total);

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

    private void addTableHeader(PdfPTable table, Font headerFont) {
        String[] headers = {"CIN", "Nom", "Prénom", "Adresse", "Email", "Téléphone", "Date Naissance", "Disponibilités"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(Color.LIGHT_GRAY);
            cell.setPadding(5);
            cell.setHorizontalAlignment(Element.ALIGN_CENTER);
            cell.setVerticalAlignment(Element.ALIGN_MIDDLE);
            table.addCell(cell);
        }
    }

    private void addMoniteurToTable(PdfPTable table, Moniteur moniteur, Font cellFont) {
        // CIN
        PdfPCell cell = new PdfPCell(new Phrase(String.valueOf(moniteur.getCIN()), cellFont));
        cell.setPadding(5);
        table.addCell(cell);

        // Nom
        cell = new PdfPCell(new Phrase(moniteur.getNom(), cellFont));
        cell.setPadding(5);
        table.addCell(cell);

        // Prénom
        cell = new PdfPCell(new Phrase(moniteur.getPrenom(), cellFont));
        cell.setPadding(5);
        table.addCell(cell);

        // Adresse
        cell = new PdfPCell(new Phrase(moniteur.getAdresse(), cellFont));
        cell.setPadding(5);
        table.addCell(cell);

        // Email
        cell = new PdfPCell(new Phrase(moniteur.getMail(), cellFont));
        cell.setPadding(5);
        table.addCell(cell);

        // Téléphone
        cell = new PdfPCell(new Phrase(String.valueOf(moniteur.getNumTelephone()), cellFont));
        cell.setPadding(5);
        table.addCell(cell);

        // Date de naissance
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        String dateStr = moniteur.getDateNaissance() != null ? format.format(moniteur.getDateNaissance()) : "";
        cell = new PdfPCell(new Phrase(dateStr, cellFont));
        cell.setPadding(5);
        table.addCell(cell);

        // Disponibilités
        Disponibility dispo = moniteur.getDisponibilite();
        StringBuilder dispoStr = new StringBuilder();
        if (dispo != null && dispo.getDaysOfWeek() != null) {
            Map<DayOfWeek, Hours> schedule = dispo.getDaysOfWeek();
            if (!schedule.isEmpty()) {
                for (Map.Entry<DayOfWeek, Hours> entry : schedule.entrySet()) {
                    dispoStr.append(translateDayOfWeek(entry.getKey()))
                            .append(": ")
                            .append(entry.getValue().getStarthour())
                            .append("h-")
                            .append(entry.getValue().getEndhour())
                            .append("h\n");
                }
            }
        }
        cell = new PdfPCell(new Phrase(dispoStr.toString(), cellFont));
        cell.setPadding(5);
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