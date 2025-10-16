interface HashTable {
    void clear();
    void insert(Registro r);
    boolean contains(Registro r);
    long getCollisionCount();
    int getOccupiedSlots();
    int[] getOccupiedIndices();
    int[] getTop3ChainSizes();
    int capacity();
}
