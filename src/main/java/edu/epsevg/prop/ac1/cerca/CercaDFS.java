package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.*;

public class CercaDFS extends Cerca {
    
    public CercaDFS(boolean usarLNT) { 
        super(usarLNT); 
    }
    
    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        Map<Mapa, Integer> LNT = usarLNT ? new HashMap<>() : null;
        int maxMemoria = 0;
        
        Stack<Node> LNO = new Stack<>();
        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        LNO.push(nodeInicial);
        
        while (!LNO.isEmpty()) {
            Node actual = LNO.pop();
            
            if (LNO.size() > maxMemoria) {
                maxMemoria = LNO.size();
            }
            
            if (usarLNT) {
                if (LNT.containsKey(actual.estat) && LNT.get(actual.estat) <= actual.depth) {
                    rc.incNodesTallats();
                    continue;
                }
                LNT.put(actual.estat, actual.depth);
            } else {
                if (estaEnCami(actual.pare, actual.estat)) {
                    rc.incNodesTallats();
                    continue;
                }
            }
            
            rc.incNodesExplorats();
            
            if (actual.estat.esMeta()) {
                rc.setCami(reconstruirCami(actual));
                rc.updateMemoria(maxMemoria + (usarLNT ? LNT.size() : 0));
                return;
            }
            
            List<Moviment> accions = actual.estat.getAccionsPossibles();
            
            for (int i = accions.size() - 1; i >= 0; i--) {
                Moviment accio = accions.get(i);
                Mapa nouEstat = actual.estat.mou(accio);
                boolean descartar = false;
                
                if (usarLNT) {
                    if (LNT.containsKey(nouEstat) && LNT.get(nouEstat) <= actual.depth + 1) {
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
                    LNO.push(nouNode);
                }
            }
        }
        
        rc.setCami(null);
        rc.updateMemoria(maxMemoria + (usarLNT ? LNT.size() : 0));
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
    
    private static class Node {
        Mapa estat;
        Node pare;
        Moviment accio;
        int depth;
        int g;
        
        Node(Mapa estat, Node pare, Moviment accio, int depth, int g) {
            this.estat = estat;
            this.pare = pare;
            this.accio = accio;
            this.depth = depth;
            this.g = g;
        }
    }
}