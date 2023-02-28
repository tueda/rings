#!/bin/bash
set -eo pipefail

# NOTE: GNU sed (-i option) is required.

find . -name '*.java' | xargs sed -i '
/gnu.trove.impl.Constants/d;
/gnu.trove.impl.hash.TIntIntHash/d;

s/gnu.trove.iterator.TIntIterator/com.carrotsearch.hppc.cursors.IntCursor/;
s/gnu.trove.iterator.TIntObjectIterator/com.carrotsearch.hppc.cursors.IntObjectCursor/;
s/gnu.trove.iterator.TLongObjectIterator/com.carrotsearch.hppc.cursors.LongObjectCursor/;
s/gnu.trove.list.array.TIntArrayList/com.carrotsearch.hppc.IntArrayList/;
s/gnu.trove.list.array.TLongArrayList/com.carrotsearch.hppc.LongArrayList/;
s/gnu.trove.map.hash.TIntIntHashMap/com.carrotsearch.hppc.IntIntHashMap/;
s/gnu.trove.map.hash.TIntObjectHashMap/com.carrotsearch.hppc.IntObjectHashMap/;
s/gnu.trove.map.hash.TLongObjectHashMap/com.carrotsearch.hppc.LongObjectHashMap/;
s/gnu.trove.map.hash.TObjectIntHashMap/com.carrotsearch.hppc.ObjectIntHashMap/;
s/gnu.trove.map.TIntObjectMap/com.carrotsearch.hppc.IntObjectMap/;
s/gnu.trove.set.hash.TIntHashSet/com.carrotsearch.hppc.TIntHashSet/;
s/gnu.trove.set.hash.TLongHashSet/com.carrotsearch.hppc.TLongHashSet/;

s/TIntArrayList/IntArrayList/g;
s/TIntHashSet/IntHashSet/g;
s/TIntIntHashMap/IntIntHashMap/g;
s/TIntObjectHashMap/IntObjectHashMap/g;
s/TIntObjectMap/IntObjectMap/g;
s/TLongArrayList/LongArrayList/g;
s/TLongHashSet/LongHashSet/g;
s/TLongObjectHashMap/LongObjectHashMap/g;
s/TObjectIntHashMap/ObjectIntHashMap/g;
'