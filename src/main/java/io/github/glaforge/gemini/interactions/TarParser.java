/*
 * Copyright 2026 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.github.glaforge.gemini.interactions;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

/**
 * A simple, dependency-free USTAR-compatible TAR scanner.
 * <p>
 * This class reads the 512-byte headers of a TAR archive to build a lightweight
 * index of file paths mapped to their byte offsets and sizes within the TAR
 * file,
 * without eagerly loading the contents of all files into memory.
 * </p>
 */
class TarParser {

    /**
     * Metadata representing a file entry within a TAR archive.
     *
     * @param offset The byte offset where the file contents start in the TAR.
     * @param size   The size of the file in bytes.
     */
    public record TarEntryMetadata(long offset, long size) {
    }

    private TarParser() {
    }

    /**
     * Scans a TAR file and returns a map of relative file paths to their TAR entry
     * metadata.
     *
     * @param tarPath The path to the local TAR archive.
     * @return A map of relative file paths to their offset and size metadata.
     * @throws IOException If an I/O error occurs while reading the TAR file.
     */
    public static Map<String, TarEntryMetadata> scanTar(Path tarPath) throws IOException {
        Map<String, TarEntryMetadata> index = new HashMap<>();
        try (InputStream in = Files.newInputStream(tarPath)) {
            long currentOffset = 0;
            byte[] header = new byte[512];

            while (true) {
                int read = readFully(in, header);
                if (read < 512) {
                    break;
                }
                currentOffset += 512;

                // Check for end of archive (a block of all zeros)
                boolean allZeros = true;
                for (int i = 0; i < 512; i++) {
                    if (header[i] != 0) {
                        allZeros = false;
                        break;
                    }
                }
                if (allZeros) {
                    break;
                }

                // File name (100 bytes starting at offset 0)
                String name = readNullTerminatedString(header, 0, 100);
                if (name.isEmpty()) {
                    break;
                }

                // File size (12 bytes octal string starting at offset 124)
                String sizeStr = readNullTerminatedString(header, 124, 12).trim();
                long size = 0;
                if (!sizeStr.isEmpty()) {
                    try {
                        size = Long.parseLong(sizeStr, 8);
                    } catch (NumberFormatException e) {
                        // Ignore parse failures, treat as 0
                    }
                }

                // Type flag (1 byte at offset 156)
                byte typeFlag = header[156];

                // Filename prefix (155 bytes starting at offset 345, used for paths longer than
                // 100 chars in USTAR)
                String prefix = readNullTerminatedString(header, 345, 155);
                String fullName = name;
                if (!prefix.isEmpty()) {
                    fullName = prefix + "/" + name;
                }

                // We only index regular files ('0' or null/0 type indicator)
                if (typeFlag == '0' || typeFlag == 0) {
                    index.put(fullName, new TarEntryMetadata(currentOffset, size));
                }

                // Calculate the block-aligned offset advancement
                long numBlocks = (size + 511) / 512;
                long skipBytes = numBlocks * 512;

                // Skip the content blocks to advance to the next header
                long skipped = in.skip(skipBytes);
                while (skipped < skipBytes) {
                    long s = in.skip(skipBytes - skipped);
                    if (s <= 0) {
                        break; // EOF or error
                    }
                    skipped += s;
                }
                currentOffset += skipBytes;
            }
        }
        return index;
    }

    private static int readFully(InputStream in, byte[] b) throws IOException {
        int total = 0;
        while (total < b.length) {
            int count = in.read(b, total, b.length - total);
            if (count < 0) {
                break;
            }
            total += count;
        }
        return total;
    }

    private static String readNullTerminatedString(byte[] bytes, int offset, int length) {
        int len = 0;
        while (len < length && bytes[offset + len] != 0) {
            len++;
        }
        return new String(bytes, offset, len, StandardCharsets.UTF_8);
    }
}
