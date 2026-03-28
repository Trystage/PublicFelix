package win.trystage.felix.client.core;

import net.minecraft.client.MinecraftClient;
import net.minecraft.resource.Resource;
import net.minecraft.util.Identifier;

import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public final class RawSound {
    private static final ExecutorService EXECUTOR = Executors.newCachedThreadPool(r -> {
        Thread t = new Thread(r, "Felix-RawSound");
        t.setDaemon(true);
        return t;
    });
    private RawSound() {}
    public static void playFelix(String path, float volume) {
        play("felix", path, volume);
    }
    public static void play(String namespace, String path, float volume) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getResourceManager() == null) return;
        String normalized = path == null ? "" : path.toLowerCase(java.util.Locale.ROOT);
        Identifier id = new Identifier(namespace, normalized);
        Optional<Resource> opt = mc.getResourceManager().getResource(id);
        if (opt.isEmpty()) {
            System.err.println("Missing resource: " + id);
            return;
        }
        EXECUTOR.execute(() -> {
            try (InputStream in = new BufferedInputStream(opt.get().getInputStream());
                 AudioInputStream audioIn = AudioSystem.getAudioInputStream(in)) {
                Clip clip = AudioSystem.getClip();
                clip.open(audioIn);
                applyVolume(clip, volume);
                clip.addLineListener(event -> {
                    if (event.getType() == LineEvent.Type.STOP) {
                        clip.close();
                    }
                });
                clip.start();
            } catch (UnsupportedAudioFileException | IOException | LineUnavailableException e) {
                e.printStackTrace();
            }
        });
    }
    private static void applyVolume(Clip clip, float volume) {
        try {
            FloatControl gain = (FloatControl) clip.getControl(FloatControl.Type.MASTER_GAIN);
            float clamped = Math.max(0f, Math.min(1f, volume));
            float range = gain.getMaximum() - gain.getMinimum();
            float gainValue = (range * clamped) + gain.getMinimum();
            gain.setValue(gainValue);
        } catch (IllegalArgumentException ignored) {
        }
    }
}