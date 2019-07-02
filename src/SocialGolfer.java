/********** SocialGolfer.java **********/

import java.util.*;
import java.io.*;
import localsolver.*;

public class SocialGolfer {
    // liczba grup
    private int nbGroups;
    // wielkośc każdej grupy
    private int groupSize;
    // liczba tygodni
    private int nbWeeks;
    // liczba graczy
    private int nbGolfers;

    // wynik
    private LSExpression obj;

    // LocalSolver.
    private LocalSolver localsolver;

    // zmienna decyzyjna
    private LSExpression[][][] x;

    private SocialGolfer(LocalSolver localsolver) {
        this.localsolver = localsolver;
    }

    // pobieranie danych wejsciowych z pliku
    private void readInstance(String fileName) throws IOException {
        try (Scanner input = new Scanner(new File(fileName))) {
            nbGroups = input.nextInt();
            groupSize = input.nextInt();
            nbWeeks = input.nextInt();
        }
        nbGolfers = nbGroups * groupSize;
    }

    // deklarowanie modelu obliczeniowego
    private void solve(int limit) {
        LSModel model = localsolver.getModel();

        x = new LSExpression[nbWeeks][nbGroups][nbGolfers];

        // zmienne decyzyjne
        // x[w][gr][gf]=1 jeżeli gracz gf jest w grupie gr w tygodniu w
        for (int w = 0; w < nbWeeks; w++) {
            for (int gr = 0; gr < nbGroups; gr++) {
                for (int gf = 0; gf < nbGolfers; gf++) {
                    x[w][gr][gf] = model.boolVar();
                }
            }
        }

        // każdego tygodnia, każdy gracz jest przydzielony do tylko jednej grupy
        for (int w = 0; w < nbWeeks; w++) {
            for (int gf = 0; gf < nbGolfers; gf++) {
                LSExpression nbGroupsAssigned = model.sum();
                for (int gr = 0; gr < nbGroups; gr++) {
                    nbGroupsAssigned.addOperand(x[w][gr][gf]);
                }
                model.constraint(model.eq(nbGroupsAssigned, 1));
            }
        }

        // w każdym tygodniu, każda grupa ma dokładnie tyle samo graczy (gropusSize)
        for (int w = 0; w < nbWeeks; w++) {
            for (int gr = 0; gr < nbGroups; gr++) {
                LSExpression nbGolfersInGroup = model.sum();
                for (int gf = 0; gf < nbGolfers; gf++) {
                    nbGolfersInGroup.addOperand(x[w][gr][gf]);
                }
                model.constraint(model.eq(nbGolfersInGroup, groupSize));
            }
        }

        // gracz gf0 i gf1 spotykają się w gtupie gr w tygodniu w jeżeli oboje są przydzieleni do tej grupy w tygodniu w
        LSExpression[][][][] meetings = new LSExpression[nbWeeks][nbGroups][nbGolfers][nbGolfers];
        for (int w = 0; w < nbWeeks; w++) {
            for (int gr = 0; gr < nbGroups; gr++) {
                for (int gf0 = 0; gf0 < nbGolfers; gf0++) {
                    for (int gf1 = gf0 + 1; gf1 < nbGolfers; gf1++) {
                        meetings[w][gr][gf0][gf1] = model.and(x[w][gr][gf0], x[w][gr][gf1]);
                    }
                }
            }
        }

        // liczba spotkań graczy gf0 i gf1 jest równa sumie ich zmiennych meetings pośród wszystkich tygodni i grup
        LSExpression[][] redundantMeetings;
        redundantMeetings = new LSExpression[nbGolfers][nbGolfers];
        for (int gf0 = 0; gf0 < nbGolfers; gf0++) {
            for (int gf1 = gf0 + 1; gf1 < nbGolfers; gf1++) {
                LSExpression nbMeetings = model.sum();
                for (int w = 0; w < nbWeeks; w++) {
                    for (int gr = 0; gr < nbGroups; gr++) {
                        nbMeetings.addOperand(meetings[w][gr][gf0][gf1]);
                    }
                }
                redundantMeetings[gf0][gf1] = model.max(model.sub(nbMeetings, 1), 0);
            }
        }

        // celem jest usunięcie redundantnych spotkań
        obj = model.sum();
        for (int gf0 = 0; gf0 < nbGolfers; gf0++) {
            for (int gf1 = gf0 + 1; gf1 < nbGolfers; gf1++) {
                obj.addOperand(redundantMeetings[gf0][gf1]);
            }
        }
        model.minimize(obj);

        model.close();

        // parametryzacja solvera
        localsolver.getParam().setTimeLimit(limit);

        localsolver.solve();
    }

    // Format zapisu wyniku końcowego:
    // - wartość celu
    // - dla każdego tygodnia i grupy, wypisuje graczy w grupie
    private void writeSolution(String fileName) throws IOException {
        try(PrintWriter output = new PrintWriter(fileName)) {
            output.println(obj.getValue());
            for (int w = 0; w < nbWeeks; w++) {
                for (int gr = 0; gr < nbGroups; gr++) {
                    for (int gf = 0; gf < nbGolfers; gf++) {
                        if (x[w][gr][gf].getValue() == 1) {
                            output.print(gf + " ");
                        }
                    }
                    output.println();
                }
                output.println();
            }
        }
    }

    public static void run(String[] args) {

        if (args.length < 1) {
            System.err.println("Usage: java SocialGolfer inputFile [outputFile] [timeLimit]");
            System.exit(1);
        }

        String instanceFile = args[0];
        String outputFile = args.length > 1 ? args[1] : null;
        String strTimeLimit = args.length > 2 ? args[2] : "10";

        try (LocalSolver localsolver = new LocalSolver()) {
            SocialGolfer model = new SocialGolfer(localsolver);
            model.readInstance(instanceFile);
            model.solve(Integer.parseInt(strTimeLimit));
            if (outputFile != null) {
                model.writeSolution(outputFile);
            }
        } catch(Exception ex) {
            System.err.println(ex);
            ex.printStackTrace();
            System.exit(1);
        }
    }

    public static void main(String[] args) {

        //ścieżki określające położenie plików wejściowych i wyjściowych
        String[] arg = new String[2];
        arg[0] = "C:\\Users\\przem\\OneDrive\\Dokumenty\\GitHub\\SocialGolferProblem\\src\\in.txt";
        arg[1] = "C:\\Users\\przem\\OneDrive\\Dokumenty\\GitHub\\SocialGolferProblem\\src\\outs.txt";

        run(arg);
    }

}