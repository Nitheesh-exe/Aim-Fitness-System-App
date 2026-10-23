package Main;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

/**
 * Service to dispatch automated membership expiry reminders via Meta WhatsApp Cloud API.
 * Credentials can be supplied via environment variables (AIM_WHATSAPP_PHONE_ID, AIM_WHATSAPP_TOKEN,
 * AIM_WHATSAPP_TEMPLATE) or fallback to configured defaults.
 */
public class WhatsAppService {

    // Default Fallbacks from Meta Developer Console
    private static final String DEFAULT_PHONE_NUMBER_ID = "1231447643392765";
    private static final String DEFAULT_ACCESS_TOKEN = "EAAPZATBDkfkgBSSZC57uH9A4RgbIKYMKVDKmy3vBjM6ctdy9UU2AEacqOrwoPzQNVaEIreqnhNZCEm6bT4mbQ2ChwHs64OmLcaWo5QEMOmZAiMZCGoLRboFCTc4KgfNy9IDzgsnLn77sZAcCFgLPrieqynYfYVol7Jmxxq2SPoCU7NfyrpJY7mSo2bLPZBr5gZDZD";
    private static final String DEFAULT_TEMPLATE_NAME = "gym_remainder";

    // Shared singleton HTTP Client with connection timeout
    private static final HttpClient HTTP_CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(10))
            .build();

    public static String getPhoneNumberId() {
        String env = System.getenv("AIM_WHATSAPP_PHONE_ID");
        return (env != null && !env.isBlank()) ? env.trim() : DEFAULT_PHONE_NUMBER_ID;
    }

    public static String getAccessToken() {
        String env = System.getenv("AIM_WHATSAPP_TOKEN");
        return (env != null && !env.isBlank()) ? env.trim() : DEFAULT_ACCESS_TOKEN;
    }

    public static String getTemplateName() {
        String env = System.getenv("AIM_WHATSAPP_TEMPLATE");
        return (env != null && !env.isBlank()) ? env.trim() : DEFAULT_TEMPLATE_NAME;
    }

    /**
     * Sends an automated WhatsApp expiry template message to a member.
     *
     * @param recipientPhone Member phone number (e.g. "9876543210")
     * @param memberName     Member full name
     * @param expiryDate     Formatted expiry date string (e.g. "2026-10-30")
     * @return true if Meta API returned HTTP 200, false otherwise
     */
    public static boolean sendReminder(String recipientPhone, String memberName, String expiryDate) {
        if (recipientPhone == null || recipientPhone.isBlank()) {
            System.out.println("⚠️ Cannot send WhatsApp reminder: recipient phone is empty.");
            return false;
        }

        try {
            // 1. Sanitize phone number (strip non-digits and normalize to +91 country code for India)
            String cleanPhone = recipientPhone.replaceAll("[^0-9]", "");
            if (cleanPhone.length() == 10) {
                cleanPhone = "91" + cleanPhone;
            }

            // 2. Build the API endpoint
            String phoneId = getPhoneNumberId();
            String token = getAccessToken();
            String templateName = getTemplateName();
            String url = "https://graph.facebook.com/v20.0/" + phoneId + "/messages";

            // 3. Construct JSON template payload with escaped strings
            String safeName = escapeJson(memberName);
            String safeExpiry = escapeJson(expiryDate);

            String jsonPayload = "{"
                + "\"messaging_product\": \"whatsapp\","
                + "\"to\": \"" + cleanPhone + "\","
                + "\"type\": \"template\","
                + "\"template\": {"
                + "  \"name\": \"" + templateName + "\","
                + "  \"language\": { \"code\": \"en\" },"
                + "  \"components\": ["
                + "    {"
                + "      \"type\": \"body\","
                + "      \"parameters\": ["
                + "        { \"type\": \"text\", \"text\": \"" + safeName + "\" },"
                + "        { \"type\": \"text\", \"text\": \"" + safeExpiry + "\" }"
                + "      ]"
                + "    }"
                + "  ]"
                + "}"
                + "}";

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .header("Authorization", "Bearer " + token.trim())
                    .header("Content-Type", "application/json")
                    .timeout(Duration.ofSeconds(15))
                    .POST(HttpRequest.BodyPublishers.ofString(jsonPayload))
                    .build();

            HttpResponse<String> response = HTTP_CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 200) {
                System.out.println("✅ WhatsApp reminder sent to " + cleanPhone);
                return true;
            } else {
                System.out.println("❌ WhatsApp API Error (" + response.statusCode() + "): " + response.body());
                return false;
            }

        } catch (Exception e) {
            System.out.println("❌ Network error sending WhatsApp: " + e.getMessage());
            return false;
        }
    }

    /**
     * Opens the standard WhatsApp Web / app chat link in the system default browser.
     */
    public static void openWhatsAppChat(String recipientPhone, String message) {
        if (recipientPhone == null || recipientPhone.isBlank()) {
            return;
        }
        try {
            String cleanPhone = recipientPhone.replaceAll("[^0-9]", "");
            if (cleanPhone.length() == 10) {
                cleanPhone = "91" + cleanPhone;
            }
            String url = "https://wa.me/" + cleanPhone
                    + (message != null && !message.isBlank()
                    ? "?text=" + java.net.URLEncoder.encode(message, java.nio.charset.StandardCharsets.UTF_8)
                    : "");
            if (java.awt.Desktop.isDesktopSupported() && java.awt.Desktop.getDesktop().isSupported(java.awt.Desktop.Action.BROWSE)) {
                java.awt.Desktop.getDesktop().browse(new java.net.URI(url));
            }
        } catch (Exception e) {
            System.err.println("Could not open WhatsApp URL: " + e.getMessage());
        }
    }

    private static String escapeJson(String raw) {
        if (raw == null) {
            return "";
        }
        return raw.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
