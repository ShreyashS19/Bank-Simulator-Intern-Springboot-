package com.bank.simulator.service;

import com.bank.simulator.dto.LoanEligibilityResultDto;
import com.bank.simulator.exception.BusinessException;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.springframework.stereotype.Service;

import java.awt.Color;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Service
@Slf4j
public class LoanPdfService {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd MMM yyyy HH:mm");

    // Light gradient-like colors for the watermark (yellow → salmon/pink, matching the reference)
    private static final Color WM_COLOR_YELLOW = new Color(255, 230, 150);   // warm yellow
    private static final Color WM_COLOR_SALMON = new Color(255, 160, 140);   // salmon/pink
    private static final Color WM_COLOR_CENTER = new Color(255, 195, 145);   // warm center blend

    public byte[] generateEligibilityPdf(LoanEligibilityResultDto result) {
        try (PDDocument document = new PDDocument();
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {

            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);

            // Layer 1: tiled small bank icons (background pattern)
            drawTiledBankIcons(document, page);
            // Layer 2: actual letter content on top
            drawLetterContent(document, page, result);

            document.save(outputStream);
            return outputStream.toByteArray();
        } catch (IOException ex) {
            log.error("Failed to generate eligibility PDF for reference {}", result.getReferenceNumber(), ex);
            throw new BusinessException("Unable to generate eligibility PDF");
        }
    }

    // ─── Watermark Layer 1: Tiled small bank icons ───────────────────────────

    private void drawTiledBankIcons(PDDocument document, PDPage page) throws IOException {
        PDRectangle box = page.getMediaBox();
        float pageWidth = box.getWidth();
        float pageHeight = box.getHeight();

        try (PDPageContentStream cs = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(0.12f);
            gs.setAlphaSourceFlag(true);
            cs.saveGraphicsState();
            cs.setGraphicsStateParameters(gs);
            cs.setStrokingColor(WM_COLOR_YELLOW);
            cs.setNonStrokingColor(WM_COLOR_YELLOW);
            cs.setLineWidth(1.0f);

            float tileSize = 85f;
            float iconSize = 26f;

            for (float tx = 10f; tx < pageWidth; tx += tileSize) {
                for (float ty = 10f; ty < pageHeight; ty += tileSize) {
                    float cx = tx + tileSize / 2f;
                    float cy = ty + tileSize / 2f;
                    drawSmallBankIcon(cs, cx - iconSize / 2f, cy - iconSize / 2f, iconSize);
                }
            }
            cs.restoreGraphicsState();
        }
    }

    // ─── Watermark Layer 2: Large centered bank icon ─────────────────────────

    private void drawCenterBankIcon(PDDocument document, PDPage page) throws IOException {
        PDRectangle box = page.getMediaBox();
        float cx = box.getWidth() / 2f;
        float cy = box.getHeight() / 2f;
        float size = 320f;
        float ox = cx - size / 2f;
        float oy = cy - size / 2f;

        try (PDPageContentStream cs = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

            // Outer circle
            PDExtendedGraphicsState gs = new PDExtendedGraphicsState();
            gs.setNonStrokingAlphaConstant(0.04f);
            gs.setAlphaSourceFlag(true);
            cs.saveGraphicsState();
            cs.setGraphicsStateParameters(gs);
            cs.setStrokingColor(WM_COLOR_SALMON);
            cs.setLineWidth(7f);
            drawCircleStroke(cs, cx, cy, size / 2f - 4f);
            cs.restoreGraphicsState();

            // Bank building body
            PDExtendedGraphicsState gs2 = new PDExtendedGraphicsState();
            gs2.setNonStrokingAlphaConstant(0.04f);
            gs2.setAlphaSourceFlag(true);
            cs.saveGraphicsState();
            cs.setGraphicsStateParameters(gs2);
            cs.setStrokingColor(WM_COLOR_CENTER);
            cs.setLineWidth(5.5f);
            drawLargeBankBuilding(cs, ox, oy, size);
            cs.restoreGraphicsState();
        }
    }

    // ─── Bank icon drawing: small (tile) ────────────────────────────────────

    /**
     * Draws a tiny bank building icon (outline style) at position (ox, oy) with given size.
     */
    private void drawSmallBankIcon(PDPageContentStream cs, float ox, float oy, float s) throws IOException {
        float u = s / 30f; // unit scale

        // Outer circle
        drawCircleStroke(cs, ox + s / 2f, oy + s / 2f, s / 2f - u);

        // Roof (triangle)
        cs.moveTo(ox + s * 0.50f, oy + s * 0.82f);
        cs.lineTo(ox + s * 0.20f, oy + s * 0.62f);
        cs.lineTo(ox + s * 0.80f, oy + s * 0.62f);
        cs.closePath();
        cs.stroke();

        // Columns
        for (float col : new float[]{0.33f, 0.50f, 0.67f}) {
            cs.moveTo(ox + s * col, oy + s * 0.61f);
            cs.lineTo(ox + s * col, oy + s * 0.38f);
            cs.stroke();
        }

        // Base rectangle
        cs.addRect(ox + s * 0.18f, oy + s * 0.33f, s * 0.64f, s * 0.07f);
        cs.stroke();

        // Dollar sign (simplified — vertical bar)
        cs.moveTo(ox + s * 0.50f, oy + s * 0.57f);
        cs.lineTo(ox + s * 0.50f, oy + s * 0.38f);
        cs.stroke();
    }

    // ─── Bank icon drawing: large (center) ──────────────────────────────────

    private void drawLargeBankBuilding(PDPageContentStream cs, float ox, float oy, float s) throws IOException {
        // Roof triangle
        cs.moveTo(ox + s * 0.50f, oy + s * 0.80f);
        cs.lineTo(ox + s * 0.18f, oy + s * 0.60f);
        cs.lineTo(ox + s * 0.82f, oy + s * 0.60f);
        cs.closePath();
        cs.stroke();

        // Columns (5 columns)
        float[] cols = {0.27f, 0.37f, 0.50f, 0.63f, 0.73f};
        for (float col : cols) {
            cs.moveTo(ox + s * col, oy + s * 0.59f);
            cs.lineTo(ox + s * col, oy + s * 0.36f);
            cs.stroke();
        }

        // Dollar sign – S-curve approximation via arc lines
        float dcx = ox + s * 0.50f;
        float dcy = oy + s * 0.50f;
        float dr = s * 0.08f;
        // top arc
        cs.moveTo(dcx + dr, dcy + dr * 0.5f);
        cs.curveTo(dcx + dr, dcy + dr * 1.8f, dcx - dr, dcy + dr * 1.8f, dcx - dr, dcy + dr * 0.5f);
        cs.stroke();
        // bottom arc
        cs.moveTo(dcx - dr, dcy - dr * 0.5f);
        cs.curveTo(dcx - dr, dcy - dr * 1.8f, dcx + dr, dcy - dr * 1.8f, dcx + dr, dcy - dr * 0.5f);
        cs.stroke();
        // vertical bar through dollar
        cs.moveTo(dcx, dcy + dr * 2.2f);
        cs.lineTo(dcx, dcy - dr * 2.2f);
        cs.stroke();

        // Base rectangle
        cs.addRect(ox + s * 0.16f, oy + s * 0.30f, s * 0.68f, s * 0.07f);
        cs.stroke();

        // Second base line
        cs.addRect(ox + s * 0.10f, oy + s * 0.22f, s * 0.80f, s * 0.06f);
        cs.stroke();
    }

    // ─── Circle helper (PDFBox has no built-in circle) ──────────────────────

    private void drawCircleStroke(PDPageContentStream cs, float cx, float cy, float r) throws IOException {
        float k = 0.5522847f * r;
        cs.moveTo(cx + r, cy);
        cs.curveTo(cx + r, cy + k, cx + k, cy + r, cx, cy + r);
        cs.curveTo(cx - k, cy + r, cx - r, cy + k, cx - r, cy);
        cs.curveTo(cx - r, cy - k, cx - k, cy - r, cx, cy - r);
        cs.curveTo(cx + k, cy - r, cx + r, cy - k, cx + r, cy);
        cs.closePath();
        cs.stroke();
    }

    // ─── Letter content (drawn on top) ──────────────────────────────────────

    private void drawLetterContent(PDDocument document, PDPage page, LoanEligibilityResultDto result) throws IOException {
        PDRectangle mediaBox = page.getMediaBox();
        float pageWidth = mediaBox.getWidth();
        float margin = 50f;
        float y = mediaBox.getHeight() - margin;

        PDFont headingFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_BOLD);
        PDFont bodyFont   = new PDType1Font(Standard14Fonts.FontName.HELVETICA);
        PDFont italicFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA_OBLIQUE);

        try (PDPageContentStream cs = new PDPageContentStream(
                document, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

            // ── Header ────────────────────────────────────────────────────────
            // Bank name at very top
            cs.beginText();
            cs.setFont(headingFont, 20);
            cs.setNonStrokingColor(new Color(15, 23, 42));
            cs.newLineAtOffset(margin, y);
            cs.showText("BANK SIMULATOR");
            cs.endText();

            // Tagline right-aligned (same baseline as title)
            String tagline = "Loan Eligibility Advisory";
            float tagW = italicFont.getStringWidth(tagline) / 1000f * 10;
            cs.beginText();
            cs.setFont(italicFont, 10);
            cs.setNonStrokingColor(new Color(100, 116, 139));
            cs.newLineAtOffset(pageWidth - margin - tagW, y);
            cs.showText(tagline);
            cs.endText();

            // Blue accent bar BELOW the header text
            y -= 8;
            cs.setNonStrokingColor(new Color(30, 64, 175));
            cs.addRect(margin, y - 3, pageWidth - 2 * margin, 3f);
            cs.fill();

            y -= 18;

            // ── Title ─────────────────────────────────────────────────────────
            cs.beginText();
            cs.setFont(headingFont, 14);
            cs.setNonStrokingColor(new Color(15, 23, 42));
            cs.newLineAtOffset(margin, y);
            cs.showText("LOAN ELIGIBILITY ASSESSMENT LETTER");
            cs.endText();

            // ── Reference / Date / Customer ──────────────────────────────────
            y -= 18;
            writeLine(cs, bodyFont, 10, margin, y, new Color(71, 85, 105),
                    "Reference No: " + safeText(result.getReferenceNumber()));
            y -= 14;
            writeLine(cs, bodyFont, 10, margin, y, new Color(71, 85, 105),
                    "Date: " + DATE_FORMATTER.format(result.getGeneratedAt()));
            y -= 14;
            writeLine(cs, bodyFont, 10, margin, y, new Color(71, 85, 105),
                    "Customer: " + safeText(result.getCustomerName())
                    + "   |   Email: " + safeText(result.getCustomerEmail()));

            // ── Status badge ─────────────────────────────────────────────────
            y -= 20;
            boolean eligible = "ELIGIBLE".equalsIgnoreCase(result.getEligibilityStatus());
            Color badgeBg   = eligible ? new Color(220, 252, 231) : new Color(254, 226, 226);
            Color badgeBorder = eligible ? new Color(34, 197, 94) : new Color(239, 68, 68);
            Color badgeText = eligible ? new Color(20, 83, 45) : new Color(127, 29, 29);

            float badgeH = 24f;
            // background
            cs.setNonStrokingColor(badgeBg);
            cs.addRect(margin, y - badgeH + 8, pageWidth - 2 * margin, badgeH);
            cs.fill();
            // left accent bar
            cs.setNonStrokingColor(badgeBorder);
            cs.addRect(margin, y - badgeH + 8, 4f, badgeH);
            cs.fill();
            // text
            cs.beginText();
            cs.setFont(headingFont, 11);
            cs.setNonStrokingColor(badgeText);
            cs.newLineAtOffset(margin + 10, y - 8);
            cs.showText(eligible ? "STATUS:  ELIGIBLE FOR LOAN" : "STATUS:  NOT ELIGIBLE FOR LOAN");
            cs.endText();

            // ── Score + Loan details side-by-side ────────────────────────────
            y -= 34;
            float colW = (pageWidth - 2 * margin - 12) / 3f;

            drawInfoBox(cs, headingFont, bodyFont, margin, y - 36, colW, 42,
                    "ELIGIBILITY SCORE",
                    (result.getEligibilityScore() != null ? result.getEligibilityScore().toPlainString() : "0") + " / 100",
                    new Color(239, 246, 255), new Color(30, 64, 175));

            drawInfoBox(cs, headingFont, bodyFont, margin + colW + 6, y - 36, colW, 42,
                    "LOAN AMOUNT",
                    "Rs. " + String.format("%,.0f", result.getLoanAmount() != null ? result.getLoanAmount().doubleValue() : 0.0),
                    new Color(240, 253, 244), new Color(22, 101, 52));

            drawInfoBox(cs, headingFont, bodyFont, margin + 2 * (colW + 6), y - 36, colW, 42,
                    "LOAN PURPOSE",
                    safeText(result.getLoanPurpose()),
                    new Color(255, 251, 235), new Color(92, 65, 0));

            y -= 58;

            // ── Assessment summary ────────────────────────────────────────────
            y -= 6;
            writeSectionTitle(cs, headingFont, margin, y, "ASSESSMENT SUMMARY");
            y -= 14;
            y = drawWrappedText(cs, bodyFont, 10, safeText(result.getEligibilityMessage()),
                    margin, y, pageWidth - 2 * margin, 13);

            // ── Documents ────────────────────────────────────────────────────
            y -= 10;
            writeSectionTitle(cs, headingFont, margin, y, "DOCUMENTS REQUIRED");
            y -= 14;
            List<String> docs = result.getRequiredDocuments() == null ? List.of() : result.getRequiredDocuments();
            for (int i = 0; i < docs.size(); i++) {
                y = drawWrappedText(cs, bodyFont, 10, (i + 1) + ".  " + safeText(docs.get(i)),
                        margin + 4, y, pageWidth - 2 * margin - 4, 13);
                y -= 2;
            }

            // ── Special notes ─────────────────────────────────────────────────
            y -= 10;
            writeSectionTitle(cs, headingFont, margin, y, "IMPORTANT NOTES");
            y -= 14;
            List<String> notes = result.getSpecialNotes() == null ? List.of() : result.getSpecialNotes();
            for (String note : notes) {
                y = drawWrappedText(cs, bodyFont, 10, "•  " + safeText(note),
                        margin + 4, y, pageWidth - 2 * margin - 4, 13);
                y -= 2;
            }
            if (notes.isEmpty()) {
                for (String note : List.of(
                        "This is a PRELIMINARY eligibility check only. Final approval at bank's discretion.",
                        "This letter is valid for 30 days from the date of issue.",
                        "Please quote the reference number when visiting the branch.",
                        "Bring this printed document along with original ID and income proof.")) {
                    y = drawWrappedText(cs, bodyFont, 10, "•  " + note,
                            margin + 4, y, pageWidth - 2 * margin - 4, 13);
                    y -= 2;
                }
            }

            // ── Footer ────────────────────────────────────────────────────────
            float footerY = 42f;
            cs.setNonStrokingColor(new Color(30, 64, 175));
            cs.addRect(margin, footerY + 14, pageWidth - 2 * margin, 1.5f);
            cs.fill();

            writeLine(cs, bodyFont, 8, margin, footerY + 8, new Color(100, 116, 139),
                    "This is a system-generated document. No signature required.   "
                    + "Ref: " + safeText(result.getReferenceNumber()));
            writeLine(cs, bodyFont, 8, margin, footerY - 4, new Color(148, 163, 184),
                    "Bank Simulator  |  Loan Assessment Division  |  Valid 30 days from issue");
        }
    }

    // ─── Helpers ─────────────────────────────────────────────────────────────

    private void writeSectionTitle(PDPageContentStream cs, PDFont font, float x, float y, String title) throws IOException {
        cs.setNonStrokingColor(new Color(30, 64, 175));
        cs.addRect(x, y - 2, 3f, 14f);
        cs.fill();

        cs.beginText();
        cs.setFont(font, 11);
        cs.setNonStrokingColor(new Color(15, 23, 42));
        cs.newLineAtOffset(x + 8, y);
        cs.showText(title);
        cs.endText();
    }

    private void drawInfoBox(PDPageContentStream cs, PDFont headingFont, PDFont bodyFont,
                             float x, float y, float w, float h,
                             String label, String value,
                             Color bgColor, Color accentColor) throws IOException {
        // Background
        cs.setNonStrokingColor(bgColor);
        cs.addRect(x, y, w, h);
        cs.fill();

        // Top accent
        cs.setNonStrokingColor(accentColor);
        cs.addRect(x, y + h - 3, w, 3f);
        cs.fill();

        // Label
        cs.beginText();
        cs.setFont(headingFont, 7);
        cs.setNonStrokingColor(accentColor);
        cs.newLineAtOffset(x + 6, y + h - 14);
        cs.showText(label);
        cs.endText();

        // Value
        cs.beginText();
        cs.setFont(headingFont, 11);
        cs.setNonStrokingColor(new Color(15, 23, 42));
        cs.newLineAtOffset(x + 6, y + 8);
        cs.showText(safeText(value));
        cs.endText();
    }

    private void writeLine(PDPageContentStream cs, PDFont font, float fontSize,
                           float x, float y, Color color, String text) throws IOException {
        cs.beginText();
        cs.setFont(font, fontSize);
        cs.setNonStrokingColor(color);
        cs.newLineAtOffset(x, y);
        cs.showText(safeText(text));
        cs.endText();
    }

    private float drawWrappedText(PDPageContentStream cs, PDFont font, float fontSize,
                                  String text, float startX, float startY,
                                  float maxWidth, float lineHeight) throws IOException {
        List<String> lines = wrapText(font, fontSize, text, maxWidth);
        float y = startY;
        for (String line : lines) {
            cs.beginText();
            cs.setFont(font, fontSize);
            cs.setNonStrokingColor(new Color(51, 65, 85));
            cs.newLineAtOffset(startX, y);
            cs.showText(safeText(line));
            cs.endText();
            y -= lineHeight;
        }
        return y;
    }

    private List<String> wrapText(PDFont font, float fontSize, String text, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        if (text == null || text.isBlank()) {
            lines.add("");
            return lines;
        }
        String[] words = text.split("\\s+");
        StringBuilder current = new StringBuilder();
        for (String word : words) {
            String trial = current.isEmpty() ? word : current + " " + word;
            float width = font.getStringWidth(trial) / 1000f * fontSize;
            if (width <= maxWidth) {
                current = new StringBuilder(trial);
            } else {
                if (!current.isEmpty()) lines.add(current.toString());
                current = new StringBuilder(word);
            }
        }
        if (!current.isEmpty()) lines.add(current.toString());
        return lines;
    }

    private String safeText(String value) {
        return value == null ? "" : value.replace('\n', ' ').replace('\r', ' ').trim();
    }
}
