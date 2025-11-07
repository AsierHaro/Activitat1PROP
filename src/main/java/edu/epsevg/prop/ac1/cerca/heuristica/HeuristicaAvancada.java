package edu.epsevg.prop.ac1.cerca.heuristica;

import edu.epsevg.prop.ac1.model.Mapa;
import edu.epsevg.prop.ac1.model.Posicio;
import java.util.*;

/**
 * Heurística avançada (versió neta i robusta).
 * - No guarda "inicialitzat" global; s'inicialitza quan cal.
 * - Evita supòsits perillosos i compila net amb CercaAStar.
 */
public class HeuristicaAvancada implements Heuristica {

    private List<Posicio> posicionsClaus = null;
    private Posicio sortida = null;
    private Map<Character, Posicio> portesMap = null;
    private int n = 0, m = 0;

    @Override
    public int h(Mapa estat) {
        if (estat == null) return 0;

        // Inicialitzar si encara no ho hem fet (o si el mapa sembla diferent)
        if (posicionsClaus == null || portesMap == null) {
            inicialitzar(estat);
        }

        if (estat.esMeta()) return 0;

        List<Posicio> agents = estat.getAgents();
        int clausMask = estat.getClausMask();

        List<Posicio> clausPendents = getClausPendents(estat, clausMask);

        if (!clausPendents.isEmpty()) {
            return estimarCostTotalClaus(agents, clausPendents, estat)
                    + penalitzacioDispersio(agents)
                    + bonificacioPortesNecessaries(agents, clausPendents, estat, clausMask);
        } else {
            int distMinSortida = Integer.MAX_VALUE;
            for (Posicio agent : agents) {
                int dist = distanciaAmbObstacles(agent, sortida, estat);
                distMinSortida = Math.min(distMinSortida, dist);
            }
            if (distMinSortida == Integer.MAX_VALUE) distMinSortida = 0;
            return distMinSortida + penalitzacioDispersio(agents) / 2;
        }
    }

    private int estimarCostTotalClaus(List<Posicio> agents, List<Posicio> clausPendents, Mapa estat) {
        if (clausPendents.isEmpty()) return 0;
        int costMinim = Integer.MAX_VALUE;
        for (Posicio agent : agents) {
            int costAgent = estimarRecorregutClaus(agent, clausPendents, estat);
            costMinim = Math.min(costMinim, costAgent);
        }
        return costMinim == Integer.MAX_VALUE ? 0 : costMinim;
    }

    private int estimarRecorregutClaus(Posicio inici, List<Posicio> claus, Mapa estat) {
        if (claus.isEmpty()) return 0;

        Set<Posicio> visitats = new HashSet<>();
        Posicio actual = inici;
        int costTotal = 0;

        while (visitats.size() < claus.size()) {
            Posicio clauMesPropera = null;
            int distMinima = Integer.MAX_VALUE;

            for (Posicio clau : claus) {
                if (!visitats.contains(clau)) {
                    int dist = distanciaAmbObstacles(actual, clau, estat);
                    if (dist < distMinima) {
                        distMinima = dist;
                        clauMesPropera = clau;
                    }
                }
            }

            if (clauMesPropera != null) {
                costTotal += distMinima;
                actual = clauMesPropera;
                visitats.add(clauMesPropera);
            } else {
                break;
            }
        }

        // Afegir distància a la sortida des de l'última clau (si hi ha sortida)
        if (sortida != null) {
            costTotal += distanciaAmbObstacles(actual, sortida, estat);
        }

        return costTotal;
    }

    private int distanciaAmbObstacles(Posicio origen, Posicio desti, Mapa estat) {
        if (origen == null || desti == null) return 0;
        if (origen.equals(desti)) return 0;

        Queue<PosicioDist> cua = new LinkedList<>();
        Set<Posicio> visitats = new HashSet<>();

        cua.add(new PosicioDist(origen, 0));
        visitats.add(origen);

        int[][] dirs = {{-1,0}, {1,0}, {0,-1}, {0,1}};
        int maxIteracions = Math.max(100, n * m); // assegurar un límit raonable
        int iteracions = 0;

        while (!cua.isEmpty() && iteracions < maxIteracions) {
            PosicioDist actual = cua.poll();
            iteracions++;

            if (actual.pos.equals(desti)) {
                return actual.dist;
            }

            for (int[] dir : dirs) {
                int nx = actual.pos.x + dir[0];
                int ny = actual.pos.y + dir[1];
                Posicio nova = new Posicio(nx, ny);

                if (esPosicioValida(nx, ny, estat) && !visitats.contains(nova)) {
                    int cell = estat.getCellAt(nx, ny);
                    // Permetre passar per quasi tot (claus, portes, sortida, espai buit),
                    // i bloquejar només parets (assumem '#' com a mur)
                    if (cell != '#' ) {
                        visitats.add(nova);
                        cua.add(new PosicioDist(nova, actual.dist + 1));
                    }
                }
            }
        }

        // fallback: Manhattan si BFS falla
        return Math.abs(origen.x - desti.x) + Math.abs(origen.y - desti.y);
    }

    private int penalitzacioDispersio(List<Posicio> agents) {
        if (agents == null || agents.size() <= 1) return 0;

        int distanciaMaxima = 0;
        for (int i = 0; i < agents.size(); i++) {
            for (int j = i + 1; j < agents.size(); j++) {
                int dist = distanciaManhattan(agents.get(i), agents.get(j));
                distanciaMaxima = Math.max(distanciaMaxima, dist);
            }
        }
        return distanciaMaxima / 4;
    }

    private int bonificacioPortesNecessaries(List<Posicio> agents, List<Posicio> clausPendents,
                                             Mapa estat, int clausMask) {
        if (portesMap == null || agents == null) return 0;
        int bonificacio = 0;

        for (Map.Entry<Character, Posicio> entrada : portesMap.entrySet()) {
            char porta = entrada.getKey();
            int idxClau = Character.toLowerCase(porta) - 'a';
            if (idxClau < 0 || idxClau >= 32) continue;

            // Si no tenim la clau d'aquesta porta
            if ((clausMask & (1 << idxClau)) == 0) {
                Posicio posPorta = entrada.getValue();
                for (Posicio agent : agents) {
                    int distPorta = distanciaManhattan(agent, posPorta);
                    if (distPorta <= 3) {
                        bonificacio -= distPorta;
                    }
                }
            }
        }

        return bonificacio;
    }

    private List<Posicio> getClausPendents(Mapa estat, int clausMask) {
        List<Posicio> pendents = new ArrayList<>();
        if (posicionsClaus == null) return pendents;

        for (Posicio posClau : posicionsClaus) {
            int cell = estat.getCellAt(posClau.x, posClau.y);
            if (!Character.isLowerCase((char) cell)) continue;
            char clau = (char) cell;
            int idx = clau - 'a';
            if (idx < 0 || idx >= 32) continue;
            if ((clausMask & (1 << idx)) == 0) {
                pendents.add(posClau);
            }
        }
        return pendents;
    }

    private void inicialitzar(Mapa estat) {
        posicionsClaus = new ArrayList<>();
        portesMap = new HashMap<>();
        n = estat.getN();
        m = estat.getM();

        for (int i = 0; i < n; i++) {
            for (int j = 0; j < m; j++) {
                int cell = estat.getCellAt(i, j);
                if (Character.isLowerCase((char) cell)) {
                    posicionsClaus.add(new Posicio(i, j));
                } else if (Character.isUpperCase((char) cell)) {
                    portesMap.put((char) cell, new Posicio(i, j));
                } else if (cell == '@') {
                    sortida = new Posicio(i, j);
                }
            }
        }
    }

    private boolean esPosicioValida(int x, int y, Mapa estat) {
        return x >= 0 && x < n && y >= 0 && y < m;
    }

    private int distanciaManhattan(Posicio p1, Posicio p2) {
        return Math.abs(p1.x - p2.x) + Math.abs(p1.y - p2.y);
    }

    private static class PosicioDist {
        Posicio pos;
        int dist;
        PosicioDist(Posicio pos, int dist) { this.pos = pos; this.dist = dist; }
    }
}
