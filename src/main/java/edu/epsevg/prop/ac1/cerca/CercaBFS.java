package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.*;


public class CercaBFS extends Cerca {
    public CercaBFS(boolean usarLNT) { super(usarLNT); }
    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        Queue<Node> LNO = new LinkedList<>(); 
        Set<Mapa> LNT = usarLNT ? new HashSet<>() : null;
        int maxLNO = 0;

        Node nodeInicial = new Node(inicial, null, null, 0, 0);
        LNO.add(nodeInicial);

        if (usarLNT) {
            LNT.add(inicial);
        }

        while (!LNO.isEmpty()) {
            Node actual = LNO.poll();

            if (LNO.size() > maxLNO) {
                maxLNO = LNO.size();
            }

            rc.incNodesExplorats();

            if (actual.estat.esMeta()) {
                rc.setCami(reconstruirCami(actual));
                rc.updateMemoria(maxLNO + (usarLNT ? LNT.size() : 0));
                return;
            }

            List<Moviment> accions = actual.estat.getAccionsPossibles();
            for (Moviment accio : accions) {
                Mapa nouEstat = actual.estat.mou(accio);

                boolean descartar = false;

                if (usarLNT) {
                    if (LNT.contains(nouEstat)) {
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

                    if (usarLNT) {
                        LNT.add(nouEstat);
                    }
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
