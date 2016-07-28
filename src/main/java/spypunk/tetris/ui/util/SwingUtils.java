package spypunk.tetris.ui.util;

import java.awt.Graphics2D;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.font.FontRenderContext;
import java.awt.font.GlyphVector;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SwingUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(SwingUtils.class);

    private SwingUtils() {
        throw new IllegalAccessError();
    }

    public static void doInAWTThread(Runnable runnable) {
        doInAWTThread(runnable, false);
    }

    public static void doInAWTThread(Runnable runnable, boolean wait) {
        if (wait) {
            try {
                SwingUtilities.invokeAndWait(runnable);
            } catch (final InvocationTargetException e) {
                LOGGER.error(e.getMessage(), e);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                LOGGER.error(e.getMessage(), e);
            }
        } else {
            SwingUtilities.invokeLater(runnable);
        }
    }

    public static Point getCenteredTextLocation(Graphics2D graphics, String text, Rectangle rectangle) {
        FontRenderContext frc = graphics.getFontRenderContext();
        GlyphVector gv = graphics.getFont().createGlyphVector(frc, text);
        Rectangle textBounds = gv.getPixelBounds(null, 0, 0);

        int x = rectangle.x + (rectangle.width - textBounds.width) / 2;
        int y = rectangle.y + (rectangle.height + textBounds.height) / 2;

        return new Point(x, y);
    }
}
