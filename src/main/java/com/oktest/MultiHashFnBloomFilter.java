package com.oktest;

import com.sangupta.murmur.Murmur3;

import java.util.Random;
import java.util.UUID;

public class MultiHashFnBloomFilter {
    private byte[] filter;
    private int size;
    private int[] hashFnSeeds;

    public MultiHashFnBloomFilter(int size, int hashFnSeedCount) {
        int arrSize = size /8;
        if(size % 8 != 0)
            arrSize++;
        this.filter = new byte[arrSize];
        this.size = size;
        this.hashFnSeeds = new int[hashFnSeedCount];

        for (int i = 0; i < hashFnSeedCount; i++) {
            hashFnSeeds[i] = new Random().nextInt();
        }
    }

    public void add(String key, int hashFnIndex){
        for(int i=0; i < hashFnIndex; i++) {
            int index = murmurhash(key,i);
            int aIndex = index / 8;
            int bIndex = index % 8;
            filter[aIndex] |= (1 << bIndex);
        }

    }

    public boolean exists(String key, int hashFnIndex) {
        for (int i = 0; i < hashFnIndex; i++) {
            int index = murmurhash(key,i);
            int aIndex = index / 8;
            int bIndex = index % 8;
            boolean match = ((filter[aIndex] >> bIndex) & 1) == 1;
            if(!match)
                return false;
        }
        return true;
    }

    private int murmurhash(String key, int seedIndex){
        byte[] bytes = key.getBytes();
        return (int) (Murmur3.hash_x86_32(bytes, bytes.length, hashFnSeeds[seedIndex]) % size);
    }

    /**
     * We have tracked the effect of using multiple hash functions on the result of bloom filter
     * When increased the hash function count, initially the false positivity rate decreased which is good
     * But after certain threshold, the false positivity rate increased with increase in hash functions
     * This happens because, in case of very high hash functions for each key large number of keys get set hence resulting in false positives
     */
    public static void main(String[] args) {
        String[] keys = new String[1000];
        for (int i = 0; i < keys.length; i++) {
            keys[i] = UUID.randomUUID().toString();
        }

        for (int i = 1; i <= 100; i++) {
            MultiHashFnBloomFilter bloom = new MultiHashFnBloomFilter(5000,100);
            for (int j = 0; j < keys.length / 2; j++) {
                bloom.add(keys[j],i);
            }

            int positiveCount = 0;
            for (String key :
                    keys) {
                if (bloom.exists(key,i))
                    positiveCount++;
            }

            double falsePositivity = (positiveCount - keys.length / 2) / (double) keys.length;
            System.out.println(falsePositivity );
        }

    }

}
