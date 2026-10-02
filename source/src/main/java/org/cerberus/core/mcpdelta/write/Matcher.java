/**
 * Cerberus Copyright (C) 2013 - 2026 cerberustesting
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This file is part of Cerberus.
 *
 * Cerberus is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Cerberus is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Cerberus.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.cerberus.core.mcpdelta.write;

import java.util.Arrays;
import java.util.List;

/**
 * Pairs the items of a new document with the items they replace, the way a diff pairs lines: identical
 * items are matched first (longest common subsequence), then the items left between two matches are
 * paired in order, as edits of each other. A paired item keeps its database identity.
 */
public final class Matcher {

    private Matcher() {
    }

    /** For each new item, the index of the base item it continues, or -1 when it is new. */
    public static int[] align(List<String> base, List<String> next) {
        int n = base.size();
        int m = next.size();
        int[][] lcs = new int[n + 1][m + 1];
        for (int i = n - 1; i >= 0; i--) {
            for (int j = m - 1; j >= 0; j--) {
                lcs[i][j] = base.get(i).equals(next.get(j)) ? lcs[i + 1][j + 1] + 1 : Math.max(lcs[i + 1][j], lcs[i][j + 1]);
            }
        }
        int[] match = new int[m];
        Arrays.fill(match, -1);
        // Walk the LCS; at each anchor, pair the unmatched items of the gap that precedes it.
        int i = 0;
        int j = 0;
        int gapBase = 0;
        int gapNext = 0;
        while (i < n && j < m) {
            if (base.get(i).equals(next.get(j)) && lcs[i][j] == lcs[i + 1][j + 1] + 1) {
                pairGap(match, gapBase, i, gapNext, j);
                match[j] = i;
                i++;
                j++;
                gapBase = i;
                gapNext = j;
            } else if (lcs[i + 1][j] >= lcs[i][j + 1]) {
                i++;
            } else {
                j++;
            }
        }
        pairGap(match, gapBase, n, gapNext, m);
        return match;
    }

    private static void pairGap(int[] match, int baseFrom, int baseTo, int nextFrom, int nextTo) {
        int count = Math.min(baseTo - baseFrom, nextTo - nextFrom);
        for (int k = 0; k < count; k++) {
            match[nextFrom + k] = baseFrom + k;
        }
    }
}
