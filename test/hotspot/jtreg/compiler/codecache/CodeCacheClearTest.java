/*
 * Copyright (c) 2022, 2022, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * @test
 * @summary test for the value of full_count in the message of insufficient codecache
 * @library /test/lib
 * @run main/othervm -Xbootclasspath/a:. compiler.codecache.CodeCacheClearTest
 */

package compiler.codecache;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import jdk.test.lib.process.OutputAnalyzer;
import jdk.test.lib.process.ProcessTools;

public class CodeCacheClearTest {
    /**
    * Check the result of segmented code cache related VM options.
    */
    public static void main(String[] args) throws Exception {
        runTest();
    }

    public static void runTest() throws Exception {
        ProcessBuilder pb;

        // Enable PrintCodeHeapClear option to print code heap clear details
        pb = ProcessTools.createJavaProcessBuilder("-XX:+PrintCodeHeapClear",
                                                   "-version");
        OutputAnalyzer oa = ProcessTools.executeProcess(pb);
        oa.shouldHaveExitValue(0);
        String stdout = oa.getStdout();

        // parse heap total blob count
        Matcher tatalBlobMatcher = Pattern.compile("heap_blob_count=(\\d+)").matcher(stdout);
        if (!tatalBlobMatcher.find()) {
            throw new RuntimeException("heap_blob_count not found");
        }
        int totalBlobNum = Integer.parseInt(tatalBlobMatcher.group(1));

        // parse cleaned blob count
        Matcher cleanedBlobMatcher = Pattern.compile("cleaned_blob_num=(\\d+)").matcher(stdout);
        int cleanedBlobNum = 0;
        while(cleanedBlobMatcher.find()) {
            cleanedBlobNum += Integer.parseInt(cleanedBlobMatcher.group(1));
        }

        if (totalBlobNum > cleanedBlobNum) {
            throw new RuntimeException("Code blobs are not cleared, total blob number " + totalBlobNum +
                ", actual cleaned blob number " + cleanedBlobNum);
        }

        if (stdout.contains("Code cache not clean")) {
            throw new RuntimeException("Code cache not clean");
        }

        if (!stdout.contains("Code cache cleared")) {
            throw new RuntimeException("Code cache not cleared");
        }
    }
}
