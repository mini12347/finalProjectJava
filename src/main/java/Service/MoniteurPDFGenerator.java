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
import java.io.IOException;
import java.util.Date;
import java.text.SimpleDateFormat;

public class MoniteurPDFGenerator {
    private AutoEcoleDAO autoEcoleDAO;

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
        HeaderFooter event = new HeaderFooter(autoEcole);
        writer.setPageEvent(event);

        document.open();

        // Titre du document
        Font titleFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 16, Color.BLUE);
        Paragraph title = new Paragraph("Liste des Moniteurs", titleFont);
        title.setAlignment(Element.ALIGN_CENTER);
        title.setSpacingAfter(20);
        document.add(title);

        // Date d'impression
        Font normalFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.BLACK);
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
        Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 10, Color.WHITE);
        addTableHeader(table, headerFont);

        // Données du tableau
        Font cellFont = FontFactory.getFont(FontFactory.HELVETICA, 9, Color.BLACK);
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
    }

    private void addTableHeader(PdfPTable table, Font headerFont) {
        String[] headers = {"CIN", "Nom", "Prénom", "Adresse", "Email", "Téléphone", "Date Naissance", "Disponibilités"};
        for (String header : headers) {
            PdfPCell cell = new PdfPCell(new Phrase(header, headerFont));
            cell.setBackgroundColor(new Color(30, 144, 255));
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
        private AutoEcole autoEcole;

        public HeaderFooter(AutoEcole autoEcole) {
            this.autoEcole = autoEcole;
        }

        @Override
        public void onStartPage(PdfWriter writer, Document document) {
            try {
                PdfPTable headerTable = new PdfPTable(1);
                headerTable.setWidthPercentage(100);
                headerTable.setSpacingAfter(20);

                Font headerFont = FontFactory.getFont(FontFactory.HELVETICA_BOLD, 12, Color.BLUE);
                Font subHeaderFont = FontFactory.getFont(FontFactory.HELVETICA, 10, Color.DARK_GRAY);

                PdfPCell cell = new PdfPCell();
                cell.setBorder(Rectangle.NO_BORDER);
                cell.setPadding(5);

                // Logo ou titre de l'auto-école
                Paragraph header = new Paragraph(autoEcole.getNom(), headerFont);
                header.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(header);

                // Email de l'auto-école
                Paragraph subHeader = new Paragraph("Email: " + autoEcole.getEmail(), subHeaderFont);
                subHeader.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(subHeader);

                headerTable.addCell(cell);
                document.add(headerTable);
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                PdfPTable footerTable = new PdfPTable(1);
                footerTable.setWidthPercentage(100);
                footerTable.setTotalWidth(document.right() - document.left());

                Font footerFont = FontFactory.getFont(FontFactory.HELVETICA, 8, Color.DARK_GRAY);

                PdfPCell cell = new PdfPCell();
                cell.setBorder(Rectangle.TOP);
                cell.setPadding(5);

                // Adresse et téléphone
                Paragraph footer = new Paragraph(
                        "Adresse: " + autoEcole.getAdresse() + " | Tél: " + autoEcole.getNumtel() +
                                " | Page " + writer.getPageNumber(),
                        footerFont);
                footer.setAlignment(Element.ALIGN_CENTER);
                cell.addElement(footer);

                footerTable.addCell(cell);

                // Position du pied de page
                footerTable.writeSelectedRows(0, -1, document.left(), document.bottom() + 10, writer.getDirectContent());
            } catch (DocumentException e) {
                e.printStackTrace();
            }
        }
    }
}
