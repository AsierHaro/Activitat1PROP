package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.*;


public class CercaBFS extends Cerca {
    public CercaBFS(boolean usarLNT) { super(usarLNT); }
    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        Queue<Node> LNO = new LinkedList<>(); 
        Set<Mapa> LNT = new HashSet<>();
  
        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        LNO.add(nodeInicial);
        while(!LNO.isEmpty()){
            Node actual = LNO.poll();
            
            if (LNT.contains(actual.estat)) continue;
            rc.incNodesExplorats();
            LNT.add(actual.estat);
            
            boolean solucioTrobada = false;
            for (Posicio posAgent : actual.estat.getAgents()) {
                if (actual.estat.esSortida(posAgent)) {
                    solucioTrobada = true;
                    break;
                }
            }
            
            if(solucioTrobada){
                rc.setCami(reconstruirCami(actual));
                return;
            }
            
            List<Moviment> accions = actual.estat.getAccionsPossibles();
            for(Moviment accio : accions){
                Mapa nouEstat = actual.estat.mou(accio);
                
                if (!LNT.contains(nouEstat)) {
                    Node nouNode = new Node(nouEstat, actual, accio, 
                                       actual.depth + 1, actual.g + 1);
                    LNO.add(nouNode);
                }    
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
}
