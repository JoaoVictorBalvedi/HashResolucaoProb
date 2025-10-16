interface HashTable {
    void clear();
    void insert(Registro r);
    boolean contains(Registro r);
    long getCollisionCount();      // colisões acumuladas desde o último clear
    int getOccupiedSlots();        // número de posições do vetor ocupadas
    int[] getOccupiedIndices();    // índices ocupados, para cálculo de gaps
    int[] getTop3ChainSizes();     // para encadeamento retorna tamanhos; para aberto retorna [0,0,0]
    int capacity();                // tamanho do vetor subjacente
}
