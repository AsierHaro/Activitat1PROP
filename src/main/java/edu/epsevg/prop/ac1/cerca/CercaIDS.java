package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.*;

public class CercaIDS extends Cerca {

    private static final int PROF_MAX = 50;

    public CercaIDS(boolean usarLNT) {
        super(usarLNT);
    }

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        for (int limit = 0; limit <= PROF_MAX; limit++) {
            List<Moviment> cami = cercaLimitada(inicial, rc, limit);
            if (cami != null) {
                rc.setCami(cami);
                return;
            }
        }
        rc.setCami(null);
    }

    private List<Moviment> cercaLimitada(Mapa inicial, ResultatCerca rc, int limit) {
        Deque<Node> LNO = new ArrayDeque<>();
        Map<Mapa, Integer> LNT = usarLNT ? new HashMap<>() : null;

        int memMax = 0;
        Node arrel = new Node(inicial, null, null, 0, 0);
        LNO.push(arrel);

        while (!LNO.isEmpty()) {
            Node actual = LNO.pop();

            int memActual = LNO.size() + (LNT != null ? LNT.size() : 0);
            memMax = Math.max(memMax, memActual);
            rc.updateMemoria(memMax);

            if (LNT != null) {
                Integer profAnterior = LNT.get(actual.estat);
                if (profAnterior != null && profAnterior <= actual.depth) {
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
                return reconstruirCami(actual);
            }

            if (actual.depth >= limit) {
                continue;
            }

            List<Moviment> moviments = actual.estat.getAccionsPossibles();
            for (int i = moviments.size() - 1; i >= 0; i--) {
                Moviment mov = moviments.get(i);
                Mapa nouEstat;
                try {
                    nouEstat = actual.estat.mou(mov);
                } catch (IllegalArgumentException e) {
                    continue;
                }

                int novaProf = actual.depth + 1;
                boolean descartar = false;

                if (LNT != null) {
                    Integer profVist = LNT.get(nouEstat);
                    if (profVist != null && profVist <= novaProf) {
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
                    LNO.push(new Node(nouEstat, actual, mov, novaProf, novaProf));
                }
            }
        }

        return null;
    }

    private List<Moviment> reconstruirCami(Node nodeFinal) {
        LinkedList<Moviment> cami = new LinkedList<>();
        for (Node n = nodeFinal; n.pare != null; n = n.pare) {
            cami.addFirst(n.accio);
        }
        return cami;
    }

    private boolean estaEnCami(Node node, Mapa estat) {
        for (Node n = node; n != null; n = n.pare) {
            if (n.estat.equals(estat)) {
                return true;
            }
        }
        return false;
    }
}