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
        Set<Mapa> LNT = new HashSet<>();
        
        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        LNO.add(nodeInicial);
        
        while(!LNO.isEmpty()){
            Node actual = LNO.poll();
            if(usarLNT){
                if (LNT.contains(actual.estat)) continue;
                LNT.add(actual.estat);
            }else{
                if (estaEnCami(actual.pare, actual.estat)) {
                    rc.incNodesTallats();
                    continue;
                }
            }
            
            rc.incNodesExplorats();
            
            if(actual.estat.esMeta()){
                rc.setCami(reconstruirCami(actual));
                return;
            }
            
            
            List<Moviment> accions = actual.estat.getAccionsPossibles();
            for(Moviment accio : accions){
                Mapa nouEstat = actual.estat.mou(accio);
                
                if (usarLNT && LNT.contains(nouEstat)) continue;
                Node nouNode = new Node(nouEstat, actual, accio, 
                                       actual.depth + 1, actual.g + 1);
                LNO.add(nouNode);
                 
            }
             
        }
        rc.setCami(null);
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
