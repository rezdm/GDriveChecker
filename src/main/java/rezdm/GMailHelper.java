package rezdm;

import com.google.api.client.repackaged.org.apache.commons.codec.binary.Base64;
import com.google.api.services.gmail.Gmail;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Collection;
import java.util.Properties;

class GMailHelper {
    private static MimeMessage CreateEmail(String from, Collection<String> recipients, String subject, String body) throws MessagingException {
        final Properties props = new Properties();
        final Session session = Session.getDefaultInstance(props, null);
        final MimeMessage email = new MimeMessage(session);
        email.setFrom(new InternetAddress(from));
        recipients.forEach((recipient) -> {
            try {
                email.addRecipient(Message.RecipientType.BCC, new InternetAddress(recipient));
            } catch (MessagingException ex) {
                throw new RuntimeException(ex);
            }
        });

        email.setSubject(subject);
        email.setText(body);
        return email;
    }

    private static com.google.api.services.gmail.model.Message CreateMessageWithEmail(MimeMessage email) throws MessagingException, IOException {
        final ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        email.writeTo(bytes);
        final String encodedEmail = Base64.encodeBase64URLSafeString(bytes.toByteArray());
        final com.google.api.services.gmail.model.Message message = new com.google.api.services.gmail.model.Message();
        message.setRaw(encodedEmail);
        return message;
    }

    public static void Send(Gmail service, String from, Collection<String> to, String subject, String body) throws MessagingException, IOException {
        final MimeMessage email = CreateEmail(from, to, subject, body);
        final com.google.api.services.gmail.model.Message message = CreateMessageWithEmail(email);
        //String userId = "me";
        service.users().messages().send("me", message).execute();
    }

    private GMailHelper() {
    }

}
