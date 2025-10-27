package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.model.Mapa;
import java.util.*;
import edu.epsevg.prop.ac1.model.Posicio;


/** 
 * Distància de Manhattan a la clau més propera 
 * (si queden per recollir) o a la sortida.
 */
public class HeuristicaBasica implements Heuristica {
    @Override
    public int h(Mapa estat) {
   
        if(estat.esMeta()) 
            return 0;
        
        List<Posicio> agents = estat.getAgents();
        int clausMask = estat.getClausMask();
        int distanciaMinima = Integer.MAX_VALUE;
        List<Posicio> clausPendents = new ArrayList<>();
        
        
        for(int i = 0; i < estat.getN(); i++){
            for(int j = 0; j < estat.getM(); j++){
                   int cell = estat.getCellAt(i,j);
                  
                   if(Character.isLowerCase((char) cell)){
                       char clau = (char) cell;
                       int idx = clau - 'a';
                       if((clausMask & (1 << idx)) == 0){
                           clausPendents.add(new Posicio(i,j));
                       }
                   }
            }
        }
        if(!clausPendents.isEmpty()){
            for(Posicio posAgent: estat.getAgents()){
                for(Posicio posClau: clausPendents){
                    int distancia = distanciaManhattan(posAgent, posClau);
                    distanciaMinima = Math.min(distanciaMinima, distancia);
                }
            }
        }else{
            Posicio sortida = estat.getSortidaPosicio();
            for (Posicio posAgent : agents) {
                int distancia = distanciaManhattan(posAgent, sortida);
                distanciaMinima = Math.min(distanciaMinima, distancia);
            }
        }
        
        return distanciaMinima;
    }
    
    
    private int distanciaManhattan(Posicio p1, Posicio p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }
}
