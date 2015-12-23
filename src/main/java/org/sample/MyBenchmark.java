/*
 * Copyright (c) 2014, Oracle America, Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  * Redistributions of source code must retain the above copyright notice,
 *    this list of conditions and the following disclaimer.
 *
 *  * Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 *  * Neither the name of Oracle nor the names of its contributors may be used
 *    to endorse or promote products derived from this software without
 *    specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF
 * THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.sample;

/*import com.logentries.re2.RE2;
import com.logentries.re2.RegExprException;*/
import org.jcodings.specific.UTF8Encoding;
import org.joni.Regex;
import org.openjdk.jmh.annotations.Benchmark;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.airlift.slice.Slices;

import static java.nio.charset.StandardCharsets.UTF_8;
import static java.nio.file.Files.readAllLines;
import static org.joni.Option.DEFAULT;
import static org.joni.Option.NONE;

public class MyBenchmark
{

    private static final String[] LINES;
    private static final byte[][] LINES_UTF8;

    static {
        try {
            List<String> strings = readAllLines(new File("gutenberg-pg1661.txt").toPath());
            LINES = strings.toArray(new String[strings.size()]);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }

        LINES_UTF8 = new byte[LINES.length][];
        for (int i = 0; i < LINES.length; i++) {
            LINES_UTF8[i] = LINES[i].getBytes(UTF_8);
        }
    }

    private static final String[] SEARCH_PATTERNS1 = {
            "Sherlock",                                 // The literal string, “Sherlock”
            "^Sherlock",                                // “Sherlock” at the beginning of a line
            "Sherlock$",                                // “Sherlock” at the end of a line
            "a[^x]{20}b",                               // The letters “a” and “b”, separated by 20 other characters that aren’t “x”
            "Holmes|Watson",                            // Either of the strings “Holmes” or “Watson”
            ".{0,3}(Holmes|Watson)",                    // Zero to three characters, followed by either of the strings “Holmes” or “Watson”
            "[a-zA-Z]+ing",                             // Any word ending in “ing”
            "^([a-zA-Z]{0,4}ing)[^a-zA-Z]",             // Up to four letters followed by “ing” and then a non-letter, at the beginning of a line
            "[a-zA-Z]+ing$",                            // Any word ending in “ing”, at the end of a line
            "^[a-zA-Z ]{5,}$",                          // Lines consisting of five or more letters and spaces, only
            "^.{16,20}$",                               // Lines of between 16 and 20 characters
            "([a-f](.[d-m].){0,2}[h-n]){2}",            // Sequences of characters from certain sets (complex to explain!)
            "([A-Za-z]olmes|[A-Za-z]atson)[^a-zA-Z]",   // A word ending in “olmes” or “atson”, followed by a non-letter
            "Holmes.{10,60}Watson|Watson.{10,60}Holmes" // The names “Holmes” and “Watson” on the same line, separated by 10 to 60 other characters
    };

    private static final String[] SEARCH_PATTERNS2 = {
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson", // Either of the strings “Holmes” or “Watson”
            "Holmes|Watson"  // Either of the strings “Holmes” or “Watson”
    };

    private static final String[] SEARCH_PATTERNS3 = {
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes",
            "H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?H?Holmes"
    };

    @Benchmark
    public void joni_pattern1()
    {
        joni(SEARCH_PATTERNS1);
    }

    @Benchmark
    public void joni_pattern2()
    {
        joni(SEARCH_PATTERNS2);
    }

    @Benchmark
    public void joni_pattern3()
    {
        joni(SEARCH_PATTERNS3);
    }

    private void joni(String[] patterns)
    {
        for (String pattern : patterns) {
            byte[] patternBytes = pattern.getBytes();
            Regex regex = new Regex(patternBytes, 0, patternBytes.length, NONE, UTF8Encoding.INSTANCE);

            for (String lineString : LINES) {
                byte[] str = lineString.getBytes();
                org.joni.Matcher matcher = regex.matcher(str);
                matcher.search(0, str.length, DEFAULT);
            }
        }
    }

    /*@Benchmark
    public void re2j_pattern1()
    {
        re2j(SEARCH_PATTERNS1);
    }

    @Benchmark
    public void re2j_pattern2()
    {
        re2j(SEARCH_PATTERNS2);
    }

    @Benchmark
    public void re2j_pattern3()
    {
        re2j(SEARCH_PATTERNS3);
    }

    private void re2j(String[] patterns)
    {
        for (String pattern : patterns) {
            com.google.re2j.Pattern p = com.google.re2j.Pattern.compile(pattern);

            for (String lineString : LINES) {
                com.google.re2j.Matcher m = p.matcher(lineString);
                m.find();
            }
        }
    }*/

    @Benchmark
    public void re2j_slice_pattern1()
    {
        re2j_slice(SEARCH_PATTERNS1);
    }

    @Benchmark
    public void re2j_slice_pattern2()
    {
        re2j_slice(SEARCH_PATTERNS2);
    }

    @Benchmark
    public void re2j_slice_pattern3()
    {
        re2j_slice(SEARCH_PATTERNS3);
    }

    private void re2j_slice(String[] patterns)
    {
        for (String pattern : patterns) {
            com.google.re2j.Pattern p = com.google.re2j.Pattern.compile(pattern);

            for (byte[] lineBytes : LINES_UTF8) {
                com.google.re2j.Matcher m = p.matcher(Slices.wrappedBuffer(lineBytes));
                m.find();
            }
        }
    }

    /*
    //@Benchmark
    public void re2java_pattern1()
    {
        re2java(SEARCH_PATTERNS1);
    }

    //@Benchmark
    public void re2java_pattern2()
    {
        re2java(SEARCH_PATTERNS2);
    }

    //@Benchmark
    public void re2java_pattern3()
    {
        re2java(SEARCH_PATTERNS3);
    }

    private void re2java(String[] patterns)
    {
        try {
            for (String pattern : patterns) {
                RE2 re = new RE2(pattern);

                for (String lineString : LINES) {
                    re.partialMatch(lineString);
                }

                re.dispoze();
            }
        }
        catch (RegExprException exception) {
            exception.printStackTrace();
        }
    }*/

    @Benchmark
    public void defaultRegex_pattern1()
    {
        defaultRegex(SEARCH_PATTERNS1);
    }

    @Benchmark
    public void defaultRegex_pattern2()
    {
        defaultRegex(SEARCH_PATTERNS2);
    }

    @Benchmark
    public void defaultRegex_pattern3()
    {
        defaultRegex(SEARCH_PATTERNS3);
    }

    private void defaultRegex(String[] patterns)
    {
        for (String pattern : patterns) {
            Pattern p = Pattern.compile(pattern);
            for (String lineString : LINES) {
                Matcher m = p.matcher(lineString);
                m.find();
            }
        }
    }

    static public void main(String[] args)
    {
        MyBenchmark benchmark = new MyBenchmark();
        double startTime = System.currentTimeMillis();
        for (int i = 0; i < 20; ++i) {
            benchmark.re2j_slice_pattern1();
        }
        double endTime = System.currentTimeMillis();
        System.err.println("total time: " + (endTime - startTime) / 1000d);
    }
}



