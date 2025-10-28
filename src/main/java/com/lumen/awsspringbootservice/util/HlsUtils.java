package com.lumen.awsspringbootservice.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.LinkedHashMap;
import java.util.Map;

public class HlsUtils {

    /**
     * Generates a valid HLS manifest ({@code .m3u8}) file content from
     * a map of video fragment URLs and their corresponding durations.
     * <p>
     * The generated manifest includes:
     * <ul>
     *   <li>{@code #EXTM3U} — playlist header</li>
     *   <li>{@code #EXT-X-VERSION:3} — HLS protocol version</li>
     *   <li>{@code #EXT-X-TARGETDURATION} — maximum rounded-up fragment duration</li>
     *   <li>{@code #EXTINF} lines for each fragment with its duration</li>
     *   <li>Fragment URLs in the correct playback order</li>
     *   <li>{@code #EXT-X-ENDLIST} — marker indicating the end of the playlist</li>
     * </ul>
     *
     * @param fragmentsDurations an ordered {@link Map} where each key is the fragment URL
     *                           and the value is the fragment duration in seconds (as String).
     *                           The map order determines playback order.
     * @return the generated manifest content as a UTF-8 encoded {@link String}.
     */
    public static String generateManifest(Map<String, String> fragmentsDurations) {
        StringBuilder m3u8 = new StringBuilder();

        long maxTargetDuration = fragmentsDurations.values().stream()
                .mapToDouble(Double::parseDouble)
                .mapToLong(duration -> (long) Math.ceil(duration))
                .max()
                .orElse(10);

        m3u8.append("#EXTM3U\n");
        m3u8.append("#EXT-X-VERSION:3\n");
        m3u8.append("#EXT-X-TARGETDURATION:").append(maxTargetDuration).append("\n");
        m3u8.append("#EXT-X-MEDIA-SEQUENCE:0\n");

        fragmentsDurations.forEach((url, durationStr) -> {
            m3u8.append("#EXTINF:").append(durationStr).append(",\n");
            m3u8.append(url).append("\n");
        });

        m3u8.append("#EXT-X-ENDLIST\n");

        return m3u8.toString();
    }

    /**
     * Parses the content of an existing HLS manifest ({@code .m3u8}) and extracts
     * the durations of each video fragment in playback order.
     * <p>
     * This method reads line by line and detects the pattern:
     * <pre>
     *     #EXTINF:&lt;duration&gt;,
     *     &lt;fragment URL&gt;
     * </pre>
     * It builds a {@link LinkedHashMap} where keys are sequential fragment indexes
     * (starting from 0), and values are duration strings in seconds.
     *
     * @param manifestContent the full textual content of an {@code .m3u8} manifest.
     * @return a {@link LinkedHashMap} mapping fragment indexes (0-based)
     * to their duration strings, preserving the original order.
     * @throws RuntimeException if the manifest cannot be parsed or read.
     */
    public static Map<Integer, String> parseManifest(String manifestContent) {
        Map<Integer, String> fragmentDurations = new LinkedHashMap<>();
        BufferedReader reader = new BufferedReader(new StringReader(manifestContent));
        String line;
        String currentDuration = null;

        try {
            int fragmentIndex = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();

                if (line.startsWith("#EXTINF:")) {
                    currentDuration = line.substring(8).replace(",", "").trim();

                } else if (!line.startsWith("#") && currentDuration != null) {
                    fragmentDurations.put(fragmentIndex, currentDuration);
                    currentDuration = null;
                    fragmentIndex++;
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse manifest", e);
        }

        return fragmentDurations;
    }
}
