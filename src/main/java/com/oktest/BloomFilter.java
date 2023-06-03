package com.oktest;

import com.sangupta.murmur.Murmur3;

import java.util.UUID;

public class BloomFilter {
    private byte[] filter;
    private int size;

    public BloomFilter(int size) {
        int arrSize = size /8;
        if(size % 8 != 0)
            arrSize++;
        this.filter = new byte[arrSize];
        this.size = size;
    }

    public void add(String key){
        int index = murmurhash(key);
        int aIndex = index / 8;
        int bIndex = index % 8;
        try {
            filter[aIndex] |= (1 << bIndex);
        }catch (Exception ex){
            System.out.println(ex);
        }
        //System.out.println(String.format("Wrote %s at index %d", key,index));
    }

    public boolean exists(String key) {
        int index = murmurhash(key);
        int aIndex = index / 8;
        int bIndex = index % 8;
        return ((filter[aIndex] >> bIndex) & 1) == 1;
    }

    private int murmurhash(String key){
        byte[] bytes = key.getBytes();
        int seed = 11;
        return (int) (Murmur3.hash_x86_32(bytes, bytes.length, seed) % size);
    }

    public static void main(String[] args) {
        String[] keys = new String[1000];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = UUID.randomUUID().toString();
        }

        for (int i = 100; i < 10000; i+= 100) {
            BloomFilter bloom = new BloomFilter(i);
            for (int j = 0; j < keys.length / 2; j++) {
                bloom.add(keys[j]);
            }

            int positiveCount = 0;
            for (String key :
                    keys) {
                if (bloom.exists(key))
                    positiveCount++;
            }

            double falsePositivity = (positiveCount - keys.length / 2) / (double) keys.length;

//        System.out.println(positiveCount);
            //System.out.println("Filter size: "+i+", False positivity rate: " + falsePositivity);
            System.out.println(falsePositivity );
        }

//        BloomFilter bloomFilter = new BloomFilter(16);
//        bloomFilter.add("a");
//        bloomFilter.add("b");
//        bloomFilter.add("c");
//        bloomFilter.add("d");
//
//        System.out.println(bloomFilter.exists("a"));
//        System.out.println(bloomFilter.exists("b"));
//        System.out.println(bloomFilter.exists("c"));
//        System.out.println(bloomFilter.exists("d"));
    }

}
