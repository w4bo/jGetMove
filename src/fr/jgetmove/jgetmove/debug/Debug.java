package fr.jgetmove.jgetmove.debug;

import java.util.Objects;

/**
 * Utilisée afin d'affichier les erreurs
 */
public class Debug {
    private static boolean debug = false;
    private static String stack = "";

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.print(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void print(Object o) {
        if (debug)
            System.out.print(stack.concat(o.toString()));
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.out.println(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void println(Object o) {
        if (debug)
            System.out.println(stack.concat(o.toString()));
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.err.print(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void err(Object o) {
        if (debug)
            System.err.print(stack.concat(o.toString()));
    }

    /**
     * Affiche l'objet, alias de
     * <pre>
     *     System.err.print(Object)
     * </pre>
     *
     * @param o L'objet a afficher
     */
    public static void errln(Object o) {
        if (debug)
            System.err.println(stack.concat(o.toString()));

    }

    /**
     * Affiche l'objet suivant le option passée en parametre.
     *
     * @param o   l'objet à afficher dans la console
     * @param opt l'option choisie (ln, err, errln)
     * @see #print(Object)
     * @see #println(Object)
     * @see #err(Object)
     * @see #errln(Object)
     */
    public static void print(Object o, String opt) {
        if (Objects.equals(opt, "ln")) {
            println(o);
        } else if (Objects.equals(opt, "err")) {
            err(o);
        } else if (Objects.equals(opt, "errln")) {
            errln(o);
        } else {
            print(o);
        }
    }

    public static void enable() {
        debug = true;
    }

    public static void stack(char letter) {
        stack += letter;
    }

    public static void unstack() {
        if (stack.length() > 0) {
            stack = stack.substring(0, stack.length() - 1);
        }
    }
}
