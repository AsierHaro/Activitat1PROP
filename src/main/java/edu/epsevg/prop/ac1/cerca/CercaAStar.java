package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.cerca.heuristica.Heuristica;
import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.*;


public class CercaAStar extends Cerca {

    private final Heuristica heur;

    public CercaAStar(boolean usarLNT, Heuristica heur) { 
        super(usarLNT); 
        this.heur = heur; 
    }

    @Override
    public  void ferCerca(Mapa inicial, ResultatCerca rc) {
        PriorityQueue<Node> LNO = new PriorityQueue<>(
            Comparator.comparingInt((Node n) -> n.g + heur.h(n.estat))
        );
        Map<Mapa, Integer> LNT = new HashMap<>();
        int maxLNO = 0;
        
        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        LNO.add(nodeInicial);
        
        while(!LNO.isEmpty()){
            Node actual = LNO.poll();
            if(LNO.size() > maxLNO) maxLNO = LNO.size();
            if (usarLNT) {
                if (LNT.containsKey(actual.estat) && LNT.get(actual.estat) <= actual.g) {
                    rc.incNodesTallats();
                    continue;
                }
                LNT.put(actual.estat, actual.g);
            }else{
                if (estaEnCami(actual.pare, actual.estat)) {
                    rc.incNodesTallats();
                    continue;
                }
            }
            
            rc.incNodesExplorats();
            
            if(actual.estat.esMeta()){
                rc.setCami(reconstruirCami(actual));
                rc.updateMemoria(maxLNO + (usarLNT ? LNT.size() : 0));
                return;
            }
            
            
            List<Moviment> accions = actual.estat.getAccionsPossibles();
            for(Moviment accio : accions){
                Mapa nouEstat = actual.estat.mou(accio);
                boolean descartar = false;
                
                if (usarLNT) {
                    if (LNT.containsKey(nouEstat) && LNT.get(nouEstat) <= actual.g + 1) {
                        rc.incNodesTallats();
                        descartar = true;
                    }
                } else {
                    if (estaEnCami(actual, nouEstat)) {
                        rc.incNodesTallats();
                        descartar = true;
                    }
                }
                if (!descartar) {
                    Node nouNode = new Node(nouEstat, actual, accio, 
                                           actual.depth + 1, actual.g + 1);
                    LNO.add(nouNode);
                }  
            }      
        }
        rc.setCami(null);
        rc.updateMemoria(maxLNO + (usarLNT ? LNT.size() : 0));
    }
    
    private List<Moviment> reconstruirCami(Node nodeFinal) {
        List<Moviment> cami = new ArrayList<>();
        Node actual = nodeFinal;
        
        while (actual.pare != null) {
            cami.add(0, actual.accio); 
            actual = actual.pare;
        }
        
        return cami;
    }
    
    
     private boolean estaEnCami(Node node, Mapa estat) {
        while (node != null) {
            if (node.estat.equals(estat)) {
                return true;
            }
            node = node.pare;
        }
        return false;
    }

}
