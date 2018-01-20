package edu.usf.myweb.jcameron2;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.HttpClientBuilder;
import sx.blah.discord.api.events.IListener;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.util.MessageBuilder;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class MessageListener implements IListener<MessageReceivedEvent> {

    //private static final Pattern IMAGE_URL_PATTERN = Pattern.compile("(http(s?):/)(/[^/]+)+\\.(?:jpg|gif|png)");

    private final AjitPaiBot ajitPaiBot;
    private final HttpClient httpClient = HttpClientBuilder.create().setUserAgent("AjitPai-Discord-Bot").build();

    public MessageListener(AjitPaiBot ajitPaiBot) {
        this.ajitPaiBot = ajitPaiBot;
    }

    @Override
    public void handle(MessageReceivedEvent event) {

        if (event.getMessage().isDeleted())
            return;

        //TODO Image urls

		/*for(String phrase : event.getMessage().getContent().split("\\s+")){
			if(IMAGE_URL_PATTERN.matcher(phrase).matches()){
			}
		}*/

        if (event.getMessage().getAttachments().isEmpty())
            return;

        List<BufferedImage> images = new ArrayList<>();

        for (IMessage.Attachment attachment : event.getMessage().getAttachments()) {

            BufferedImage image;

            try {

                System.out.println("Reading " + attachment.getUrl());

                HttpGet request = new HttpGet(attachment.getUrl());

                HttpResponse response = this.httpClient.execute(request);

                System.out.println("Response Code : " + response.getStatusLine().getStatusCode());

                InputStream in = response.getEntity().getContent();

                image = ImageIO.read(in);

                in.close();

                if (image == null)
                    throw new IOException("Image read is null");

                System.out.println("Image read successfully");

                images.add(image);

            } catch (IOException e) {
                System.out.println("Error: " + e.getMessage());
                //Not an image
                continue;
            }

        }

        if (images.isEmpty())
            return;

        event.getMessage().delete();

        MessageBuilder builder = new MessageBuilder(event.getMessage().getClient());

        builder.withContent(event.getMessage().getAuthor().mention());

        int count = 1;

        for (BufferedImage image : images) {

            BufferedImage blurred = resize(image, 600, 540);

            for (int i = 0; i < 150; i++)
                blurred = transposedHBlur(blurred);

            Graphics g = blurred.getGraphics();

            Font font = new Font(g.getFont().getName(), Font.BOLD, 40);

            g.setFont(font);
            g.setColor(Color.RED.brighter());

            drawWrappedString(g, "Please buy the \"Discord Image Sharing\" package.", 20, (blurred.getHeight() / 2) - g.getFontMetrics().getHeight(), blurred.getWidth() - 20);

            ByteArrayOutputStream outStream = new ByteArrayOutputStream();

            try {

                ImageIO.write(blurred, "jpg", outStream);
                outStream.close();

                byte[] imgData = outStream.toByteArray();

                ByteArrayInputStream in = new ByteArrayInputStream(imgData);

                builder.withFile(in, count + ".jpg");

                in.close();

            } catch (IOException e) {
                e.printStackTrace();
            }

            g.dispose();

            count++;

        }

        builder.withChannel(event.getChannel());
        builder.send();

    }

    private static BufferedImage transposedHBlur(BufferedImage im) {
        int height = im.getHeight();
        int width = im.getWidth();
        // result is transposed, so the width/height are swapped
        BufferedImage temp = new BufferedImage(height, width, BufferedImage.TYPE_INT_RGB);
        float[] k = new float[]{0.00598f, 0.060626f, 0.241843f, 0.383103f, 0.241843f, 0.060626f, 0.00598f};
        // horizontal blur, transpose result
        for (int y = 0; y < height; y++) {
            for (int x = 3; x < width - 3; x++) {
                float r = 0, g = 0, b = 0;
                for (int i = 0; i < 7; i++) {
                    int pixel = im.getRGB(x + i - 3, y);
                    b += (pixel & 0xFF) * k[i];
                    g += ((pixel >> 8) & 0xFF) * k[i];
                    r += ((pixel >> 16) & 0xFF) * k[i];
                }
                int p = (int) b + ((int) g << 8) + ((int) r << 16);
                // transpose result!
                temp.setRGB(y, x, p);
            }
        }
        return temp;
    }

    private static BufferedImage resize(BufferedImage img, int newW, int newH) {
        Image tmp = img.getScaledInstance(newW, newH, Image.SCALE_SMOOTH);
        BufferedImage dimg = new BufferedImage(newW, newH, BufferedImage.TYPE_INT_ARGB);

        Graphics2D g2d = dimg.createGraphics();
        g2d.drawImage(tmp, 0, 0, null);
        g2d.dispose();

        return dimg;
    }

    private static void drawWrappedString(Graphics g, String s, int x, int y, int width) {
        // FontMetrics gives us information about the width,
        // height, etc. of the current Graphics object's Font.
        FontMetrics fm = g.getFontMetrics();

        int lineHeight = fm.getHeight();

        int curX = x;
        int curY = y;

        String[] words = s.split(" ");

        for (String word : words) {
            // Find out thw width of the word.
            int wordWidth = fm.stringWidth(word + " ");

            // If text exceeds the width, then move to next line.
            if (curX + wordWidth >= x + width) {
                curY += lineHeight;
                curX = x;
            }

            g.drawString(word, curX, curY);

            // Move over to the right for next word.
            curX += wordWidth;
        }
    }

}
