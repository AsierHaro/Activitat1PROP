package edu.epsevg.prop.ac1.cerca.heuristica;
import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;

/** 
 * Heurística avançada optimitzada per competició.
 * 
 * Estratègia:
 * 1. Si queden claus pendents: distància Manhattan mínima des de qualsevol agent 
 *    a la clau no recollida més propera
 * 2. Si totes les claus recollides: distància Manhattan mínima des de qualsevol 
 *    agent a la sortida
 * 
 * Optimitzacions implementades:
 * - Inicialització lazy: només es recorre el mapa una vegada
 * - Arrays en lloc de Lists per evitar overhead
 * - Càlcul directe de màscara completa de claus
 * - Accés directe a grid via getCellAt (més ràpid que getCell privat)
 * - Sortida primerenca si ja som a meta
 * - Comparacions mínimes necessàries
 * 
 * Propietat d'admissibilitat:
 * L'heurística és admissible perquè la distància Manhattan (ignorant obstacles)
 * mai sobreestima el cost real del camí més curt, ja que cada moviment té cost 1
 * i la Manhattan és el mínim de moviments necessaris en graella sense obstacles.
 */
public class HeuristicaAvancada implements Heuristica {
    private Posicio[] posicionsClaus;
    private int[] indicesClaus; 
    private Posicio sortida;
    private int numClaus;
    private int maskComplet; 
    private boolean inicialitzat = false;
    
    @Override
    public int h(Mapa estat) {
        if (!inicialitzat) {
            inicialitzar(estat);
        }
        
        if (estat.esMeta()) {
            return 0;
        }
        
        int clausMask = estat.getClausMask();
        
        if (clausMask == maskComplet) {
            return distanciaMinimaSortida(estat);
        }
        
        return distanciaMinimaClauPendent(estat, clausMask);
    }
    
    private void inicialitzar(Mapa estat) {
        int n = estat.getN();
        int m = estat.getM();
        
        numClaus = 0;
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int cell = estat.getCellAt(i, j);
                if (cell >= 'a' && cell <= 'z') {
                    numClaus++;
                }
            }
        }
        
        maskComplet = (1 << numClaus) - 1;
        
        posicionsClaus = new Posicio[numClaus];
        indicesClaus = new int[numClaus];
        int idx = 0;
        
        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int cell = estat.getCellAt(i, j);
                if (cell >= 'a' && cell <= 'z') {
                    posicionsClaus[idx] = new Posicio(i, j);
                    indicesClaus[idx] = cell - 'a';
                    idx++;
                } else if (cell == Mapa.SORTIDA) {
                    sortida = new Posicio(i, j);
                }
            }
        }
        
        inicialitzat = true;
    }
    
    private int distanciaMinimaSortida(Mapa estat) {
        int min = Integer.MAX_VALUE;
        for (Posicio agent : estat.getAgents()) {
            int dist = Math.abs(agent.x - sortida.x) + Math.abs(agent.y - sortida.y);
            if (dist < min) {
                min = dist;
            }
        }
        return min;
    }
    
    private int distanciaMinimaClauPendent(Mapa estat, int clausMask) {
        int min = Integer.MAX_VALUE;
        
        for (int i = 0; i < numClaus; i++) {
            if ((clausMask & (1 << indicesClaus[i])) == 0) {
                Posicio clau = posicionsClaus[i];
                
                for (Posicio agent : estat.getAgents()) {
                    int dist = Math.abs(agent.x - clau.x) + Math.abs(agent.y - clau.y);
                    if (dist < min) {
                        min = dist;
                    }
                }
            }
        }
        
        return min;
    }
}