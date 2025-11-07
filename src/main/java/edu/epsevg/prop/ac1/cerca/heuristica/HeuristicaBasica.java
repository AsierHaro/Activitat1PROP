package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.model.Mapa;
import java.util.*;
import edu.epsevg.prop.ac1.model.Posicio;


/** 
 * Distància de Manhattan a la clau més propera 
 * (si queden per recollir) o a la sortida.
 */
public class HeuristicaBasica implements Heuristica {
    private List<Posicio> posicionsClaus;
    private Posicio sortida;
    private boolean inicialitzat = false;
    @Override
    public int h(Mapa estat) {
   
        if (!inicialitzat) {
            inicialitzar(estat);
            inicialitzat = true;
        }
        
        if (estat.esMeta()) {
            return 0;
        }
        
        List<Posicio> agents = estat.getAgents();
        int clausMask = estat.getClausMask();
        int distanciaMinima = Integer.MAX_VALUE;
        
        boolean hiHaClausPendents = false;
        for (Posicio posClau : posicionsClaus) {
            char clau = (char) estat.getCellAt(posClau.x, posClau.y);
            int idx = clau - 'a';
            
            if ((clausMask & (1 << idx)) == 0) {
                hiHaClausPendents = true;
                for (Posicio posAgent : agents) {
                    int distancia = distanciaManhattan(posAgent, posClau);
                    distanciaMinima = Math.min(distanciaMinima, distancia);
                }
            }
        }
        
        if (!hiHaClausPendents) {
            for (Posicio posAgent : agents) {
                int distancia = distanciaManhattan(posAgent, sortida);
                distanciaMinima = Math.min(distanciaMinima, distancia);
            }
        }
        
        return distanciaMinima;
    }
    
    private void inicialitzar(Mapa estat) {
        posicionsClaus = new ArrayList<>();
        
        for (int i = 0; i < estat.getN(); i++) {
            for (int j = 0; j < estat.getM(); j++) {
                int cell = estat.getCellAt(i, j);
                
                if (Character.isLowerCase((char) cell)) {
                    posicionsClaus.add(new Posicio(i, j));
                } else if (cell == '@') {
                    sortida = new Posicio(i, j);
                }
            }
        }
    }
    
    
    private int distanciaManhattan(Posicio p1, Posicio p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }
}
