package Service;

import Entities.Candidat;
import Entities.AutoEcole;
import com.lowagie.text.*;
import com.lowagie.text.pdf.*;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.awt.Color;
import Service.AutoEcoleInfosS;

import static com.lowagie.text.FontFactory.*;
import static com.lowagie.text.StandardFonts.HELVETICA_ITALIC;

public class PdfGeneratorCandidat {
    private final AutoEcoleInfosS autoEcoleService = new AutoEcoleInfosS();

    /**
     * Génère un PDF contenant les données d'un candidat avec l'en-tête et
     * le pied de page contenant les informations de l'auto-école
     *
     * @param candidate Le candidat dont les données seront incluses dans le PDF
     * @param filePath Le chemin où enregistrer le fichier PDF
     * @throws IOException En cas d'erreur lors de la création du fichier
     */
    public void generateCandidatePdf(Candidat candidate, String filePath) throws IOException {
        Document document = new Document(PageSize.A4);

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

            // Ajouter l'event pour en-tête et pied de page
            AutoEcoleHeaderFooter event = new AutoEcoleHeaderFooter();
            writer.setPageEvent(event);

            document.open();

            // Ajout du titre
            Font titleFont = getFont(HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Fiche Candidat", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Ajout des détails du candidat
            Font contentFont = getFont(HELVETICA, 12);

            document.add(createDetailsParagraph("CIN", String.valueOf(candidate.getCIN()), contentFont));
            document.add(createDetailsParagraph("Nom", candidate.getNom(), contentFont));
            document.add(createDetailsParagraph("Prénom", candidate.getPrenom(), contentFont));
            document.add(createDetailsParagraph("Email", candidate.getMail(), contentFont));
            document.add(createDetailsParagraph("Téléphone", String.valueOf(candidate.getNumTelephone()), contentFont));

            if (candidate.getAdresse() != null && !candidate.getAdresse().isEmpty()) {
                document.add(createDetailsParagraph("Adresse", candidate.getAdresse(), contentFont));
            }

            if (candidate.getDateNaissance() != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                document.add(createDetailsParagraph("Date de naissance", sdf.format(candidate.getDateNaissance()), contentFont));
            }

            // Ajout de la date de génération
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
            Paragraph dateGeneration = new Paragraph("Document généré le " + dateFormat.format(new Date()), contentFont);
            dateGeneration.setAlignment(Element.ALIGN_RIGHT);
            dateGeneration.setSpacingBefore(30);
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
        Document document = new Document(PageSize.A4.rotate()); // Format paysage

        try {
            PdfWriter writer = PdfWriter.getInstance(document, new FileOutputStream(filePath));

            // Ajouter l'event pour en-tête et pied de page
            AutoEcoleHeaderFooter event = new AutoEcoleHeaderFooter();
            writer.setPageEvent(event);

            document.open();

            // Ajout du titre
            Font titleFont = getFont(HELVETICA_BOLD, 18);
            Paragraph title = new Paragraph("Liste des Candidats", titleFont);
            title.setAlignment(Element.ALIGN_CENTER);
            title.setSpacingAfter(20);
            document.add(title);

            // Création du tableau
            PdfPTable table = new PdfPTable(5); // 5 colonnes
            table.setWidthPercentage(100);

            // Définition des largeurs de colonnes
            float[] columnWidths = {1.5f, 2.5f, 2.5f, 4f, 2.5f};
            table.setWidths(columnWidths);

            // Ajout des en-têtes
// For the table header cells
            Font headerFont = getFont(HELVETICA_BOLD, 12, Color.WHITE);

            PdfPCell headerCell = new PdfPCell();
            headerCell.setBackgroundColor(Color.DARK_GRAY);
            headerCell.setPadding(5);

            headerCell.setPhrase(new Phrase("CIN", headerFont));
            table.addCell(headerCell);

            headerCell.setPhrase(new Phrase("Nom", headerFont));
            table.addCell(headerCell);

            headerCell.setPhrase(new Phrase("Prénom", headerFont));
            table.addCell(headerCell);

            headerCell.setPhrase(new Phrase("Email", headerFont));
            table.addCell(headerCell);

            headerCell.setPhrase(new Phrase("Téléphone", headerFont));
            table.addCell(headerCell);

            // Ajout des données
            Font cellFont = getFont(HELVETICA, 10);

            for (Candidat candidate : candidates) {
                table.addCell(new PdfPCell(new Phrase(String.valueOf(candidate.getCIN()), cellFont)));
                table.addCell(new PdfPCell(new Phrase(candidate.getNom(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(candidate.getPrenom(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(candidate.getMail(), cellFont)));
                table.addCell(new PdfPCell(new Phrase(String.valueOf(candidate.getNumTelephone()), cellFont)));
            }

            document.add(table);

            // Ajout de la date de génération
            SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
// For the date generation paragraph
            Paragraph dateGeneration = new Paragraph("Document généré le " + dateFormat.format(new Date()),
                    getFont(String.valueOf(HELVETICA_ITALIC), 10));
            dateGeneration.setAlignment(Element.ALIGN_RIGHT);
            dateGeneration.setSpacingBefore(15);
            document.add(dateGeneration);

            // Ajout du nombre total de candidats
            Paragraph totalCandidats = new Paragraph("Nombre total de candidats : " + candidates.size(),
                    getFont(HELVETICA_BOLD, 10));
            totalCandidats.setAlignment(Element.ALIGN_LEFT);
            totalCandidats.setSpacingBefore(5);
            document.add(totalCandidats);

        } catch (Exception e) {
            throw new IOException("Erreur lors de la génération du PDF: " + e.getMessage(), e);
        } finally {
            document.close();
        }
    }

    /**
     * Crée un paragraphe formaté pour les détails du candidat
     *
     * @param label Le libellé du champ
     * @param value La valeur du champ
     * @param font La police à utiliser
     * @return Un paragraphe formaté
     */
    private Paragraph createDetailsParagraph(String label, String value, Font font) {
        Font boldFont = getFont(HELVETICA_BOLD, font.getSize());
        Paragraph paragraph = new Paragraph();
        paragraph.add(new Chunk(label + " : ", boldFont));
        paragraph.add(new Chunk(value, font));
        paragraph.setSpacingAfter(10);
        return paragraph;
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
        public void onEndPage(PdfWriter writer, Document document) {
            try {
                // En-tête avec nom et email de l'auto-école
                if (autoEcole != null) {
                    PdfContentByte cb = writer.getDirectContent();

                    // En-tête (Nom et Email de l'auto-école)
                    Font headerFont = getFont(HELVETICA_BOLD, 12);
                    Phrase header = new Phrase(autoEcole.getNom() + " | Email: " + autoEcole.getEmail(), headerFont);

                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                            header,
                            document.right() / 2 + document.leftMargin(),
                            document.top() + 10,
                            0);

                    // Ligne de séparation après l'en-tête
                    cb.setLineWidth(0.5f);
                    cb.moveTo(document.leftMargin(), document.top());
                    cb.lineTo(document.right(), document.top());
                    cb.stroke();

                    // Pied de page (Adresse et Numéro de téléphone)
                    Font footerFont = getFont(HELVETICA, 10);
                    Phrase footer = new Phrase("Adresse: " + autoEcole.getAdresse() + " | Tél: " + autoEcole.getNumtel(), footerFont);

                    ColumnText.showTextAligned(cb, Element.ALIGN_CENTER,
                            footer,
                            document.right() / 2 + document.leftMargin(),
                            document.bottom() - 20,
                            0);

                    // Ligne de séparation avant le pied de page
                    cb.setLineWidth(0.5f);
                    cb.moveTo(document.leftMargin(), document.bottom() - 10);
                    cb.lineTo(document.right(), document.bottom() - 10);
                    cb.stroke();

                    // Numéro de page
                    Font pageNumberFont = getFont(HELVETICA, 8);
                    Phrase pageNumber = new Phrase("Page " + writer.getPageNumber(), pageNumberFont);
                    ColumnText.showTextAligned(cb, Element.ALIGN_RIGHT,
                            pageNumber,
                            document.right(),
                            document.bottom() - 30,
                            0);
                }
            } catch (Exception e) {
                System.err.println("Erreur lors de l'ajout de l'en-tête ou du pied de page: " + e.getMessage());
            }
        }
    }
}