package de.vorb.platon.web.mvc.avatars;

import com.google.common.collect.ImmutableList;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import javax.imageio.ImageIO;
import javax.servlet.http.HttpServletResponse;
import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.List;
import java.util.Random;

@Slf4j
@RestController
@RequiredArgsConstructor
public class AvatarController {

    private static final List<Color> COLORS = ImmutableList.of(
            new Color(0xe24e42), new Color(0xe9b000), new Color(0xeb6e80), new Color(0x008f95));

    @GetMapping(value = "/avatars/{hash}", produces = MediaType.IMAGE_PNG_VALUE)
    public void getAvatar(@PathVariable("hash") String hash, HttpServletResponse response) throws IOException {

        final Random random = new Random(hash.hashCode());

        final BufferedImage image = new BufferedImage(64, 64, BufferedImage.TYPE_INT_RGB);

        final int colorIndex = random.nextInt(COLORS.size());
        final Color backgroundColor = COLORS.get(colorIndex);
        final Color foregroundColor = COLORS.get((colorIndex + 1) % COLORS.size());

        final Graphics2D g2d = image.createGraphics();
        g2d.setColor(backgroundColor);
        g2d.fillRect(0, 0, 64, 64);

        g2d.setColor(foregroundColor);
        if (random.nextBoolean()) {
            g2d.fillRect(5, 5, 18, 18);
        }
        if (random.nextBoolean()) {
            g2d.fillRect(23, 5, 18, 18);
        }
        if (random.nextBoolean()) {
            g2d.fillRect(41, 5, 18, 18);
        }
        if (random.nextBoolean()) {
            g2d.fillRect(5, 23, 18, 18);
        }
        if (random.nextBoolean()) {
            g2d.fillRect(23, 23, 18, 18);
        }
        if (random.nextBoolean()) {
            g2d.fillRect(41, 23, 18, 18);
        }
        if (random.nextBoolean()) {
            g2d.fillRect(5, 41, 18, 18);
        }
        if (random.nextBoolean()) {
            g2d.fillRect(23, 41, 18, 18);
        }
        if (random.nextBoolean()) {
            g2d.fillRect(41, 41, 18, 18);
        }

        g2d.dispose();

        ImageIO.write(image, "PNG", response.getOutputStream());
    }

}
