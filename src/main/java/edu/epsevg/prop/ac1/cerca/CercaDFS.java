package edu.epsevg.prop.ac1.cerca;

import edu.epsevg.prop.ac1.model.*;
import edu.epsevg.prop.ac1.resultat.ResultatCerca;
import java.util.*;

/**
 * Cerca en profunditat (DFS)
 * 
 * Implementació segons les especificacions del PDF:
 *  - Control de cicles configurable (usarLNT)
 *  - Límit de profunditat de 50 nivells
 *  - LNT desa profunditat i només descarta si ja visitat a menor o igual profunditat
 */
public class CercaDFS extends Cerca {

    private static final int PROF_MAX = 50;

    public CercaDFS(boolean usarLNT) { super(usarLNT);}

    @Override
    public void ferCerca(Mapa inicial, ResultatCerca rc) {
        Deque<Node> LNO = new ArrayDeque<>();
        Map<Mapa, Integer> LNT = usarLNT ? new HashMap<>() : null;

        Node arrel = new Node(inicial, null, null, 0, 0);
        LNO.push(arrel);

        while (!LNO.isEmpty()) {
            Node actual = LNO.pop();

            if (actual.depth > PROF_MAX) {
                rc.incNodesTallats();
                continue;
            }

            if (usarLNT) {
                LNT.put(actual.estat, actual.depth);
            }

            int memActual = LNO.size() + (usarLNT ? LNT.size() : 0);
            rc.updateMemoria(memActual);

            rc.incNodesExplorats();

            if (actual.estat.esMeta()) {
                rc.setCami(reconstruir(actual));
                return;
            }

            List<Moviment> moviments = actual.estat.getAccionsPossibles();

            for (Moviment mov : moviments) {
                try {
                    Mapa nouEstat = actual.estat.mou(mov);
                    int novaProf = actual.depth + 1;

                    if (novaProf > PROF_MAX) {
                        rc.incNodesTallats();
                        continue;
                    }

                    boolean descartar = false;

                    if (usarLNT) {
                        Integer profAnt = LNT.get(nouEstat);
                        if (profAnt != null && profAnt <= novaProf) {
                            rc.incNodesTallats();
                            descartar = true;
                        }
                    } else {
                        for (Node pare = actual.pare; pare != null; pare = pare.pare) {
                            if (pare.estat.equals(nouEstat)) {
                                rc.incNodesTallats();
                                descartar = true;
                                break;
                            }
                        }
                    }

                    if (!descartar) {
                        Node fill = new Node(nouEstat, actual, mov, novaProf, novaProf);
                        LNO.push(fill);
                    }

                } catch (IllegalArgumentException e) {
                }
            }
        }

        rc.setCami(null);
    }

    private List<Moviment> reconstruir(Node nodeFinal) {
        LinkedList<Moviment> cami = new LinkedList<>();
        for (Node n = nodeFinal; n.pare != null; n = n.pare) {
            cami.addFirst(n.accio);
        }
        return cami;
    }
}
