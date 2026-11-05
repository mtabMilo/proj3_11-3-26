import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Starter test suite for Project 3 (Huffman Encoding/Decoding).
 * Write more tests!
 * These tests assume the test files are in the same directory as HuffmanTest.java
 */
public class HuffmanTest {

    @Test
    public void testCntFreq() throws IOException {
        System.out.println(new java.io.File("file2.txt").getAbsolutePath());
        System.out.println(new java.io.File("file2.txt").exists());
        int[] freqList = Huffman.cntFreq("file2.txt");
        int[] ansList = {2, 4, 8, 16, 0, 2, 0}; // counts for ascii 97..103
        for (int i = 0; i < ansList.length; i++) {
            assertEquals(ansList[i], freqList[97 + i], "mismatch at ascii " + (97 + i));
        }
    }

    @Test
    public void testComesBefore() {
        HuffmanNode d = new HuffmanNode('d', 5);
        HuffmanNode k = new HuffmanNode('k', 5);
        assertTrue(HuffmanNode.comesBefore(d, k));
        assertFalse(HuffmanNode.comesBefore(k, d));
    }

    @Test
    public void testCreateHeader() throws IOException {
        int[] freqList = Huffman.cntFreq("file2.txt");
        assertEquals("97 2 98 4 99 8 100 16 102 2", Huffman.createHeader(freqList));
    }

    @Test
    public void test01Textfile() throws IOException {
        Huffman.huffmanEncode("file1.txt", "file1_out.txt");
        assertTrue(compareFiles("file1_out.txt", "file1_soln.txt"));
    }

    /**
     * Compares two files line by line, trimming trailing CR/LF so the
     * comparison works the same on Windows and Unix-style line endings.
     */
    private static boolean compareFiles(String file1, String file2) throws IOException {
        try (BufferedReader r1 = new BufferedReader(new FileReader(file1));
             BufferedReader r2 = new BufferedReader(new FileReader(file2))) {
            String line1, line2;
            while (true) {
                line1 = r1.readLine();
                line2 = r2.readLine();
                if (line1 == null && line2 == null) {
                    return true;
                }
                if (line1 == null || line2 == null) {
                    return false;
                }
                if (!line1.trim().equals(line2.trim())) {
                    return false;
                }
            }
        }
    }

    @TempDir
    Path tmp;

    // "aaabbbbcc": a=3, b=4, c=2  ->  b="0", c="10", a="11"
    private static final String TEXT = "aaabbbbcc";
    private static final String HEADER = "97 3 98 4 99 2";
    private static final String BITS = "11111100001010";

    private HuffmanNode leaf(int ascii, int freq) {
        return new HuffmanNode(ascii, freq);
    }

    private Path write(String name, String content) throws IOException {
        Path p = tmp.resolve(name);
        Files.writeString(p, content);
        return p;
    }

    private String read(Path p) throws IOException {
        return Files.readString(p);
    }

    @Test
    void isLeaf()
    {
        HuffmanNode l = leaf(97, 1);
        assertTrue(l.isLeaf());
        assertFalse(HuffmanNode.combine(l, leaf(98, 1)).isLeaf());
    }

    @Test
    void comesBefore()
    {
        assertTrue(HuffmanNode.comesBefore(leaf(200, 1), leaf(97, 2)));
        assertFalse(HuffmanNode.comesBefore(leaf(97, 2), leaf(200, 1)));
        assertTrue(HuffmanNode.comesBefore(leaf(97, 3), leaf(98, 3)));
        assertFalse(HuffmanNode.comesBefore(leaf(98, 3), leaf(97, 3)));
        assertFalse(HuffmanNode.comesBefore(leaf(97, 3), leaf(97, 3)));
    }

    @Test
    void compareTo()
    {
        assertTrue(leaf(97, 1).compareTo(leaf(98, 2)) < 0);
        assertTrue(leaf(98, 2).compareTo(leaf(97, 1)) > 0);
        assertEquals(0, leaf(97, 2).compareTo(leaf(97, 2)));
    }

    @Test
    void combine()
    {
        HuffmanNode small = leaf(99, 2);
        HuffmanNode big = leaf(97, 3);

        HuffmanNode p1 = HuffmanNode.combine(small, big);
        assertSame(small, p1.left());
        assertSame(big, p1.right());
        assertEquals(5, p1.freq());
        assertEquals(97, p1.charAscii());

        HuffmanNode p2 = HuffmanNode.combine(big, small);
        assertSame(small, p2.left());
        assertSame(big, p2.right());

        HuffmanNode p3 = HuffmanNode.combine(leaf(98, 4), leaf(97, 4));
        assertEquals(97, p3.left().charAscii());
        assertEquals(98, p3.right().charAscii());
    }

    @Test
    void addNode()
    {
        List<HuffmanNode> list = new ArrayList<>();
        Huffman.addNode(list, leaf(97, 5));
        Huffman.addNode(list, leaf(98, 1));
        Huffman.addNode(list, leaf(99, 3));
        Huffman.addNode(list, leaf(100, 3));
        assertEquals(4, list.size());
        assertEquals(98, list.get(0).charAscii());
        assertEquals(99, list.get(1).charAscii());
        assertEquals(100, list.get(2).charAscii());
        assertEquals(97, list.get(3).charAscii());

        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        Huffman.addNode(pq, leaf(97, 5));
        Huffman.addNode(pq, leaf(98, 1));
        assertEquals(2, pq.size());
        assertEquals(98, pq.peek().charAscii());

        Collection<HuffmanNode> set = new HashSet<>();
        Huffman.addNode(set, leaf(97, 5));
        assertEquals(1, set.size());
    }

    @Test
    void removeMin()
    {
        PriorityQueue<HuffmanNode> pq = new PriorityQueue<>();
        pq.add(leaf(97, 5));
        pq.add(leaf(98, 1));
        pq.add(leaf(99, 3));
        assertEquals(98, Huffman.removeMin(pq).charAscii());
        assertEquals(99, Huffman.removeMin(pq).charAscii());
        assertEquals(97, Huffman.removeMin(pq).charAscii());
        assertNull(Huffman.removeMin(pq));

        List<HuffmanNode> list = new ArrayList<>();
        list.add(leaf(97, 5));
        list.add(leaf(98, 1));
        list.add(leaf(99, 3));
        HuffmanNode min = Huffman.removeMin(list);
        assertEquals(98, min.charAscii());
        assertEquals(2, list.size());
        assertFalse(list.contains(min));

        Collection<HuffmanNode> tie = new ArrayDeque<>();
        tie.add(leaf(100, 2));
        tie.add(leaf(98, 2));
        assertEquals(98, Huffman.removeMin(tie).charAscii());

        assertNull(Huffman.removeMin(new ArrayList<>()));
    }

    @Test
    void createHuffTree()
    {
        assertNull(Huffman.createHuffTree(new int[Huffman.NUM_CHARS]));

        int[] one = new int[Huffman.NUM_CHARS];
        one[97] = 5;
        HuffmanNode single = Huffman.createHuffTree(one);
        assertTrue(single.isLeaf());
        assertEquals(97, single.charAscii());
        assertEquals(5, single.freq());

        HuffmanNode root = Huffman.createHuffTree(Huffman.parseHeader(HEADER));
        assertEquals(9, root.freq());
        assertEquals(98, root.left().charAscii());
        assertEquals(99, root.right().left().charAscii());
        assertEquals(97, root.right().right().charAscii());
    }

    @Test
    void createCode()
    {
        for (String s : Huffman.createCode(null)) {
            assertEquals("", s);
        }
        String[] leafCodes = Huffman.createCode(leaf(97, 5));
        assertEquals(Huffman.NUM_CHARS, leafCodes.length);
        for (String s : leafCodes) {
            assertEquals("", s);
        }

        String[] codes = Huffman.createCode(
                Huffman.createHuffTree(Huffman.parseHeader(HEADER)));
        assertEquals(Huffman.NUM_CHARS, codes.length);
        assertEquals("0", codes['b']);
        assertEquals("10", codes['c']);
        assertEquals("11", codes['a']);
        assertEquals("", codes['z']);
    }

    @Test
    void cntFreq() throws IOException
    {
        int[] empty = Huffman.cntFreq(write("empty.txt", "").toString());
        assertEquals(Huffman.NUM_CHARS, empty.length);
        for (int f : empty) {
            assertEquals(0, f);
        }

        int[] f = Huffman.cntFreq(
                write("ex.txt", "ddddddddddddddddccccccccbbbbaaff").toString());
        assertArrayEquals(new int[] {2, 4, 8, 16, 0, 2, 0},
                Arrays.copyOfRange(f, 97, 104));

        int[] nl = Huffman.cntFreq(write("nl.txt", "a\n").toString());
        assertEquals(1, nl['a']);
        assertEquals(1, nl['\n']);
    }

    @Test
    void createHeader()
    {
        assertEquals("", Huffman.createHeader(new int[Huffman.NUM_CHARS]));

        int[] one = new int[Huffman.NUM_CHARS];
        one[97] = 5;
        assertEquals("97 5", Huffman.createHeader(one));

        int[] f = new int[Huffman.NUM_CHARS];
        f[99] = 2;
        f[97] = 3;
        f[98] = 4;
        assertEquals(HEADER, Huffman.createHeader(f));
    }

    @Test
    void parseHeader()
    {
        for (String h : new String[] {null, "", "   "}) {
            int[] f = Huffman.parseHeader(h);
            assertEquals(Huffman.NUM_CHARS, f.length);
            for (int v : f) {
                assertEquals(0, v);
            }
        }

        int[] f = Huffman.parseHeader(HEADER);
        assertEquals(3, f[97]);
        assertEquals(4, f[98]);
        assertEquals(2, f[99]);
        assertEquals(0, f[100]);

        int[] g = Huffman.parseHeader("  97 3   98 4  ");
        assertEquals(3, g[97]);
        assertEquals(4, g[98]);

        // round trip
        assertArrayEquals(f, Huffman.parseHeader(Huffman.createHeader(f)));
    }

    @Test
    void huffmanEncode() throws IOException
    {
        Path emptyOut = tmp.resolve("empty_out.txt");
        Huffman.huffmanEncode(write("empty_in.txt", "").toString(), emptyOut.toString());
        assertEquals("", read(emptyOut));

        Path oneOut = tmp.resolve("one_out.txt");
        Huffman.huffmanEncode(write("one_in.txt", "aaaaa").toString(), oneOut.toString());
        assertEquals("97 5\n", read(oneOut));

        Path in = write("in.txt", TEXT);
        Path out = tmp.resolve("out.txt");
        Huffman.huffmanEncode(in.toString(), out.toString());
        assertArrayEquals(
                (HEADER + "\n" + BITS).getBytes(StandardCharsets.ISO_8859_1),
                Files.readAllBytes(out));

        Path existing = write("existing.txt", "old old old old old old old old");
        Huffman.huffmanEncode(in.toString(), existing.toString());
        assertEquals(HEADER + "\n" + BITS, read(existing));
    }

    @Test
    void huffmanDecode() throws IOException
    {
        Path never = tmp.resolve("never.txt");
        assertThrows(FileNotFoundException.class, () ->
                Huffman.huffmanDecode(tmp.resolve("missing.txt").toString(), never.toString()));
        assertFalse(Files.exists(never));

        Path emptyDec = tmp.resolve("empty_dec.txt");
        Huffman.huffmanDecode(write("empty_enc.txt", "").toString(), emptyDec.toString());
        assertEquals("", read(emptyDec));

        Path blankDec = tmp.resolve("blank_dec.txt");
        Huffman.huffmanDecode(write("blank_enc.txt", "\n").toString(), blankDec.toString());
        assertEquals("", read(blankDec));

        Path oneDec = tmp.resolve("one_dec.txt");
        Huffman.huffmanDecode(write("one_enc.txt", "97 5\n").toString(), oneDec.toString());
        assertEquals("aaaaa", read(oneDec));

        Path enc = write("enc.txt", HEADER + "\n" + BITS);
        Path dec = tmp.resolve("dec.txt");
        Huffman.huffmanDecode(enc.toString(), dec.toString());
        assertEquals(TEXT, read(dec));

        Path encNl = write("enc_nl.txt", HEADER + "\n" + BITS + "\n");
        Path decNl = tmp.resolve("dec_nl.txt");
        Huffman.huffmanDecode(encNl.toString(), decNl.toString());
        assertEquals(TEXT, read(decNl));

        Path existing = write("existing_dec.txt", "old old old old old old old old");
        Huffman.huffmanDecode(enc.toString(), existing.toString());
        assertEquals(TEXT, read(existing));

        String text = "ddddddddddddddddccccccccbbbbaaff";
        Path rtEnc = tmp.resolve("rt_enc.txt");
        Path rtOut = tmp.resolve("rt_out.txt");
        Huffman.huffmanEncode(write("rt_in.txt", text).toString(), rtEnc.toString());
        Huffman.huffmanDecode(rtEnc.toString(), rtOut.toString());
        assertEquals(text, read(rtOut));
    }
}
